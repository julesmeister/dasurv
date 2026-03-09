package com.dasurv.ui.screen.client

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.ClientUpdate
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.FMT_DATE
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUpdateScreen(
    clientId: Long,
    updateId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: ClientViewModel = hiltViewModel()
) {
    val dateFormat = remember { SimpleDateFormat(FMT_DATE, Locale.getDefault()) }
    val sessions by viewModel.getSessionsForClient(clientId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val updates by viewModel.getUpdatesForClient(clientId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val existingUpdate = remember(updateId, updates) {
        if (updateId != null && updateId != 0L) updates.find { it.id == updateId } else null
    }
    val isEdit = existingUpdate != null

    val initialTags = remember(existingUpdate) {
        if (existingUpdate != null) {
            try {
                val array = JSONArray(existingUpdate.tags)
                (0 until array.length()).map { array.getString(it) }.toSet()
            } catch (_: Exception) { emptySet() }
        } else emptySet()
    }

    var selectedTags by remember(existingUpdate) { mutableStateOf(initialTags) }
    var notes by remember(existingUpdate) { mutableStateOf(existingUpdate?.notes ?: "") }
    var photoUri by remember(existingUpdate) { mutableStateOf(existingUpdate?.photoUri ?: "") }
    var customTag by remember { mutableStateOf("") }
    var selectedSessionId by remember(existingUpdate) { mutableStateOf(existingUpdate?.sessionId) }
    var sessionDropdownExpanded by remember { mutableStateOf(false) }

    val canSave = selectedTags.isNotEmpty() || notes.isNotBlank()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        containerColor = M3SurfaceContainer,
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle(if (isEdit) "Edit Update" else "Add Update") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            val tagsJson = JSONArray(selectedTags.toList()).toString()
                            val update = (existingUpdate ?: ClientUpdate(clientId = clientId)).copy(
                                sessionId = selectedSessionId,
                                tags = tagsJson,
                                notes = notes,
                                photoUri = photoUri.ifBlank { null },
                            )
                            viewModel.saveUpdate(update) { onNavigateBack() }
                        },
                        enabled = canSave,
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = M3Primary,
                            contentColor = Color.White,
                            disabledContainerColor = M3Primary.copy(alpha = 0.3f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f),
                        ),
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DasurvTheme.spacing.sm),
        ) {
            Spacer(modifier = Modifier.height(DasurvTheme.spacing.xs))

            // ── Session Link Section ──────────────────────────────────
            if (sessions.isNotEmpty()) {
                DetailSectionHeader(
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    title = "LINKED SESSION",
                    accentColor = M3Primary,
                )

                M3ListCard {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Column {
                            val displayText = if (selectedSessionId != null) {
                                val s = sessions.find { it.id == selectedSessionId }
                                if (s != null) "${dateFormat.format(Date(s.date))} — ${s.procedure.ifBlank { "Session" }}"
                                else "Select session..."
                            } else "None (standalone check-in)"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(M3FieldBg)
                                    .clickable { sessionDropdownExpanded = true }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = displayText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedSessionId != null) M3OnSurface else M3OnSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f),
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = M3OnSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            DropdownMenu(
                                expanded = sessionDropdownExpanded,
                                onDismissRequest = { sessionDropdownExpanded = false },
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = 2.dp,
                                tonalElevation = 0.dp,
                                containerColor = Color.White,
                            ) {
                                val noneSelected = selectedSessionId == null
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "None (standalone check-in)",
                                            fontWeight = if (noneSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (noneSelected) M3Primary else M3OnSurface,
                                        )
                                    },
                                    onClick = {
                                        selectedSessionId = null
                                        sessionDropdownExpanded = false
                                    },
                                    trailingIcon = if (noneSelected) {
                                        { Icon(Icons.Default.Check, null, tint = M3Primary, modifier = Modifier.size(18.dp)) }
                                    } else null,
                                    modifier = if (noneSelected) Modifier.background(M3Primary.copy(alpha = 0.06f)) else Modifier,
                                )
                                sessions.forEach { session ->
                                    val isSelected = session.id == selectedSessionId
                                    val label = "${dateFormat.format(Date(session.date))} — ${session.procedure.ifBlank { "Session" }}"
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                label,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isSelected) M3Primary else M3OnSurface,
                                            )
                                        },
                                        onClick = {
                                            selectedSessionId = session.id
                                            sessionDropdownExpanded = false
                                        },
                                        trailingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, null, tint = M3Primary, modifier = Modifier.size(18.dp)) }
                                        } else null,
                                        modifier = if (isSelected) Modifier.background(M3Primary.copy(alpha = 0.06f)) else Modifier,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Tags Section ──────────────────────────────────────────
            DetailSectionHeader(
                icon = Icons.AutoMirrored.Filled.Label,
                title = "TAGS",
                accentColor = M3CyanColor,
            )

            M3ListCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    TagSelectionSection(
                        selectedTags = selectedTags,
                        onToggleTag = { tag ->
                            selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
                        },
                        customTag = customTag,
                        onCustomTagChange = { customTag = it },
                        onAddCustomTag = {
                            if (customTag.isNotBlank()) {
                                selectedTags = selectedTags + customTag.trim().lowercase()
                                customTag = ""
                            }
                        },
                    )
                }
            }

            // ── Photo Section ─────────────────────────────────────────
            DetailSectionHeader(
                icon = Icons.Default.Image,
                title = "PHOTO",
                accentColor = M3PinkAccent,
            )

            M3ListCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    DasurvTextField(
                        value = photoUri,
                        onValueChange = { photoUri = it },
                        label = "Photo URI (optional)",
                        placeholder = "Paste photo path or URI",
                        autoCapitalize = false,
                    )
                }
            }

            // ── Notes Section ─────────────────────────────────────────
            DetailSectionHeader(
                icon = Icons.AutoMirrored.Filled.Notes,
                title = "NOTES",
                accentColor = M3IndigoColor,
            )

            M3ListCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    DasurvTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes (optional)",
                        placeholder = "Any additional observations...",
                        singleLine = false,
                        minLines = 4,
                    )
                }
            }

            Spacer(modifier = Modifier.height(DasurvTheme.spacing.xl))
        }
    }
}
