package com.xpwnit.dualbt.bt;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.xpwnit.dualbt.logging.AppLogger;
import com.xpwnit.dualbt.state.StreamDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class AndroidBluetoothScanner {
    private final BluetoothSpeakerCatalog catalog = new BluetoothSpeakerCatalog();

    public ScanResult scan(Context context, boolean allowMockFallback) {
        if (context == null) {
            return fallback(allowMockFallback, "Context unavailable");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return fallback(allowMockFallback, "Bluetooth permission missing");
        }
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();
        if (adapter == null) {
            return fallback(allowMockFallback, "Bluetooth adapter unavailable");
        }
        if (!adapter.isEnabled()) {
            return fallback(allowMockFallback, "Bluetooth adapter disabled");
        }
        try {
            Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
            ArrayList<BluetoothSpeakerCatalog.Candidate> candidates = new ArrayList<>();
            for (BluetoothDevice device : bondedDevices) {
                BluetoothClass bluetoothClass = device.getBluetoothClass();
                candidates.add(new BluetoothSpeakerCatalog.Candidate(
                        device.getName(),
                        device.getAddress(),
                        subtitleFor(bluetoothClass),
                        isAudioDevice(bluetoothClass)
                ));
            }
            List<StreamDevice> devices = catalog.toStreamDevices(candidates, allowMockFallback);
            boolean mockFallback = candidates.isEmpty() || devicesAreMock(devices);
            AppLogger.i("BTScanner", "Bonded Bluetooth scan: candidates=" + candidates.size() + ", devices=" + devices.size() + ", mock=" + mockFallback);
            return new ScanResult(devices, mockFallback, mockFallback ? "Using mock speaker fallback" : "Using bonded Bluetooth audio devices");
        } catch (SecurityException exception) {
            AppLogger.e("BTScanner", "Bluetooth scan failed due to permissions", exception);
            return fallback(allowMockFallback, "Bluetooth permission denied");
        }
    }

    private ScanResult fallback(boolean allowMockFallback, String reason) {
        List<StreamDevice> devices = catalog.toStreamDevices(new ArrayList<>(), allowMockFallback);
        AppLogger.w("BTScanner", reason + "; devices=" + devices.size());
        return new ScanResult(devices, allowMockFallback && !devices.isEmpty(), reason);
    }

    private boolean isAudioDevice(BluetoothClass bluetoothClass) {
        if (bluetoothClass == null) {
            return false;
        }
        if (bluetoothClass.getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO) {
            return true;
        }
        int deviceClass = bluetoothClass.getDeviceClass();
        return deviceClass == BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER
                || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES
                || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO
                || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO
                || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET;
    }

    private String subtitleFor(BluetoothClass bluetoothClass) {
        if (bluetoothClass == null) {
            return "Bonded Bluetooth audio";
        }
        int deviceClass = bluetoothClass.getDeviceClass();
        if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER) {
            return "Bonded Bluetooth loudspeaker";
        }
        if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES) {
            return "Bonded Bluetooth headphones";
        }
        if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO) {
            return "Bonded Bluetooth hi-fi audio";
        }
        if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO) {
            return "Bonded Bluetooth car audio";
        }
        if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {
            return "Bonded Bluetooth headset";
        }
        return "Bonded Bluetooth audio";
    }

    private boolean devicesAreMock(List<StreamDevice> devices) {
        if (devices.isEmpty()) {
            return false;
        }
        for (StreamDevice device : devices) {
            if (!device.address.startsWith("00:11:22:33:44:")) {
                return false;
            }
        }
        return true;
    }

    public static final class ScanResult {
        public final List<StreamDevice> devices;
        public final boolean mockFallback;
        public final String reason;

        public ScanResult(List<StreamDevice> devices, boolean mockFallback, String reason) {
            this.devices = devices;
            this.mockFallback = mockFallback;
            this.reason = reason;
        }
    }
}
