package com.dasurv.ui.screen.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToClient: (Long) -> Unit,
    onNavigateToAppointment: (Long) -> Unit,
    onNavigateToSession: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val spacing = DasurvTheme.spacing

    Scaffold(
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Search") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) }
            )
        },
        containerColor = M3SurfaceContainer
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            DasurvTextField(
                value = query,
                onValueChange = { viewModel.updateQuery(it) },
                label = "Search clients, appointments, sessions...",
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm)
            )

            if (query.length < 2) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Type at least 2 characters to search",
                        color = M3OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else if (results.isEmpty) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No results found",
                        color = M3OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

                LazyColumn(
                    contentPadding = PaddingValues(vertical = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    // Clients
                    if (results.clients.isNotEmpty()) {
                        item {
                            Text(
                                "Clients (${results.clients.size})",
                                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = M3Primary
                            )
                        }
                        item {
                            M3ListCard {
                                results.clients.forEachIndexed { index, client ->
                                    Column(modifier = Modifier.clickable { onNavigateToClient(client.id) }) {
                                        M3ListRow(
                                            icon = Icons.Default.Person,
                                            iconTint = M3Primary,
                                            iconBg = M3PrimaryContainer,
                                            label = client.name,
                                            description = client.phone.ifBlank { client.email }
                                        )
                                    }
                                    if (index < results.clients.lastIndex) M3ListDivider()
                                }
                            }
                        }
                    }

                    // Appointments
                    if (results.appointments.isNotEmpty()) {
                        item {
                            Text(
                                "Appointments (${results.appointments.size})",
                                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = M3AmberColor
                            )
                        }
                        item {
                            M3ListCard {
                                results.appointments.forEachIndexed { index, appt ->
                                    Column(modifier = Modifier.clickable { onNavigateToAppointment(appt.id) }) {
                                        M3ListRow(
                                            icon = Icons.Default.CalendarMonth,
                                            iconTint = M3AmberColor,
                                            iconBg = M3AmberColor.copy(alpha = 0.1f),
                                            label = appt.procedureType.ifBlank { "Appointment" },
                                            description = dateFormat.format(Date(appt.scheduledDateTime))
                                        )
                                    }
                                    if (index < results.appointments.lastIndex) M3ListDivider()
                                }
                            }
                        }
                    }

                    // Sessions
                    if (results.sessions.isNotEmpty()) {
                        item {
                            Text(
                                "Sessions (${results.sessions.size})",
                                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = M3GreenColor
                            )
                        }
                        item {
                            M3ListCard {
                                results.sessions.forEachIndexed { index, session ->
                                    Column(modifier = Modifier.clickable { onNavigateToSession(session.id) }) {
                                        M3ListRow(
                                            icon = Icons.Default.EventAvailable,
                                            iconTint = M3GreenColor,
                                            iconBg = M3GreenColor.copy(alpha = 0.1f),
                                            label = session.procedure.ifBlank { "Session" },
                                            description = dateFormat.format(Date(session.date))
                                        )
                                    }
                                    if (index < results.sessions.lastIndex) M3ListDivider()
                                }
                            }
                        }
                    }

                    // Equipment
                    if (results.equipment.isNotEmpty()) {
                        item {
                            Text(
                                "Equipment (${results.equipment.size})",
                                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xs),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = M3CyanColor
                            )
                        }
                        item {
                            M3ListCard {
                                results.equipment.forEachIndexed { index, eq ->
                                    M3ListRow(
                                        icon = if (eq.type == "consumable") Icons.Default.Healing else Icons.Default.Build,
                                        iconTint = M3CyanColor,
                                        iconBg = M3CyanContainer,
                                        label = eq.name,
                                        description = eq.brand.ifBlank { eq.category }
                                    )
                                    if (index < results.equipment.lastIndex) M3ListDivider()
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(spacing.lg)) }
                }
            }
        }
    }
}
