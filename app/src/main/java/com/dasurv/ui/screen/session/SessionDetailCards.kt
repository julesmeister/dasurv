package com.dasurv.ui.screen.session

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.data.local.entity.ClientUpdate
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.PigmentBottleUsage
import com.dasurv.data.local.entity.SessionEquipment
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.ui.component.DetailDivider
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3CyanColor
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3PinkAccent
import com.dasurv.ui.component.M3RedColor
import com.dasurv.ui.screen.client.UpdateTagChip
import com.dasurv.util.formatCurrency
import com.dasurv.util.formatPrecise
import java.text.SimpleDateFormat
import java.util.*

// ── Equipment Used Card ──────────────────────────────────────────────

@Composable
internal fun SessionEquipmentCard(
    sessionEquipment: List<SessionEquipment>,
    allEquipment: List<Equipment>,
) {
    M3ListCard {
        Column(modifier = Modifier.padding(16.dp)) {
            sessionEquipment.forEach { se ->
                val eqName = allEquipment.find { it.id == se.equipmentId }?.name ?: "Unknown"
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            eqName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = M3OnSurface,
                        )
                        Text(
                            "${se.quantityUsed.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }} x $${se.costPerPiece.formatPrecise()}",
                            fontSize = 12.sp,
                            color = M3OnSurfaceVariant,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = M3AmberColor.copy(alpha = 0.08f),
                    ) {
                        Text(
                            "₱${(se.quantityUsed * se.costPerPiece).formatCurrency()}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = M3AmberColor,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            DetailDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Equipment Total",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = M3OnSurface,
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = M3AmberColor.copy(alpha = 0.08f),
                ) {
                    Text(
                        "₱${sessionEquipment.sumOf { it.quantityUsed * it.costPerPiece }.formatCurrency()}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = M3AmberColor,
                    )
                }
            }
        }
    }
}

// ── Pigments Used Card ───────────────────────────────────────────────

@Composable
internal fun SessionPigmentCard(
    sessionBottleUsages: List<PigmentBottleUsage>,
    allBottles: List<PigmentBottle>,
) {
    M3ListCard {
        Column(modifier = Modifier.padding(16.dp)) {
            sessionBottleUsages.forEach { usage ->
                val bottleName = allBottles.find { it.id == usage.bottleId }?.pigmentName ?: "Unknown"
                val lipAreaText = when (usage.lipArea) {
                    UsageLipArea.UPPER -> "Upper Lip"
                    UsageLipArea.LOWER -> "Lower Lip"
                    UsageLipArea.BOTH -> "Both Lips"
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            bottleName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = M3OnSurface,
                        )
                        Text(
                            "${usage.mlUsed.formatCurrency()} ml — $lipAreaText",
                            fontSize = 12.sp,
                            color = M3OnSurfaceVariant,
                        )
                    }
                    if (usage.costAtTimeOfUse > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = M3PinkAccent.copy(alpha = 0.08f),
                        ) {
                            Text(
                                "₱${usage.costAtTimeOfUse.formatCurrency()}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = M3PinkAccent,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            DetailDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Pigment Total",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = M3OnSurface,
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = M3PinkAccent.copy(alpha = 0.08f),
                ) {
                    Text(
                        "₱${sessionBottleUsages.sumOf { it.costAtTimeOfUse }.formatCurrency()}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = M3PinkAccent,
                    )
                }
            }
        }
    }
}

// ── Tags & Updates Card ──────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SessionUpdatesCard(
    sessionUpdates: List<ClientUpdate>,
    onAddUpdate: () -> Unit,
    onEditUpdate: (ClientUpdate) -> Unit,
    onDeleteUpdate: (ClientUpdate) -> Unit,
) {
    M3ListCard {
        Column(modifier = Modifier.animateContentSize()) {
            if (sessionUpdates.isEmpty()) {
                Text(
                    "No tags yet",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp,
                    color = M3OnSurfaceVariant,
                )
            } else {
                sessionUpdates.forEachIndexed { index, update ->
                    val tags = remember(update.tags) {
                        try {
                            val array = org.json.JSONArray(update.tags)
                            (0 until array.length()).map { array.getString(it) }
                        } catch (_: Exception) { emptyList() }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                dateFormat.format(Date(update.date)),
                                fontSize = 12.sp,
                                color = M3OnSurfaceVariant,
                            )
                            Row {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { onEditUpdate(update) },
                                    tint = M3OnSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { onDeleteUpdate(update) },
                                    tint = M3RedColor.copy(alpha = 0.6f),
                                )
                            }
                        }
                        if (tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                tags.forEach { tag -> UpdateTagChip(tag) }
                            }
                        }
                        if (update.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(update.notes, fontSize = 13.sp, color = M3OnSurfaceVariant)
                        }
                    }
                    if (index < sessionUpdates.lastIndex) {
                        M3ListDivider()
                    }
                }
            }
            // Add update button
            DetailDivider()
            Surface(
                onClick = onAddUpdate,
                color = Color.Transparent,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = M3CyanColor,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Add Tags",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = M3CyanColor,
                    )
                }
            }
        }
    }
}
