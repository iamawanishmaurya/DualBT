package com.xpwnit.dualbt.audio;

public final class PcmGainTest {
    public static void main(String[] args) {
        leavesSamplesUnchangedAtUnityGain();
        lowersSamplesAtHalfGain();
        clampsBoostedSamplesToSigned16BitRange();
    }

    private static void leavesSamplesUnchangedAtUnityGain() {
        byte[] pcm = pcm16(1200, -1200);

        PcmGain.applyInPlace(pcm, pcm.length, 1.0f);

        assertSample(1200, pcm, 0, "positive sample should stay unchanged");
        assertSample(-1200, pcm, 2, "negative sample should stay unchanged");
    }

    private static void lowersSamplesAtHalfGain() {
        byte[] pcm = pcm16(1000, -1000);

        PcmGain.applyInPlace(pcm, pcm.length, 0.5f);

        assertSample(500, pcm, 0, "positive sample should be halved");
        assertSample(-500, pcm, 2, "negative sample should be halved");
    }

    private static void clampsBoostedSamplesToSigned16BitRange() {
        byte[] pcm = pcm16(30_000, -30_000);

        PcmGain.applyInPlace(pcm, pcm.length, 2.0f);

        assertSample(32_767, pcm, 0, "positive sample should clamp");
        assertSample(-32_768, pcm, 2, "negative sample should clamp");
    }

    private static byte[] pcm16(int first, int second) {
        byte[] pcm = new byte[4];
        writeSample(pcm, 0, first);
        writeSample(pcm, 2, second);
        return pcm;
    }

    private static void writeSample(byte[] pcm, int offset, int sample) {
        pcm[offset] = (byte) (sample & 0xFF);
        pcm[offset + 1] = (byte) ((sample >> 8) & 0xFF);
    }

    private static void assertSample(int expected, byte[] pcm, int offset, String message) {
        int actual = (pcm[offset] & 0xFF) | (pcm[offset + 1] << 8);
        if (actual != expected) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
