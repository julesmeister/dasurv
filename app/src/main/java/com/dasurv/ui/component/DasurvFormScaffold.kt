package com.dasurv.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

val FormScreenBackground = Color(0xFFEEEEEE)
private val SaveButtonColor = Color(0xFF263238)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DasurvFormScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit) = {},
    saveText: String = "Save",
    onSave: (onDone: () -> Unit) -> Unit,
    saveEnabled: Boolean = true,
    snackbarMessage: String = "Saved",
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        containerColor = FormScreenBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FormScreenBackground
                )
            )
        }
    ) { padding ->
        val columnModifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(FormScreenBackground)
            .padding(16.dp)
        Column(
            modifier = if (scrollable) columnModifier.verticalScroll(rememberScrollState()) else columnModifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        onSave {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = snackbarMessage,
                                    duration = SnackbarDuration.Short
                                )
                                onNavigateBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SaveButtonColor,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 16.dp),
                enabled = saveEnabled && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(saveText)
                }
            }
        }
    }
}
