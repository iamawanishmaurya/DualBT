package com.xpwnit.dualbt.audio;

public final class StreamingRouteCapabilityPolicyTest {
    public static void main(String[] args) {
        supportsTwoDirectMediaRoutes();
        supportsMatchedHybridSplit();
        supportsSystemRouteGroup();
        allowsBoundedSystemRouteGroupProbe();
        rejectsActiveA2dpHandoffForSimultaneousStreaming();
        rejectsMissingSecondRoute();
    }

    private static void supportsTwoDirectMediaRoutes() {
        StreamingRouteCapabilityPolicy.Result result = StreamingRouteCapabilityPolicy.evaluate(2, false, false);

        assertTrue(result.supported, "two direct media routes can stream simultaneously");
        assertEquals(StreamingRouteCapabilityPolicy.Decision.DIRECT_MEDIA_ROUTES, result.decision, "direct route decision");
    }

    private static void supportsMatchedHybridSplit() {
        StreamingRouteCapabilityPolicy.Result result = StreamingRouteCapabilityPolicy.evaluate(1, true, false);

        assertTrue(result.supported, "matched hybrid A2DP/SCO route can attempt simultaneous playback");
        assertEquals(StreamingRouteCapabilityPolicy.Decision.MATCHED_HYBRID_SPLIT, result.decision, "hybrid route decision");
    }

    private static void supportsSystemRouteGroup() {
        StreamingRouteCapabilityPolicy.Result result = StreamingRouteCapabilityPolicy.evaluate(1, false, true, true);

        assertTrue(result.supported, "system route group can provide synchronized dual output");
        assertEquals(StreamingRouteCapabilityPolicy.Decision.SYSTEM_ROUTE_GROUP, result.decision, "system route group decision");
    }

    private static void allowsBoundedSystemRouteGroupProbe() {
        StreamingRouteCapabilityPolicy.Result result = StreamingRouteCapabilityPolicy.evaluate(1, false, true, false, true);

        assertTrue(result.supported, "system route group probe should be allowed before service startup");
        assertEquals(StreamingRouteCapabilityPolicy.Decision.SYSTEM_ROUTE_GROUP_PROBE, result.decision, "system route group probe decision");
    }

    private static void rejectsActiveA2dpHandoffForSimultaneousStreaming() {
        StreamingRouteCapabilityPolicy.Result result = StreamingRouteCapabilityPolicy.evaluate(1, false, true);

        assertFalse(result.supported, "active A2DP handoff is only a one-active-device switch, not simultaneous streaming");
        assertEquals(StreamingRouteCapabilityPolicy.Decision.SINGLE_A2DP_HANDOFF_ONLY, result.decision, "single A2DP decision");
    }

    private static void rejectsMissingSecondRoute() {
        StreamingRouteCapabilityPolicy.Result result = StreamingRouteCapabilityPolicy.evaluate(0, false, false);

        assertFalse(result.supported, "missing second route must block streaming");
        assertEquals(StreamingRouteCapabilityPolicy.Decision.NO_SECOND_ROUTE, result.decision, "missing route decision");
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

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }
}
