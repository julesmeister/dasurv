package com.dasurv.ui.screen.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.local.entity.LipPhoto
import com.dasurv.data.local.entity.LipPhotoPigment
import com.dasurv.data.local.entity.LipZone
import com.dasurv.ui.component.ColorSwatch
import com.dasurv.ui.component.DasurvConfirmDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun LipPhotoCard(
    photo: LipPhoto,
    pigments: List<LipPhotoPigment>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onViewSummary: () -> Unit,
    onOpenAnalysis: () -> Unit,
    onUpdateNotes: (String) -> Unit,
    onUpdateCaptureType: (CaptureType, String?) -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var showCaptureTypeMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggleExpand() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = File(photo.photoUri),
                    contentDescription = "Lip photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        dateFormat.format(Date(photo.capturedAt)),
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (photo.followUpInterval != null) {
                        Text(
                            photo.followUpInterval,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        photo.upperLipColorHex?.let { hex ->
                            ColorSwatch(colorHex = hex, label = "U")
                        }
                        photo.lowerLipColorHex?.let { hex ->
                            ColorSwatch(colorHex = hex, label = "L")
                        }
                    }
                }

                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    "Toggle details"
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    AsyncImage(
                        model = File(photo.photoUri),
                        contentDescription = "Full lip photo",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(Modifier.height(8.dp))

                    PigmentDetails(pigments)

                    if (photo.notes.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Notes: ${photo.notes}", style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(8.dp))

                    // Navigation buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (pigments.isNotEmpty()) {
                            FilledTonalButton(onClick = onViewSummary, modifier = Modifier.weight(1f)) {
                                Text("View Summary")
                            }
                        }
                        FilledTonalButton(onClick = onOpenAnalysis, modifier = Modifier.weight(1f)) {
                            Text("Open Analysis")
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Control buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { showNotesDialog = true }) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = "Edit notes",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box {
                            IconButton(onClick = { showCaptureTypeMenu = true }) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = "Change type",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            CaptureTypeDropdown(
                                expanded = showCaptureTypeMenu,
                                currentType = photo.captureType,
                                onDismiss = { showCaptureTypeMenu = false },
                                onSelect = { type, interval ->
                                    onUpdateCaptureType(type, interval)
                                    showCaptureTypeMenu = false
                                }
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete photo",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DasurvConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = Icons.Default.Delete,
            title = "Delete Photo",
            message = "This will permanently delete the photo and its pigment data.",
            confirmText = "Delete",
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            }
        )
    }

    if (showNotesDialog) {
        var text by remember { mutableStateOf(photo.notes) }
        DasurvConfirmDialog(
            onDismissRequest = { showNotesDialog = false },
            icon = Icons.Default.EditNote,
            iconTint = MaterialTheme.colorScheme.primary,
            title = "Edit Notes",
            content = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text("Add notes about this photo...") }
                )
            },
            confirmText = "Save",
            confirmColor = MaterialTheme.colorScheme.primary,
            onConfirm = {
                onUpdateNotes(text)
                showNotesDialog = false
            }
        )
    }
}

@Composable
private fun PigmentDetails(pigments: List<LipPhotoPigment>) {
    val upperPigments = pigments.filter { it.lipZone == LipZone.UPPER }
    val lowerPigments = pigments.filter { it.lipZone == LipZone.LOWER }

    if (upperPigments.isNotEmpty()) {
        Text("Upper Lip Pigments", style = MaterialTheme.typography.labelLarge)
        upperPigments.forEach { pigment ->
            PigmentRow(pigment)
        }
    }

    if (lowerPigments.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        Text("Lower Lip Pigments", style = MaterialTheme.typography.labelLarge)
        lowerPigments.forEach { pigment ->
            PigmentRow(pigment)
        }
    }
}

@Composable
private fun PigmentRow(pigment: LipPhotoPigment) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        ColorSwatch(colorHex = pigment.pigmentColorHex, label = "")
        Spacer(Modifier.width(8.dp))
        Column {
            Text(pigment.pigmentName, style = MaterialTheme.typography.bodyMedium)
            Text(
                pigment.pigmentBrand,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CaptureTypeDropdown(
    expanded: Boolean,
    currentType: CaptureType,
    onDismiss: () -> Unit,
    onSelect: (CaptureType, String?) -> Unit
) {
    var showFollowUpDialog by remember { mutableStateOf(false) }

    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss, scrollState = rememberScrollState(), shadowElevation = 0.dp) {
        DropdownMenuItem(
            text = { Text("Before") },
            onClick = { onSelect(CaptureType.BEFORE, null) },
            enabled = currentType != CaptureType.BEFORE
        )
        DropdownMenuItem(
            text = { Text("After") },
            onClick = { onSelect(CaptureType.AFTER, null) },
            enabled = currentType != CaptureType.AFTER
        )
        DropdownMenuItem(
            text = { Text("Follow-up...") },
            onClick = {
                onDismiss()
                showFollowUpDialog = true
            }
        )
    }

    if (showFollowUpDialog) {
        FollowUpIntervalDialog(
            onDismiss = { showFollowUpDialog = false },
            onConfirm = { interval ->
                onSelect(CaptureType.FOLLOW_UP, interval)
                showFollowUpDialog = false
            }
        )
    }
}

@Composable
private fun FollowUpIntervalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val intervals = listOf("1 Week", "2 Weeks", "1 Month", "3 Months", "6 Months", "1 Year")
    var selected by remember { mutableStateOf(intervals[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Follow-up Interval") },
        text = {
            Column {
                intervals.forEach { interval ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = interval }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selected == interval,
                            onClick = { selected = interval }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(interval, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Set") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
