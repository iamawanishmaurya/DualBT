package com.xpwnit.dualbt;

import android.app.Application;

import com.xpwnit.dualbt.logging.AppLogger;

public final class DualBTApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppLogger.initialize(this);
        AppLogger.i("DualBTApp", "Application created");
    }
}
