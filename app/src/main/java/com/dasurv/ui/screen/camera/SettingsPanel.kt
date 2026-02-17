package com.dasurv.ui.screen.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.model.FollowUpInterval
import com.dasurv.ui.theme.GlassBrush
import com.dasurv.ui.theme.RosePrimary
import com.dasurv.ui.theme.RoseTertiary

@Composable
internal fun AnalysisPill(label: String, categoryName: String, colorHex: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier
            .background(GlassBrush, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Color dot
            val dotColor = try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (_: Exception) {
                RoseTertiary
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            )
            Text(
                "$label: $categoryName",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
internal fun SettingsPanel(
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    selectedClientName: String?,
    onClientClick: () -> Unit,
    captureType: CaptureType,
    onCaptureTypeChange: (CaptureType) -> Unit,
    followUpInterval: FollowUpInterval,
    onFollowUpIntervalChange: (FollowUpInterval) -> Unit,
    customInterval: String,
    onCustomIntervalChange: (String) -> Unit,
    arMode: Boolean,
    onToggleAr: () -> Unit
) {
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
            // Top row: Client + AR toggle (always visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Client chip
                SuggestionChip(
                    onClick = onClientClick,
                    label = {
                        Text(
                            selectedClientName ?: "Select Client",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = RoseTertiary
                        )
                    },
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = RoseTertiary.copy(alpha = 0.5f)
                    ),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(Modifier.width(8.dp))

                // Collapsed: show capture type text
                if (!expanded) {
                    Text(
                        when (captureType) {
                            CaptureType.BEFORE -> "Before"
                            CaptureType.AFTER -> "After"
                            CaptureType.FOLLOW_UP -> "Follow-up"
                        },
                        color = RoseTertiary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // AR toggle
                FilterChip(
                    selected = arMode,
                    onClick = onToggleAr,
                    label = { Text("AR", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (arMode) {
                        { Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RosePrimary.copy(alpha = 0.7f),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = Color.Transparent,
                        labelColor = Color.White.copy(alpha = 0.7f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = arMode,
                        borderColor = RoseTertiary.copy(alpha = 0.4f),
                        selectedBorderColor = RosePrimary
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }

            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(8.dp))

                    // Capture type segmented row
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CaptureType.entries.forEachIndexed { index, type ->
                            SegmentedButton(
                                selected = captureType == type,
                                onClick = { onCaptureTypeChange(type) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = CaptureType.entries.size
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
                                    when (type) {
                                        CaptureType.BEFORE -> "Before"
                                        CaptureType.AFTER -> "After"
                                        CaptureType.FOLLOW_UP -> "Follow-up"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    // Follow-up interval picker
                    AnimatedVisibility(visible = captureType == CaptureType.FOLLOW_UP) {
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(FollowUpInterval.entries.toList()) { interval ->
                                    FilterChip(
                                        selected = followUpInterval == interval,
                                        onClick = { onFollowUpIntervalChange(interval) },
                                        label = { Text(interval.displayName, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RosePrimary.copy(alpha = 0.5f),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color.Transparent,
                                            labelColor = Color.White.copy(alpha = 0.7f)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = followUpInterval == interval,
                                            borderColor = RoseTertiary.copy(alpha = 0.3f),
                                            selectedBorderColor = RosePrimary
                                        ),
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }
                            if (followUpInterval == FollowUpInterval.CUSTOM) {
                                OutlinedTextField(
                                    value = customInterval,
                                    onValueChange = onCustomIntervalChange,
                                    label = { Text("Custom interval") },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedTextColor = Color.White,
                                        focusedTextColor = Color.White,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                        focusedLabelColor = RoseTertiary,
                                        unfocusedBorderColor = RoseTertiary.copy(alpha = 0.4f),
                                        focusedBorderColor = RoseTertiary,
                                        cursorColor = RoseTertiary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Collapse toggle handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(top = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
