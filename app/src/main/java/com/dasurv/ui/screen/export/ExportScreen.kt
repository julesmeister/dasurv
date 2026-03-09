package com.dasurv.ui.screen.export

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val spacing = DasurvTheme.spacing

    // Share when export is done
    LaunchedEffect(exportState) {
        val state = exportState
        if (state is ExportState.Done) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, state.uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "${state.type} Export")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share ${state.type} CSV"))
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Export Data") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) }
            )
        },
        containerColor = M3SurfaceContainer
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            item {
                Text(
                    "Export your data as CSV files",
                    modifier = Modifier.padding(horizontal = spacing.lg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = M3OnSurfaceVariant
                )
            }

            item {
                M3ListCard {
                    ExportRow(
                        title = "Export Clients",
                        description = "Names, phone numbers, emails",
                        icon = Icons.Default.People,
                        color = M3Primary,
                        isExporting = exportState is ExportState.Exporting,
                        onClick = { viewModel.exportClients(context) }
                    )
                    M3ListDivider()
                    ExportRow(
                        title = "Export Sessions",
                        description = "Dates, procedures, costs, durations",
                        icon = Icons.Default.EventAvailable,
                        color = M3GreenColor,
                        isExporting = exportState is ExportState.Exporting,
                        onClick = { viewModel.exportSessions(context) }
                    )
                    M3ListDivider()
                    ExportRow(
                        title = "Export Transactions",
                        description = "Charges, payments, deposits",
                        icon = Icons.Default.Receipt,
                        color = M3AmberColor,
                        isExporting = exportState is ExportState.Exporting,
                        onClick = { viewModel.exportTransactions(context) }
                    )
                }
            }

            if (exportState is ExportState.Error) {
                item {
                    M3ErrorCard((exportState as ExportState.Error).message)
                }
            }
        }
    }
}

@Composable
private fun ExportRow(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    isExporting: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = !isExporting,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(DasurvTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(DasurvTheme.spacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = M3OnSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = M3OnSurfaceVariant
                )
            }
            if (isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = color
                )
            } else {
                Icon(
                    Icons.Default.FileDownload,
                    contentDescription = "Export",
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
