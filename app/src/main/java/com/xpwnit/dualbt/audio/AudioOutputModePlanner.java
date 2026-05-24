package com.xpwnit.dualbt.audio;

import java.util.List;

public final class AudioOutputModePlanner {
    private AudioOutputModePlanner() {
    }

    public static Plan plan(List<Integer> matches, List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs) {
        int unmatchedRoute = firstUnmatchedRoute(matches);
        if (unmatchedRoute < 0 || hasTwoMatchedMediaOutputs(matches, outputs) || !hasBluetoothSco(outputs)) {
            return new Plan(false, -1);
        }
        return new Plan(true, unmatchedRoute);
    }

    private static int firstUnmatchedRoute(List<Integer> matches) {
        if (matches == null) {
            return -1;
        }
        for (int i = 0; i < matches.size(); i++) {
            Integer match = matches.get(i);
            if (match == null || match < 0) {
                return i;
            }
        }
        return -1;
    }

    private static boolean hasTwoMatchedMediaOutputs(
            List<Integer> matches,
            List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs
    ) {
        int a2dpMatches = 0;
        if (matches == null || outputs == null) {
            return false;
        }
        for (Integer match : matches) {
            if (match != null && match >= 0 && match < outputs.size() && outputs.get(match).type == 8) {
                a2dpMatches++;
            }
        }
        return a2dpMatches >= 2;
    }

    private static boolean hasBluetoothSco(List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs) {
        if (outputs == null) {
            return false;
        }
        for (AudioOutputRouteMatcher.OutputDeviceDescriptor output : outputs) {
            if (output.type == 7) {
                return true;
            }
        }
        return false;
    }

    public static final class Plan {
        public final boolean useCommunicationFallback;
        public final int communicationRouteIndex;

        public Plan(boolean useCommunicationFallback, int communicationRouteIndex) {
            this.useCommunicationFallback = useCommunicationFallback;
            this.communicationRouteIndex = communicationRouteIndex;
        }
    }
}
