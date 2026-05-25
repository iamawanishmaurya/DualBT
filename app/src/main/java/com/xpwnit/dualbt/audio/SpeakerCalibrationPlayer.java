package com.xpwnit.dualbt.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.SystemClock;

import com.xpwnit.dualbt.logging.AppLogger;
import com.xpwnit.dualbt.state.StreamDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class SpeakerCalibrationPlayer {
    private static final int SAMPLE_RATE = 48_000;
    private static final int SCO_SAMPLE_RATE = 16_000;
    private static final int DURATION_MS = 3_200;
    private static final long PLAYBACK_DRAIN_POLL_MS = 50L;

    private final Context context;
    private final SpeakerTestRunGate runGate = new SpeakerTestRunGate();
    private final BluetoothA2dpRouteActivator routeActivator;
    private final BluetoothHeadsetRouteActivator headsetRouteActivator;
    private AudioTrack currentTrack;
    private AudioManager activeAudioManager;
    private boolean communicationFallbackActive;
    private AudioFocusRequest activeFocusRequest;
    private boolean legacyFocusActive;
    private int previousAudioMode = AudioManager.MODE_NORMAL;

    public SpeakerCalibrationPlayer(Context context) {
        this.context = context.getApplicationContext();
        this.routeActivator = new BluetoothA2dpRouteActivator(this.context);
        this.headsetRouteActivator = new BluetoothHeadsetRouteActivator(this.context);
    }

    public synchronized void play(StreamDevice device, int speakerNumber) {
        if (!runGate.tryStart()) {
            AppLogger.w("SpeakerTest", "Test " + speakerNumber + " ignored because another calibration test is still running");
            return;
        }
        Thread worker = new Thread(() -> {
            try {
                playBlocking(device, speakerNumber);
            } finally {
                runGate.finish();
            }
        }, "dualbt-speaker-test");
        worker.start();
    }

    public synchronized void stop() {
        releaseTrack(currentTrack);
        currentTrack = null;
        stopCommunicationFallback();
        runGate.finish();
    }

    private void playBlocking(StreamDevice device, int speakerNumber) {
        AudioTrack track = null;
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager == null) {
                AppLogger.w("SpeakerTest", "AudioManager unavailable for " + routeName(device, speakerNumber));
                return;
            }
            BluetoothA2dpRouteActivator.Result activation = routeActivator.activate(device, 900L);
            if (activation.attempted) {
                AppLogger.i("SpeakerTest", "A2DP route activation for " + routeName(device, speakerNumber) + ": " + activation.message);
            } else {
                AppLogger.d("SpeakerTest", "A2DP route activation skipped for " + routeName(device, speakerNumber) + ": " + activation.message);
            }
            List<OutputBinding> outputs = bluetoothOutputs(audioManager);
            OutputBinding output = matchingOutput(device, outputs);
            if (RouteRetryPolicy.shouldRescanAfterActivation(activation.attempted, output != null)) {
                output = waitForMatchingOutput(audioManager, device, 6, 220L);
            }
            boolean genericScoVisible = firstBluetoothScoOutput(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) != null;
            SpeakerCalibrationRoutePlanner.Plan routePlan = SpeakerCalibrationRoutePlanner.plan(
                    output != null,
                    activation.attempted,
                    activation.accepted,
                    false,
                    genericScoVisible
            );
            boolean communicationTrack = false;
            if (!routePlan.useDirectMedia && !routePlan.useDefaultMedia && genericScoVisible) {
                AudioDeviceInfo communicationOutput = startCommunicationFallback(audioManager, device);
                if (communicationOutput != null) {
                    output = new OutputBinding(communicationOutput);
                    communicationTrack = true;
                    routePlan = SpeakerCalibrationRoutePlanner.plan(false, false, false, true, true);
                    AppLogger.w(
                            "SpeakerTest",
                            "Test " + speakerNumber + " using targeted Headset/SCO route for "
                                    + routeName(device, speakerNumber)
                    );
                } else {
                    routePlan = SpeakerCalibrationRoutePlanner.plan(false, false, false, false, true);
                }
            }
            if ((output == null && !routePlan.useDefaultMedia) || routePlan.blocked) {
                AppLogger.w(
                        "SpeakerTest",
                        "Test " + speakerNumber + " blocked for " + routeName(device, speakerNumber)
                                + ": Android exposes no direct or targeted communication route for this speaker."
                );
                return;
            }
            byte[] pcm = CalibrationTone.stereoSinePcm(
                    SAMPLE_RATE,
                    DURATION_MS,
                    speakerNumber == 2 ? 880.0 : 660.0,
                    CalibrationAudioFocusPolicy.testToneGain()
            );
            if (communicationTrack) {
                Pcm16ScoConverter converter = new Pcm16ScoConverter(SAMPLE_RATE, SCO_SAMPLE_RATE);
                byte[] scoPcm = new byte[converter.outputCapacityBytes(pcm.length)];
                int scoBytes = converter.convert(pcm, pcm.length, scoPcm);
                byte[] trimmed = new byte[scoBytes];
                System.arraycopy(scoPcm, 0, trimmed, 0, scoBytes);
                pcm = trimmed;
            }
            int minBufferBytes = AudioTrack.getMinBufferSize(
                    communicationTrack ? SCO_SAMPLE_RATE : SAMPLE_RATE,
                    communicationTrack ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT
            );
            if (minBufferBytes <= 0) {
                AppLogger.w("SpeakerTest", "Calibration minimum buffer unavailable: " + minBufferBytes);
                return;
            }
            int bufferBytes = Math.max(minBufferBytes * 2, 8192);
            track = buildTrack(bufferBytes, communicationTrack);
            if (track.getState() != AudioTrack.STATE_INITIALIZED) {
                AppLogger.w("SpeakerTest", "Calibration AudioTrack was not initialized for " + routeName(device, speakerNumber));
                return;
            }
            AudioAttributes focusAttributes = calibrationAudioAttributes(communicationTrack);
            boolean focusGranted = requestCalibrationFocus(audioManager, focusAttributes, communicationTrack);
            boolean preferred = output != null && track.setPreferredDevice(output.device);
            ensureCalibrationVolume(audioManager, communicationTrack);
            track.setVolume(1.0f);
            synchronized (this) {
                currentTrack = track;
            }
            track.play();
            int written = writePcm(track, pcm, bufferBytes);
            int bytesPerFrame = communicationTrack ? 2 : 4;
            int targetFrames = CalibrationPlaybackWaitPolicy.framesFromBytes(written, bytesPerFrame);
            long drainTimeoutMs = CalibrationPlaybackWaitPolicy.timeoutMs(
                    targetFrames,
                    communicationTrack ? SCO_SAMPLE_RATE : SAMPLE_RATE
            );
            AppLogger.i(
                    "SpeakerTest",
                    "Test " + speakerNumber + " started for " + routeName(device, speakerNumber)
                            + ", mode=" + (communicationTrack ? "communication-sco" : "media")
                            + (routePlan.useDefaultMedia ? "-default-after-a2dp-activation" : "")
                            + ", preferred=" + describe(output == null ? null : output.device)
                            + ", preferredAccepted=" + preferred
                            + ", routed=" + describe(track.getRoutedDevice())
                            + ", focusGranted=" + focusGranted
                            + ", bytes=" + written
                            + ", targetFrames=" + targetFrames
                            + ", drainTimeoutMs=" + drainTimeoutMs
            );
            waitForPlaybackDrain(track, targetFrames, drainTimeoutMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            AppLogger.w("SpeakerTest", "Calibration interrupted for " + routeName(device, speakerNumber));
        } catch (RuntimeException exception) {
            AppLogger.e("SpeakerTest", "Calibration failed for " + routeName(device, speakerNumber), exception);
        } finally {
            synchronized (this) {
                if (currentTrack == track) {
                    currentTrack = null;
                }
            }
            releaseTrack(track);
            abandonCalibrationFocus();
            stopCommunicationFallback();
            AppLogger.i("SpeakerTest", "Test " + speakerNumber + " finished for " + routeName(device, speakerNumber));
        }
    }

    private OutputBinding waitForMatchingOutput(
            AudioManager audioManager,
            StreamDevice device,
            int attempts,
            long delayMs
    ) throws InterruptedException {
        for (int attempt = 1; attempt <= attempts; attempt++) {
            Thread.sleep(delayMs);
            List<OutputBinding> refreshedOutputs = bluetoothOutputs(audioManager);
            OutputBinding refreshed = matchingOutput(device, refreshedOutputs);
            if (refreshed != null) {
                AppLogger.i(
                        "SpeakerTest",
                        "Matched A2DP route for " + routeName(device, 0)
                                + " after delayed rescan attempt " + attempt + "/" + attempts
                );
                return refreshed;
            }
        }
        AppLogger.w(
                "SpeakerTest",
                "No matching A2DP route for " + routeName(device, 0)
                        + " after delayed route rescans"
        );
        return null;
    }

    private AudioTrack buildTrack(int bufferBytes, boolean communicationTrack) {
        AudioAttributes attributes = calibrationAudioAttributes(communicationTrack);
        AudioFormat format = new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(communicationTrack ? SCO_SAMPLE_RATE : SAMPLE_RATE)
                .setChannelMask(communicationTrack ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO)
                .build();
        return new AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
    }

    private AudioAttributes calibrationAudioAttributes(boolean communicationTrack) {
        AudioAttributes.Builder attributes = new AudioAttributes.Builder();
        if (communicationTrack) {
            attributes.setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setLegacyStreamType(AudioManager.STREAM_VOICE_CALL);
        } else {
            attributes.setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            attributes.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_NONE);
        }
        return attributes.build();
    }

    private boolean requestCalibrationFocus(
            AudioManager audioManager,
            AudioAttributes attributes,
            boolean communicationTrack
    ) {
        if (!CalibrationAudioFocusPolicy.shouldRequestTransientFocus(communicationTrack)) {
            return true;
        }
        try {
            int result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioFocusRequest request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(attributes)
                        .setAcceptsDelayedFocusGain(false)
                        .setWillPauseWhenDucked(false)
                        .build();
                result = audioManager.requestAudioFocus(request);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    activeFocusRequest = request;
                    AppLogger.i("SpeakerTest", "Calibration audio focus granted");
                    return true;
                }
            } else {
                result = audioManager.requestAudioFocus(
                        null,
                        communicationTrack ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                );
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    legacyFocusActive = true;
                    AppLogger.i("SpeakerTest", "Calibration legacy audio focus granted");
                    return true;
                }
            }
            AppLogger.w("SpeakerTest", "Calibration audio focus not granted: result=" + result);
        } catch (RuntimeException exception) {
            AppLogger.w("SpeakerTest", "Calibration audio focus request failed: " + exception.getClass().getSimpleName());
        }
        return false;
    }

    private void abandonCalibrationFocus() {
        AudioManager audioManager = activeAudioManager;
        if (audioManager == null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        if (audioManager == null) {
            activeFocusRequest = null;
            legacyFocusActive = false;
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activeFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(activeFocusRequest);
                AppLogger.i("SpeakerTest", "Calibration audio focus abandoned");
            } else if (legacyFocusActive) {
                audioManager.abandonAudioFocus(null);
                AppLogger.i("SpeakerTest", "Calibration legacy audio focus abandoned");
            }
        } catch (RuntimeException exception) {
            AppLogger.w("SpeakerTest", "Calibration audio focus abandon failed: " + exception.getClass().getSimpleName());
        } finally {
            activeFocusRequest = null;
            legacyFocusActive = false;
        }
    }

    private void ensureCalibrationVolume(AudioManager audioManager, boolean communicationTrack) {
        int streamType = communicationTrack ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC;
        try {
            int minVolume = audioManager.getStreamMinVolume(streamType);
            int maxVolume = audioManager.getStreamMaxVolume(streamType);
            int target = CalibrationVolumePolicy.targetIndex(minVolume, maxVolume);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioManager.isStreamMute(streamType)) {
                audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_UNMUTE, 0);
                AppLogger.i("SpeakerTest", "Calibration stream unmuted: stream=" + streamType);
            }
            int current = audioManager.getStreamVolume(streamType);
            if (current < target) {
                audioManager.setStreamVolume(streamType, target, 0);
                AppLogger.i(
                        "SpeakerTest",
                        "Calibration volume restored: stream=" + streamType
                                + ", from=" + current + ", target=" + target + "/" + maxVolume
                );
            } else {
                AppLogger.d(
                        "SpeakerTest",
                        "Calibration volume already audible: stream=" + streamType
                                + ", current=" + current + ", target=" + target + "/" + maxVolume
                );
            }
        } catch (RuntimeException exception) {
            AppLogger.w("SpeakerTest", "Calibration volume restore failed: " + exception.getClass().getSimpleName());
        }
    }

    private int writePcm(AudioTrack track, byte[] pcm, int bufferBytes) {
        int offset = 0;
        int writtenTotal = 0;
        while (offset < pcm.length) {
            int bytes = Math.min(bufferBytes, pcm.length - offset);
            int written = track.write(pcm, offset, bytes, AudioTrack.WRITE_BLOCKING);
            if (written < 0) {
                AppLogger.w("SpeakerTest", "Calibration write returned " + written);
                return writtenTotal;
            }
            if (written == 0) {
                Thread.yield();
                continue;
            }
            offset += written;
            writtenTotal += written;
        }
        return writtenTotal;
    }

    private void waitForPlaybackDrain(AudioTrack track, int targetFrames, long timeoutMs) throws InterruptedException {
        long startedMs = SystemClock.elapsedRealtime();
        int playedFrames = 0;
        while (SystemClock.elapsedRealtime() - startedMs < timeoutMs) {
            playedFrames = track.getPlaybackHeadPosition();
            if (targetFrames > 0 && playedFrames >= targetFrames) {
                AppLogger.i(
                        "SpeakerTest",
                        "Calibration playback drained: playedFrames=" + playedFrames
                                + "/" + targetFrames
                                + ", waitedMs=" + (SystemClock.elapsedRealtime() - startedMs)
                );
                return;
            }
            Thread.sleep(PLAYBACK_DRAIN_POLL_MS);
        }
        AppLogger.i(
                "SpeakerTest",
                "Calibration playback drain wait ended: playedFrames=" + playedFrames
                        + "/" + targetFrames
                        + ", timeoutMs=" + timeoutMs
        );
    }

    private OutputBinding matchingOutput(StreamDevice route, List<OutputBinding> outputs) {
        List<Integer> matches = AudioOutputRouteMatcher.match(
                Collections.singletonList(route),
                descriptors(outputs)
        );
        if (matches.isEmpty() || matches.get(0) < 0 || matches.get(0) >= outputs.size()) {
            return null;
        }
        OutputBinding output = outputs.get(matches.get(0));
        AudioOutputRouteMatcher.OutputDeviceDescriptor descriptor = descriptors(outputs).get(matches.get(0));
        return AudioOutputRouteSupport.isDirectMediaRoute(descriptor) ? output : null;
    }

    private OutputBinding firstDirectMediaOutput(List<OutputBinding> outputs) {
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> descriptors = descriptors(outputs);
        for (int i = 0; i < outputs.size(); i++) {
            if (AudioOutputRouteSupport.isDirectMediaRoute(descriptors.get(i))) {
                return outputs.get(i);
            }
        }
        return null;
    }

    private AudioDeviceInfo startCommunicationFallback(AudioManager audioManager, StreamDevice route) {
        BluetoothHeadsetRouteActivator.Result activation = headsetRouteActivator.activate(route, 700L);
        if (activation.attempted) {
            AppLogger.i(
                    "SpeakerTest",
                    "Headset/SCO route activation for " + routeName(route, 0) + ": " + activation.message
            );
        } else {
            AppLogger.d(
                    "SpeakerTest",
                    "Headset/SCO route activation skipped for " + routeName(route, 0) + ": " + activation.message
            );
        }
        if (!activation.accepted) {
            AppLogger.w("SpeakerTest", "Targeted Headset/SCO route was not accepted for " + routeName(route, 0));
            return null;
        }
        AudioDeviceInfo genericScoDevice = firstBluetoothScoOutput(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS));
        if (genericScoDevice == null) {
            AppLogger.w("SpeakerTest", "No Bluetooth SCO route is exposed for " + routeName(route, 0));
            return null;
        }
        previousAudioMode = audioManager.getMode();
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        activeAudioManager = audioManager;
        boolean communicationDeviceSet = false;
        AudioDeviceInfo selectedCommunicationDevice = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AudioDeviceInfo communicationDevice = preferredCommunicationDevice(audioManager, route);
            if (communicationDevice == null) {
                AppLogger.w(
                        "SpeakerTest",
                        "No matching Bluetooth communication route is exposed for " + routeName(route, 0)
                                + "; generic SCO output will not be reused"
                );
                audioManager.setMode(previousAudioMode);
                activeAudioManager = null;
                return null;
            }
            if (communicationDevice != null) {
                communicationDeviceSet = audioManager.setCommunicationDevice(communicationDevice);
                selectedCommunicationDevice = communicationDevice;
                AppLogger.i(
                        "SpeakerTest",
                        "Communication route requested for " + routeName(route, 0)
                                + ", requested=" + describe(communicationDevice)
                                + ", accepted=" + communicationDeviceSet
                );
            }
        } else {
            audioManager.startBluetoothSco();
            communicationDeviceSet = true;
            selectedCommunicationDevice = genericScoDevice;
            AppLogger.i("SpeakerTest", "Legacy Bluetooth SCO start requested for " + routeName(route, 0));
        }
        communicationFallbackActive = communicationDeviceSet;
        if (!communicationDeviceSet) {
            AppLogger.w("SpeakerTest", "Communication route could not be set for " + routeName(route, 0));
            return null;
        }
        waitForCommunicationRoute();
        return selectedCommunicationDevice;
    }

    private void waitForCommunicationRoute() {
        try {
            Thread.sleep(850L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            AppLogger.w("SpeakerTest", "Communication route settle interrupted");
        }
    }

    private void stopCommunicationFallback() {
        AudioManager audioManager = activeAudioManager;
        if (audioManager == null) {
            return;
        }
        if (communicationFallbackActive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.clearCommunicationDevice();
            } else {
                audioManager.stopBluetoothSco();
            }
            AppLogger.i("SpeakerTest", "Communication route stopped");
        }
        audioManager.setMode(previousAudioMode);
        activeAudioManager = null;
        communicationFallbackActive = false;
        previousAudioMode = AudioManager.MODE_NORMAL;
    }

    private void releaseTrack(AudioTrack track) {
        if (track == null) {
            return;
        }
        try {
            if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop();
            }
        } catch (RuntimeException ignored) {
            // The platform may stop the track while route changes are in progress.
        }
        track.release();
    }

    private List<OutputBinding> bluetoothOutputs(AudioManager audioManager) {
        ArrayList<OutputBinding> outputs = new ArrayList<>();
        for (AudioDeviceInfo device : audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            if (device.isSink() && isBluetoothOutput(device)) {
                outputs.add(new OutputBinding(device));
                AppLogger.d("SpeakerTest", "Bluetooth output: " + describe(device));
            }
        }
        return outputs;
    }

    private List<AudioOutputRouteMatcher.OutputDeviceDescriptor> descriptors(List<OutputBinding> outputs) {
        ArrayList<AudioOutputRouteMatcher.OutputDeviceDescriptor> descriptors = new ArrayList<>();
        for (OutputBinding output : outputs) {
            descriptors.add(new AudioOutputRouteMatcher.OutputDeviceDescriptor(
                    name(output.device),
                    output.device.getAddress(),
                    output.device.getType()
            ));
        }
        return descriptors;
    }

    private AudioDeviceInfo preferredCommunicationDevice(AudioManager audioManager, StreamDevice route) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return null;
        }
        for (AudioDeviceInfo device : audioManager.getAvailableCommunicationDevices()) {
            if (!isBluetoothSco(device)) {
                continue;
            }
            AppLogger.d("SpeakerTest", "Communication device: " + describe(device));
            if (matchesRoute(route, device)) {
                return device;
            }
        }
        return null;
    }

    private AudioDeviceInfo firstBluetoothScoOutput(AudioDeviceInfo[] devices) {
        if (devices == null) {
            return null;
        }
        for (AudioDeviceInfo device : devices) {
            if (device != null && device.isSink() && isBluetoothSco(device)) {
                return device;
            }
        }
        return null;
    }

    private boolean matchesRoute(StreamDevice route, AudioDeviceInfo device) {
        String routeAddress = route == null ? "" : normalizeAddress(route.address);
        String deviceAddress = normalizeAddress(device == null ? "" : device.getAddress());
        if (!routeAddress.isEmpty() && routeAddress.equals(deviceAddress)) {
            return true;
        }
        String routeName = route == null || route.name == null ? "" : route.name.trim();
        String deviceName = device == null ? "" : name(device).trim();
        return !routeName.isEmpty() && routeName.equalsIgnoreCase(deviceName);
    }

    private boolean isBluetoothOutput(AudioDeviceInfo device) {
        int type = device.getType();
        if (type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return type == AudioDeviceInfo.TYPE_BLE_HEADSET
                    || type == AudioDeviceInfo.TYPE_BLE_SPEAKER
                    || type == AudioDeviceInfo.TYPE_HEARING_AID;
        }
        return type == AudioDeviceInfo.TYPE_HEARING_AID;
    }

    private boolean isBluetoothSco(AudioDeviceInfo device) {
        return device != null && device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO;
    }

    private String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        String upper = value.toUpperCase(Locale.US);
        for (int i = 0; i < upper.length(); i++) {
            char c = upper.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private String routeName(StreamDevice device, int speakerNumber) {
        if (device == null || device.name == null || device.name.trim().isEmpty()) {
            return speakerNumber > 0 ? "speaker " + speakerNumber : "speaker";
        }
        return device.name;
    }

    private String describe(AudioDeviceInfo device) {
        if (device == null) {
            return "default";
        }
        return name(device) + "@" + device.getAddress() + "#type=" + device.getType();
    }

    private String name(AudioDeviceInfo device) {
        CharSequence productName = device.getProductName();
        return productName == null ? "" : productName.toString();
    }

    private static final class OutputBinding {
        final AudioDeviceInfo device;

        OutputBinding(AudioDeviceInfo device) {
            this.device = device;
        }
    }
}
