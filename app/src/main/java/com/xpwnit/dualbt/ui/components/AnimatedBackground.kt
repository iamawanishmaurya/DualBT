package com.xpwnit.dualbt.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.xpwnit.dualbt.ui.theme.LocalGlassColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false
) {
    val glassColors = LocalGlassColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "bgAnim")

    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle1"
    )

    val angle2 by infiniteTransition.animateFloat(
        initialValue = 120f,
        targetValue = 480f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle2"
    )

    val angle3 by infiniteTransition.animateFloat(
        initialValue = 240f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle3"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isStreaming) 0.3f else 0.15f,
        targetValue = if (isStreaming) 0.6f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isStreaming) 1500 else 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val r1 = Math.toRadians(angle1.toDouble())
        val r2 = Math.toRadians(angle2.toDouble())
        val r3 = Math.toRadians(angle3.toDouble())

        // Orb 1 - Purple
        val x1 = w * 0.3f + (w * 0.2f * cos(r1)).toFloat()
        val y1 = h * 0.25f + (h * 0.15f * sin(r1)).toFloat()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glassColors.gradientStart.copy(alpha = pulseAlpha),
                    Color.Transparent
                ),
                center = Offset(x1, y1),
                radius = w * 0.5f
            ),
            radius = w * 0.5f,
            center = Offset(x1, y1)
        )

        // Orb 2 - Teal
        val x2 = w * 0.7f + (w * 0.15f * cos(r2)).toFloat()
        val y2 = h * 0.6f + (h * 0.1f * sin(r2)).toFloat()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glassColors.gradientMiddle.copy(alpha = pulseAlpha * 0.7f),
                    Color.Transparent
                ),
                center = Offset(x2, y2),
                radius = w * 0.4f
            ),
            radius = w * 0.4f,
            center = Offset(x2, y2)
        )

        // Orb 3 - Pink
        val x3 = w * 0.5f + (w * 0.2f * cos(r3)).toFloat()
        val y3 = h * 0.85f + (h * 0.1f * sin(r3)).toFloat()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glassColors.gradientEnd.copy(alpha = pulseAlpha * 0.5f),
                    Color.Transparent
                ),
                center = Offset(x3, y3),
                radius = w * 0.35f
            ),
            radius = w * 0.35f,
            center = Offset(x3, y3)
        )
    }
}
