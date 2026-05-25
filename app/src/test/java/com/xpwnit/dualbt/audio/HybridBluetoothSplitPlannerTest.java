package com.xpwnit.dualbt.audio;

import java.util.Arrays;
import java.util.List;

public final class HybridBluetoothSplitPlannerTest {
    public static void main(String[] args) {
        enablesHybridSplitForOneA2dpRouteAndOneMatchedScoOutput();
        rejectsHybridSplitForOneA2dpRouteAndOneGenericScoOutput();
        rejectsHybridSplitWithoutScoOutput();
        rejectsHybridSplitWhenBothDirectRoutesAreAvailable();
    }

    private static void enablesHybridSplitForOneA2dpRouteAndOneMatchedScoOutput() {
        List<Integer> matches = Arrays.asList(0, 1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 1", "41:42:26:B3:62:1C", 7),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 2", "41:42:2E:9E:5E:AE", 8)
        );

        HybridBluetoothSplitPlanner.Plan plan = HybridBluetoothSplitPlanner.plan(matches, outputs);

        assertTrue(plan.useHybridSplit, "one direct A2DP route plus one matched SCO route should enable hybrid split");
        assertEquals(0, plan.scoRouteIndex, "matched SCO route should use targeted SCO");
        assertEquals(1, plan.mediaRouteIndex, "matched A2DP route should stay on media");
    }

    private static void rejectsHybridSplitForOneA2dpRouteAndOneGenericScoOutput() {
        List<Integer> matches = Arrays.asList(-1, 1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Redmi Note 9 Pro", "00:00:00:00:00:00", 7),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 2", "41:42:2E:9E:5E:AE", 8)
        );

        HybridBluetoothSplitPlanner.Plan plan = HybridBluetoothSplitPlanner.plan(matches, outputs);

        assertFalse(plan.useHybridSplit, "generic phone SCO output must not be treated as a selected speaker");
    }

    private static void rejectsHybridSplitWithoutScoOutput() {
        List<Integer> matches = Arrays.asList(0, -1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 1", "41:42:26:B3:62:1C", 8)
        );

        HybridBluetoothSplitPlanner.Plan plan = HybridBluetoothSplitPlanner.plan(matches, outputs);

        assertFalse(plan.useHybridSplit, "hybrid split needs a SCO output");
    }

    private static void rejectsHybridSplitWhenBothDirectRoutesAreAvailable() {
        List<Integer> matches = Arrays.asList(0, 1);
        List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs = Arrays.asList(
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 1", "41:42:26:B3:62:1C", 8),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Mini boost 2", "41:42:2E:9E:5E:AE", 8),
                new AudioOutputRouteMatcher.OutputDeviceDescriptor("Redmi Note 9 Pro", "00:00:00:00:00:00", 7)
        );

        HybridBluetoothSplitPlanner.Plan plan = HybridBluetoothSplitPlanner.plan(matches, outputs);

        assertFalse(plan.useHybridSplit, "two direct A2DP routes should not use hybrid SCO split");
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
