package com.xpwnit.dualbt.audio;

public final class SpeakerCalibrationRoutePlanner {
    private SpeakerCalibrationRoutePlanner() {
    }

    public static Plan plan(
            boolean hasMatchingDirectMediaOutput,
            boolean a2dpActivationAttempted,
            boolean a2dpActivationAccepted,
            boolean hasMatchingCommunicationOutput,
            boolean hasGenericScoOutput
    ) {
        if (hasMatchingDirectMediaOutput) {
            return new Plan(true, false, false, false);
        }
        if (a2dpActivationAccepted || a2dpActivationAttempted) {
            return new Plan(false, true, false, false);
        }
        if (hasMatchingCommunicationOutput) {
            return new Plan(false, false, true, false);
        }
        return new Plan(false, false, false, true);
    }

    public static final class Plan {
        public final boolean useDirectMedia;
        public final boolean useDefaultMedia;
        public final boolean useTargetedSco;
        public final boolean blocked;

        private Plan(boolean useDirectMedia, boolean useDefaultMedia, boolean useTargetedSco, boolean blocked) {
            this.useDirectMedia = useDirectMedia;
            this.useDefaultMedia = useDefaultMedia;
            this.useTargetedSco = useTargetedSco;
            this.blocked = blocked;
        }
    }
}
