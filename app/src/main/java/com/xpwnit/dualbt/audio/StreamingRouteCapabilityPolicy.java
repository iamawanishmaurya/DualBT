package com.xpwnit.dualbt.audio;

public final class StreamingRouteCapabilityPolicy {
    private StreamingRouteCapabilityPolicy() {
    }

    public enum Decision {
        DIRECT_MEDIA_ROUTES,
        MATCHED_HYBRID_SPLIT,
        SYSTEM_ROUTE_GROUP,
        SYSTEM_ROUTE_GROUP_PROBE,
        SINGLE_A2DP_HANDOFF_ONLY,
        NO_SECOND_ROUTE
    }

    public static Result evaluate(int directMediaRoutes, boolean matchedHybridSplit, boolean activeA2dpHandoff) {
        return evaluate(directMediaRoutes, matchedHybridSplit, activeA2dpHandoff, false);
    }

    public static Result evaluate(
            int directMediaRoutes,
            boolean matchedHybridSplit,
            boolean activeA2dpHandoff,
            boolean systemRouteGroup
    ) {
        return evaluate(directMediaRoutes, matchedHybridSplit, activeA2dpHandoff, systemRouteGroup, false);
    }

    public static Result evaluate(
            int directMediaRoutes,
            boolean matchedHybridSplit,
            boolean activeA2dpHandoff,
            boolean systemRouteGroup,
            boolean systemRouteGroupProbe
    ) {
        int directRoutes = Math.max(0, directMediaRoutes);
        if (directRoutes >= 2) {
            return new Result(
                    true,
                    Decision.DIRECT_MEDIA_ROUTES,
                    "Android exposes 2/2 direct media routes",
                    "Ready to stream"
            );
        }
        if (matchedHybridSplit) {
            return new Result(
                    true,
                    Decision.MATCHED_HYBRID_SPLIT,
                    "Android exposes one direct media route plus one matched communication route",
                    "Hybrid dual route available"
            );
        }
        if (systemRouteGroup) {
            return new Result(
                    true,
                    Decision.SYSTEM_ROUTE_GROUP,
                    "Android system media route grouping is available for the selected speakers",
                    "System dual audio available"
            );
        }
        if (systemRouteGroupProbe) {
            return new Result(
                    true,
                    Decision.SYSTEM_ROUTE_GROUP_PROBE,
                    "Android can probe system media route grouping after DualBT starts a media routing session",
                    "Try system dual audio"
            );
        }
        if (activeA2dpHandoff) {
            return new Result(
                    false,
                    Decision.SINGLE_A2DP_HANDOFF_ONLY,
                    "Android exposes one active A2DP route at a time; active-device handoff cannot keep two classic Bluetooth speakers playing simultaneously.",
                    "Dual route unavailable: " + directRoutes + "/2 live routes"
            );
        }
        return new Result(
                false,
                Decision.NO_SECOND_ROUTE,
                "Android exposes " + directRoutes + "/2 direct media route(s). Generic SCO fallback is disabled because it can route both tracks to one speaker.",
                "Dual route unavailable: " + directRoutes + "/2 live routes"
        );
    }

    public static final class Result {
        public final boolean supported;
        public final Decision decision;
        public final String message;
        public final String statusMessage;

        private Result(boolean supported, Decision decision, String message, String statusMessage) {
            this.supported = supported;
            this.decision = decision;
            this.message = message;
            this.statusMessage = statusMessage;
        }
    }
}
