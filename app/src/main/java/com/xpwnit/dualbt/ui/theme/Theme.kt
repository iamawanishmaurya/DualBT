package com.xpwnit.dualbt.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val DarkColorScheme = darkColorScheme(
    primary = DarkColors.Primary,
    onPrimary = DarkColors.TextPrimary,
    primaryContainer = DarkColors.PrimaryVariant,
    secondary = DarkColors.Secondary,
    onSecondary = DarkColors.TextPrimary,
    secondaryContainer = DarkColors.SecondaryVariant,
    tertiary = DarkColors.Accent,
    tertiaryContainer = DarkColors.Accent.copy(alpha = 0.15f),
    background = DarkColors.Background,
    onBackground = DarkColors.TextPrimary,
    surface = DarkColors.Surface,
    onSurface = DarkColors.TextPrimary,
    surfaceVariant = DarkColors.SurfaceVariant,
    onSurfaceVariant = DarkColors.TextSecondary,
    outline = DarkColors.GlassBorder,
    error = DarkColors.Error,
    onError = DarkColors.TextPrimary
)

val LightColorScheme = lightColorScheme(
    primary = LightColors.Primary,
    onPrimary = LightColors.Surface,
    primaryContainer = LightColors.PrimaryVariant,
    secondary = LightColors.Secondary,
    onSecondary = LightColors.Surface,
    secondaryContainer = LightColors.SecondaryVariant,
    tertiary = LightColors.Accent,
    tertiaryContainer = LightColors.Accent.copy(alpha = 0.1f),
    background = LightColors.Background,
    onBackground = LightColors.TextPrimary,
    surface = LightColors.Surface,
    onSurface = LightColors.TextPrimary,
    surfaceVariant = LightColors.SurfaceVariant,
    onSurfaceVariant = LightColors.TextSecondary,
    outline = LightColors.GlassBorder,
    error = LightColors.Error,
    onError = LightColors.Surface
)

// Custom colors accessible outside MaterialTheme
data class GlassColors(
    val glassSurface: androidx.compose.ui.graphics.Color,
    val glassBorder: androidx.compose.ui.graphics.Color,
    val gradientStart: androidx.compose.ui.graphics.Color,
    val gradientMiddle: androidx.compose.ui.graphics.Color,
    val gradientEnd: androidx.compose.ui.graphics.Color,
    val cardGlow: androidx.compose.ui.graphics.Color,
    val selectedGlow: androidx.compose.ui.graphics.Color,
    val success: androidx.compose.ui.graphics.Color,
    val warning: androidx.compose.ui.graphics.Color,
    val textTertiary: androidx.compose.ui.graphics.Color
)

val LocalGlassColors = compositionLocalOf {
    GlassColors(
        glassSurface = DarkColors.GlassSurface,
        glassBorder = DarkColors.GlassBorder,
        gradientStart = DarkColors.GradientStart,
        gradientMiddle = DarkColors.GradientMiddle,
        gradientEnd = DarkColors.GradientEnd,
        cardGlow = DarkColors.CardGlow,
        selectedGlow = DarkColors.SelectedGlow,
        success = DarkColors.Success,
        warning = DarkColors.Warning,
        textTertiary = DarkColors.TextTertiary
    )
}

@Composable
fun DualBTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val glassColors = if (darkTheme) {
        GlassColors(
            glassSurface = DarkColors.GlassSurface,
            glassBorder = DarkColors.GlassBorder,
            gradientStart = DarkColors.GradientStart,
            gradientMiddle = DarkColors.GradientMiddle,
            gradientEnd = DarkColors.GradientEnd,
            cardGlow = DarkColors.CardGlow,
            selectedGlow = DarkColors.SelectedGlow,
            success = DarkColors.Success,
            warning = DarkColors.Warning,
            textTertiary = DarkColors.TextTertiary
        )
    } else {
        GlassColors(
            glassSurface = LightColors.GlassSurface,
            glassBorder = LightColors.GlassBorder,
            gradientStart = LightColors.GradientStart,
            gradientMiddle = LightColors.GradientMiddle,
            gradientEnd = LightColors.GradientEnd,
            cardGlow = LightColors.CardGlow,
            selectedGlow = LightColors.SelectedGlow,
            success = LightColors.Success,
            warning = LightColors.Warning,
            textTertiary = LightColors.TextTertiary
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalGlassColors provides glassColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DualBTTypography,
            content = content
        )
    }
}
