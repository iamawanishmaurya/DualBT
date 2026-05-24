package com.xpwnit.dualbt.audio;

public final class AudioCaptureSpecTest {
    public static void main(String[] args) {
        AudioCaptureSpec spec = AudioCaptureSpec.defaultPlaybackSpec();

        assertEquals(48000, spec.sampleRate(), "sample rate");
        assertEquals(2, spec.channelCount(), "channel count");
        assertEquals(2, spec.bytesPerSample(), "bytes per sample");
        assertEquals(4, spec.bufferMultiplier(), "buffer multiplier");
        assertEquals(3, spec.matchingUsages().length, "matching usages count");
        assertEquals("media", spec.matchingUsages()[0], "first usage");
        assertEquals("game", spec.matchingUsages()[1], "second usage");
        assertEquals("unknown", spec.matchingUsages()[2], "third usage");
        assertEquals(3840, spec.bufferSizeBytes(960), "buffer size multiplies min buffer");
        assertEquals(4, spec.bufferSizeBytes(0), "buffer has at least one stereo frame");
        assertEquals(4, spec.frameSizeBytes(), "stereo 16-bit frame size");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
