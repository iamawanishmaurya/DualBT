package com.xpwnit.dualbt.audio;

import java.util.Arrays;
import java.util.Collections;

public final class SystemMediaRouteGroupPolicyTest {
    public static void main(String[] args) {
        reportsActiveWhenBothTargetsAreSelected();
        reportsAvailableWhenSecondTargetIsSelectable();
        rejectsSingleRouteOnly();
        normalizesRouteNames();
    }

    private static void reportsActiveWhenBothTargetsAreSelected() {
        SystemMediaRouteGroupPolicy.Result result = SystemMediaRouteGroupPolicy.evaluate(
                Arrays.asList("Mini boost 1", "Mini boost 2"),
                Arrays.asList("Mini boost 1", "Mini boost 2"),
                Collections.emptyList()
        );

        assertTrue(result.supported, "selected system route group should be supported");
        assertTrue(result.active, "both selected routes should be active");
        assertEquals(SystemMediaRouteGroupPolicy.Decision.ACTIVE_GROUP, result.decision, "active decision");
    }

    private static void reportsAvailableWhenSecondTargetIsSelectable() {
        SystemMediaRouteGroupPolicy.Result result = SystemMediaRouteGroupPolicy.evaluate(
                Arrays.asList("Mini boost 1", "Mini boost 2"),
                Collections.singletonList("Mini boost 1"),
                Collections.singletonList("Mini boost 2")
        );

        assertTrue(result.supported, "selected plus selectable system routes should be supported");
        assertFalse(result.active, "selectable route still needs activation");
        assertEquals(SystemMediaRouteGroupPolicy.Decision.SELECTABLE_GROUP, result.decision, "selectable decision");
    }

    private static void rejectsSingleRouteOnly() {
        SystemMediaRouteGroupPolicy.Result result = SystemMediaRouteGroupPolicy.evaluate(
                Arrays.asList("Mini boost 1", "Mini boost 2"),
                Collections.singletonList("Mini boost 2"),
                Collections.emptyList()
        );

        assertFalse(result.supported, "one selected route alone is not simultaneous dual audio");
        assertEquals(SystemMediaRouteGroupPolicy.Decision.INCOMPLETE_GROUP, result.decision, "incomplete decision");
    }

    private static void normalizesRouteNames() {
        SystemMediaRouteGroupPolicy.Result result = SystemMediaRouteGroupPolicy.evaluate(
                Arrays.asList("  mini BOOST 1 ", "Mini boost 2"),
                Collections.singletonList("Mini Boost 1"),
                Collections.singletonList("Living room - mini boost 2")
        );

        assertTrue(result.supported, "case and containment should match user-visible route names");
        assertEquals(2, result.coveredTargets, "covered targets");
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
