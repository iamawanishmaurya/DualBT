package com.xpwnit.dualbt.state;

import java.util.Objects;

public final class StreamDevice {
    public final String name;
    public final String subtitle;
    public final String address;

    public StreamDevice(String name, String subtitle, String address) {
        this.name = name;
        this.subtitle = subtitle;
        this.address = address;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StreamDevice)) {
            return false;
        }
        StreamDevice that = (StreamDevice) other;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
