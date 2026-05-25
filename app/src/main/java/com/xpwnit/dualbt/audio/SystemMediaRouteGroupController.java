package com.xpwnit.dualbt.audio;

import android.content.Context;
import android.media.MediaRoute2Info;
import android.media.MediaRouter2;
import android.os.Build;

import com.xpwnit.dualbt.logging.AppLogger;
import com.xpwnit.dualbt.state.StreamDevice;
import com.xpwnit.dualbt.state.StreamRoutePlan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SystemMediaRouteGroupController {
    private static final long ROUTE_GROUP_SETTLE_MILLIS = 1_200L;

    private final Context context;

    public SystemMediaRouteGroupController(Context context) {
        this.context = context.getApplicationContext();
    }

    public static boolean canProbeSystemRouteGroup() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    public Result inspect(StreamRoutePlan routePlan) {
        return evaluate(routePlan, false);
    }

    public Result prepare(StreamRoutePlan routePlan) {
        return evaluate(routePlan, true);
    }

    private Result evaluate(StreamRoutePlan routePlan, boolean activateSelectableRoutes) {
        if (routePlan == null || routePlan.targetCount() != 2) {
            return Result.unavailable("Route plan must contain exactly 2 speakers", "Select 2 speakers");
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return Result.unavailable("System route grouping requires Android 11 or newer", "System dual audio unavailable");
        }
        try {
            MediaRouter2 router = MediaRouter2.getInstance(context);
            MediaRouter2.RoutingController controller = router.getSystemController();
            List<MediaRoute2Info> selectedRoutes = safe(controller.getSelectedRoutes());
            List<MediaRoute2Info> selectableRoutes = safe(controller.getSelectableRoutes());
            ArrayList<String> targetNames = targetNames(routePlan);
            SystemMediaRouteGroupPolicy.Result initialPolicy = SystemMediaRouteGroupPolicy.evaluate(
                    targetNames,
                    routeNames(selectedRoutes),
                    routeNames(selectableRoutes)
            );
            if (!activateSelectableRoutes || initialPolicy.active || !initialPolicy.supported) {
                return fromPolicy(initialPolicy, false, selectedRoutes, selectableRoutes);
            }
            boolean attemptedSelection = false;
            for (String targetName : targetNames) {
                if (matchesAny(targetName, selectedRoutes)) {
                    continue;
                }
                MediaRoute2Info selectableRoute = firstMatchingRoute(targetName, selectableRoutes);
                if (selectableRoute != null) {
                    controller.selectRoute(selectableRoute);
                    attemptedSelection = true;
                    AppLogger.i(
                            "SystemRouteGroup",
                            "Requested system media route group selection for " + targetName
                                    + " using route " + routeName(selectableRoute)
                    );
                }
            }
            if (attemptedSelection) {
                waitForRouteGroup();
            }
            List<MediaRoute2Info> selectedAfter = safe(controller.getSelectedRoutes());
            List<MediaRoute2Info> selectableAfter = safe(controller.getSelectableRoutes());
            SystemMediaRouteGroupPolicy.Result afterPolicy = SystemMediaRouteGroupPolicy.evaluate(
                    targetNames,
                    routeNames(selectedAfter),
                    routeNames(selectableAfter)
            );
            return fromPolicy(afterPolicy, attemptedSelection, selectedAfter, selectableAfter);
        } catch (RuntimeException exception) {
            AppLogger.e("SystemRouteGroup", "Unable to inspect or prepare system media route group", exception);
            return Result.unavailable(
                    "System media route group inspection failed: " + exception.getClass().getSimpleName(),
                    "System dual audio unavailable"
            );
        }
    }

    private Result fromPolicy(
            SystemMediaRouteGroupPolicy.Result policy,
            boolean attemptedSelection,
            List<MediaRoute2Info> selectedRoutes,
            List<MediaRoute2Info> selectableRoutes
    ) {
        if (policy.active) {
            return new Result(
                    true,
                    true,
                    attemptedSelection,
                    "Android system media route group is active for both selected speakers",
                    "System dual audio active",
                    policy.selectedTargets,
                    policy.coveredTargets
            );
        }
        if (policy.supported) {
            return new Result(
                    true,
                    false,
                    attemptedSelection,
                    "Android exposes selected/selectable system routes for both selected speakers",
                    "System dual audio available",
                    policy.selectedTargets,
                    policy.coveredTargets
            );
        }
        return new Result(
                false,
                false,
                attemptedSelection,
                "System route group unavailable. Selected=" + routeNames(selectedRoutes)
                        + ", selectable=" + routeNames(selectableRoutes),
                "System dual audio unavailable",
                policy.selectedTargets,
                policy.coveredTargets
        );
    }

    private void waitForRouteGroup() {
        try {
            Thread.sleep(ROUTE_GROUP_SETTLE_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            AppLogger.w("SystemRouteGroup", "Route group settle interrupted");
        }
    }

    private ArrayList<String> targetNames(StreamRoutePlan routePlan) {
        ArrayList<String> names = new ArrayList<>();
        for (StreamDevice target : routePlan.targets()) {
            names.add(target.name);
        }
        return names;
    }

    private List<MediaRoute2Info> safe(List<MediaRoute2Info> routes) {
        return routes == null ? Collections.emptyList() : routes;
    }

    private ArrayList<String> routeNames(List<MediaRoute2Info> routes) {
        ArrayList<String> names = new ArrayList<>();
        for (MediaRoute2Info route : routes) {
            names.add(routeName(route));
        }
        return names;
    }

    private boolean matchesAny(String targetName, List<MediaRoute2Info> routes) {
        return firstMatchingRoute(targetName, routes) != null;
    }

    private MediaRoute2Info firstMatchingRoute(String targetName, List<MediaRoute2Info> routes) {
        String target = normalize(targetName);
        if (target.isEmpty()) {
            return null;
        }
        for (MediaRoute2Info route : routes) {
            String routeName = normalize(routeName(route));
            if (!routeName.isEmpty()
                    && (routeName.equals(target) || routeName.contains(target) || target.contains(routeName))) {
                return route;
            }
        }
        return null;
    }

    private String routeName(MediaRoute2Info route) {
        if (route == null || route.getName() == null) {
            return "";
        }
        return route.getName().toString();
    }

    private String normalize(String value) {
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
        public final boolean attemptedSelection;
        public final String message;
        public final String statusMessage;
        public final int selectedTargets;
        public final int coveredTargets;

        private Result(
                boolean supported,
                boolean active,
                boolean attemptedSelection,
                String message,
                String statusMessage,
                int selectedTargets,
                int coveredTargets
        ) {
            this.supported = supported;
            this.active = active;
            this.attemptedSelection = attemptedSelection;
            this.message = message;
            this.statusMessage = statusMessage;
            this.selectedTargets = selectedTargets;
            this.coveredTargets = coveredTargets;
        }

        public static Result unavailable(String message, String statusMessage) {
            return new Result(false, false, false, message, statusMessage, 0, 0);
        }
    }
}
