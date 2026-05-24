package com.xpwnit.dualbt.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

import com.xpwnit.dualbt.logging.AppLogger;
import com.xpwnit.dualbt.state.StreamDevice;
import com.xpwnit.dualbt.state.StreamRoutePlan;

import java.util.ArrayList;
import java.util.List;

public final class AndroidAudioOutputRouter {
    private final Context context;
    private final ArrayList<AudioTrack> tracks = new ArrayList<>();
    private final ArrayList<String> trackRoutes = new ArrayList<>();
    private AudioManager activeAudioManager;
    private boolean communicationFallbackActive;
    private int previousAudioMode = AudioManager.MODE_NORMAL;
    private long writes;

    public AndroidAudioOutputRouter(Context context) {
        this.context = context.getApplicationContext();
    }

    public synchronized boolean start(AudioCaptureSpec spec, StreamRoutePlan routePlan) {
        stop();
        if (routePlan == null || routePlan.targetCount() != 2) {
            AppLogger.w("AudioOutputRouter", "Cannot start output router without exactly 2 routes");
            return false;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            AppLogger.w("AudioOutputRouter", "AudioManager unavailable");
            return false;
        }
        List<OutputBinding> outputs = bluetoothOutputs(audioManager);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> descriptors = descriptors(outputs);
        List<Integer> matches = AudioOutputRouteMatcher.match(routePlan.targets(), descriptors);
        AudioOutputModePlanner.Plan outputPlan = AudioOutputModePlanner.plan(matches, descriptors);
        AudioDeviceInfo communicationDevice = null;
        if (outputPlan.useCommunicationFallback) {
            communicationDevice = startCommunicationFallback(audioManager, routePlan.targets().get(outputPlan.communicationRouteIndex));
        }
        int matchedOutputCount = matchedOutputCount(matches);
        if (matchedOutputCount < routePlan.targetCount()) {
            AppLogger.w(
                    "AudioOutputRouter",
                    "Android exposed " + matchedOutputCount + "/" + routePlan.targetCount()
                            + " matching Bluetooth output route(s); unmatched tracks will use default routing"
            );
        }
        int minBufferBytes = AudioTrack.getMinBufferSize(
                spec.sampleRate(),
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
        );
        if (minBufferBytes <= 0) {
            AppLogger.w("AudioOutputRouter", "AudioTrack minimum buffer unavailable: " + minBufferBytes);
            return false;
        }
        int bufferBytes = Math.max(minBufferBytes * 4, spec.bufferSizeBytes(minBufferBytes));
        int routeIndex = 0;
        for (StreamDevice route : routePlan.targets()) {
            OutputBinding output = outputFor(matches, outputs, routeIndex);
            boolean communicationTrack = outputPlan.useCommunicationFallback
                    && routeIndex == outputPlan.communicationRouteIndex
                    && communicationDevice != null;
            if (communicationTrack) {
                output = new OutputBinding(communicationDevice);
            }
            AudioTrack track = buildTrack(spec, bufferBytes, communicationTrack);
            if (track.getState() != AudioTrack.STATE_INITIALIZED) {
                track.release();
                AppLogger.w("AudioOutputRouter", "AudioTrack was not initialized for " + route.name);
                stop();
                return false;
            }
            boolean preferred = output != null && track.setPreferredDevice(output.device);
            track.play();
            tracks.add(track);
            trackRoutes.add(route.name);
            AppLogger.i(
                    "AudioOutputRouter",
                    "Track " + routeIndex + " started for " + route.name
                            + ", mode=" + (communicationTrack ? "communication-sco" : "media")
                            + ", preferred=" + describe(output == null ? null : output.device)
                            + ", preferredAccepted=" + preferred
                            + ", routed=" + describe(track.getRoutedDevice())
            );
            routeIndex++;
        }
        writes = 0L;
        int distinctRoutes = distinctRoutedDeviceCount();
        if (distinctRoutes < tracks.size()) {
            AppLogger.w(
                    "AudioOutputRouter",
                    "Android routed " + tracks.size() + " output track(s) to "
                            + distinctRoutes + " distinct device route(s); dual-speaker playback may be limited by platform routing"
            );
        }
        AppLogger.i("AudioOutputRouter", "Output router started with " + tracks.size() + " track(s)");
        return tracks.size() == routePlan.targetCount();
    }

    public synchronized void write(byte[] firstOutput, byte[] secondOutput, int bytes) {
        if (tracks.size() < 2 || bytes <= 0) {
            return;
        }
        writeTrack(0, firstOutput, bytes);
        writeTrack(1, secondOutput, bytes);
        writes++;
        if (writes == 1 || writes % 500 == 0) {
            AppLogger.d("AudioOutputRouter", "PCM writes=" + writes + ", bytes=" + bytes + ", routes=" + routeNames());
        }
    }

    public synchronized void stop() {
        for (AudioTrack track : tracks) {
            try {
                if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop();
                }
            } catch (RuntimeException ignored) {
                // AudioTrack may already be stopped by the platform.
            }
            track.release();
        }
        if (!tracks.isEmpty()) {
            AppLogger.i("AudioOutputRouter", "Output router stopped");
        }
        tracks.clear();
        trackRoutes.clear();
        stopCommunicationFallback();
    }

    private void writeTrack(int index, byte[] data, int bytes) {
        AudioTrack track = tracks.get(index);
        int written = track.write(data, 0, bytes);
        if (written < 0) {
            AppLogger.w("AudioOutputRouter", "Track " + index + " write returned " + written);
        }
    }

    private AudioTrack buildTrack(AudioCaptureSpec spec, int bufferBytes, boolean communicationTrack) {
        AudioAttributes.Builder attributes = new AudioAttributes.Builder()
                .setUsage(communicationTrack ? AudioAttributes.USAGE_VOICE_COMMUNICATION : AudioAttributes.USAGE_MEDIA)
                .setContentType(communicationTrack ? AudioAttributes.CONTENT_TYPE_SPEECH : AudioAttributes.CONTENT_TYPE_MUSIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            attributes.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_NONE);
        }
        AudioFormat format = new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(spec.sampleRate())
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build();
        return new AudioTrack.Builder()
                .setAudioAttributes(attributes.build())
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
    }

    private AudioDeviceInfo startCommunicationFallback(AudioManager audioManager, StreamDevice route) {
        AudioDeviceInfo device = firstBluetoothScoOutput(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS));
        if (device == null) {
            AppLogger.w("AudioOutputRouter", "Communication fallback requested but no Bluetooth SCO output is exposed");
            return null;
        }
        previousAudioMode = audioManager.getMode();
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        activeAudioManager = audioManager;
        boolean communicationDeviceSet = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AudioDeviceInfo communicationDevice = preferredCommunicationDevice(audioManager, route);
            if (communicationDevice != null) {
                communicationDeviceSet = audioManager.setCommunicationDevice(communicationDevice);
                AppLogger.i(
                        "AudioOutputRouter",
                        "Communication route requested for " + route.name
                                + ", requested=" + describe(communicationDevice)
                                + ", accepted=" + communicationDeviceSet
                );
            }
        } else {
            audioManager.startBluetoothSco();
            communicationDeviceSet = true;
            AppLogger.i("AudioOutputRouter", "Legacy Bluetooth SCO start requested for " + route.name);
        }
        communicationFallbackActive = communicationDeviceSet;
        if (!communicationDeviceSet) {
            AppLogger.w("AudioOutputRouter", "Communication fallback could not set a communication device");
        }
        return device;
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
            AppLogger.i("AudioOutputRouter", "Communication fallback stopped");
        }
        audioManager.setMode(previousAudioMode);
        activeAudioManager = null;
        communicationFallbackActive = false;
        previousAudioMode = AudioManager.MODE_NORMAL;
    }

    private AudioDeviceInfo preferredCommunicationDevice(AudioManager audioManager, StreamDevice route) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return null;
        }
        AudioDeviceInfo fallback = null;
        for (AudioDeviceInfo device : audioManager.getAvailableCommunicationDevices()) {
            if (!isBluetoothSco(device)) {
                continue;
            }
            AppLogger.d("AudioOutputRouter", "Communication device: " + describe(device));
            if (fallback == null) {
                fallback = device;
            }
            if (matchesRoute(route, device)) {
                return device;
            }
        }
        return fallback;
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

    private List<OutputBinding> bluetoothOutputs(AudioManager audioManager) {
        ArrayList<OutputBinding> outputs = new ArrayList<>();
        for (AudioDeviceInfo device : audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            if (device.isSink() && isBluetoothOutput(device)) {
                outputs.add(new OutputBinding(device));
                AppLogger.d("AudioOutputRouter", "Bluetooth output: " + describe(device));
            }
        }
        return outputs;
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

    private String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        String upper = value.toUpperCase();
        for (int i = 0; i < upper.length(); i++) {
            char c = upper.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                builder.append(c);
            }
        }
        return builder.toString();
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

    private OutputBinding outputFor(List<Integer> matches, List<OutputBinding> outputs, int routeIndex) {
        if (matches == null || routeIndex >= matches.size()) {
            return null;
        }
        int outputIndex = matches.get(routeIndex);
        if (outputIndex < 0 || outputIndex >= outputs.size()) {
            return null;
        }
        return outputs.get(outputIndex);
    }

    private int matchedOutputCount(List<Integer> matches) {
        int count = 0;
        if (matches == null) {
            return 0;
        }
        for (Integer match : matches) {
            if (match != null && match >= 0) {
                count++;
            }
        }
        return count;
    }

    private int distinctRoutedDeviceCount() {
        ArrayList<String> routes = new ArrayList<>();
        for (AudioTrack track : tracks) {
            String route = describe(track.getRoutedDevice());
            if (!routes.contains(route)) {
                routes.add(route);
            }
        }
        return routes.size();
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

    private String routeNames() {
        StringBuilder builder = new StringBuilder();
        for (String route : trackRoutes) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(route);
        }
        return builder.toString();
    }

    private static final class OutputBinding {
        final AudioDeviceInfo device;

        OutputBinding(AudioDeviceInfo device) {
            this.device = device;
        }
    }
}
