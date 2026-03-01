package com.dasurv.ui.screen.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.LipZone
import com.dasurv.ui.theme.GlassBrush
import com.dasurv.ui.theme.RosePrimary
import com.dasurv.ui.theme.RoseTertiary
import com.dasurv.util.ColorMatcher

@Composable
internal fun ArPigmentCard(
    selectedLipZone: LipZone,
    upperRecs: List<ColorMatcher.PigmentRecommendation>,
    lowerRecs: List<ColorMatcher.PigmentRecommendation>,
    selectedUpperIdx: Int,
    selectedLowerIdx: Int,
    onZoneSelected: (LipZone) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val currentRecs = if (selectedLipZone == LipZone.UPPER) upperRecs else lowerRecs
    val currentIdx = if (selectedLipZone == LipZone.UPPER) selectedUpperIdx else selectedLowerIdx

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(GlassBrush, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Pigment swiper
            if (currentRecs.isNotEmpty()) {
                val currentPigment = currentRecs.getOrNull(currentIdx)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            "Previous",
                            tint = RoseTertiary
                        )
                    }
                    if (currentPigment != null) {
                        val swatchColor = remember(currentPigment.pigment.colorHex) {
                            try {
                                Color(android.graphics.Color.parseColor(currentPigment.pigment.colorHex))
                            } catch (_: Exception) {
                                Color.Gray
                            }
                        }
                        // Larger swatch
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                currentPigment.pigment.name,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${currentPigment.pigment.brand.displayName} (${currentIdx + 1}/${currentRecs.size})",
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            "Next",
                            tint = RoseTertiary
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            // Zone selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                LipZone.entries.forEachIndexed { index, zone ->
                    SegmentedButton(
                        selected = selectedLipZone == zone,
                        onClick = { onZoneSelected(zone) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = LipZone.entries.size
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = RosePrimary.copy(alpha = 0.6f),
                            activeContentColor = Color.White,
                            inactiveContainerColor = Color.Transparent,
                            inactiveContentColor = Color.White.copy(alpha = 0.7f),
                            activeBorderColor = RosePrimary,
                            inactiveBorderColor = RoseTertiary.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            zone.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
