package com.dasurv.ui.screen.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.data.model.DualLipAnalysis
import com.dasurv.data.model.LipColorAnalysis
import com.dasurv.ui.theme.GlassBrush
import com.dasurv.ui.theme.RosePrimary
import com.dasurv.ui.theme.RoseTertiary

@Composable
internal fun AnalysisPillsOverlay(
    dualAnalysis: DualLipAnalysis?,
    lipAnalysis: LipColorAnalysis?,
    modifier: Modifier = Modifier
) {
    if (dualAnalysis != null || lipAnalysis != null) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (dualAnalysis != null) {
                dualAnalysis.upperLip?.let { upper ->
                    AnalysisPill(
                        label = "Upper",
                        categoryName = upper.category.displayName,
                        colorHex = upper.dominantColorHex
                    )
                }
                dualAnalysis.lowerLip?.let { lower ->
                    AnalysisPill(
                        label = "Lower",
                        categoryName = lower.category.displayName,
                        colorHex = lower.dominantColorHex
                    )
                }
            } else if (lipAnalysis != null) {
                AnalysisPill(
                    label = "Detected",
                    categoryName = lipAnalysis.category.displayName,
                    colorHex = lipAnalysis.dominantColorHex
                )
            }
        }
    }
}

@Composable
internal fun ArCalibrationSlider(
    visible: Boolean,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + expandVertically(),
        exit = fadeOut(tween(200)) + shrinkVertically(),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(GlassBrush, RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            // Vertical slider for AR offset
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = -200f..200f,
                modifier = Modifier
                    .height(180.dp)
                    .width(36.dp)
                    .graphicsLayer { rotationZ = 270f },
                colors = SliderDefaults.colors(
                    thumbColor = RosePrimary,
                    activeTrackColor = RoseTertiary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                "${value.toInt()}",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
internal fun ControlsHintOverlay(
    onShowControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.4f),
            modifier = Modifier.clickable { onShowControls() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Tap to show controls",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
internal fun CaptureSuccessOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF4CAF50).copy(alpha = 0.9f),
            modifier = Modifier.padding(32.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, "Success", tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Photo Saved!", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
