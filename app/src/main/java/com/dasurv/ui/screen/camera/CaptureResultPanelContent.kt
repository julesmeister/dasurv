package com.dasurv.ui.screen.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import com.dasurv.ui.util.parseHexSafe
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.LipZone
import com.dasurv.data.model.DualLipAnalysis
import com.dasurv.data.model.Pigment
import com.dasurv.data.model.PigmentBrand
import com.dasurv.ui.theme.RosePrimary
import com.dasurv.util.ColorMatcher
import com.google.mlkit.vision.face.Face

@Composable
internal fun ColumnScope.CaptureResultPanelContent(
    dualAnalysis: DualLipAnalysis?,
    activeZone: LipZone,
    selectedUpperPigment: Pigment?,
    selectedLowerPigment: Pigment?,
    activeZonePigment: Pigment?,
    activeZoneIntensity: Float,
    anyPigmentSelected: Boolean,
    detectedFace: Face?,
    upperLipScale: Float,
    lowerLipScale: Float,
    blendedUpperHex: String?,
    blendedLowerHex: String?,
    upperRecs: List<ColorMatcher.PigmentRecommendation>,
    lowerRecs: List<ColorMatcher.PigmentRecommendation>,
    filteredPigments: List<Pigment>,
    selectedBrand: PigmentBrand?,
    onSetActiveZone: (LipZone) -> Unit,
    onSetLipScale: (LipZone, Float) -> Unit,
    onSetIntensity: (Float) -> Unit,
    onSelectPigment: (Pigment) -> Unit,
    onSelectBrand: (PigmentBrand?) -> Unit
) {
    var showAllPigments by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
    ) {
        // Zone toggle chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = activeZone == LipZone.UPPER,
                onClick = { onSetActiveZone(LipZone.UPPER) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Upper Lip")
                        if (selectedUpperPigment != null) {
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(parseHexSafe(selectedUpperPigment.colorHex))
                                    .border(0.5.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                            )
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RosePrimary,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = null
            )
            FilterChip(
                selected = activeZone == LipZone.LOWER,
                onClick = { onSetActiveZone(LipZone.LOWER) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Lower Lip")
                        if (selectedLowerPigment != null) {
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(parseHexSafe(selectedLowerPigment.colorHex))
                                    .border(0.5.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                            )
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RosePrimary,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                border = null
            )
        }

        // Natural Lip Color card
        if (dualAnalysis != null) {
            NaturalLipColorCard(
                dualAnalysis = dualAnalysis,
                activeZone = activeZone,
                closestUpperPigmentName = upperRecs.maxByOrNull { it.matchScore }?.let {
                    "${it.pigment.name} (${(it.matchScore * 100).toInt()}%)"
                },
                closestLowerPigmentName = lowerRecs.maxByOrNull { it.matchScore }?.let {
                    "${it.pigment.name} (${(it.matchScore * 100).toInt()}%)"
                }
            )
            Spacer(Modifier.height(8.dp))
        }

        // Per-zone lip width sliders (when face detected and pigment selected)
        if (detectedFace != null && selectedUpperPigment != null) {
            LipWidthSlider(
                zoneLabel = "Upper",
                scale = upperLipScale,
                onScaleChange = { onSetLipScale(LipZone.UPPER, it) }
            )
            Spacer(Modifier.height(8.dp))
        }
        if (detectedFace != null && selectedLowerPigment != null) {
            LipWidthSlider(
                zoneLabel = "Lower",
                scale = lowerLipScale,
                onScaleChange = { onSetLipScale(LipZone.LOWER, it) }
            )
            Spacer(Modifier.height(8.dp))
        }

        // Intensity slider (when active zone has pigment selected)
        if (activeZonePigment != null) {
            IntensitySlider(
                pigmentName = activeZonePigment.name,
                intensity = activeZoneIntensity,
                onIntensityChange = onSetIntensity
            )
            Spacer(Modifier.height(8.dp))
        }

        // Predicted Result card
        if (anyPigmentSelected && (blendedUpperHex != null || blendedLowerHex != null)) {
            PredictedResultCard(
                selectedUpperPigment = selectedUpperPigment,
                selectedLowerPigment = selectedLowerPigment,
                naturalUpperHex = dualAnalysis?.upperLip?.dominantColorHex,
                naturalLowerHex = dualAnalysis?.lowerLip?.dominantColorHex,
                blendedUpperHex = blendedUpperHex,
                blendedLowerHex = blendedLowerHex
            )
            Spacer(Modifier.height(8.dp))
        }

        // Recommended Pigments
        val activeRecs = if (activeZone == LipZone.UPPER) upperRecs else lowerRecs
        val recsList = activeRecs
            .distinctBy { it.pigment.name }
            .sortedByDescending { it.matchScore }
            .take(10)

        if (recsList.isNotEmpty()) {
            Text(
                "Recommended for ${if (activeZone == LipZone.UPPER) "Upper" else "Lower"} Lip",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recsList) { rec ->
                    RecommendedPigmentSwatch(
                        rec = rec,
                        isSelected = activeZonePigment == rec.pigment,
                        onClick = { onSelectPigment(rec.pigment) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // All Pigments expandable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAllPigments = !showAllPigments },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "All Pigments",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            Icon(
                if (showAllPigments) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = showAllPigments,
            enter = fadeIn(tween(200)) + expandVertically(),
            exit = fadeOut(tween(200)) + shrinkVertically()
        ) {
            Column {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedBrand == null,
                        onClick = { onSelectBrand(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RosePrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = null
                    )
                    PigmentBrand.entries.forEach { brand ->
                        FilterChip(
                            selected = selectedBrand == brand,
                            onClick = { onSelectBrand(brand) },
                            label = {
                                Text(
                                    when (brand) {
                                        PigmentBrand.PERMABLEND_LUXE -> "LUXE"
                                        PigmentBrand.PERMABLEND_ORIGINAL -> "Original"
                                        PigmentBrand.EVENFLO -> "Evenflo"
                                        PigmentBrand.TRUNM -> "TRUNM"
                                    }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RosePrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = null
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))

                val gridRows = (filteredPigments.size + 3) / 4
                val gridHeight = (gridRows * 90).dp
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = false
                ) {
                    items(filteredPigments, key = { "${it.name}|${it.brand.displayName}" }) { pigment ->
                        AllPigmentSwatch(
                            pigment = pigment,
                            isSelected = activeZonePigment == pigment,
                            onClick = { onSelectPigment(pigment) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
