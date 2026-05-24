package com.xpwnit.dualbt.audio;

import java.util.Arrays;
import java.util.List;

public final class AudioOutputModePlannerTest {
    public static void main(String[] args) {
        usesCommunicationFallbackForOneA2dpAndOneUnmatchedRoute();
        keepsPureMediaModeWhenBothA2dpRoutesAreAvailable();
        avoidsFallbackWhenScoRouteIsUnavailable();
    }

    private static void usesCommunicationFallbackForOneA2dpAndOneUnmatchedRoute() {
        List<Integer> matches = Arrays.asList(1, -1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Redmi Note 9 Pro", "00:00:00:00:00:00", 7),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 2", "41:42:2E:9E:5E:AE", 8)
        );

        AudioOutputModePlanner.Plan plan = AudioOutputModePlanner.plan(matches, outputs);

        assertTrue(plan.useCommunicationFallback, "one unmatched route should use communication fallback when SCO exists");
        assertEquals(1, plan.communicationRouteIndex, "unmatched route index should become communication route");
    }

    private static void keepsPureMediaModeWhenBothA2dpRoutesAreAvailable() {
        List<Integer> matches = Arrays.asList(0, 1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 1", "41:42:26:B3:62:1C", 8),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 2", "41:42:2E:9E:5E:AE", 8),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Redmi Note 9 Pro", "00:00:00:00:00:00", 7)
        );

        AudioOutputModePlanner.Plan plan = AudioOutputModePlanner.plan(matches, outputs);

        assertFalse(plan.useCommunicationFallback, "two A2DP matches should stay in pure media mode");
        assertEquals(-1, plan.communicationRouteIndex, "pure media mode has no communication route");
    }

    private static void avoidsFallbackWhenScoRouteIsUnavailable() {
        List<Integer> matches = Arrays.asList(0, -1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 1", "41:42:26:B3:62:1C", 8)
        );

        AudioOutputModePlanner.Plan plan = AudioOutputModePlanner.plan(matches, outputs);

        assertFalse(plan.useCommunicationFallback, "fallback needs an available SCO output route");
        assertEquals(-1, plan.communicationRouteIndex, "no communication route without SCO");
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

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }
}
