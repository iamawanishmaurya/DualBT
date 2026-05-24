package com.xpwnit.dualbt.ui;

public final class SystemBarAppearancePolicyTest {
    public static void main(String[] args) {
        assertFalse(
                SystemBarAppearancePolicy.shouldApplyLightSystemBars(30, false, false),
                "light system bars are deferred until a content root exists"
        );
        assertTrue(
                SystemBarAppearancePolicy.shouldApplyLightSystemBars(30, false, true),
                "light system bars apply on Android R+ in light theme after content root creation"
        );
        assertFalse(
                SystemBarAppearancePolicy.shouldApplyLightSystemBars(30, true, true),
                "dark theme keeps dark system bars"
        );
        assertFalse(
                SystemBarAppearancePolicy.shouldApplyLightSystemBars(29, false, true),
                "pre-R devices do not use WindowInsetsController appearance flags"
        );
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean value, String message) {
        if (value) {
            throw new AssertionError(message);
        }
    }
}
