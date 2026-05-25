package com.xpwnit.dualbt.audio;

public final class SpeakerCalibrationRoutePlannerTest {
    public static void main(String[] args) {
        usesDirectMediaOnlyWhenTheSelectedSpeakerHasMatchingOutput();
        usesDefaultMediaWhenA2dpActivationWasAcceptedButRouteListIsStale();
        usesDefaultMediaWhenA2dpActivationWasAttemptedButPlatformReportedFailure();
        usesTargetedScoWhenCommunicationRouteMatchesSelectedSpeaker();
        blocksWhenOnlyGenericScoRouteExists();
        blocksWhenNoDirectOrScoRouteExists();
    }

    private static void usesDirectMediaOnlyWhenTheSelectedSpeakerHasMatchingOutput() {
        SpeakerCalibrationRoutePlanner.Plan plan = SpeakerCalibrationRoutePlanner.plan(true, true, true, true, true);

        assertTrue(plan.useDirectMedia, "matched direct output should use media");
        assertFalse(plan.useDefaultMedia, "matched direct output should not need default media fallback");
        assertFalse(plan.useTargetedSco, "matched direct output should not use SCO");
        assertFalse(plan.blocked, "matched direct output should not block");
    }

    private static void usesDefaultMediaWhenA2dpActivationWasAcceptedButRouteListIsStale() {
        SpeakerCalibrationRoutePlanner.Plan plan = SpeakerCalibrationRoutePlanner.plan(false, true, true, false, true);

        assertFalse(plan.useDirectMedia, "stale route list has no direct output to prefer");
        assertTrue(plan.useDefaultMedia, "accepted A2DP activation should use default media routing");
        assertFalse(plan.useTargetedSco, "accepted A2DP activation should be preferred over SCO");
        assertFalse(plan.blocked, "accepted A2DP activation should not block");
    }

    private static void usesDefaultMediaWhenA2dpActivationWasAttemptedButPlatformReportedFailure() {
        SpeakerCalibrationRoutePlanner.Plan plan = SpeakerCalibrationRoutePlanner.plan(false, true, false, false, true);

        assertFalse(plan.useDirectMedia, "stale route list has no direct output to prefer");
        assertTrue(plan.useDefaultMedia, "attempted A2DP handoff can still switch the real route");
        assertFalse(plan.useTargetedSco, "attempted A2DP handoff should be preferred over SCO");
        assertFalse(plan.blocked, "attempted A2DP handoff should not block on stale public route metadata");
    }

    private static void usesTargetedScoWhenCommunicationRouteMatchesSelectedSpeaker() {
        SpeakerCalibrationRoutePlanner.Plan plan = SpeakerCalibrationRoutePlanner.plan(false, false, false, true, true);

        assertFalse(plan.useDirectMedia, "unmatched direct output must not be reused");
        assertFalse(plan.useDefaultMedia, "missing A2DP activation cannot use default media");
        assertTrue(plan.useTargetedSco, "matched communication route should use targeted SCO");
        assertFalse(plan.blocked, "matched communication route should not block");
    }

    private static void blocksWhenOnlyGenericScoRouteExists() {
        SpeakerCalibrationRoutePlanner.Plan plan = SpeakerCalibrationRoutePlanner.plan(false, false, false, false, true);

        assertFalse(plan.useDirectMedia, "generic SCO cannot use direct media");
        assertFalse(plan.useDefaultMedia, "generic SCO cannot use default media");
        assertFalse(plan.useTargetedSco, "generic SCO must not be treated as the selected speaker");
        assertTrue(plan.blocked, "generic SCO route should block instead of playing the phone route");
    }

    private static void blocksWhenNoDirectOrScoRouteExists() {
        SpeakerCalibrationRoutePlanner.Plan plan = SpeakerCalibrationRoutePlanner.plan(false, false, false, false, false);

        assertFalse(plan.useDirectMedia, "missing routes cannot use media");
        assertFalse(plan.useDefaultMedia, "missing routes cannot use default media");
        assertFalse(plan.useTargetedSco, "missing routes cannot use SCO");
        assertTrue(plan.blocked, "missing direct and SCO routes should block");
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
