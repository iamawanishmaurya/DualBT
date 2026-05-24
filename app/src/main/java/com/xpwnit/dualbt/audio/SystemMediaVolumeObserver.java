package com.xpwnit.dualbt.audio;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

public final class SystemMediaVolumeObserver {
    public interface Listener {
        void onSystemMediaVolumePercent(int percent);
    }

    private final Context context;
    private final Listener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ContentObserver observer;
    private int lastPercent = -1;

    public SystemMediaVolumeObserver(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public void start() {
        stop();
        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                publishIfChanged();
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                publishIfChanged();
            }
        };
        context.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, observer);
        publishIfChanged();
    }

    public void stop() {
        if (observer == null) {
            return;
        }
        context.getContentResolver().unregisterContentObserver(observer);
        observer = null;
    }

    public int currentPercent() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return 100;
        }
        return OutputVolumeMapper.fromStreamVolume(
                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        );
    }

    private void publishIfChanged() {
        int percent = currentPercent();
        if (percent == lastPercent) {
            return;
        }
        lastPercent = percent;
        listener.onSystemMediaVolumePercent(percent);
    }
}
