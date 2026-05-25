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
import java.util.Collections;
import java.util.List;

public final class AndroidAudioOutputRouter {
    private static final int SCO_SAMPLE_RATE = 16_000;

    private final Context context;
    private final BluetoothA2dpRouteActivator routeActivator;
    private final BluetoothHeadsetRouteActivator headsetRouteActivator;
    private final SystemMediaRouteGroupController systemRouteGroupController;
    private final ArrayList<AudioTrack> tracks = new ArrayList<>();
    private final ArrayList<String> trackRoutes = new ArrayList<>();
    private final ArrayList<Boolean> communicationTracks = new ArrayList<>();
    private final ArrayList<Pcm16ScoConverter> scoConverters = new ArrayList<>();
    private final ArrayList<byte[]> scoBuffers = new ArrayList<>();
    private AudioManager activeAudioManager;
    private boolean communicationFallbackActive;
    private boolean systemRouteGroupActive;
    private int previousAudioMode = AudioManager.MODE_NORMAL;
    private long writes;

    public AndroidAudioOutputRouter(Context context) {
        this.context = context.getApplicationContext();
        this.routeActivator = new BluetoothA2dpRouteActivator(this.context);
        this.headsetRouteActivator = new BluetoothHeadsetRouteActivator(this.context);
        this.systemRouteGroupController = new SystemMediaRouteGroupController(this.context);
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
        HybridBluetoothSplitPlanner.Plan hybridPlan = HybridBluetoothSplitPlanner.plan(matches, descriptors);
        boolean hasTwoDirectRoutes = AudioOutputRouteSupport.hasTwoDirectMediaRoutes(matches, descriptors);
        boolean hybridScoSplit = !hasTwoDirectRoutes && hybridPlan.useHybridSplit;
        boolean activeA2dpHandoff = !hasTwoDirectRoutes && !hybridScoSplit && outputPlan.useActiveA2dpHandoff;
        SystemMediaRouteGroupController.Result systemRouteGroup = !hasTwoDirectRoutes && !hybridScoSplit
                ? systemRouteGroupController.inspect(routePlan)
                : SystemMediaRouteGroupController.Result.unavailable("Direct media routes are available", "Direct routes available");
        boolean systemRouteGroupProbe = !systemRouteGroup.supported
                && activeA2dpHandoff
                && SystemMediaRouteGroupController.canProbeSystemRouteGroup();
        if (systemRouteGroup.attemptedSelection || systemRouteGroup.supported) {
            AppLogger.i("AudioOutputRouter", "System route group: " + systemRouteGroup.message);
        }
        StreamingRouteCapabilityPolicy.Result capability = StreamingRouteCapabilityPolicy.evaluate(
                AudioOutputRouteSupport.directMediaRouteCount(matches, descriptors),
                hybridScoSplit,
                activeA2dpHandoff,
                systemRouteGroup.active,
                systemRouteGroupProbe
        );
        if (!capability.supported) {
            AppLogger.w("AudioOutputRouter", "Dual Bluetooth output blocked: " + capability.message);
            return false;
        }
        if (capability.decision == StreamingRouteCapabilityPolicy.Decision.SYSTEM_ROUTE_GROUP
                || capability.decision == StreamingRouteCapabilityPolicy.Decision.SYSTEM_ROUTE_GROUP_PROBE) {
            return startSystemRouteGroupOutput(spec, routePlan);
        }
        if (hybridScoSplit) {
            AppLogger.w(
                    "AudioOutputRouter",
                    "Using experimental hybrid Bluetooth split: one A2DP media route plus targeted Headset/SCO for "
                            + routePlan.targets().get(hybridPlan.scoRouteIndex).name
            );
        }
        AudioDeviceInfo communicationDevice = null;
        if (outputPlan.useCommunicationFallback) {
            communicationDevice = startCommunicationFallback(audioManager, routePlan.targets().get(outputPlan.communicationRouteIndex), false);
        } else if (hybridScoSplit) {
            communicationDevice = startCommunicationFallback(audioManager, routePlan.targets().get(hybridPlan.scoRouteIndex), true);
            if (communicationDevice == null) {
                AppLogger.w(
                        "AudioOutputRouter",
                        "Hybrid Bluetooth split could not prepare Headset/SCO route for "
                                + routePlan.targets().get(hybridPlan.scoRouteIndex).name
                );
                stop();
                return false;
            }
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
            boolean communicationTrack = ((outputPlan.useCommunicationFallback
                    && routeIndex == outputPlan.communicationRouteIndex)
                    || (hybridScoSplit && routeIndex == hybridPlan.scoRouteIndex))
                    && communicationDevice != null;
            if (communicationTrack) {
                output = new OutputBinding(communicationDevice);
            }
            int routeBufferBytes = bufferBytes;
            if (communicationTrack) {
                int scoMinBufferBytes = AudioTrack.getMinBufferSize(
                        SCO_SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                );
                if (scoMinBufferBytes <= 0) {
                    AppLogger.w("AudioOutputRouter", "SCO AudioTrack minimum buffer unavailable: " + scoMinBufferBytes);
                    stop();
                    return false;
                }
                routeBufferBytes = Math.max(scoMinBufferBytes * 4, 4096);
            }
            AudioTrack track = buildTrack(spec, routeBufferBytes, communicationTrack);
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
            communicationTracks.add(communicationTrack);
            scoConverters.add(communicationTrack ? new Pcm16ScoConverter(spec.sampleRate(), SCO_SAMPLE_RATE) : null);
            scoBuffers.add(null);
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
        if (systemRouteGroupActive) {
            if (tracks.isEmpty() || bytes <= 0) {
                return;
            }
            writeTrack(0, firstOutput, bytes);
            writes++;
            if (writes == 1 || writes % 500 == 0) {
                AppLogger.d("AudioOutputRouter", "PCM writes=" + writes + ", bytes=" + bytes + ", routes=" + routeNames());
            }
            return;
        }
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
        communicationTracks.clear();
        scoConverters.clear();
        scoBuffers.clear();
        systemRouteGroupActive = false;
        stopCommunicationFallback();
    }

    private boolean startSystemRouteGroupOutput(
            AudioCaptureSpec spec,
            StreamRoutePlan routePlan
    ) {
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
        AudioTrack track = buildTrack(spec, bufferBytes, false);
        if (track.getState() != AudioTrack.STATE_INITIALIZED) {
            track.release();
            AppLogger.w("AudioOutputRouter", "System route group AudioTrack was not initialized");
            return false;
        }
        track.play();
        byte[] routeProbeBuffer = new byte[Math.min(4096, bufferBytes)];
        int probeWritten = track.write(routeProbeBuffer, 0, routeProbeBuffer.length);
        AppLogger.i("AudioOutputRouter", "System route group probe track started, silentProbeBytes=" + probeWritten);
        SystemMediaRouteGroupController.Result systemRouteGroup = systemRouteGroupController.prepare(routePlan);
        if (!systemRouteGroup.active) {
            try {
                track.stop();
            } catch (RuntimeException ignored) {
                // The platform may stop a short-lived probe track itself.
            }
            track.release();
            AppLogger.w("AudioOutputRouter", "System route group probe failed: " + systemRouteGroup.message);
            return false;
        }
        tracks.add(track);
        trackRoutes.add("system route group: " + routePlan.displayNames());
        communicationTracks.add(false);
        scoConverters.add(null);
        scoBuffers.add(null);
        systemRouteGroupActive = true;
        writes = 0L;
        AppLogger.i(
                "AudioOutputRouter",
                "System route group output started for " + routePlan.displayNames()
                        + ", status=" + systemRouteGroup.message
                        + ", routed=" + describe(track.getRoutedDevice())
        );
        return true;
    }

    private void writeTrack(int index, byte[] data, int bytes) {
        AudioTrack track = tracks.get(index);
        int written;
        if (Boolean.TRUE.equals(communicationTracks.get(index))) {
            Pcm16ScoConverter converter = scoConverters.get(index);
            if (converter == null) {
                AppLogger.w("AudioOutputRouter", "Track " + index + " has no SCO converter");
                return;
            }
            int capacity = converter.outputCapacityBytes(bytes);
            byte[] buffer = scoBuffers.get(index);
            if (buffer == null || buffer.length < capacity) {
                buffer = new byte[capacity];
                scoBuffers.set(index, buffer);
            }
            int convertedBytes = converter.convert(data, bytes, buffer);
            if (convertedBytes <= 0) {
                return;
            }
            written = track.write(buffer, 0, convertedBytes);
        } else {
            written = track.write(data, 0, bytes);
        }
        if (written < 0) {
            AppLogger.w("AudioOutputRouter", "Track " + index + " write returned " + written);
        }
    }

    private AudioTrack buildTrack(AudioCaptureSpec spec, int bufferBytes, boolean communicationTrack) {
        AudioAttributes.Builder attributes = new AudioAttributes.Builder();
        if (communicationTrack) {
            attributes.setLegacyStreamType(AudioManager.STREAM_VOICE_CALL);
        } else {
            attributes.setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            attributes.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_NONE);
        }
        AudioFormat format = new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(communicationTrack ? SCO_SAMPLE_RATE : spec.sampleRate())
                .setChannelMask(communicationTrack ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO)
                .build();
        return new AudioTrack.Builder()
                .setAudioAttributes(attributes.build())
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
    }

    private AudioDeviceInfo startCommunicationFallback(AudioManager audioManager, StreamDevice route, boolean requireTargetedActivation) {
        BluetoothHeadsetRouteActivator.Result activation = headsetRouteActivator.activate(route, 350L);
        if (activation.attempted) {
            AppLogger.i(
                    "AudioOutputRouter",
                    "Headset/SCO route activation for " + route.name + ": " + activation.message
            );
        } else {
            AppLogger.d(
                    "AudioOutputRouter",
                    "Headset/SCO route activation skipped for " + route.name + ": " + activation.message
            );
        }
        if (requireTargetedActivation && !activation.accepted) {
            AppLogger.w("AudioOutputRouter", "Targeted Headset/SCO route was not accepted for " + route.name);
            return null;
        }
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
            return null;
        } else {
            waitForCommunicationRoute();
        }
        return device;
    }

    private void waitForCommunicationRoute() {
        try {
            Thread.sleep(850L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            AppLogger.w("AudioOutputRouter", "Communication route settle interrupted");
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
        for (AudioDeviceInfo device : audioManager.getAvailableCommunicationDevices()) {
            if (!isBluetoothSco(device)) {
                continue;
            }
            AppLogger.d("AudioOutputRouter", "Communication device: " + describe(device));
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

    private OutputBinding matchingOutput(StreamDevice route, List<OutputBinding> outputs) {
        List<Integer> routeMatches = AudioOutputRouteMatcher.match(
                Collections.singletonList(route),
                descriptors(outputs)
        );
        if (routeMatches.isEmpty() || routeMatches.get(0) < 0 || routeMatches.get(0) >= outputs.size()) {
            return null;
        }
        AudioOutputRouteMatcher.OutputDeviceDescriptor descriptor = descriptors(outputs).get(routeMatches.get(0));
        if (!AudioOutputRouteSupport.isDirectMediaRoute(descriptor)) {
            return null;
        }
        return outputs.get(routeMatches.get(0));
    }

    private OutputBinding firstDirectMediaOutput(List<OutputBinding> outputs) {
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputDescriptors = descriptors(outputs);
        for (int i = 0; i < outputs.size(); i++) {
            if (AudioOutputRouteSupport.isDirectMediaRoute(outputDescriptors.get(i))) {
                return outputs.get(i);
            }
        }
        return null;
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
