package com.xpwnit.dualbt.audio;

import java.util.ArrayList;
import java.util.List;

public final class AudioOutputRouteSupport {
    private static final int TYPE_BLUETOOTH_SCO = 7;

    private AudioOutputRouteSupport() {
    }

    public static boolean hasTwoDirectMediaRoutes(
            List<Integer> matches,
            List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs
    ) {
        return directMediaRouteCount(matches, outputs) >= 2;
    }

    public static int directMediaRouteCount(
            List<Integer> matches,
            List<AudioOutputRouteMatcher.OutputDeviceDescriptor> outputs
    ) {
        if (matches == null || outputs == null) {
            return 0;
        }
        ArrayList<Integer> used = new ArrayList<>();
        int count = 0;
        for (Integer match : matches) {
            if (match == null || match < 0 || match >= outputs.size() || used.contains(match)) {
                continue;
            }
            AudioOutputRouteMatcher.OutputDeviceDescriptor output = outputs.get(match);
            if (isDirectMediaRoute(output)) {
                used.add(match);
                count++;
            }
        }
        return count;
    }

    public static boolean isDirectMediaRoute(AudioOutputRouteMatcher.OutputDeviceDescriptor output) {
        return output != null && output.type != TYPE_BLUETOOTH_SCO;
    }
}
