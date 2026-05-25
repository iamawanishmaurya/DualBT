package com.xpwnit.dualbt.audio;

public final class CalibrationPlaybackWaitPolicyTest {
    public static void main(String[] args) {
        computesStereoFramesFromWrittenBytes();
        keepsBluetoothPlaybackAlivePastToneDuration();
        usesMinimumTailForShortOrPartialWrites();
    }

    private static void computesStereoFramesFromWrittenBytes() {
        int frames = CalibrationPlaybackWaitPolicy.framesFromBytes(614_400, 4);

        assertEquals(153_600, frames, "stereo 48 kHz 16-bit frames should be bytes / 4");
    }

    private static void keepsBluetoothPlaybackAlivePastToneDuration() {
        long timeoutMs = CalibrationPlaybackWaitPolicy.timeoutMs(153_600, 48_000);

        assertTrue(timeoutMs >= 4_700L, "3.2 second tone needs enough tail for Bluetooth route latency");
        assertTrue(timeoutMs <= 5_200L, "calibration should not hang for a short tone");
    }

    private static void usesMinimumTailForShortOrPartialWrites() {
        long timeoutMs = CalibrationPlaybackWaitPolicy.timeoutMs(0, 48_000);

        assertEquals(1_500L, timeoutMs, "zero written frames should still wait a bounded minimum tail");
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }
}
