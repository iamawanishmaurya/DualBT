package com.xpwnit.dualbt.audio;

import com.xpwnit.dualbt.state.StreamDevice;

import java.util.Arrays;
import java.util.List;

public final class AudioOutputRouteMatcherTest {
    public static void main(String[] args) {
        matchesSameNameSpeakersByAddress();
        fallsBackToUnusedNameMatch();
    }

    private static void matchesSameNameSpeakersByAddress() {
        List<StreamDevice> routes = Arrays.asList(
                new StreamDevice("Mini boost 4", "Bonded Bluetooth headset", "41:42:26:B3:62:1C"),
                new StreamDevice("Mini boost 4", "Bonded Bluetooth headset", "41:42:2E:9E:5E:AE")
        );
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 4", "41:42:2e:9e:5e:ae", 8),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 4", "41:42:26:b3:62:1c", 8)
        );

        List<Integer> matches = AudioOutputRouteMatcher.match(routes, outputs);

        assertEquals(1, matches.get(0), "first Mini route should match its own address");
        assertEquals(0, matches.get(1), "second Mini route should match its own address");
    }

    private static void fallsBackToUnusedNameMatch() {
        List<StreamDevice> routes = Arrays.asList(
                new StreamDevice("Desk Speaker", "Bonded Bluetooth audio", "AA:BB:CC:00:00:01"),
                new StreamDevice("Desk Speaker", "Bonded Bluetooth audio", "AA:BB:CC:00:00:02")
        );
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Desk Speaker", "", 8),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Desk Speaker", "", 8)
        );

        List<Integer> matches = AudioOutputRouteMatcher.match(routes, outputs);

        assertEquals(0, matches.get(0), "first same-name route should use first unused output");
        assertEquals(1, matches.get(1), "second same-name route should use second unused output");
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }
}
