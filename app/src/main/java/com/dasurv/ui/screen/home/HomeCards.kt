package com.dasurv.ui.screen.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.data.model.AppointmentWithClient
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3PrimaryContainer
import com.dasurv.ui.component.M3ValueBadge
import com.dasurv.ui.theme.DasurvTheme
import java.text.SimpleDateFormat
import java.util.*

// ── Home Navigation Card ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    val spacing = DasurvTheme.spacing
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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

// ── Upcoming Appointment Row ─────────────────────────────────────────

@Composable
internal fun AppointmentRow(
    awc: AppointmentWithClient,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    val spacing = DasurvTheme.spacing
    Surface(
        onClick = onClick,
        color = Color.Transparent,
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

// ── Upcoming Appointments Card ───────────────────────────────────────

@Composable
internal fun UpcomingAppointmentsCard(
    appointments: List<AppointmentWithClient>,
    onNavigateToAppointmentDetail: (Long) -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()) }
    M3ListCard {
        appointments.forEachIndexed { index, awc ->
            AppointmentRow(
                awc = awc,
                dateFormat = dateFormat,
                onClick = { onNavigateToAppointmentDetail(awc.appointment.id) }
            )
            if (index < appointments.lastIndex) {
                M3ListDivider()
            }
        }
    }
}
