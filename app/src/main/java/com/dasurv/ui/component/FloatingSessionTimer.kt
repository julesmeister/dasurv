package com.dasurv.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.dasurv.ui.screen.session.SessionTimerState
import kotlin.math.roundToInt

@Composable
fun FloatingSessionTimer(
    state: SessionTimerState,
    onToggleExpanded: () -> Unit,
    onToggleUpper: () -> Unit,
    onToggleLower: () -> Unit,
    onPauseResume: () -> Unit,
    onRequestStop: () -> Unit,
    onCancelStop: () -> Unit,
    onConfirmDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(200f) }

    // Stop confirmation dialog
    if (state.showStopConfirmation) {
        DasurvConfirmDialog(
            onDismissRequest = onCancelStop,
            icon = Icons.Default.Timer,
            iconTint = MaterialTheme.colorScheme.primary,
            title = "Stop Timer?",
            content = {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(
                        "Timer for ${state.clientName.ifBlank { "this session" }}:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total: ${formatTime(state.totalElapsed)}", fontWeight = FontWeight.Medium)
                    if (state.upperElapsed > 0) {
                        Text("Upper Lip: ${formatTime(state.upperElapsed)}")
                    }
                    if (state.lowerElapsed > 0) {
                        Text("Lower Lip: ${formatTime(state.lowerElapsed)}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Go to the session form to save these durations, or discard them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmText = "Discard",
            dismissText = "Keep Timer",
            onConfirm = onConfirmDiscard,
            onDismiss = onCancelStop
        )
    }

    AnimatedVisibility(
        visible = state.isActive,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        val dragModifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }

        if (state.isExpanded) {
            ExpandedTimer(
                state = state,
                onMinimize = onToggleExpanded,
                onPauseResume = onPauseResume,
                onRequestStop = onRequestStop,
                onToggleUpper = onToggleUpper,
                onToggleLower = onToggleLower,
                modifier = dragModifier
            )
        } else {
            CollapsedTimer(
                state = state,
                onTap = onToggleExpanded,
                modifier = dragModifier
            )
        }
    }
}

@Composable
private fun CollapsedTimer(
    state: SessionTimerState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onTap,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = if (state.isPaused) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 6.dp,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.clientName.isNotBlank()) {
                Text(
                    text = state.clientName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.isPaused) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = formatTime(state.totalElapsed),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (state.isPaused) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (state.isPaused) {
                Text(
                    "PAUSED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

internal fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
