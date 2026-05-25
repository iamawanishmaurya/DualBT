package com.xpwnit.dualbt.audio;

import java.util.List;

public final class AudioOutputModePlanner {
    private AudioOutputModePlanner() {
    }

    public static Plan plan(List<Integer> matches, List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs) {
        int directRoutes = AudioOutputRouteSupport.directMediaRouteCount(matches, outputs);
        boolean activeA2dpHandoff = directRoutes == 1;
        return new Plan(false, -1, activeA2dpHandoff);
    }

    public static final class Plan {
        public final boolean useCommunicationFallback;
        public final int communicationRouteIndex;
        public final boolean useActiveA2dpHandoff;

        public Plan(boolean useCommunicationFallback, int communicationRouteIndex) {
            this(useCommunicationFallback, communicationRouteIndex, false);
        }

        public Plan(boolean useCommunicationFallback, int communicationRouteIndex, boolean useActiveA2dpHandoff) {
            this.useCommunicationFallback = useCommunicationFallback;
            this.communicationRouteIndex = communicationRouteIndex;
            this.useActiveA2dpHandoff = useActiveA2dpHandoff;
        }
    }
}
