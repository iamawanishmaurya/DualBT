package com.xpwnit.dualbt.audio;

public final class OutputVolumeMapper {
    private OutputVolumeMapper() {
    }

    public static int fromStreamVolume(int currentVolume, int maxVolume) {
        if (maxVolume <= 0) {
            return 100;
        }
        int clampedCurrent = Math.max(0, Math.min(currentVolume, maxVolume));
        return Math.round((clampedCurrent * 100.0f) / maxVolume);
    }

    public static int clampManualPercent(int percent) {
        return Math.max(0, Math.min(200, percent));
    }
}
