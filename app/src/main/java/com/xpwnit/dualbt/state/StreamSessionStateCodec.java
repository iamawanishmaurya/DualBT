package com.xpwnit.dualbt.state;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class StreamSessionStateCodec {
    private StreamSessionStateCodec() {
    }

    public static String encodeSelectedAddresses(List<StreamDevice> selectedDevices) {
        if (selectedDevices == null || selectedDevices.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        LinkedHashSet<String> uniqueAddresses = new LinkedHashSet<>();
        for (StreamDevice device : selectedDevices) {
            if (device == null || device.address == null) {
                continue;
            }
            String address = device.address.trim();
            if (!address.isEmpty()) {
                uniqueAddresses.add(address);
            }
        }
        for (String address : uniqueAddresses) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(address);
        }
        return builder.toString();
    }

    public static List<String> decodeSelectedAddresses(String encodedAddresses) {
        LinkedHashSet<String> uniqueAddresses = new LinkedHashSet<>();
        if (encodedAddresses != null && !encodedAddresses.isEmpty()) {
            String[] lines = encodedAddresses.split("\\R");
            for (String line : lines) {
                String address = line == null ? "" : line.trim();
                if (!address.isEmpty()) {
                    uniqueAddresses.add(address);
                }
            }
        }
        return new ArrayList<>(uniqueAddresses);
    }
}
