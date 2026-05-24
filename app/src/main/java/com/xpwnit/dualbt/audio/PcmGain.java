package com.xpwnit.dualbt.audio;

public final class PcmGain {
    private PcmGain() {
    }

    public static void applyInPlace(byte[] pcm, int requestedLength, float gain) {
        if (pcm == null) {
            throw new IllegalArgumentException("pcm is required");
        }
        int length = Math.max(0, Math.min(requestedLength, pcm.length));
        int alignedLength = length - (length % 2);
        float safeGain = Math.max(0.0f, Math.min(2.0f, gain));
        if (safeGain == 1.0f) {
            return;
        }
        for (int offset = 0; offset < alignedLength; offset += 2) {
            int sample = (pcm[offset] & 0xFF) | (pcm[offset + 1] << 8);
            int scaled = Math.round(sample * safeGain);
            if (scaled > Short.MAX_VALUE) {
                scaled = Short.MAX_VALUE;
            } else if (scaled < Short.MIN_VALUE) {
                scaled = Short.MIN_VALUE;
            }
            pcm[offset] = (byte) (scaled & 0xFF);
            pcm[offset + 1] = (byte) ((scaled >> 8) & 0xFF);
        }
    }
}
