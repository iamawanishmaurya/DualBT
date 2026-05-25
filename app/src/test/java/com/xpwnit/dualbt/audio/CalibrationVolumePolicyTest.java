package com.xpwnit.dualbt.audio;

public final class CalibrationVolumePolicyTest {
    public static void main(String[] args) {
        mapsFifteenPercentToTwoOfFifteen();
        respectsNonZeroMinimumForVoiceStreams();
        neverExceedsMaximum();
    }

    private static void mapsFifteenPercentToTwoOfFifteen() {
        assertEquals(2, CalibrationVolumePolicy.targetIndex(0, 15), "15% of media max 15 should be index 2");
    }

    private static void respectsNonZeroMinimumForVoiceStreams() {
        assertEquals(2, CalibrationVolumePolicy.targetIndex(1, 11), "voice call test volume should stay low but audible");
    }

    private static void neverExceedsMaximum() {
        assertEquals(1, CalibrationVolumePolicy.targetIndex(0, 1), "small ranges should clamp to max");
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }
}
