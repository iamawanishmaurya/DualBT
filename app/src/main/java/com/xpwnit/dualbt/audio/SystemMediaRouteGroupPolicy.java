package com.xpwnit.dualbt.audio;

import java.util.ArrayList;
import java.util.List;

public final class SystemMediaRouteGroupPolicy {
    private static final int REQUIRED_TARGETS = 2;

    private SystemMediaRouteGroupPolicy() {
    }

    public enum Decision {
        ACTIVE_GROUP,
        SELECTABLE_GROUP,
        INCOMPLETE_GROUP,
        INVALID_TARGETS
    }

    public static Result evaluate(
            List<String> targetNames,
            List<String> selectedRouteNames,
            List<String> selectableRouteNames
    ) {
        ArrayList<String> targets = normalized(targetNames);
        if (targets.size() != REQUIRED_TARGETS) {
            return new Result(false, false, Decision.INVALID_TARGETS, 0, 0);
        }
        ArrayList<String> selected = normalized(selectedRouteNames);
        ArrayList<String> selectable = normalized(selectableRouteNames);
        int selectedMatches = matchCount(targets, selected);
        int coveredTargets = coveredTargetCount(targets, selected, selectable);
        if (selectedMatches == REQUIRED_TARGETS) {
            return new Result(true, true, Decision.ACTIVE_GROUP, coveredTargets, selectedMatches);
        }
        if (coveredTargets == REQUIRED_TARGETS) {
            return new Result(true, false, Decision.SELECTABLE_GROUP, coveredTargets, selectedMatches);
        }
        return new Result(false, false, Decision.INCOMPLETE_GROUP, coveredTargets, selectedMatches);
    }

    private static int coveredTargetCount(
            ArrayList<String> targets,
            ArrayList<String> selected,
            ArrayList<String> selectable
    ) {
        int covered = 0;
        for (String target : targets) {
            if (matchesAny(target, selected) || matchesAny(target, selectable)) {
                covered++;
            }
        }
        return covered;
    }

    private static int matchCount(ArrayList<String> targets, ArrayList<String> routes) {
        int count = 0;
        for (String target : targets) {
            if (matchesAny(target, routes)) {
                count++;
            }
        }
        return count;
    }

    private static boolean matchesAny(String target, ArrayList<String> routes) {
        for (String route : routes) {
            if (route.equals(target) || route.contains(target) || target.contains(route)) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<String> normalized(List<String> values) {
        ArrayList<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            String normalized = normalize(value);
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        boolean previousSpace = false;
        String lower = value.trim().toLowerCase();
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!previousSpace && builder.length() > 0) {
                    builder.append(' ');
                    previousSpace = true;
                }
            } else {
                builder.append(c);
                previousSpace = false;
            }
        }
        return builder.toString().trim();
    }

    public static final class Result {
        public final boolean supported;
        public final boolean active;
        public final Decision decision;
        public final int coveredTargets;
        public final int selectedTargets;

        private Result(boolean supported, boolean active, Decision decision, int coveredTargets, int selectedTargets) {
            this.supported = supported;
            this.active = active;
            this.decision = decision;
            this.coveredTargets = coveredTargets;
            this.selectedTargets = selectedTargets;
        }
    }
}
