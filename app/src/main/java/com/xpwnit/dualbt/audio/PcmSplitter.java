package com.xpwnit.dualbt.audio;

public final class PcmSplitter {
    public int copyToOutputs(byte[] source, int requestedLength, byte[] firstOutput, byte[] secondOutput) {
        if (source == null) {
            throw new IllegalArgumentException("source is required");
        }
        if (firstOutput == null) {
            throw new IllegalArgumentException("firstOutput is required");
        }
        if (secondOutput == null) {
            throw new IllegalArgumentException("secondOutput is required");
        }
        int length = Math.max(0, Math.min(requestedLength, source.length));
        if (firstOutput.length < length || secondOutput.length < length) {
            throw new IllegalArgumentException("output buffers must be at least " + length + " bytes");
        }
        System.arraycopy(source, 0, firstOutput, 0, length);
        System.arraycopy(source, 0, secondOutput, 0, length);
        return length;
    }
}
