package com.xpwnit.dualbt.bt;

import com.xpwnit.dualbt.state.StreamDevice;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class BluetoothSpeakerCatalogTest {
    public static void main(String[] args) {
        BluetoothSpeakerCatalog catalog = new BluetoothSpeakerCatalog();

        List<StreamDevice> devices = catalog.toStreamDevices(Arrays.asList(
                new BluetoothSpeakerCatalog.Candidate("Kitchen Speaker", "AA:BB:CC:00:01", "Loudspeaker", true),
                new BluetoothSpeakerCatalog.Candidate("Keyboard", "AA:BB:CC:00:02", "Peripheral", false),
                new BluetoothSpeakerCatalog.Candidate("Kitchen Speaker Duplicate", "AA:BB:CC:00:01", "Loudspeaker", true),
                new BluetoothSpeakerCatalog.Candidate(null, "AA:BB:CC:00:03", null, true),
                new BluetoothSpeakerCatalog.Candidate("No Address", "", "Loudspeaker", true)
        ), true);

        assertEquals(2, devices.size(), "two distinct audio devices are returned");
        assertEquals("Kitchen Speaker", devices.get(0).name, "first device name");
        assertEquals("Loudspeaker", devices.get(0).subtitle, "first device subtitle");
        assertEquals("AA:BB:CC:00:01", devices.get(0).address, "first device address");
        assertEquals("Bluetooth Speaker", devices.get(1).name, "missing names are sanitized");
        assertEquals("Bonded Bluetooth audio", devices.get(1).subtitle, "missing subtitles are sanitized");

        List<StreamDevice> mockFallback = catalog.toStreamDevices(Collections.emptyList(), true);
        assertEquals(2, mockFallback.size(), "mock fallback returns two speakers");
        assertEquals("Mock Speaker 1", mockFallback.get(0).name, "first mock name");
        assertEquals("00:11:22:33:44:01", mockFallback.get(0).address, "first mock address");

        List<StreamDevice> emptyWithoutFallback = catalog.toStreamDevices(Collections.emptyList(), false);
        assertEquals(0, emptyWithoutFallback.size(), "fallback can be disabled");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
