package com.dasurv.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.ui.theme.DasurvTheme
import kotlinx.coroutines.launch

val FormScreenBackground = Color(0xFFF1F5F9)

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
        snackbarHost = { M3SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle(title) },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
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
            .padding(DasurvTheme.spacing.lg)
        Column(
            modifier = if (scrollable) columnModifier.verticalScroll(rememberScrollState()) else columnModifier,
            verticalArrangement = Arrangement.spacedBy(DasurvTheme.spacing.lg)
        ) {
            content()

            Spacer(modifier = Modifier.height(DasurvTheme.spacing.sm))

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
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = M3Primary,
                    contentColor = Color.White,
                    disabledContainerColor = M3Primary.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f),
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
                    Text(saveText, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
