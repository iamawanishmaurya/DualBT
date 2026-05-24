package com.xpwnit.dualbt.logging;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class AppLogger {
    public interface Listener {
        void onLogsChanged();
    }

    private static final String APP_TAG = "DualBT";
    private static final LogStore STORE = new LogStore(500);
    private static final ArrayList<Listener> LISTENERS = new ArrayList<>();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static FileLogSink fileSink;

    private AppLogger() {
    }

    public static synchronized void initialize(Context context) {
        File logFile = new File(new File(context.getFilesDir(), "logs"), "dualbt.log");
        fileSink = new FileLogSink(logFile, 256 * 1024);
        i("AppLogger", "File logging initialized at " + logFile.getAbsolutePath());
    }

    public static void d(String tag, String message) {
        log("DEBUG", Log.DEBUG, tag, message, null);
    }

    public static void i(String tag, String message) {
        log("INFO", Log.INFO, tag, message, null);
    }

    public static void w(String tag, String message) {
        log("WARN", Log.WARN, tag, message, null);
    }

    public static void e(String tag, String message, Throwable throwable) {
        log("ERROR", Log.ERROR, tag, message, throwable);
    }

    public static List<LogStore.Entry> logs(String level) {
        return STORE.filter(level);
    }

    public static void clear() {
        STORE.clear();
        FileLogSink sink = fileSink;
        if (sink != null) {
            try {
                sink.clear();
            } catch (RuntimeException exception) {
                Log.w(APP_TAG + ":AppLogger", "Unable to clear file log", exception);
            }
        }
        notifyListeners();
    }

    public static void addListener(Listener listener) {
        synchronized (LISTENERS) {
            if (!LISTENERS.contains(listener)) {
                LISTENERS.add(listener);
            }
        }
    }

    public static void removeListener(Listener listener) {
        synchronized (LISTENERS) {
            LISTENERS.remove(listener);
        }
    }

    private static void log(String level, int priority, String tag, String message, Throwable throwable) {
        String fullTag = APP_TAG + ":" + tag;
        if (throwable == null) {
            Log.println(priority, fullTag, message);
        } else {
            Log.e(fullTag, message, throwable);
        }
        LogStore.Entry entry = STORE.add(level, tag, throwable == null ? message : message + " | " + throwable.getMessage());
        writeToFile(entry);
        notifyListeners();
    }

    private static void writeToFile(LogStore.Entry entry) {
        FileLogSink sink = fileSink;
        if (sink == null) {
            return;
        }
        try {
            sink.append(entry);
        } catch (RuntimeException exception) {
            Log.w(APP_TAG + ":AppLogger", "Unable to persist log entry", exception);
        }
    }

    private static void notifyListeners() {
        MAIN.post(() -> {
            List<Listener> copy;
            synchronized (LISTENERS) {
                copy = new ArrayList<>(LISTENERS);
            }
            for (Listener listener : copy) {
                listener.onLogsChanged();
            }
        });
    }
}
