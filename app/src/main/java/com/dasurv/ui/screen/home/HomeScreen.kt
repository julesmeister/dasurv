package com.dasurv.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val clients by viewModel.clients.collectAsStateWithLifecycle(initialValue = emptyList())
    val sessions by viewModel.recentSessions.collectAsStateWithLifecycle(initialValue = emptyList())
    val upcomingAppointments by viewModel.upcomingAppointments.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Dasurv",
                            fontFamily = FontFamily(Font(R.font.bodoni_moda)),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "STUDIOS",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily(Font(R.font.bodoni_moda)),
                            letterSpacing = 6.sp,
                            modifier = Modifier.offset(x = 4.dp, y = (-8).dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AnimatedContent(
                    targetState = "${clients.size} clients | ${sessions.size} sessions",
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "stats"
                ) { stats ->
                    Text(
                        text = stats,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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

            // Upcoming appointments section with animated visibility
            if (upcomingAppointments.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upcoming Appointments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(upcomingAppointments, key = { it.appointment.id }) { awc ->
                    val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
                    Card(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        onClick = { onNavigateToAppointmentDetail(awc.appointment.id) },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    awc.clientName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                if (awc.appointment.procedureType.isNotBlank()) {
                                    Text(
                                        awc.appointment.procedureType,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    dateFormat.format(Date(awc.appointment.scheduledDateTime)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Spring-based scale animation on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "card_scale"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .height(120.dp)
            .scale(scale),
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            if (badge != null) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(badge)
                }
            }
        }
    }
}
