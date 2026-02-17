package com.dasurv.ui.screen.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.dasurv.ui.util.parseHexSafe
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.LipZone
import com.dasurv.data.model.DualLipAnalysis
import com.dasurv.data.model.Pigment
import com.dasurv.ui.theme.RosePrimary
import com.dasurv.ui.theme.RoseTertiary

@Composable
internal fun LipWidthSlider(
    zoneLabel: String,
    scale: Float,
    onScaleChange: (Float) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$zoneLabel Lip Width",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${(scale * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = RosePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Slider(
                value = scale,
                onValueChange = onScaleChange,
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(
                    thumbColor = RosePrimary,
                    activeTrackColor = RoseTertiary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Narrow", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Wide", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun IntensitySlider(
    pigmentName: String,
    intensity: Float,
    onIntensityChange: (Float) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Intensity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${(intensity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = RosePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Slider(
                value = intensity,
                onValueChange = onIntensityChange,
                valueRange = 0.1f..1.0f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = RosePrimary,
                    activeTrackColor = RoseTertiary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sheer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Full", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun NaturalLipColorCard(
    dualAnalysis: DualLipAnalysis,
    activeZone: LipZone,
    closestUpperPigmentName: String?,
    closestLowerPigmentName: String?
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Natural Lip Color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            dualAnalysis.upperLip?.let { upper ->
                LipColorRow(
                    label = "Upper",
                    colorHex = upper.dominantColorHex,
                    categoryName = closestUpperPigmentName ?: upper.category.displayName,
                    isHighlighted = activeZone == LipZone.UPPER
                )
            }
            dualAnalysis.lowerLip?.let { lower ->
                Spacer(Modifier.height(4.dp))
                LipColorRow(
                    label = "Lower",
                    colorHex = lower.dominantColorHex,
                    categoryName = closestLowerPigmentName ?: lower.category.displayName,
                    isHighlighted = activeZone == LipZone.LOWER
                )
            }
        }
    }
}

@Composable
internal fun PredictedResultCard(
    selectedUpperPigment: Pigment?,
    selectedLowerPigment: Pigment?,
    naturalUpperHex: String?,
    naturalLowerHex: String?,
    blendedUpperHex: String?,
    blendedLowerHex: String?
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Predicted Result",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            if (selectedUpperPigment != null && naturalUpperHex != null && blendedUpperHex != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Upper", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(selectedUpperPigment.name, style = MaterialTheme.typography.labelSmall, color = RosePrimary)
                }
                BlendedColorRow(label = "Upper", naturalHex = naturalUpperHex, blendedHex = blendedUpperHex)
            }
            if (selectedLowerPigment != null && naturalLowerHex != null && blendedLowerHex != null) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Lower", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Text(selectedLowerPigment.name, style = MaterialTheme.typography.labelSmall, color = RosePrimary)
                }
                BlendedColorRow(label = "Lower", naturalHex = naturalLowerHex, blendedHex = blendedLowerHex)
            }
        }
    }
}

@Composable
internal fun LipColorRow(label: String, colorHex: String, categoryName: String, isHighlighted: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(parseHexSafe(colorHex))
                .border(
                    if (isHighlighted) 2.dp else 1.dp,
                    if (isHighlighted) RosePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    CircleShape
                )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "$label: $categoryName",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal
        )
        Spacer(Modifier.width(6.dp))
        Text(colorHex, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun BlendedColorRow(label: String, naturalHex: String, blendedHex: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(parseHexSafe(naturalHex))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(parseHexSafe(blendedHex))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text("$label:", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.width(4.dp))
        Text(blendedHex, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

