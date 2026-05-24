package com.xpwnit.dualbt.audio;

public final class SpeakerTestRunGateTest {
    public static void main(String[] args) {
        blocksSecondStartUntilFirstFinishes();
        allowsStartAfterFinish();
    }

    private static void blocksSecondStartUntilFirstFinishes() {
        SpeakerTestRunGate gate = new SpeakerTestRunGate();

        assertTrue(gate.tryStart(), "first test should start");
        assertFalse(gate.tryStart(), "second test should be blocked while first is running");
    }

    private static void allowsStartAfterFinish() {
        SpeakerTestRunGate gate = new SpeakerTestRunGate();

        assertTrue(gate.tryStart(), "first test should start");
        gate.finish();

        assertTrue(gate.tryStart(), "test should start again after finish");
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean value, String message) {
        if (value) {
            throw new AssertionError(message);
        }
    }
}
