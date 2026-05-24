package com.xpwnit.dualbt.audio;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

import com.xpwnit.dualbt.state.StreamRoutePlan;

import java.util.ArrayList;
import java.util.List;

public final class AndroidAudioRouteAvailability {
    private AndroidAudioRouteAvailability() {
    }

    public static Result check(Context context, StreamRoutePlan routePlan) {
        if (context == null) {
            return new Result(false, 0, "Android audio context is unavailable");
        }
        if (routePlan == null || routePlan.targetCount() != 2) {
            return new Result(false, 0, "Route plan must contain exactly 2 speakers");
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return new Result(false, 0, "Android AudioManager is unavailable");
        }
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = bluetoothOutputDescriptors(audioManager);
        List<Integer> matches = AudioOutputRouteMatcher.match(routePlan.targets(), outputs);
        int directRoutes = AudioOutputRouteSupport.directMediaRouteCount(matches, outputs);
        boolean supported = directRoutes >= 2;
        String message = supported
                ? "Android exposes 2/2 direct media routes"
                : "Android exposes " + directRoutes + "/2 direct media route(s). Generic SCO fallback is disabled.";
        return new Result(supported, directRoutes, message);
    }

    private static List<AudioOutputRouteMatcher.OutputDeviceDescriptor> bluetoothOutputDescriptors(AudioManager audioManager) {
        ArrayList<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = new ArrayList<>();
        for (AudioDeviceInfo device : audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            if (device != null && device.isSink() && isBluetoothOutput(device)) {
                outputs.add(new AudioOutputRouteMatcher.OutputDeviceDescriptor(
                        name(device),
                        device.getAddress(),
                        device.getType()
                ));
            }
        }
        return outputs;
    }

    private static boolean isBluetoothOutput(AudioDeviceInfo device) {
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

    private static String name(AudioDeviceInfo device) {
        CharSequence productName = device.getProductName();
        return productName == null ? "" : productName.toString();
    }

    public static final class Result {
        public final boolean supported;
        public final int directMediaRoutes;
        public final String message;

        Result(boolean supported, int directMediaRoutes, String message) {
            this.supported = supported;
            this.directMediaRoutes = directMediaRoutes;
            this.message = message;
        }
    }
}
