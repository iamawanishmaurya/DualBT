package com.xpwnit.dualbt.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xpwnit.dualbt.logging.AppLogger
import com.xpwnit.dualbt.ui.components.AnimatedBackground
import com.xpwnit.dualbt.ui.components.GlassCard
import com.xpwnit.dualbt.ui.components.PulseAnimation
import com.xpwnit.dualbt.ui.theme.LocalGlassColors
import com.xpwnit.dualbt.vm.MainViewModel

@Composable
fun MainScreen(
    onRequestProjection: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val glassColors = LocalGlassColors.current
    var showLogs by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Animated gradient background with floating orbs
        AnimatedBackground(isStreaming = uiState.isStreaming)

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // === Header ===
            HeaderSection(
                isStreaming = uiState.isStreaming,
                isEmulatorMode = uiState.isEmulatorMode,
                onShowLogs = { showLogs = true },
                onRefresh = { viewModel.refresh() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // === Status Bar ===
            StatusBar(
                message = uiState.statusMessage,
                isStreaming = uiState.isStreaming,
                selectedCount = uiState.selectedDevices.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === Device List ===
            Text(
                text = "Available Devices",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(
                    items = uiState.devices,
                    key = { _, device -> device.address }
                ) { index, device ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { it * (index + 1) },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn(animationSpec = tween(300, delayMillis = index * 100))
                    ) {
                        DeviceCard(
                            device = device,
                            isSelected = viewModel.isSelected(device),
                            isStreaming = uiState.isStreaming,
                            onToggle = {
                                if (!uiState.isStreaming) {
                                    viewModel.toggleDevice(device)
                                }
                            }
                        )
                    }
                }

                if (uiState.devices.isEmpty()) {
                    item {
                        EmptyDevicesCard()
                    }
                }
            }

            // === Stream Control Button ===
            StreamButton(
                isStreaming = uiState.isStreaming,
                canStream = uiState.selectedDevices.isNotEmpty(),
                isEmulatorMode = uiState.isEmulatorMode,
                onStart = {
                    viewModel.startStreaming()
                    if (!uiState.isEmulatorMode) {
                        onRequestProjection()
                    }
                },
                onStop = { viewModel.stopStreaming() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // === Error Snackbar ===
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    Text(
                        "Dismiss",
                        color = MaterialTheme.colorScheme.inversePrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Text(error)
            }
        }

        // === Log Screen Overlay ===
        AnimatedVisibility(
            visible = showLogs,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            LogScreen(onDismiss = { showLogs = false })
        }
    }
}

@Composable
private fun HeaderSection(
    isStreaming: Boolean,
    isEmulatorMode: Boolean,
    onShowLogs: () -> Unit,
    onRefresh: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isStreaming) Icons.Default.BluetoothSearching else Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = if (isStreaming) glassColors.success else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "DualBT",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                glassColors.gradientStart,
                                glassColors.gradientMiddle,
                                glassColors.gradientEnd
                            )
                        )
                    )
                )
            }
            if (isEmulatorMode) {
                Text(
                    text = "⚡ Emulator Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = glassColors.warning,
                    modifier = Modifier.padding(start = 42.dp)
                )
            }
        }

        Row {
            IconButton(onClick = onShowLogs) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = "View Logs",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyDevicesCard() {
    val glassColors = LocalGlassColors.current

    GlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BluetoothSearching,
                contentDescription = null,
                tint = glassColors.textTertiary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Bluetooth devices found",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Make sure Bluetooth is enabled\nand devices are paired",
                style = MaterialTheme.typography.bodySmall,
                color = glassColors.textTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StreamButton(
    isStreaming: Boolean,
    canStream: Boolean,
    isEmulatorMode: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    val buttonHeight by animateDpAsState(
        targetValue = if (isStreaming) 60.dp else 56.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "buttonHeight"
    )

    Button(
        onClick = { if (isStreaming) onStop() else onStart() },
        enabled = canStream || isStreaming,
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isStreaming) {
                glassColors.gradientEnd.copy(alpha = 0.9f)
            } else {
                Color.Transparent
            },
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isStreaming && canStream) {
                        Modifier.background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    glassColors.gradientStart,
                                    glassColors.gradientMiddle,
                                    glassColors.gradientEnd
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else if (!canStream && !isStreaming) {
                        Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = isStreaming,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "buttonIcon"
                ) { streaming ->
                    Icon(
                        imageVector = if (streaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (canStream || streaming) Color.White else glassColors.textTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedContent(
                    targetState = isStreaming,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "buttonText"
                ) { streaming ->
                    Text(
                        text = if (streaming) "Stop Streaming" else {
                            if (isEmulatorMode) "Start Mock Stream" else "Start Streaming"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (canStream || streaming) Color.White else glassColors.textTertiary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
