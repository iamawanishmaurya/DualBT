package com.xpwnit.dualbt.audio;

public final class CalibrationVolumePolicy {
    private CalibrationVolumePolicy() {
    }

    public static int targetIndex(int minVolume, int maxVolume) {
        int safeMax = Math.max(0, maxVolume);
        if (safeMax == 0) {
            return 0;
        }
        int safeMin = Math.max(0, Math.min(minVolume, safeMax));
        int target = Math.max(1, Math.round(safeMax * 0.15f));
        return Math.max(safeMin, Math.min(safeMax, target));
    }
}
