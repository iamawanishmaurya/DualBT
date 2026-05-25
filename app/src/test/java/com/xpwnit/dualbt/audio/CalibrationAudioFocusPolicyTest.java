package com.xpwnit.dualbt.audio;

public final class CalibrationAudioFocusPolicyTest {
    public static void main(String[] args) {
        mediaCalibrationRequestsTransientFocus();
        communicationCalibrationRequestsTransientFocus();
        testToneUsesClippedSafeMaximumGain();
    }

    private static void mediaCalibrationRequestsTransientFocus() {
        assertTrue(
                CalibrationAudioFocusPolicy.shouldRequestTransientFocus(false),
                "media calibration should request transient focus like normal media apps"
        );
    }

    private static void communicationCalibrationRequestsTransientFocus() {
        assertTrue(
                CalibrationAudioFocusPolicy.shouldRequestTransientFocus(true),
                "communication calibration should also hold focus while the test tone is active"
        );
    }

    private static void testToneUsesClippedSafeMaximumGain() {
        assertEquals(0.9, CalibrationAudioFocusPolicy.testToneGain(), "calibration gain should be the safe maximum");
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 0.000_001) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
