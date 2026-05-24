package com.xpwnit.dualbt.state;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StreamRoutePlan {
    private static final String FIELD_SEPARATOR = "|";
    private static final String TARGET_SEPARATOR = "\n";

    private final ArrayList<StreamDevice> targets;

    private StreamRoutePlan(List<StreamDevice> targets) {
        this.targets = new ArrayList<>(targets);
    }

    public static StreamRoutePlan fromSelected(List<StreamDevice> selectedDevices, int requiredTargets) {
        if (selectedDevices == null || selectedDevices.size() != requiredTargets) {
            throw new IllegalArgumentException("Route plan requires exactly " + requiredTargets + " targets");
        }
        ArrayList<StreamDevice> targets = new ArrayList<>();
        for (StreamDevice device : selectedDevices) {
            if (device == null || isBlank(device.address)) {
                throw new IllegalArgumentException("Route target address is required");
            }
            targets.add(new StreamDevice(safe(device.name), safe(device.subtitle), device.address.trim()));
        }
        return new StreamRoutePlan(targets);
    }

    public static StreamRoutePlan fromPayload(String payload, int requiredTargets) {
        if (isBlank(payload)) {
            throw new IllegalArgumentException("Route payload is required");
        }
        String[] rows = payload.split(TARGET_SEPARATOR, -1);
        ArrayList<StreamDevice> targets = new ArrayList<>();
        for (String row : rows) {
            if (isBlank(row)) {
                continue;
            }
            String[] fields = row.split("\\|", -1);
            if (fields.length != 3) {
                throw new IllegalArgumentException("Invalid route target payload");
            }
            targets.add(new StreamDevice(decode(fields[0]), decode(fields[1]), decode(fields[2])));
        }
        return fromSelected(targets, requiredTargets);
    }

    public List<StreamDevice> targets() {
        return Collections.unmodifiableList(targets);
    }

    public int targetCount() {
        return targets.size();
    }

    public String displayNames() {
        StringBuilder builder = new StringBuilder();
        for (StreamDevice target : targets) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(target.name);
        }
        return builder.toString();
    }

    public String toPayload() {
        StringBuilder builder = new StringBuilder();
        for (StreamDevice target : targets) {
            if (builder.length() > 0) {
                builder.append(TARGET_SEPARATOR);
            }
            builder.append(encode(target.name))
                    .append(FIELD_SEPARATOR)
                    .append(encode(target.subtitle))
                    .append(FIELD_SEPARATOR)
                    .append(encode(target.address));
        }
        return builder.toString();
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(safe(value), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UTF-8 encoding unavailable", exception);
        }
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UTF-8 encoding unavailable", exception);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
