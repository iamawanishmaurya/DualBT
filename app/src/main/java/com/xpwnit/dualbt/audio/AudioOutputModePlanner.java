package com.xpwnit.dualbt.audio;

import java.util.List;

public final class AudioOutputModePlanner {
    private AudioOutputModePlanner() {
    }

    public static Plan plan(List<Integer> matches, List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs) {
        return new Plan(false, -1);
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
