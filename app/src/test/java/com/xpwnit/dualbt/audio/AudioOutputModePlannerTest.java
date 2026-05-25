package com.xpwnit.dualbt.audio;

import java.util.Arrays;
import java.util.List;

public final class AudioOutputModePlannerTest {
    public static void main(String[] args) {
        avoidsCommunicationFallbackForOneA2dpAndOneUnmatchedRoute();
        keepsPureMediaModeWhenBothA2dpRoutesAreAvailable();
        avoidsFallbackWhenScoRouteIsUnavailable();
        usesActiveA2dpHandoffForOneDirectClassicRoute();
    }

    private static void avoidsCommunicationFallbackForOneA2dpAndOneUnmatchedRoute() {
        List<Integer> matches = Arrays.asList(1, -1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Redmi Note 9 Pro", "00:00:00:00:00:00", 7),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 2", "41:42:2E:9E:5E:AE", 8)
        );

        AudioOutputModePlanner.Plan plan = AudioOutputModePlanner.plan(matches, outputs);

        assertFalse(plan.useCommunicationFallback, "generic SCO fallback should not be treated as a second speaker");
        assertEquals(-1, plan.communicationRouteIndex, "unsupported routes have no communication route");
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

    private static void usesActiveA2dpHandoffForOneDirectClassicRoute() {
        List<Integer> matches = Arrays.asList(0, -1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 1", "41:42:26:B3:62:1C", 8)
        );

        AudioOutputModePlanner.Plan plan = AudioOutputModePlanner.plan(matches, outputs);

        assertTrue(plan.useActiveA2dpHandoff, "one classic A2DP route should use active-device handoff experiment");
        assertFalse(plan.useCommunicationFallback, "active A2DP handoff must not use generic SCO fallback");
        assertEquals(-1, plan.communicationRouteIndex, "active A2DP handoff has no communication route");
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
