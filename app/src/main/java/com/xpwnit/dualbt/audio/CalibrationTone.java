package com.xpwnit.dualbt.audio;

public final class CalibrationTone {
    private CalibrationTone() {
    }

    public static byte[] stereoSinePcm(int sampleRate, int durationMs, double frequencyHz, double gain) {
        int safeSampleRate = Math.max(1, sampleRate);
        int safeDurationMs = Math.max(1, durationMs);
        double safeFrequency = Math.max(20.0, frequencyHz);
        double safeGain = Math.max(0.0, Math.min(0.9, gain));
        int frames = Math.max(1, safeSampleRate * safeDurationMs / 1000);
        byte[] pcm = new byte[frames * 4];
        for (int frame = 0; frame < frames; frame++) {
            double angle = 2.0 * Math.PI * safeFrequency * frame / safeSampleRate;
            short sample = (short) Math.round(Math.sin(angle) * Short.MAX_VALUE * safeGain);
            int offset = frame * 4;
            pcm[offset] = (byte) (sample & 0xFF);
            pcm[offset + 1] = (byte) ((sample >> 8) & 0xFF);
            pcm[offset + 2] = pcm[offset];
            pcm[offset + 3] = pcm[offset + 1];
        }
        return pcm;
    }
}
