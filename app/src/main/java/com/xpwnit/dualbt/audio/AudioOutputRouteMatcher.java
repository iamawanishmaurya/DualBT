package com.xpwnit.dualbt.audio;

import com.xpwnit.dualbt.state.StreamDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AudioOutputRouteMatcher {
    private AudioOutputRouteMatcher() {
    }

    public static List<Integer> match(List<StreamDevice> routes, List<OutputDeviceDescriptor> outputs) {
        if (routes == null || routes.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Integer> matches = new ArrayList<>();
        boolean[] used = new boolean[outputs == null ? 0 : outputs.size()];
        for (StreamDevice route : routes) {
            int index = findByAddress(route, outputs, used);
            if (index < 0) {
                index = findByName(route, outputs, used);
            }
            if (index >= 0) {
                used[index] = true;
            }
            matches.add(index);
        }
        return matches;
    }

    private static int findByAddress(StreamDevice route, List<OutputDeviceDescriptor> outputs, boolean[] used) {
        String routeAddress = normalizeAddress(route == null ? "" : route.address);
        if (routeAddress.isEmpty() || outputs == null) {
            return -1;
        }
        for (int i = 0; i < outputs.size(); i++) {
            if (!used[i] && routeAddress.equals(normalizeAddress(outputs.get(i).address))) {
                return i;
            }
        }
        return -1;
    }

    private static int findByName(StreamDevice route, List<OutputDeviceDescriptor> outputs, boolean[] used) {
        String routeName = normalizeName(route == null ? "" : route.name);
        if (routeName.isEmpty() || outputs == null) {
            return -1;
        }
        for (int i = 0; i < outputs.size(); i++) {
            if (!used[i] && routeName.equals(normalizeName(outputs.get(i).name))) {
                return i;
            }
        }
        return -1;
    }

    private static String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        String upper = value.toUpperCase(Locale.US);
        for (int i = 0; i < upper.length(); i++) {
            char c = upper.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }

    public static final class OutputDeviceDescriptor {
        public final String name;
        public final String address;
        public final int type;

        public OutputDeviceDescriptor(String name, String address, int type) {
            this.name = name == null ? "" : name;
            this.address = address == null ? "" : address;
            this.type = type;
        }
    }
}
