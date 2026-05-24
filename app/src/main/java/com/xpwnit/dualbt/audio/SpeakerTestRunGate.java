package com.xpwnit.dualbt.audio;

public final class SpeakerTestRunGate {
    private boolean running;

    public synchronized boolean tryStart() {
        if (running) {
            return false;
        }
        running = true;
        return true;
    }

    public synchronized void finish() {
        running = false;
    }
}
