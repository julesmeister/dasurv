package com.dasurv.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.component.*
import com.dasurv.ui.screen.search.SearchViewModel
import com.dasurv.ui.theme.DasurvTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToPigments: () -> Unit,
    onNavigateToEquipment: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToAppointmentDetail: (Long) -> Unit,
    onNavigateToPigmentInventory: () -> Unit,
    onNavigateToStaff: () -> Unit = {},
    onNavigateToExport: () -> Unit = {},
    onNavigateToClient: (Long) -> Unit = {},
    onNavigateToSession: (Long) -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val sessionCount by viewModel.sessionCount.collectAsStateWithLifecycle()
    val upcomingAppointments by viewModel.upcomingAppointments.collectAsStateWithLifecycle()
    val lowStockCount by viewModel.lowStockCount.collectAsStateWithLifecycle()
    val searchQuery by searchViewModel.query.collectAsStateWithLifecycle()
    val searchResults by searchViewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching = searchQuery.length >= 2 && !searchResults.isEmpty
    val spacing = DasurvTheme.spacing

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 40.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.lips_logo),
                    contentDescription = "Dasurv Studios",
                    modifier = Modifier
                        .height(120.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.FillHeight
                )
            }
        },
        containerColor = M3SurfaceContainer
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            // Search bar
            item {
                DasurvTextField(
                    value = searchQuery,
                    onValueChange = { searchViewModel.updateQuery(it) },
                    label = "Search clients, appointments, sessions...",
                    modifier = Modifier.fillMaxWidth(),
                    autoCapitalize = false,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = M3OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }

            item {
                AnimatedContent(
                    targetState = "${clients.size} clients | $sessionCount sessions",
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "stats"
                ) { stats ->
                    Text(
                        text = stats,
                        style = MaterialTheme.typography.bodyMedium,
                        color = M3OnSurfaceVariant
                    )
                }
            }

            if (isSearching) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                searchResultItems(
                    searchResults = searchResults,
                    dateFormat = dateFormat,
                    onNavigateToClient = onNavigateToClient,
                    onNavigateToAppointmentDetail = onNavigateToAppointmentDetail,
                    onNavigateToSession = onNavigateToSession,
                )
            } else {
                // Dashboard grid
                item { Spacer(modifier = Modifier.height(spacing.xs)) }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md)
                    ) {
                        HomeCard(
                            title = "Schedule",
                            icon = Icons.Default.CalendarMonth,
                            badge = if (upcomingAppointments.isNotEmpty()) upcomingAppointments.size.toString() else null,
                            onClick = onNavigateToSchedule,
                            modifier = Modifier.weight(1f)
                        )
                        HomeCard(
                            title = "Lip Camera",
                            icon = Icons.Default.CameraAlt,
                            onClick = onNavigateToCamera,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md)
                    ) {
                        HomeCard(
                            title = "Clients",
                            icon = Icons.Default.People,
                            onClick = onNavigateToClients,
                            modifier = Modifier.weight(1f)
                        )
                        HomeCard(
                            title = "Pigments",
                            icon = Icons.Default.Palette,
                            onClick = onNavigateToPigments,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md)
                    ) {
                        HomeCard(
                            title = "Equipment",
                            icon = Icons.Default.Inventory,
                            onClick = onNavigateToEquipment,
                            modifier = Modifier.weight(1f)
                        )
                        HomeCard(
                            title = "Pigment Inventory",
                            icon = Icons.Default.Opacity,
                            onClick = onNavigateToPigmentInventory,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md)
                    ) {
                        HomeCard(
                            title = "Staff",
                            icon = Icons.Default.Groups,
                            onClick = onNavigateToStaff,
                            modifier = Modifier.weight(1f)
                        )
                        HomeCard(
                            title = "Transactions",
                            icon = Icons.Default.Receipt,
                            onClick = onNavigateToTransactions,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md)
                    ) {
                        HomeCard(
                            title = "Export",
                            icon = Icons.Default.FileDownload,
                            onClick = onNavigateToExport,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Low stock warning
                if (lowStockCount > 0) {
                    item {
                        M3ListCard {
                            Row(
                                modifier = Modifier.padding(spacing.lg),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = M3AmberColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(spacing.md))
                                Text(
                                    text = "$lowStockCount item${if (lowStockCount > 1) "s" else ""} low on stock",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = M3AmberColor
                                )
                            }
                        }
                    }
                }

                // Upcoming appointments
                if (upcomingAppointments.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "Upcoming Appointments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = M3OnSurface
                        )
                    }

                    item {
                        UpcomingAppointmentsCard(
                            appointments = upcomingAppointments,
                            onNavigateToAppointmentDetail = onNavigateToAppointmentDetail,
                        )
                    }
                }
            }
        }
    }
}
