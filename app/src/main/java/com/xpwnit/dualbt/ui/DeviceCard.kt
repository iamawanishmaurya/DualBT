package com.xpwnit.dualbt.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.xpwnit.dualbt.bt.BTDevice
import com.xpwnit.dualbt.ui.components.GlassCard
import com.xpwnit.dualbt.ui.theme.LocalGlassColors

@Composable
fun DeviceCard(
    device: BTDevice,
    isSelected: Boolean,
    isStreaming: Boolean = false,
    onToggle: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.85f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "alpha"
    )

    GlassCard(
        isSelected = isSelected,
        onClick = onToggle,
        glowColor = if (isSelected) glassColors.success else null,
        modifier = Modifier.alpha(alpha)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (device.isMock) Icons.Default.Speaker else Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = if (isSelected) glassColors.success else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (device.isMock) "⚡ Mock Device" else device.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textTertiary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isStreaming && isSelected) {
                    com.xpwnit.dualbt.ui.components.PulseAnimation(
                        color = glassColors.success,
                        size = 24.dp,
                        isActive = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = glassColors.success,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
