package com.xpwnit.dualbt.ui;

public final class SystemBarAppearancePolicy {
    private static final int ANDROID_R = 30;

    private SystemBarAppearancePolicy() {
    }

    public static boolean shouldApplyLightSystemBars(int sdkInt, boolean darkTheme, boolean contentRootAvailable) {
        return sdkInt >= ANDROID_R && !darkTheme && contentRootAvailable;
    }
}
