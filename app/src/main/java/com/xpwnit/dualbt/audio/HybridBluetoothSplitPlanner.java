package com.xpwnit.dualbt.audio;

import java.util.List;

public final class HybridBluetoothSplitPlanner {
    private static final int TYPE_BLUETOOTH_SCO = 7;

    private HybridBluetoothSplitPlanner() {
    }

    public static Plan plan(
            List<Integer> matches,
            List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs
    ) {
        if (matches == null || outputs == null || matches.size() != 2) {
            return Plan.disabled();
        }
        int mediaRouteIndex = -1;
        int scoRouteIndex = -1;
        for (int routeIndex = 0; routeIndex < matches.size(); routeIndex++) {
            Integer match = matches.get(routeIndex);
            if (match == null || match < 0 || match >= outputs.size()) {
                return Plan.disabled();
            }
            AudioOutputRouteMatcher.OutputDeviceDescriptor output = outputs.get(match);
            if (AudioOutputRouteSupport.isDirectMediaRoute(output)) {
                if (mediaRouteIndex >= 0) {
                    return Plan.disabled();
                }
                mediaRouteIndex = routeIndex;
            } else if (isScoOutput(output)) {
                if (scoRouteIndex >= 0) {
                    return Plan.disabled();
                }
                scoRouteIndex = routeIndex;
            } else {
                return Plan.disabled();
            }
        }
        if (mediaRouteIndex < 0 || scoRouteIndex < 0) {
            return Plan.disabled();
        }
        return new Plan(true, mediaRouteIndex, scoRouteIndex);
    }

    private static boolean isScoOutput(AudioOutputRouteMatcher.OutputDeviceDescriptor output) {
        return output != null && output.type == TYPE_BLUETOOTH_SCO;
    }

    public static final class Plan {
        public final boolean useHybridSplit;
        public final int mediaRouteIndex;
        public final int scoRouteIndex;

        private Plan(boolean useHybridSplit, int mediaRouteIndex, int scoRouteIndex) {
            this.useHybridSplit = useHybridSplit;
            this.mediaRouteIndex = mediaRouteIndex;
            this.scoRouteIndex = scoRouteIndex;
        }

        static Plan disabled() {
            return new Plan(false, -1, -1);
        }
    }
}
