package com.xpwnit.dualbt.audio;

public final class Pcm16ScoConverterTest {
    public static void main(String[] args) {
        convertsStereo48kToMono16k();
        carriesFrameRemainderAcrossChunks();
    }

    private static void convertsStereo48kToMono16k() {
        Pcm16ScoConverter converter = new Pcm16ScoConverter(48_000, 16_000);
        byte[] stereo = stereoFrames(
                100, 300,
                1_000, 1_200,
                2_000, 2_200,
                -500, -100,
                3_000, 3_200,
                4_000, 4_200
        );
        byte[] mono = new byte[16];

        int written = converter.convert(stereo, stereo.length, mono);

        assertEquals(4, written, "six stereo frames at 48 kHz should become two mono frames at 16 kHz");
        assertEquals(200, readSample(mono, 0), "first output frame should average source frame 0");
        assertEquals(-300, readSample(mono, 2), "second output frame should average source frame 3");
    }

    private static void carriesFrameRemainderAcrossChunks() {
        Pcm16ScoConverter converter = new Pcm16ScoConverter(48_000, 16_000);
        byte[] firstChunk = stereoFrames(30, 30, 60, 60, 90, 90, 120, 120, 150, 150);
        byte[] secondChunk = stereoFrames(180, 180, 210, 210, 240, 240, 270, 270);
        byte[] mono = new byte[16];

        int firstWritten = converter.convert(firstChunk, firstChunk.length, mono);
        int secondWritten = converter.convert(secondChunk, secondChunk.length, mono);

        assertEquals(4, firstWritten, "first chunk should produce frames 0 and 3");
        assertEquals(2, secondWritten, "second chunk should resume at the carried frame offset");
        assertEquals(210, readSample(mono, 0), "second chunk should resume with global frame 6");
    }

    private static byte[] stereoFrames(int... samples) {
        if (samples.length % 2 != 0) {
            throw new IllegalArgumentException("left/right sample pairs are required");
        }
        byte[] pcm = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            writeSample(pcm, i * 2, samples[i]);
        }
        return pcm;
    }

    private static int readSample(byte[] pcm, int offset) {
        return (short) ((pcm[offset] & 0xFF) | (pcm[offset + 1] << 8));
    }

    private static void writeSample(byte[] pcm, int offset, int sample) {
        pcm[offset] = (byte) (sample & 0xFF);
        pcm[offset + 1] = (byte) ((sample >> 8) & 0xFF);
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }
}
