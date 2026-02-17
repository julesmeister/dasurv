package com.dasurv.ui.screen.camera

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dasurv.ui.theme.RoseDark
import com.dasurv.ui.theme.RosePrimary
import com.dasurv.ui.theme.RoseTertiary

@Composable
internal fun ShutterButton(
    enabled: Boolean,
    isCapturing: Boolean,
    showSuccess: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (showSuccess) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
        label = "shutter_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            showSuccess -> Color(0xFF4CAF50)
            !enabled -> RoseDark.copy(alpha = 0.3f)
            else -> RosePrimary
        },
        animationSpec = tween(300),
        label = "shutter_color"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(RoseTertiary, RosePrimary)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner filled circle
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable(enabled = enabled || isCapturing) { onClick() },
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White
                    )
                } else if (showSuccess) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Saved",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                } else {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Capture",
                        modifier = Modifier.size(32.dp),
                        tint = if (enabled) Color.White else Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
