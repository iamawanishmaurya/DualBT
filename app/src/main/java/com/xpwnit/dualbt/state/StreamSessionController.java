package com.xpwnit.dualbt.state;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StreamSessionController {
    public enum StartResult {
        CAPTURE_PERMISSION_REQUIRED,
        ALREADY_STREAMING,
        ALREADY_WAITING_FOR_PERMISSION,
        NEED_TWO_DEVICES
    }

    private final int requiredDevices;
    private final LinkedHashMap<String, StreamDevice> selected = new LinkedHashMap<>();
    private boolean awaitingCapturePermission;
    private boolean streaming;

    public StreamSessionController(int requiredDevices) {
        this.requiredDevices = Math.max(1, requiredDevices);
    }

    public boolean toggle(StreamDevice device) {
        if (streaming || awaitingCapturePermission) {
            return false;
        }
        if (selected.containsKey(device.address)) {
            selected.remove(device.address);
            return true;
        }
        if (selected.size() >= requiredDevices) {
            return false;
        }
        selected.put(device.address, device);
        return true;
    }

    public boolean restoreSelected(List<StreamDevice> devices) {
        if (streaming || awaitingCapturePermission) {
            return false;
        }
        selected.clear();
        if (devices == null) {
            return true;
        }
        for (StreamDevice device : devices) {
            if (device == null || device.address == null || device.address.trim().isEmpty()) {
                continue;
            }
            if (selected.size() >= requiredDevices) {
                break;
            }
            selected.putIfAbsent(device.address, device);
        }
        return true;
    }

    public StartResult start() {
        if (streaming) {
            return StartResult.ALREADY_STREAMING;
        }
        if (awaitingCapturePermission) {
            return StartResult.ALREADY_WAITING_FOR_PERMISSION;
        }
        if (selected.size() != requiredDevices) {
            return StartResult.NEED_TWO_DEVICES;
        }
        awaitingCapturePermission = true;
        return StartResult.CAPTURE_PERMISSION_REQUIRED;
    }

    public boolean confirmCapturePermission() {
        if (!awaitingCapturePermission) {
            return false;
        }
        awaitingCapturePermission = false;
        streaming = true;
        return true;
    }

    public boolean cancelCapturePermission() {
        if (!awaitingCapturePermission) {
            return false;
        }
        awaitingCapturePermission = false;
        return true;
    }

    public void stop() {
        awaitingCapturePermission = false;
        streaming = false;
    }

    public boolean isSelected(StreamDevice device) {
        return selected.containsKey(device.address);
    }

    public boolean isStreaming() {
        return streaming;
    }

    public boolean isAwaitingCapturePermission() {
        return awaitingCapturePermission;
    }

    public boolean canStart() {
        return !streaming && !awaitingCapturePermission && selected.size() == requiredDevices;
    }

    public int selectedCount() {
        return selected.size();
    }

    public List<StreamDevice> selectedDevices() {
        return new ArrayList<>(selected.values());
    }

    public String selectedNames() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, StreamDevice> entry : selected.entrySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(entry.getValue().name);
        }
        return builder.toString();
    }

    public String statusMessage() {
        if (streaming) {
            return "Streaming to " + selected.size() + " speaker(s)";
        }
        if (awaitingCapturePermission) {
            return "Waiting for capture permission";
        }
        int remaining = requiredDevices - selected.size();
        if (remaining <= 0) {
            return "Ready to stream";
        }
        if (remaining == 1) {
            return "Select 1 more speaker";
        }
        return "Select " + remaining + " speakers";
    }
}
