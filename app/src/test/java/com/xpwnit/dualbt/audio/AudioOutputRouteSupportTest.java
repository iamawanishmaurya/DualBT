package com.xpwnit.dualbt.audio;

import java.util.Arrays;
import java.util.List;

public final class AudioOutputRouteSupportTest {
    public static void main(String[] args) {
        supportsTwoDirectBluetoothMediaRoutes();
        rejectsOneMediaRoutePlusGenericScoFallback();
        rejectsUnmatchedSecondSpeaker();
    }

    private static void supportsTwoDirectBluetoothMediaRoutes() {
        List<Integer> matches = Arrays.asList(0, 1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 1", "41:42:26:B3:62:1C", 8),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 2", "41:42:2E:9E:5E:AE", 8)
        );

        assertTrue(
                AudioOutputRouteSupport.hasTwoDirectMediaRoutes(matches, outputs),
                "two direct A2DP matches should be supported"
        );
    }

    private static void rejectsOneMediaRoutePlusGenericScoFallback() {
        List<Integer> matches = Arrays.asList(1, -1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Redmi Note 9 Pro", "00:00:00:00:00:00", 7),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 1", "41:42:26:B3:62:1C", 8)
        );

        assertFalse(
                AudioOutputRouteSupport.hasTwoDirectMediaRoutes(matches, outputs),
                "generic SCO fallback must not be treated as a second physical speaker"
        );
    }

    private static void rejectsUnmatchedSecondSpeaker() {
        List<Integer> matches = Arrays.asList(0, -1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 1", "41:42:26:B3:62:1C", 8)
        );

        assertFalse(
                AudioOutputRouteSupport.hasTwoDirectMediaRoutes(matches, outputs),
                "an unmatched second speaker should be unsupported"
        );
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean value, String message) {
        if (value) {
            throw new AssertionError(message);
        }
    }
}
