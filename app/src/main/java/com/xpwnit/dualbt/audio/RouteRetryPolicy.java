package com.xpwnit.dualbt.audio;

public final class RouteRetryPolicy {
    private RouteRetryPolicy() {
    }

    public static boolean shouldRescanAfterActivation(boolean activationAttempted, boolean matchingRouteFound) {
        return activationAttempted && !matchingRouteFound;
    }
}
