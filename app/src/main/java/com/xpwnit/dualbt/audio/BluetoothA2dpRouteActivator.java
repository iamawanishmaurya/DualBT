package com.xpwnit.dualbt.audio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

import com.xpwnit.dualbt.logging.AppLogger;
import com.xpwnit.dualbt.state.StreamDevice;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class BluetoothA2dpRouteActivator {
    private static final int A2DP_PROFILE = BluetoothProfile.A2DP;

    private final Context context;

    public BluetoothA2dpRouteActivator(Context context) {
        this.context = context.getApplicationContext();
    }

    public Result activate(StreamDevice route, long settleMs) {
        BluetoothAdapter adapter = adapter();
        if (adapter == null || route == null || isBlank(route.address)) {
            return Result.notAttempted("Bluetooth adapter or route address unavailable");
        }
        BluetoothDevice target = bondedDevice(adapter, route.address);
        if (target == null) {
            return Result.notAttempted("No bonded Bluetooth device matches " + route.address);
        }
        AtomicReference<BluetoothProfile> profileRef = new AtomicReference<>();
        CountDownLatch connected = new CountDownLatch(1);
        BluetoothProfile.ServiceListener listener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == A2DP_PROFILE) {
                    profileRef.set(proxy);
                    connected.countDown();
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (profile == A2DP_PROFILE) {
                    profileRef.set(null);
                }
            }
        };
        boolean requested;
        try {
            requested = adapter.getProfileProxy(context, listener, A2DP_PROFILE);
        } catch (RuntimeException exception) {
            return Result.failed("A2DP profile proxy request failed: " + exception.getClass().getSimpleName());
        }
        if (!requested) {
            return Result.failed("A2DP profile proxy request was rejected");
        }
        BluetoothProfile profile = null;
        try {
            if (!connected.await(1200L, TimeUnit.MILLISECONDS)) {
                return Result.failed("A2DP profile proxy timed out");
            }
            profile = profileRef.get();
            if (profile == null) {
                return Result.failed("A2DP profile proxy was unavailable");
            }
            Method method = profile.getClass().getDeclaredMethod("setActiveDevice", BluetoothDevice.class);
            method.setAccessible(true);
            Object value = method.invoke(profile, target);
            boolean accepted = value instanceof Boolean && (Boolean) value;
            if (settleMs > 0L) {
                Thread.sleep(settleMs);
            }
            return new Result(true, accepted, "setActiveDevice(" + describe(target) + ") returned " + accepted);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Result.failed("A2DP active-device switch interrupted");
        } catch (ReflectiveOperationException exception) {
            return Result.failed("A2DP active-device switch unavailable: " + reflectiveFailure(exception));
        } catch (RuntimeException exception) {
            return Result.failed("A2DP active-device switch failed: " + exception.getClass().getSimpleName());
        } finally {
            if (profile != null) {
                try {
                    adapter.closeProfileProxy(A2DP_PROFILE, profile);
                } catch (RuntimeException exception) {
                    AppLogger.w("A2dpRouteActivator", "Unable to close A2DP profile proxy: " + exception.getClass().getSimpleName());
                }
            }
        }
    }

    private BluetoothAdapter adapter() {
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            return manager.getAdapter();
        }
        return BluetoothAdapter.getDefaultAdapter();
    }

    private BluetoothDevice bondedDevice(BluetoothAdapter adapter, String address) {
        String normalized = normalizeAddress(address);
        if (normalized.isEmpty()) {
            return null;
        }
        Set<BluetoothDevice> bonded;
        try {
            bonded = adapter.getBondedDevices();
        } catch (SecurityException exception) {
            AppLogger.w("A2dpRouteActivator", "Bluetooth bonded devices unavailable: missing permission");
            return null;
        }
        if (bonded == null) {
            return null;
        }
        for (BluetoothDevice device : bonded) {
            if (device != null && normalized.equals(normalizeAddress(device.getAddress()))) {
                return device;
            }
        }
        return null;
    }

    private String describe(BluetoothDevice device) {
        if (device == null) {
            return "unknown";
        }
        String name = "";
        try {
            name = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? device.getAlias() : device.getName();
        } catch (SecurityException ignored) {
            name = "";
        }
        if (isBlank(name)) {
            try {
                name = device.getName();
            } catch (SecurityException ignored) {
                name = "";
            }
        }
        return (isBlank(name) ? "Bluetooth device" : name) + "@" + device.getAddress();
    }

    private static String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        String upper = value.toUpperCase();
        for (int i = 0; i < upper.length(); i++) {
            char c = upper.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String reflectiveFailure(ReflectiveOperationException exception) {
        if (exception instanceof InvocationTargetException) {
            Throwable target = ((InvocationTargetException) exception).getTargetException();
            if (target != null) {
                return exception.getClass().getSimpleName() + " caused by " + target.getClass().getSimpleName()
                        + (isBlank(target.getMessage()) ? "" : ": " + target.getMessage());
            }
        }
        Throwable cause = exception.getCause();
        if (cause != null) {
            return exception.getClass().getSimpleName() + " caused by " + cause.getClass().getSimpleName()
                    + (isBlank(cause.getMessage()) ? "" : ": " + cause.getMessage());
        }
        return exception.getClass().getSimpleName();
    }

    public static final class Result {
        public final boolean attempted;
        public final boolean accepted;
        public final String message;

        private Result(boolean attempted, boolean accepted, String message) {
            this.attempted = attempted;
            this.accepted = accepted;
            this.message = message;
        }

        static Result notAttempted(String message) {
            return new Result(false, false, message);
        }

        static Result failed(String message) {
            return new Result(true, false, message);
        }
    }
}
