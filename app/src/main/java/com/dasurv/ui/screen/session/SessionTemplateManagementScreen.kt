package com.dasurv.ui.screen.session

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.SessionTemplate
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionTemplateManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: SessionTemplateViewModel = hiltViewModel()
) {
    val spacing = DasurvTheme.spacing
    val templates by viewModel.allTemplates.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = rememberSnackbarState(snackbarMsg, viewModel::clearSnackbar)
    var showFormDialog by remember { mutableStateOf<Long?>(null) }
    var showDeleteDialog by remember { mutableStateOf<SessionTemplate?>(null) }

    if (showFormDialog != null) {
        TemplateFormDialog(
            templateId = showFormDialog?.takeIf { it != 0L },
            onDismiss = { showFormDialog = null },
            viewModel = viewModel
        )
    }

    if (showDeleteDialog != null) {
        DasurvConfirmDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = Icons.Default.Delete,
            title = "Delete Template",
            message = "Delete ${showDeleteDialog!!.name}?",
            onConfirm = {
                viewModel.deleteTemplate(showDeleteDialog!!) { showDeleteDialog = null }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Session Templates") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            DasurvAddFab(
                onClick = { showFormDialog = 0L },
                contentDescription = "Add Template"
            )
        },
        snackbarHost = { M3SnackbarHost(snackbarHostState) },
        containerColor = M3SurfaceContainer
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                "${templates.size} templates",
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm),
                style = MaterialTheme.typography.bodySmall,
                color = M3OnSurfaceVariant
            )

            @OptIn(ExperimentalFoundationApi::class)
            AnimatedContent(
                targetState = templates.isEmpty(),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "template-list-state"
            ) { isEmpty ->
                if (isEmpty) {
                    DasurvEmptyState(
                        icon = Icons.Default.ContentCopy,
                        message = "No templates created yet"
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            M3ListCard {
                                templates.forEachIndexed { index, template ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = { showFormDialog = template.id },
                                                onLongClick = { showDeleteDialog = template }
                                            )
                                    ) {
                                        M3ListRow(
                                            icon = Icons.Default.ContentCopy,
                                            iconTint = M3CyanColor,
                                            iconBg = M3CyanContainer,
                                            label = template.name,
                                            description = template.procedure.ifBlank { "No procedure set" }
                                        )
                                    }
                                    if (index < templates.lastIndex) {
                                        M3ListDivider()
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateFormDialog(
    templateId: Long?,
    onDismiss: () -> Unit,
    viewModel: SessionTemplateViewModel
) {
    val existingTemplate by viewModel.selectedTemplate.collectAsStateWithLifecycle()

    LaunchedEffect(templateId) {
        if (templateId != null && templateId != 0L) {
            viewModel.loadTemplate(templateId)
        } else {
            viewModel.clearSelectedTemplate()
        }
    }

    var name by remember(existingTemplate) { mutableStateOf(existingTemplate?.name ?: "") }
    var procedure by remember(existingTemplate) { mutableStateOf(existingTemplate?.procedure ?: "") }
    var notes by remember(existingTemplate) { mutableStateOf(existingTemplate?.notes ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    val isEditing = templateId != null && templateId != 0L

    DasurvFormDialog(
        title = if (isEditing) "Edit Template" else "New Template",
        icon = Icons.Default.ContentCopy,
        onDismiss = onDismiss,
        confirmEnabled = name.isNotBlank() && !isSaving,
        isLoading = isSaving,
        onConfirm = {
            if (!isSaving) {
                isSaving = true
                val template = SessionTemplate(
                    id = if (isEditing) templateId!! else 0,
                    name = name.trim(),
                    procedure = procedure.trim(),
                    notes = notes.trim(),
                    createdAt = existingTemplate?.createdAt ?: System.currentTimeMillis()
                )
                viewModel.saveTemplate(template, emptyList()) { onDismiss() }
            }
        }
    ) {
        DasurvTextField(
            value = name,
            onValueChange = { name = it },
            label = "Template Name *"
        )
        DasurvDropdownField(
            value = procedure,
            label = "Procedure",
            options = com.dasurv.util.PROCEDURE_TYPES,
            onOptionSelected = { procedure = it }
        )
        DasurvTextField(
            value = notes,
            onValueChange = { notes = it },
            label = "Notes",
            singleLine = false,
            minLines = 2
        )
    }
}
