package com.dasurv.ui.screen.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.data.local.entity.Appointment
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.Session
import com.dasurv.data.model.FinancialSummary
import com.dasurv.ui.component.DasurvEmptyState
import com.dasurv.ui.component.DetailActionButton
import com.dasurv.ui.component.DetailDivider
import com.dasurv.ui.component.DetailValueRow
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3CyanColor
import com.dasurv.ui.component.M3GreenColor
import com.dasurv.ui.component.M3IndigoColor
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListRow
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3PinkAccent
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3PurpleColor
import com.dasurv.ui.component.M3TealColor
import com.dasurv.ui.component.M3PrimaryContainer
import com.dasurv.ui.component.M3RedColor
import com.dasurv.ui.component.M3RedContainer
import com.dasurv.ui.component.M3ValueBadge
import com.dasurv.util.FMT_DATE
import com.dasurv.util.FMT_DATETIME
import com.dasurv.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun ClientContactCard(client: Client) {
    M3ListCard {
        if (client.phone.isNotBlank()) {
            DetailValueRow(
                icon = Icons.Default.Phone,
                label = "Phone",
                value = client.phone,
                iconTint = M3GreenColor,
                iconBg = M3GreenColor.copy(alpha = 0.10f),
                valueBg = M3GreenColor.copy(alpha = 0.08f),
                valueColor = M3GreenColor,
            )
        }
        if (client.email.isNotBlank()) {
            if (client.phone.isNotBlank()) DetailDivider()
            DetailValueRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = client.email,
                iconTint = M3CyanColor,
                iconBg = M3CyanColor.copy(alpha = 0.10f),
                valueBg = M3CyanColor.copy(alpha = 0.08f),
                valueColor = M3CyanColor,
            )
        }
        if (client.notes.isNotBlank()) {
            if (client.phone.isNotBlank() || client.email.isNotBlank()) DetailDivider()
            DetailValueRow(
                icon = Icons.AutoMirrored.Filled.Notes,
                label = "Notes",
                value = client.notes,
                iconTint = M3OnSurfaceVariant,
                iconBg = M3OnSurfaceVariant.copy(alpha = 0.10f),
                valueBg = M3OnSurfaceVariant.copy(alpha = 0.06f),
                valueColor = M3OnSurface,
            )
        }
    }
}

@Composable
internal fun LipPhotosCard(
    clientId: Long,
    onNavigateToLipCamera: (Long) -> Unit,
    onNavigateToLipPhotoGallery: (Long) -> Unit,
    onNavigateToTryOn: (Long) -> Unit
) {
    M3ListCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailActionButton(
                    label = "Camera",
                    icon = Icons.Default.CameraAlt,
                    color = M3PinkAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToLipCamera(clientId) },
                )
                DetailActionButton(
                    label = "Gallery",
                    icon = Icons.Default.PhotoLibrary,
                    color = M3PurpleColor,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToLipPhotoGallery(clientId) },
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            DetailActionButton(
                label = "Try On Colors",
                icon = Icons.Default.Brush,
                color = M3TealColor,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigateToTryOn(clientId) },
            )
        }
    }
}

@Composable
internal fun BookAppointmentCard(
    clientId: Long,
    onNavigateToBookAppointment: (Long) -> Unit,
    onNavigateToSessions: (Long) -> Unit = {}
) {
    M3ListCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailActionButton(
                label = "Book Appt",
                icon = Icons.Default.CalendarMonth,
                color = M3AmberColor,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToBookAppointment(clientId) },
            )
            DetailActionButton(
                label = "Sessions",
                icon = Icons.AutoMirrored.Filled.EventNote,
                color = M3Primary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToSessions(clientId) },
            )
        }
    }
}

@Composable
internal fun FinancialSummaryCard(
    financialSummary: FinancialSummary,
    clientId: Long,
    onNavigateToTransactions: (Long) -> Unit
) {
    val hasBalance = financialSummary.balance > 0.01
    val balanceColor = if (hasBalance) M3RedColor else M3GreenColor
    val balanceBg = if (hasBalance) M3RedColor.copy(alpha = 0.08f) else M3GreenColor.copy(alpha = 0.08f)

    M3ListCard {
        // Balance hero
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (hasBalance) M3RedContainer else M3GreenColor.copy(alpha = 0.08f),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (hasBalance) "Outstanding" else "Balance",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = balanceColor,
                )
                Text(
                    "₱${financialSummary.balance.formatCurrency()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = balanceColor,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Charged row
        DetailValueRow(
            icon = Icons.Default.Receipt,
            label = "Total Charged",
            value = "₱${financialSummary.totalCharged.formatCurrency()}",
            iconTint = M3IndigoColor,
            iconBg = M3IndigoColor.copy(alpha = 0.10f),
            valueBg = M3IndigoColor.copy(alpha = 0.08f),
            valueColor = M3IndigoColor,
        )
        DetailDivider()
        // Paid row
        DetailValueRow(
            icon = Icons.Default.Payments,
            label = "Total Paid",
            value = "₱${financialSummary.totalPaid.formatCurrency()}",
            iconTint = M3GreenColor,
            iconBg = M3GreenColor.copy(alpha = 0.10f),
            valueBg = M3GreenColor.copy(alpha = 0.08f),
            valueColor = M3GreenColor,
        )

        // View transactions button
        Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)) {
            DetailActionButton(
                label = "View Transactions",
                icon = Icons.Default.AttachMoney,
                color = M3GreenColor,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigateToTransactions(clientId) },
            )
        }
    }
}

@Composable
internal fun AppointmentsList(
    appointments: List<Appointment>,
    onNavigateToAppointmentDetail: (Long) -> Unit
) {
    val appointmentDateFormat = remember {
        SimpleDateFormat(FMT_DATETIME, Locale.getDefault())
    }

    M3ListCard {
        appointments.forEachIndexed { index, appointment ->
            Surface(
                onClick = { onNavigateToAppointmentDetail(appointment.id) },
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Icon box
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                M3AmberColor.copy(alpha = 0.10f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = M3AmberColor,
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appointmentDateFormat.format(Date(appointment.scheduledDateTime)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = M3OnSurface,
                        )
                        if (appointment.procedureType.isNotBlank()) {
                            Text(
                                appointment.procedureType,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = M3OnSurfaceVariant,
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = M3AmberColor.copy(alpha = 0.08f),
                    ) {
                        Text(
                            "${appointment.durationMinutes} min",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = M3AmberColor,
                        )
                    }
                }
            }
            if (index < appointments.lastIndex) {
                DetailDivider()
            }
        }
    }
}

@Composable
fun SessionsList(
    sessions: List<Session>,
    onNavigateToSession: (Long) -> Unit
) {
    if (sessions.isEmpty()) {
        DasurvEmptyState(
            icon = Icons.AutoMirrored.Filled.EventNote,
            message = "No sessions yet"
        )
    } else {
        M3ListCard {
            val sessionDateFormat = remember {
                SimpleDateFormat(FMT_DATE, Locale.getDefault())
            }
            sessions.forEachIndexed { index, session ->
                Surface(
                    onClick = { onNavigateToSession(session.id) },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    M3ListRow(
                        icon = Icons.AutoMirrored.Filled.EventNote,
                        iconTint = M3Primary,
                        iconBg = M3PrimaryContainer,
                        label = session.procedure.ifBlank { "Session" },
                        description = sessionDateFormat.format(Date(session.date)),
                        trailing = {
                            if (session.totalCost > 0) {
                                M3ValueBadge(
                                    text = "₱${session.totalCost.formatCurrency()}",
                                    color = M3Primary,
                                    containerColor = M3PrimaryContainer
                                )
                            }
                        }
                    )
                }
                if (index < sessions.lastIndex) {
                    M3ListDivider()
                }
            }
        }
    }
}
