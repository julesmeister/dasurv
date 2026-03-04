package com.dasurv.ui.screen.staff

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
import com.dasurv.data.local.entity.Staff
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffListScreen(
    onNavigateBack: () -> Unit,
    viewModel: StaffViewModel = hiltViewModel()
) {
    var staffDialogId by remember { mutableStateOf<Long?>(null) }

    if (staffDialogId != null) {
        StaffFormDialog(
            staffId = staffDialogId?.takeIf { it != 0L },
            onDismiss = { staffDialogId = null },
            viewModel = viewModel
        )
    }

    val spacing = DasurvTheme.spacing
    val staffList by viewModel.allStaff.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showDeleteDialog by remember { mutableStateOf<Staff?>(null) }
    var sheetItem by remember { mutableStateOf<Staff?>(null) }

    if (showDeleteDialog != null) {
        DasurvConfirmDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = Icons.Default.Delete,
            title = "Delete",
            message = "Delete ${showDeleteDialog!!.name}?",
            onConfirm = {
                viewModel.deleteStaff(showDeleteDialog!!) { showDeleteDialog = null }
            }
        )
    }

    if (sheetItem != null) {
        val item = sheetItem!!
        DasurvOptionsSheet(
            onDismiss = { sheetItem = null },
            icon = Icons.Default.Person,
            iconBg = M3IndigoContainer,
            iconTint = M3IndigoColor,
            title = item.name,
            subtitle = if (item.isActive) "Active" else "Inactive",
        ) {
            DasurvSheetOptionRow(
                icon = Icons.Default.Edit,
                iconBg = M3IndigoContainer,
                iconTint = M3IndigoColor,
                label = "Edit",
                subtitle = "Change staff details",
                onClick = { sheetItem = null; staffDialogId = item.id },
            )
            DasurvSheetOptionRow(
                icon = Icons.Default.Delete,
                iconBg = M3RedContainer,
                iconTint = M3RedColor,
                label = "Delete",
                subtitle = "Remove staff member",
                onClick = { sheetItem = null; showDeleteDialog = item },
                isDestructive = true,
            )
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Staff") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            DasurvAddFab(
                onClick = { staffDialogId = 0L },
                contentDescription = "Add Staff"
            )
        },
        containerColor = M3SurfaceContainer
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                "${staffList.size} staff members",
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm),
                style = MaterialTheme.typography.bodySmall,
                color = M3OnSurfaceVariant
            )

            @OptIn(ExperimentalFoundationApi::class)
            AnimatedContent(
                targetState = staffList.isEmpty(),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "staff-list-state"
            ) { isEmpty ->
                if (isEmpty) {
                    DasurvEmptyState(
                        icon = Icons.Default.People,
                        message = "No staff members added yet"
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            M3ListCard {
                                staffList.forEachIndexed { index, staff ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = { staffDialogId = staff.id },
                                                onLongClick = { sheetItem = staff }
                                            )
                                    ) {
                                        M3ListRow(
                                            icon = Icons.Default.Person,
                                            iconTint = M3IndigoColor,
                                            iconBg = M3IndigoContainer,
                                            label = staff.name,
                                            description = buildString {
                                                if (staff.phone.isNotBlank()) append(staff.phone)
                                                if (staff.email.isNotBlank()) {
                                                    if (isNotEmpty()) append(" · ")
                                                    append(staff.email)
                                                }
                                            }.ifEmpty { "" },
                                            trailing = {
                                                M3StatusBadge(
                                                    text = if (staff.isActive) "Active" else "Inactive",
                                                    color = if (staff.isActive) M3GreenColor else M3OnSurfaceVariant,
                                                    containerColor = if (staff.isActive) M3GreenColor.copy(alpha = 0.1f) else M3FieldBg
                                                )
                                            }
                                        )
                                    }
                                    if (index < staffList.lastIndex) {
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
fun StaffFormDialog(
    staffId: Long?,
    onDismiss: () -> Unit,
    viewModel: StaffViewModel
) {
    val existingStaff by viewModel.selectedStaff.collectAsStateWithLifecycle()

    LaunchedEffect(staffId) {
        if (staffId != null && staffId != 0L) {
            viewModel.loadStaff(staffId)
        } else {
            viewModel.clearSelectedStaff()
        }
    }

    var name by remember(existingStaff) { mutableStateOf(existingStaff?.name ?: "") }
    var phone by remember(existingStaff) { mutableStateOf(existingStaff?.phone ?: "") }
    var email by remember(existingStaff) { mutableStateOf(existingStaff?.email ?: "") }
    var notes by remember(existingStaff) { mutableStateOf(existingStaff?.notes ?: "") }
    var isActive by remember(existingStaff) { mutableStateOf(existingStaff?.isActive ?: true) }
    var isSaving by remember { mutableStateOf(false) }

    val isEditing = staffId != null && staffId != 0L

    DasurvFormDialog(
        title = if (isEditing) "Edit Staff" else "Add Staff",
        icon = Icons.Default.Person,
        onDismiss = onDismiss,
        confirmEnabled = name.isNotBlank() && !isSaving,
        isLoading = isSaving,
        onConfirm = {
            if (!isSaving) {
                isSaving = true
                val staff = Staff(
                    id = if (isEditing) staffId!! else 0,
                    name = name.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    notes = notes.trim(),
                    isActive = isActive,
                    createdAt = existingStaff?.createdAt ?: System.currentTimeMillis()
                )
                viewModel.saveStaff(staff) { onDismiss() }
            }
        }
    ) {
        DasurvTextField(
            value = name,
            onValueChange = { name = it },
            label = "Name *"
        )
        DasurvTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Phone"
        )
        DasurvTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email"
        )
        DasurvSwitchRow(
            label = "Active",
            checked = isActive,
            onCheckedChange = { isActive = it }
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
