package com.xpwnit.dualbt.audio;

public final class Pcm16ScoConverter {
    private static final int STEREO_PCM16_FRAME_BYTES = 4;
    private static final int MONO_PCM16_FRAME_BYTES = 2;

    private final int frameStep;
    private int nextFrameOffset;

    public Pcm16ScoConverter(int sourceSampleRate, int targetSampleRate) {
        int safeSource = Math.max(1, sourceSampleRate);
        int safeTarget = Math.max(1, targetSampleRate);
        this.frameStep = Math.max(1, Math.round((float) safeSource / (float) safeTarget));
        this.nextFrameOffset = 0;
    }

    public int convert(byte[] stereoPcm16, int sourceBytes, byte[] monoPcm16) {
        if (stereoPcm16 == null) {
            throw new IllegalArgumentException("stereoPcm16 is required");
        }
        if (monoPcm16 == null) {
            throw new IllegalArgumentException("monoPcm16 is required");
        }
        int safeSourceBytes = Math.max(0, Math.min(sourceBytes, stereoPcm16.length));
        int sourceFrames = safeSourceBytes / STEREO_PCM16_FRAME_BYTES;
        int outputOffset = 0;
        int sourceFrame = nextFrameOffset;
        while (sourceFrame < sourceFrames && outputOffset + MONO_PCM16_FRAME_BYTES <= monoPcm16.length) {
            int inputOffset = sourceFrame * STEREO_PCM16_FRAME_BYTES;
            int left = readSample(stereoPcm16, inputOffset);
            int right = readSample(stereoPcm16, inputOffset + 2);
            writeSample(monoPcm16, outputOffset, (left + right) / 2);
            outputOffset += MONO_PCM16_FRAME_BYTES;
            sourceFrame += frameStep;
        }
        nextFrameOffset = Math.max(0, sourceFrame - sourceFrames);
        return outputOffset;
    }

    public int outputCapacityBytes(int sourceBytes) {
        int sourceFrames = Math.max(0, sourceBytes) / STEREO_PCM16_FRAME_BYTES;
        int frames = Math.max(1, (sourceFrames + frameStep - 1) / frameStep);
        return frames * MONO_PCM16_FRAME_BYTES;
    }

    private static int readSample(byte[] pcm, int offset) {
        return (short) ((pcm[offset] & 0xFF) | (pcm[offset + 1] << 8));
    }

    private static void writeSample(byte[] pcm, int offset, int sample) {
        pcm[offset] = (byte) (sample & 0xFF);
        pcm[offset + 1] = (byte) ((sample >> 8) & 0xFF);
    }
}
