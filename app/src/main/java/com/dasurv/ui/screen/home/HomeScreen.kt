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
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3PrimaryContainer
import com.dasurv.ui.component.M3SurfaceContainer
import com.dasurv.ui.component.M3ValueBadge
import com.dasurv.data.model.AppointmentWithClient
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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val sessionCount by viewModel.sessionCount.collectAsStateWithLifecycle()
    val upcomingAppointments by viewModel.upcomingAppointments.collectAsStateWithLifecycle()
    val spacing = DasurvTheme.spacing

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Dasurv",
                            fontFamily = FontFamily(Font(R.font.bodoni_moda)),
                            fontWeight = FontWeight.Bold,
                            color = M3OnSurface
                        )
                        Text(
                            "STUDIOS",
                            style = MaterialTheme.typography.labelLarge,
                            color = M3OnSurfaceVariant,
                            fontFamily = FontFamily(Font(R.font.bodoni_moda)),
                            letterSpacing = 6.sp,
                            modifier = Modifier.offset(x = spacing.xs, y = (-8).dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = M3SurfaceContainer,
                    scrolledContainerColor = M3SurfaceContainer
                )
            )
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

            item {
                Spacer(modifier = Modifier.height(spacing.xs))
            }

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

            // Upcoming appointments section with animated visibility
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
                    M3ListCard {
                        val dateFormat = remember { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()) }
                        upcomingAppointments.forEachIndexed { index, awc ->
                            AppointmentRow(
                                awc = awc,
                                dateFormat = dateFormat,
                                onClick = { onNavigateToAppointmentDetail(awc.appointment.id) }
                            )
                            if (index < upcomingAppointments.lastIndex) {
                                M3ListDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentRow(
    awc: AppointmentWithClient,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val spacing = DasurvTheme.spacing
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = spacing.lg, vertical = spacing.md)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    awc.clientName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = M3OnSurface
                )
                if (awc.appointment.procedureType.isNotBlank()) {
                    Text(
                        awc.appointment.procedureType,
                        style = MaterialTheme.typography.bodySmall,
                        color = M3OnSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            M3ValueBadge(
                text = dateFormat.format(Date(awc.appointment.scheduledDateTime)),
                color = M3Primary,
                containerColor = M3PrimaryContainer
            )
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
    val spacing = DasurvTheme.spacing
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.lg),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier.size(36.dp),
                    tint = M3Primary
                )
                Spacer(modifier = Modifier.height(spacing.sm))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = M3OnSurface
                )
            }
            if (badge != null) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(spacing.sm),
                    containerColor = M3Primary
                ) {
                    Text(badge)
                }
            }
        }
    }
}
