package com.xpwnit.dualbt.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xpwnit.dualbt.ui.theme.LocalGlassColors

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    glowColor: Color? = null,
    cornerRadius: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 4.dp,
        animationSpec = tween(300),
        label = "elevation"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            glowColor ?: glassColors.gradientStart
        } else {
            glassColors.glassBorder
        },
        animationSpec = tween(400),
        label = "borderColor"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            glassColors.glassSurface.copy(alpha = 0.25f)
        } else {
            glassColors.glassSurface
        },
        animationSpec = tween(400),
        label = "bgColor"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isSelected) (glowColor ?: glassColors.selectedGlow) else Color.Transparent,
                spotColor = if (isSelected) (glowColor ?: glassColors.selectedGlow) else Color.Transparent
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        bgColor,
                        bgColor.copy(alpha = bgColor.alpha * 0.7f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.3f)
                    )
                ),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(16.dp)
    ) {
        content()
    }
}
