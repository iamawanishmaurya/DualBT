package com.xpwnit.dualbt.audio;

public final class CalibrationPlaybackWaitPolicy {
    private static final long BLUETOOTH_TAIL_MS = 1_500L;
    private static final long MAX_WAIT_MS = 8_000L;

    private CalibrationPlaybackWaitPolicy() {
    }

    public static int framesFromBytes(int bytesWritten, int bytesPerFrame) {
        if (bytesWritten <= 0 || bytesPerFrame <= 0) {
            return 0;
        }
        return bytesWritten / bytesPerFrame;
    }

    public static long timeoutMs(int writtenFrames, int sampleRateHz) {
        if (writtenFrames <= 0 || sampleRateHz <= 0) {
            return BLUETOOTH_TAIL_MS;
        }
        long toneMs = Math.round((writtenFrames * 1000.0) / sampleRateHz);
        return Math.min(MAX_WAIT_MS, toneMs + BLUETOOTH_TAIL_MS);
    }
}
