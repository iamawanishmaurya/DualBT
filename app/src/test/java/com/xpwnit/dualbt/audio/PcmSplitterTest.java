package com.xpwnit.dualbt.audio;

public final class PcmSplitterTest {
    public static void main(String[] args) {
        PcmSplitter splitter = new PcmSplitter();
        byte[] source = new byte[]{1, 2, 3, 4, 5, 6};
        byte[] first = new byte[6];
        byte[] second = new byte[6];

        int copied = splitter.copyToOutputs(source, source.length, first, second);
        assertEquals(6, copied, "full source length is copied");
        assertBytes(source, first, 6, "first output matches source");
        assertBytes(source, second, 6, "second output matches source");

        byte[] partialFirst = new byte[4];
        byte[] partialSecond = new byte[4];
        int partial = splitter.copyToOutputs(source, 4, partialFirst, partialSecond);
        assertEquals(4, partial, "partial length is copied");
        assertBytes(new byte[]{1, 2, 3, 4}, partialFirst, 4, "first partial output matches");
        assertBytes(new byte[]{1, 2, 3, 4}, partialSecond, 4, "second partial output matches");

        int clamped = splitter.copyToOutputs(source, 999, first, second);
        assertEquals(source.length, clamped, "length clamps to source size");

        assertThrows(() -> splitter.copyToOutputs(source, 5, new byte[4], second), "short output is rejected");
        assertThrows(() -> splitter.copyToOutputs(null, 1, first, second), "null source is rejected");
    }

    private static void assertBytes(byte[] expected, byte[] actual, int length, String message) {
        for (int index = 0; index < length; index++) {
            if (expected[index] != actual[index]) {
                throw new AssertionError(message + ": mismatch at " + index);
            }
        }
    }

    private static void assertThrows(Runnable action, String message) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
