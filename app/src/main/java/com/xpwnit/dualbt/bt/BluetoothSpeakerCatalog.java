package com.xpwnit.dualbt.bt;

import com.xpwnit.dualbt.state.StreamDevice;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class BluetoothSpeakerCatalog {
    public List<StreamDevice> toStreamDevices(List<Candidate> candidates, boolean allowMockFallback) {
        ArrayList<StreamDevice> devices = new ArrayList<>();
        LinkedHashSet<String> seenAddresses = new LinkedHashSet<>();
        if (candidates != null) {
            for (Candidate candidate : candidates) {
                if (candidate == null || !candidate.audioCapable || isBlank(candidate.address)) {
                    continue;
                }
                String address = candidate.address.trim();
                if (!seenAddresses.add(address)) {
                    continue;
                }
                devices.add(new StreamDevice(
                        isBlank(candidate.name) ? "Bluetooth Speaker" : candidate.name.trim(),
                        isBlank(candidate.subtitle) ? "Bonded Bluetooth audio" : candidate.subtitle.trim(),
                        address
                ));
            }
        }
        if (devices.isEmpty() && allowMockFallback) {
            devices.add(new StreamDevice("Mock Speaker 1", "A2DP emulator route", "00:11:22:33:44:01"));
            devices.add(new StreamDevice("Mock Speaker 2", "A2DP emulator route", "00:11:22:33:44:02"));
        }
        return devices;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static final class Candidate {
        public final String name;
        public final String address;
        public final String subtitle;
        public final boolean audioCapable;

        public Candidate(String name, String address, String subtitle, boolean audioCapable) {
            this.name = name;
            this.address = address;
            this.subtitle = subtitle;
            this.audioCapable = audioCapable;
        }
    }
}
