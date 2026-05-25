package com.xpwnit.dualbt.audio;

public final class ActiveA2dpHandoffPolicyTest {
    public static void main(String[] args) {
        usesMatchedOutputWhenRouteMetadataIsCurrent();
        usesDefaultMediaWhenActivationWasAttemptedButRouteMetadataIsStale();
        blocksWhenActivationWasNotAttemptedAndNoOutputMatches();
    }

    private static void usesMatchedOutputWhenRouteMetadataIsCurrent() {
        ActiveA2dpHandoffPolicy.Decision decision = ActiveA2dpHandoffPolicy.decide(true, true, true);

        assertEquals(ActiveA2dpHandoffPolicy.Decision.MATCHED_OUTPUT, decision, "matched route should use preferred output");
    }

    private static void usesDefaultMediaWhenActivationWasAttemptedButRouteMetadataIsStale() {
        ActiveA2dpHandoffPolicy.Decision decision = ActiveA2dpHandoffPolicy.decide(false, true, false);

        assertEquals(ActiveA2dpHandoffPolicy.Decision.DEFAULT_MEDIA, decision, "attempted handoff should use default media when metadata is stale");
    }

    private static void blocksWhenActivationWasNotAttemptedAndNoOutputMatches() {
        ActiveA2dpHandoffPolicy.Decision decision = ActiveA2dpHandoffPolicy.decide(false, false, false);

        assertEquals(ActiveA2dpHandoffPolicy.Decision.BLOCK, decision, "missing activation and missing output should block");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }
}
