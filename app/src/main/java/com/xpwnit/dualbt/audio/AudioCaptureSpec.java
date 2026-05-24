package com.xpwnit.dualbt.audio;

public final class AudioCaptureSpec {
    private final int sampleRate;
    private final int channelCount;
    private final int bytesPerSample;
    private final int bufferMultiplier;
    private final String[] matchingUsages;

    private AudioCaptureSpec(
            int sampleRate,
            int channelCount,
            int bytesPerSample,
            int bufferMultiplier,
            String[] matchingUsages
    ) {
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        this.bytesPerSample = bytesPerSample;
        this.bufferMultiplier = bufferMultiplier;
        this.matchingUsages = matchingUsages.clone();
    }

    public static AudioCaptureSpec defaultPlaybackSpec() {
        return new AudioCaptureSpec(
                48000,
                2,
                2,
                4,
                new String[]{"media", "game", "unknown"}
        );
    }

    public int sampleRate() {
        return sampleRate;
    }

    public int channelCount() {
        return channelCount;
    }

    public int bytesPerSample() {
        return bytesPerSample;
    }

    public int bufferMultiplier() {
        return bufferMultiplier;
    }

    public String[] matchingUsages() {
        return matchingUsages.clone();
    }

    public int frameSizeBytes() {
        return channelCount * bytesPerSample;
    }

    public int bufferSizeBytes(int minBufferBytes) {
        return Math.max(frameSizeBytes(), minBufferBytes * bufferMultiplier);
    }
}
