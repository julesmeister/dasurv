package com.dasurv.ui.screen.client

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Client
import com.dasurv.ui.component.DasurvFormCard
import com.dasurv.ui.component.DasurvFormScaffold
import com.dasurv.ui.component.DasurvTextField

@Composable
fun AddEditClientScreen(
    clientId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: ClientViewModel = hiltViewModel()
) {
    val existingClient by viewModel.selectedClient.collectAsStateWithLifecycle()

    LaunchedEffect(clientId) {
        if (clientId != null && clientId != 0L) {
            viewModel.loadClient(clientId)
        }
    }

    var name by remember(existingClient) { mutableStateOf(existingClient?.name ?: "") }
    var phone by remember(existingClient) { mutableStateOf(existingClient?.phone ?: "") }
    var email by remember(existingClient) { mutableStateOf(existingClient?.email ?: "") }
    var notes by remember(existingClient) { mutableStateOf(existingClient?.notes ?: "") }

    val isEditing = clientId != null && clientId != 0L

    DasurvFormScaffold(
        title = if (isEditing) "Edit Client" else "Add Client",
        onNavigateBack = onNavigateBack,
        saveText = if (isEditing) "Update" else "Save",
        saveEnabled = name.isNotBlank(),
        snackbarMessage = if (isEditing) "Client updated" else "Client saved",
        onSave = { onDone ->
            val client = Client(
                id = if (isEditing) clientId!! else 0,
                name = name.trim(),
                phone = phone.trim(),
                email = email.trim(),
                notes = notes.trim()
            )
            viewModel.saveClient(client) { onDone() }
        }
    ) {
        DasurvFormCard {
            DasurvTextField(value = name, onValueChange = { name = it }, label = "Name *")
            DasurvTextField(
                value = phone, onValueChange = { phone = it }, label = "Phone",
                autoCapitalize = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            DasurvTextField(
                value = email, onValueChange = { email = it }, label = "Email",
                autoCapitalize = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            DasurvTextField(
                value = notes, onValueChange = { notes = it }, label = "Notes",
                singleLine = false, minLines = 2
            )
        }
    }
}
