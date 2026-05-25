package com.xpwnit.dualbt.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;

import com.xpwnit.dualbt.audio.AndroidPlaybackCaptureEngine;
import com.xpwnit.dualbt.audio.OutputVolumeMapper;
import com.xpwnit.dualbt.audio.SystemMediaVolumeObserver;
import com.xpwnit.dualbt.logging.AppLogger;
import com.xpwnit.dualbt.state.StreamRoutePlan;

public final class DualBTService extends Service {
    public static final String ACTION_SET_OUTPUT_VOLUME = "com.xpwnit.dualbt.SET_OUTPUT_VOLUME";
    public static final String ACTION_ADJUST_OUTPUT_VOLUME = "com.xpwnit.dualbt.ADJUST_OUTPUT_VOLUME";
    public static final String EXTRA_RESULT_CODE = "result_code";
    public static final String EXTRA_RESULT_DATA = "result_data";
    public static final String EXTRA_ROUTE_PLAN = "route_plan";
    public static final String EXTRA_OUTPUT_VOLUME_PERCENT = "output_volume_percent";
    public static final String EXTRA_OUTPUT_VOLUME_DELTA = "output_volume_delta";
    private static final String CHANNEL_ID = "dualbt_streaming";
    private static final int REQUIRED_ROUTE_TARGETS = 2;
    private AndroidPlaybackCaptureEngine captureEngine;
    private SystemMediaVolumeObserver systemVolumeObserver;
    private Thread captureStartThread;
    private int currentOutputVolumePercent = 100;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        captureEngine = new AndroidPlaybackCaptureEngine(this);
        AppLogger.i("DualBTService", "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_SET_OUTPUT_VOLUME.equals(intent.getAction())) {
            applyOutputVolume(intent.getIntExtra(EXTRA_OUTPUT_VOLUME_PERCENT, 100));
            stopIfVolumeOnlyWithoutActiveStream(startId);
            return START_STICKY;
        }
        if (intent != null && ACTION_ADJUST_OUTPUT_VOLUME.equals(intent.getAction())) {
            applyOutputVolume(currentOutputVolumePercent + intent.getIntExtra(EXTRA_OUTPUT_VOLUME_DELTA, 0));
            stopIfVolumeOnlyWithoutActiveStream(startId);
            return START_STICKY;
        }
        if (!hasCaptureConsent(intent)) {
            AppLogger.w("DualBTService", "Foreground service start blocked without MediaProjection consent extras");
            stopSelf(startId);
            return START_NOT_STICKY;
        }
        StreamRoutePlan routePlan = routePlanFrom(intent);
        if (routePlan == null) {
            AppLogger.w("DualBTService", "Foreground service start blocked without exactly 2 route targets");
            stopSelf(startId);
            return START_NOT_STICKY;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(42, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        } else {
            startForeground(42, notification());
        }
        applyOutputVolume(intent.getIntExtra(EXTRA_OUTPUT_VOLUME_PERCENT, 100));
        startSystemVolumeSync();
        MediaProjection projection = mediaProjectionFrom(intent);
        if (projection == null) {
            AppLogger.w("DualBTService", "Foreground service stopping because audio capture could not start");
            stopSystemVolumeSync();
            stopSelf(startId);
            return START_NOT_STICKY;
        }
        startCaptureAsync(projection, routePlan, startId);
        return START_STICKY;
    }

    private synchronized void startCaptureAsync(MediaProjection projection, StreamRoutePlan routePlan, int startId) {
        Thread existing = captureStartThread;
        if (existing != null && existing.isAlive()) {
            existing.interrupt();
        }
        Thread starter = new Thread(() -> {
            boolean started = captureEngine != null && captureEngine.start(projection, routePlan);
            synchronized (DualBTService.this) {
                if (Thread.currentThread() == captureStartThread) {
                    captureStartThread = null;
                }
            }
            if (!started) {
                AppLogger.w("DualBTService", "Foreground service stopping because audio capture could not start");
                stopSystemVolumeSync();
                stopSelf(startId);
                return;
            }
            AppLogger.i("DualBTService", "Foreground service started with capture routes: " + routePlan.displayNames());
        }, "DualBT-CaptureStart");
        captureStartThread = starter;
        starter.start();
    }

    private void applyOutputVolume(int percent) {
        int clampedPercent = OutputVolumeMapper.clampManualPercent(percent);
        currentOutputVolumePercent = clampedPercent;
        if (captureEngine != null) {
            captureEngine.setOutputGain(clampedPercent / 100.0f);
        }
        AppLogger.i("DualBTService", "Output volume set to " + clampedPercent + "%");
        if (captureEngine != null && captureEngine.isRunning()) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(42, notification());
            }
        }
    }

    private void startSystemVolumeSync() {
        if (systemVolumeObserver == null) {
            systemVolumeObserver = new SystemMediaVolumeObserver(this, percent -> {
                applyOutputVolume(percent);
                AppLogger.i("DualBTService", "System media volume synced to " + percent + "%");
            });
        }
        systemVolumeObserver.start();
        AppLogger.i("DualBTService", "System media volume sync started");
    }

    private void stopSystemVolumeSync() {
        if (systemVolumeObserver == null) {
            return;
        }
        systemVolumeObserver.stop();
        AppLogger.i("DualBTService", "System media volume sync stopped");
    }

    private void stopIfVolumeOnlyWithoutActiveStream(int startId) {
        if (captureEngine == null || !captureEngine.isRunning()) {
            AppLogger.w("DualBTService", "Volume update received without active stream; stopping service");
            stopSelf(startId);
        }
    }

    @Override
    public void onDestroy() {
        Thread starter = captureStartThread;
        captureStartThread = null;
        if (starter != null) {
            starter.interrupt();
        }
        stopSystemVolumeSync();
        if (captureEngine != null) {
            captureEngine.stop();
        }
        AppLogger.i("DualBTService", "Service destroyed");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "DualBT Streaming",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private boolean hasCaptureConsent(Intent intent) {
        if (intent == null || !intent.hasExtra(EXTRA_RESULT_CODE) || !intent.hasExtra(EXTRA_RESULT_DATA)) {
            return false;
        }
        return intent.getParcelableExtra(EXTRA_RESULT_DATA) instanceof Intent;
    }

    private StreamRoutePlan routePlanFrom(Intent intent) {
        if (intent == null || !intent.hasExtra(EXTRA_ROUTE_PLAN)) {
            return null;
        }
        try {
            return StreamRoutePlan.fromPayload(intent.getStringExtra(EXTRA_ROUTE_PLAN), REQUIRED_ROUTE_TARGETS);
        } catch (IllegalArgumentException exception) {
            AppLogger.e("DualBTService", "Invalid route plan payload", exception);
            return null;
        }
    }

    private MediaProjection mediaProjectionFrom(Intent intent) {
        if (intent == null) {
            return null;
        }
        Object data = intent.getParcelableExtra(EXTRA_RESULT_DATA);
        if (!(data instanceof Intent)) {
            return null;
        }
        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (manager == null) {
            AppLogger.w("DualBTService", "MediaProjectionManager unavailable");
            return null;
        }
        try {
            return manager.getMediaProjection(intent.getIntExtra(EXTRA_RESULT_CODE, 0), (Intent) data);
        } catch (RuntimeException exception) {
            AppLogger.e("DualBTService", "Unable to obtain MediaProjection", exception);
            return null;
        }
    }

    private Notification notification() {
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);
        return builder
                .setContentTitle("DualBT")
                .setContentText("Streaming audio to selected speakers · Volume " + currentOutputVolumePercent + "%")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setOngoing(true)
                .addAction(volumeAction("Vol -", -10, 101))
                .addAction(volumeAction("Vol +", 10, 102))
                .build();
    }

    private Notification.Action volumeAction(String title, int delta, int requestCode) {
        Intent intent = new Intent(this, DualBTService.class);
        intent.setAction(ACTION_ADJUST_OUTPUT_VOLUME);
        intent.putExtra(EXTRA_OUTPUT_VOLUME_DELTA, delta);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getService(this, requestCode, intent, flags);
        return new Notification.Action.Builder(
                android.R.drawable.ic_media_play,
                title,
                pendingIntent
        ).build();
    }
}
