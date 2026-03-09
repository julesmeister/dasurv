package com.dasurv.ui.screen.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.ui.component.*
import com.dasurv.ui.util.parseHexSafe
import com.dasurv.util.formatMl

@Composable
internal fun BottleRow(
    bottle: PigmentBottle,
    isSelected: Boolean,
    entry: PigmentBottleSessionEntry?,
    onToggle: () -> Unit,
    onSetMlUsed: (Double) -> Unit,
    onSetLipArea: (UsageLipArea) -> Unit,
) {
    val bottleColor = remember(bottle.colorHex) { parseHexSafe(bottle.colorHex) }
    val mlUsed = entry?.mlUsed ?: 0.5
    val afterUse = (bottle.remainingMl - mlUsed).coerceAtLeast(0.0)
    val afterPct = if (bottle.bottleSizeMl > 0) afterUse / bottle.bottleSizeMl else 0.0

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = M3Primary),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bottleColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(bottleColor)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bottle.pigmentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = M3OnSurface,
                )
                Text(
                    "${bottle.pigmentBrand} · ${bottle.remainingMl.formatMl()} ml",
                    fontSize = 13.sp,
                    color = M3OnSurfaceVariant,
                )
            }
        }

        // Expanded usage details
        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically() + fadeIn(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 56.dp, end = 12.dp, bottom = 10.dp),
            ) {
                // Amount row — icon box + label + input + remaining pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(M3Primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Opacity, null,
                            modifier = Modifier.size(18.dp),
                            tint = M3Primary,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Amount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = M3OnSurface,
                        modifier = Modifier.weight(1f),
                    )
                    var mlText by remember(bottle.id, entry?.mlUsed) {
                        mutableStateOf(entry?.mlUsed?.let {
                            if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
                        } ?: "0.5")
                    }
                    DasurvTextField(
                        value = mlText,
                        onValueChange = { newVal ->
                            mlText = newVal
                            newVal.toDoubleOrNull()?.let { onSetMlUsed(it) }
                        },
                        label = "ml",
                        modifier = Modifier.width(64.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        autoCapitalize = false,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val leftColor = when {
                        afterPct > 0.5 -> M3GreenColor
                        afterPct >= 0.2 -> M3AmberColor
                        else -> M3RedColor
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = leftColor.copy(alpha = 0.15f),
                    ) {
                        Text(
                            "${afterUse.formatMl()} left",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = leftColor,
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = M3Outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp,
                )

                // Lip area row — icon box + label + chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(M3Primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Colorize, null,
                            modifier = Modifier.size(18.dp),
                            tint = M3Primary,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Lip Area",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = M3OnSurface,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        UsageLipArea.entries.forEach { area ->
                            val isAreaSelected = (entry?.lipArea ?: UsageLipArea.BOTH) == area
                            DasurvFilterChip(
                                label = when (area) {
                                    UsageLipArea.UPPER -> "Upper"
                                    UsageLipArea.LOWER -> "Lower"
                                    UsageLipArea.BOTH -> "Both"
                                },
                                selected = isAreaSelected,
                                onClick = { onSetLipArea(area) },
                            )
                        }
                    }
                }
            }
        }
    }
}
