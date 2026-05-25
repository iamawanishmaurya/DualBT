package com.xpwnit.dualbt.audio;

public final class CalibrationAudioFocusPolicy {
    private static final double TEST_TONE_GAIN = 0.9;

    private CalibrationAudioFocusPolicy() {
    }

    public static boolean shouldRequestTransientFocus(boolean communicationTrack) {
        return true;
    }

    public static double testToneGain() {
        return TEST_TONE_GAIN;
    }
}
