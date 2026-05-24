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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class SpeakerCalibrationPlayer {
    private static final int SAMPLE_RATE = 48_000;
    private static final int DURATION_MS = 3_200;

    private final Context context;
    private AudioTrack currentTrack;
    private AudioManager activeAudioManager;
    private boolean communicationFallbackActive;
    private int previousAudioMode = AudioManager.MODE_NORMAL;

    public SpeakerCalibrationPlayer(Context context) {
        this.context = context.getApplicationContext();
    }

    public synchronized void play(StreamDevice device, int speakerNumber) {
        stop();
        Thread worker = new Thread(() -> playBlocking(device, speakerNumber), "dualbt-speaker-test");
        worker.start();
    }

    public synchronized void stop() {
        releaseTrack(currentTrack);
        currentTrack = null;
        stopCommunicationFallback();
    }

    private void playBlocking(StreamDevice device, int speakerNumber) {
        AudioTrack track = null;
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager == null) {
                AppLogger.w("SpeakerTest", "AudioManager unavailable for " + routeName(device, speakerNumber));
                return;
            }
            List<OutputBinding> outputs = bluetoothOutputs(audioManager);
            OutputBinding output = matchingOutput(device, outputs);
            boolean communicationTrack = output == null;
            if (communicationTrack) {
                AudioDeviceInfo communicationDevice = startCommunicationFallback(audioManager, device);
                if (communicationDevice != null) {
                    output = new OutputBinding(communicationDevice);
                }
            }
            byte[] pcm = CalibrationTone.stereoSinePcm(
                    SAMPLE_RATE,
                    DURATION_MS,
                    speakerNumber == 2 ? 880.0 : 660.0,
                    0.72
            );
            int minBufferBytes = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_STEREO,
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
            boolean preferred = output != null && track.setPreferredDevice(output.device);
            track.setVolume(1.0f);
            synchronized (this) {
                currentTrack = track;
            }
            track.play();
            int written = writePcm(track, pcm, bufferBytes);
            AppLogger.i(
                    "SpeakerTest",
                    "Test " + speakerNumber + " started for " + routeName(device, speakerNumber)
                            + ", mode=" + (communicationTrack ? "communication-sco" : "media")
                            + ", preferred=" + describe(output == null ? null : output.device)
                            + ", preferredAccepted=" + preferred
                            + ", routed=" + describe(track.getRoutedDevice())
                            + ", bytes=" + written
            );
            Thread.sleep(300L);
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
            stopCommunicationFallback();
            AppLogger.i("SpeakerTest", "Test " + speakerNumber + " finished for " + routeName(device, speakerNumber));
        }
    }

    private AudioTrack buildTrack(int bufferBytes, boolean communicationTrack) {
        AudioAttributes.Builder attributes = new AudioAttributes.Builder()
                .setUsage(communicationTrack ? AudioAttributes.USAGE_VOICE_COMMUNICATION : AudioAttributes.USAGE_MEDIA)
                .setContentType(communicationTrack ? AudioAttributes.CONTENT_TYPE_SPEECH : AudioAttributes.CONTENT_TYPE_MUSIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            attributes.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_NONE);
        }
        AudioFormat format = new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build();
        return new AudioTrack.Builder()
                .setAudioAttributes(attributes.build())
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
    }

    private int writePcm(AudioTrack track, byte[] pcm, int bufferBytes) {
        int offset = 0;
        int writtenTotal = 0;
        while (offset < pcm.length) {
            int bytes = Math.min(bufferBytes, pcm.length - offset);
            int written = track.write(pcm, offset, bytes);
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

    private OutputBinding matchingOutput(StreamDevice route, List<OutputBinding> outputs) {
        List<Integer> matches = AudioOutputRouteMatcher.match(
                Collections.singletonList(route),
                descriptors(outputs)
        );
        if (matches.isEmpty() || matches.get(0) < 0 || matches.get(0) >= outputs.size()) {
            return null;
        }
        return outputs.get(matches.get(0));
    }

    private AudioDeviceInfo startCommunicationFallback(AudioManager audioManager, StreamDevice route) {
        AudioDeviceInfo device = firstBluetoothScoOutput(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS));
        if (device == null) {
            AppLogger.w("SpeakerTest", "No Bluetooth SCO route is exposed for " + routeName(route, 0));
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
                        "SpeakerTest",
                        "Communication route requested for " + routeName(route, 0)
                                + ", requested=" + describe(communicationDevice)
                                + ", accepted=" + communicationDeviceSet
                );
            }
        } else {
            audioManager.startBluetoothSco();
            communicationDeviceSet = true;
            AppLogger.i("SpeakerTest", "Legacy Bluetooth SCO start requested for " + routeName(route, 0));
        }
        communicationFallbackActive = communicationDeviceSet;
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
        AudioDeviceInfo fallback = null;
        for (AudioDeviceInfo device : audioManager.getAvailableCommunicationDevices()) {
            if (!isBluetoothSco(device)) {
                continue;
            }
            AppLogger.d("SpeakerTest", "Communication device: " + describe(device));
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
