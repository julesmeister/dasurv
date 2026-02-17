package com.dasurv.ui.screen.session

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SessionTimerControls(
    isTimerForThisClient: Boolean,
    isTimerForOtherClient: Boolean,
    timerState: SessionTimerState,
    onPauseResume: () -> Unit,
    onRequestStop: () -> Unit,
    onShowConflictDialog: () -> Unit,
    onStartTimer: () -> Unit,
    onToggleUpper: () -> Unit = {},
    onToggleLower: () -> Unit = {}
) {
    when {
        isTimerForThisClient -> {
            ActiveTimerCard(
                timerState = timerState,
                onPauseResume = onPauseResume,
                onRequestStop = onRequestStop,
                onToggleUpper = onToggleUpper,
                onToggleLower = onToggleLower
            )
        }
        isTimerForOtherClient -> {
            FilledTonalButton(
                onClick = onShowConflictDialog,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Timer (${timerState.clientName} has active timer)")
            }
        }
        else -> {
            FilledTonalButton(
                onClick = onStartTimer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Session Timer")
            }
        }
    }
}

@Composable
private fun ActiveTimerCard(
    timerState: SessionTimerState,
    onPauseResume: () -> Unit,
    onRequestStop: () -> Unit,
    onToggleUpper: () -> Unit,
    onToggleLower: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stopwatch display
            val pulsingAlpha = if (timerState.isPaused) {
                val transition = rememberInfiniteTransition(label = "pulse")
                val alpha by transition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )
                alpha
            } else 1f

            Text(
                text = formatTimerTime(timerState.totalElapsed),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = if (timerState.isPaused) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(pulsingAlpha)
            )

            if (timerState.isPaused) {
                Text(
                    "PAUSED",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(16.dp))

            // Main controls: Pause/Resume + Stop
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume
                FilledIconButton(
                    onClick = onPauseResume,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (timerState.isPaused)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = if (timerState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (timerState.isPaused) "Resume" else "Pause",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Stop
                FilledIconButton(
                    onClick = onRequestStop,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Stop",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))

            // Zone toggles
            Text(
                "Lip Zone Tracking",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ZoneToggle(
                    label = "Upper Lip",
                    elapsed = timerState.upperElapsed,
                    isRunning = timerState.isUpperRunning && timerState.isRunning,
                    enabled = !timerState.isPaused,
                    onToggle = onToggleUpper,
                    modifier = Modifier.weight(1f)
                )
                ZoneToggle(
                    label = "Lower Lip",
                    elapsed = timerState.lowerElapsed,
                    isRunning = timerState.isLowerRunning && timerState.isRunning,
                    enabled = !timerState.isPaused,
                    onToggle = onToggleLower,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ZoneToggle(
    label: String,
    elapsed: Long,
    isRunning: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isRunning -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val contentColor = when {
        isRunning -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onToggle,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                formatTimerTime(elapsed),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                ),
                color = contentColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (isRunning) "Tracking" else "Tap to start",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

internal fun formatTimerTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
