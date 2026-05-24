package com.xpwnit.dualbt.bt;

import com.xpwnit.dualbt.state.StreamDevice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        devices = aliasDuplicateNames(devices);
        if (devices.isEmpty() && allowMockFallback) {
            devices.add(new StreamDevice("Mock Speaker 1", "A2DP emulator route", "00:11:22:33:44:01"));
            devices.add(new StreamDevice("Mock Speaker 2", "A2DP emulator route", "00:11:22:33:44:02"));
        }
        return devices;
    }

    private ArrayList<StreamDevice> aliasDuplicateNames(ArrayList<StreamDevice> devices) {
        LinkedHashMap<String, Integer> totals = new LinkedHashMap<>();
        for (StreamDevice device : devices) {
            String key = normalizedName(device.name);
            totals.put(key, totals.containsKey(key) ? totals.get(key) + 1 : 1);
        }
        LinkedHashMap<String, Integer> seen = new LinkedHashMap<>();
        ArrayList<StreamDevice> aliased = new ArrayList<>();
        for (StreamDevice device : devices) {
            String key = normalizedName(device.name);
            int total = valueOrZero(totals, key);
            if (total <= 1) {
                aliased.add(device);
                continue;
            }
            int index = valueOrZero(seen, key) + 1;
            seen.put(key, index);
            String aliasedName = duplicateAlias(device.name, index);
            String subtitle = device.subtitle + " · " + device.name;
            aliased.add(new StreamDevice(aliasedName, subtitle, device.address));
        }
        return aliased;
    }

    private int valueOrZero(Map<String, Integer> values, String key) {
        Integer value = values.get(key);
        return value == null ? 0 : value;
    }

    private String duplicateAlias(String originalName, int index) {
        String trimmed = originalName == null ? "" : originalName.trim();
        String lower = trimmed.toLowerCase(Locale.US);
        if (lower.startsWith("mini boost")) {
            return "Mini boost " + index;
        }
        return trimmed + " " + index;
    }

    private String normalizedName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
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
