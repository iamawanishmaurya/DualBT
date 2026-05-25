package com.xpwnit.dualbt.audio;

public final class ActiveA2dpHandoffPolicy {
    private ActiveA2dpHandoffPolicy() {
    }

    public static Decision decide(
            boolean matchingOutputFound,
            boolean activationAttempted,
            boolean activationAccepted
    ) {
        if (matchingOutputFound) {
            return Decision.MATCHED_OUTPUT;
        }
        if (activationAccepted || activationAttempted) {
            return Decision.DEFAULT_MEDIA;
        }
        return Decision.BLOCK;
    }

    public enum Decision {
        MATCHED_OUTPUT,
        DEFAULT_MEDIA,
        BLOCK
    }
}
