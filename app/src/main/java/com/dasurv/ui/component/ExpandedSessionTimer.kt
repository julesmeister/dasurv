package com.dasurv.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.ui.screen.session.SessionTimerState

@Composable
internal fun ExpandedTimer(
    state: SessionTimerState,
    onMinimize: () -> Unit,
    onPauseResume: () -> Unit,
    onRequestStop: () -> Unit,
    onToggleUpper: () -> Unit,
    onToggleLower: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(260.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Session Timer",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.clientName.isNotBlank()) {
                        Text(
                            state.clientName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onMinimize, modifier = Modifier.size(32.dp)) {
                    Text(
                        "\u2212",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Total time + controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = formatTime(state.totalElapsed),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isPaused) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.primary
                    )
                    if (state.isPaused) {
                        Text(
                            "PAUSED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Row {
                    // Pause / Resume
                    FilledIconButton(
                        onClick = onPauseResume,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (state.isPaused)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        if (state.isPaused) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                "\u275A\u275A",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Stop
                    FilledIconButton(
                        onClick = onRequestStop,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Stop",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Zone rows
            ZoneRow(
                label = "Upper Lip",
                elapsed = state.upperElapsed,
                isRunning = state.isUpperRunning && state.isRunning,
                enabled = !state.isPaused,
                onToggle = onToggleUpper
            )
            Spacer(modifier = Modifier.height(8.dp))
            ZoneRow(
                label = "Lower Lip",
                elapsed = state.lowerElapsed,
                isRunning = state.isLowerRunning && state.isRunning,
                enabled = !state.isPaused,
                onToggle = onToggleLower
            )
        }
    }
}

@Composable
internal fun ZoneRow(
    label: String,
    elapsed: Long,
    isRunning: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledIconButton(
                onClick = onToggle,
                enabled = enabled,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRunning)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (isRunning) "\u275A\u275A" else "\u25B6",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isRunning)
                        MaterialTheme.colorScheme.onTertiary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = formatTime(elapsed),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isRunning) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
