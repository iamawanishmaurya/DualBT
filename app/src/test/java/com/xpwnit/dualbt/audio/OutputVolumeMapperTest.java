package com.xpwnit.dualbt.audio;

public final class OutputVolumeMapperTest {
    public static void main(String[] args) {
        mapsMaximumStreamVolumeToUnityGain();
        mapsHalfStreamVolumeToHalfOutputGain();
        clampsInvalidStreamVolumeToSafeRange();
        keepsManualBoostWithinDualBtLimit();
    }

    private static void mapsMaximumStreamVolumeToUnityGain() {
        int percent = OutputVolumeMapper.fromStreamVolume(15, 15);

        assertEquals(100, percent, "maximum media volume should map to 100%");
    }

    private static void mapsHalfStreamVolumeToHalfOutputGain() {
        int percent = OutputVolumeMapper.fromStreamVolume(5, 10);

        assertEquals(50, percent, "half media volume should map to 50%");
    }

    private static void clampsInvalidStreamVolumeToSafeRange() {
        assertEquals(0, OutputVolumeMapper.fromStreamVolume(-3, 10), "negative media volume should clamp to silence");
        assertEquals(100, OutputVolumeMapper.fromStreamVolume(99, 10), "media volume above max should clamp to 100%");
        assertEquals(100, OutputVolumeMapper.fromStreamVolume(5, 0), "unknown max media volume should use unity gain");
    }

    private static void keepsManualBoostWithinDualBtLimit() {
        assertEquals(0, OutputVolumeMapper.clampManualPercent(-10), "manual volume should not go below 0%");
        assertEquals(175, OutputVolumeMapper.clampManualPercent(175), "manual boost below limit should be preserved");
        assertEquals(200, OutputVolumeMapper.clampManualPercent(250), "manual volume should not exceed 200%");
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (actual != expected) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
