package com.xpwnit.dualbt.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;

import com.xpwnit.dualbt.audio.AndroidPlaybackCaptureEngine;
import com.xpwnit.dualbt.logging.AppLogger;
import com.xpwnit.dualbt.state.StreamRoutePlan;

public final class DualBTService extends Service {
    public static final String EXTRA_RESULT_CODE = "result_code";
    public static final String EXTRA_RESULT_DATA = "result_data";
    public static final String EXTRA_ROUTE_PLAN = "route_plan";
    private static final String CHANNEL_ID = "dualbt_streaming";
    private static final int REQUIRED_ROUTE_TARGETS = 2;
    private AndroidPlaybackCaptureEngine captureEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        captureEngine = new AndroidPlaybackCaptureEngine(this);
        AppLogger.i("DualBTService", "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        MediaProjection projection = mediaProjectionFrom(intent);
        if (projection == null || !captureEngine.start(projection, routePlan)) {
            AppLogger.w("DualBTService", "Foreground service stopping because audio capture could not start");
            stopSelf(startId);
            return START_NOT_STICKY;
        }
        AppLogger.i("DualBTService", "Foreground service started with capture routes: " + routePlan.displayNames());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
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
                .setContentText("Streaming audio to selected speakers")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setOngoing(true)
                .build();
    }
}
