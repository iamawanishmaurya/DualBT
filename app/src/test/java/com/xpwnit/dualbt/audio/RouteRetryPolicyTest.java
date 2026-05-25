package com.xpwnit.dualbt.audio;

public final class RouteRetryPolicyTest {
    public static void main(String[] args) {
        rescansWhenActivationWasAttemptedButRouteIsStillMissing();
        skipsRescanWhenTheMatchingRouteIsAlreadyVisible();
        skipsRescanWhenNoActivationWasAttempted();
    }

    private static void rescansWhenActivationWasAttemptedButRouteIsStillMissing() {
        assertTrue(
                RouteRetryPolicy.shouldRescanAfterActivation(true, false),
                "stale AudioManager route list should be refreshed after an activation attempt"
        );
    }

    private static void skipsRescanWhenTheMatchingRouteIsAlreadyVisible() {
        assertFalse(
                RouteRetryPolicy.shouldRescanAfterActivation(true, true),
                "visible matching route does not need a delayed rescan"
        );
    }

    private static void skipsRescanWhenNoActivationWasAttempted() {
        assertFalse(
                RouteRetryPolicy.shouldRescanAfterActivation(false, false),
                "without an activation attempt there is no route change to settle"
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
