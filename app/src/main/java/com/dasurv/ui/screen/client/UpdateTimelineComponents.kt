package com.dasurv.ui.screen.client

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dasurv.data.local.entity.ClientUpdate
import com.dasurv.data.local.entity.Session
import com.dasurv.ui.component.DasurvEmptyState
import com.dasurv.ui.component.DetailActionButton
import com.dasurv.ui.component.DetailSectionHeader
import com.dasurv.ui.component.M3CyanColor
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3RedColor
import com.dasurv.util.FMT_DATE
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

// ── Update Timeline Card ───────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UpdateTimelineCard(
    update: ClientUpdate,
    sessions: List<Session> = emptyList(),
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat(FMT_DATE, Locale.getDefault()) }
    val tags = remember(update.tags) {
        try {
            val array = JSONArray(update.tags)
            (0 until array.length()).map { array.getString(it) }
        } catch (_: Exception) { emptyList() }
    }
    val sessionLabel = if (update.sessionId != null) {
        val session = sessions.find { it.id == update.sessionId }
        if (session != null) {
            val sessionDate = dateFormat.format(Date(session.date))
            "Session $sessionDate"
        } else "Linked Session"
    } else null

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    dateFormat.format(Date(update.date)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = M3OnSurface,
                )
                if (sessionLabel != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = M3Primary.copy(alpha = 0.10f),
                    ) {
                        Text(
                            sessionLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = M3Primary,
                        )
                    }
                }
            }
            Row {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onEdit() },
                    tint = M3OnSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDelete() },
                    tint = M3RedColor.copy(alpha = 0.6f),
                )
            }
        }

        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                tags.forEach { tag -> UpdateTagChip(tag) }
            }
        }

        if (update.photoUri != null) {
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = update.photoUri,
                contentDescription = "Update photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        }

        if (update.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                update.notes,
                fontSize = 13.sp,
                color = M3OnSurfaceVariant,
            )
        }
    }
}

// ── Updates Timeline Section (for ClientDetailScreen) ──────────────

@Composable
internal fun UpdatesTimelineSection(
    updates: List<ClientUpdate>,
    sessions: List<Session>,
    onAddUpdate: () -> Unit,
    onEditUpdate: (ClientUpdate) -> Unit,
    onDeleteUpdate: (ClientUpdate) -> Unit,
) {
    Column {
        DetailSectionHeader(
            icon = Icons.Default.Timeline,
            title = "UPDATES (${updates.size})",
            accentColor = M3CyanColor,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
            DetailActionButton(
                label = "Add Update",
                icon = Icons.Default.Add,
                color = M3CyanColor,
                onClick = onAddUpdate,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (updates.isEmpty()) {
            DasurvEmptyState(
                icon = Icons.AutoMirrored.Filled.Label,
                message = "No updates yet"
            )
        } else {
            M3ListCard {
                Column(modifier = Modifier.animateContentSize()) {
                    updates.forEachIndexed { index, update ->
                        UpdateTimelineCard(
                            update = update,
                            sessions = sessions,
                            onEdit = { onEditUpdate(update) },
                            onDelete = { onDeleteUpdate(update) },
                        )
                        if (index < updates.lastIndex) {
                            M3ListDivider()
                        }
                    }
                }
            }
        }
    }
}
