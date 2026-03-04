package com.dasurv.ui.screen.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.Appointment
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.Session
import com.dasurv.data.model.FinancialSummary
import com.dasurv.ui.component.ChargedPaidRow
import com.dasurv.ui.component.DasurvEmptyState
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3AmberContainer
import com.dasurv.ui.component.M3GreenColor
import com.dasurv.ui.component.M3GreenContainer
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3PinkAccent
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3PrimaryContainer
import com.dasurv.ui.component.M3RedColor
import com.dasurv.ui.component.M3RedContainer
import com.dasurv.ui.component.M3FieldBg
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.FMT_DATE
import com.dasurv.util.FMT_DATETIME
import com.dasurv.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun ClientContactCard(client: Client) {
    val spacing = DasurvTheme.spacing
    M3ListCard {
        Column(modifier = Modifier.padding(spacing.lg)) {
            if (client.phone.isNotBlank()) {
                Text(
                    "Phone: ${client.phone}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = M3OnSurface
                )
            }
            if (client.email.isNotBlank()) {
                Text(
                    "Email: ${client.email}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = M3OnSurface
                )
            }
            if (client.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(spacing.sm))
                Text(
                    "Notes:",
                    style = MaterialTheme.typography.labelLarge,
                    color = M3OnSurface
                )
                Text(
                    client.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = M3OnSurfaceVariant
                )
            }
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
    val spacing = DasurvTheme.spacing
    M3ListCard {
        Column(modifier = Modifier.padding(spacing.lg)) {
            Text(
                "Lip Photos",
                style = MaterialTheme.typography.titleMedium,
                color = M3OnSurface
            )
            Spacer(modifier = Modifier.height(spacing.sm))
            val pinkBg = M3PinkAccent.copy(alpha = 0.10f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                FilledTonalButton(
                    onClick = { onNavigateToLipCamera(clientId) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = pinkBg,
                        contentColor = M3PinkAccent
                    )
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(spacing.xs))
                    Text("Take Photo")
                }
                FilledTonalButton(
                    onClick = { onNavigateToLipPhotoGallery(clientId) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = pinkBg,
                        contentColor = M3PinkAccent
                    )
                ) {
                    Text("View All")
                }
            }
            Spacer(modifier = Modifier.height(spacing.sm))
            FilledTonalButton(
                onClick = { onNavigateToTryOn(clientId) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = pinkBg,
                    contentColor = M3PinkAccent
                )
            ) {
                Icon(
                    Icons.Default.Brush,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(spacing.xs))
                Text("Try On Colors")
            }
        }
    }
}

@Composable
internal fun BookAppointmentCard(
    clientId: Long,
    onNavigateToBookAppointment: (Long) -> Unit,
    onNavigateToSessions: (Long) -> Unit = {}
) {
    val spacing = DasurvTheme.spacing
    M3ListCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            FilledTonalButton(
                onClick = { onNavigateToBookAppointment(clientId) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = M3AmberColor.copy(alpha = 0.10f),
                    contentColor = M3AmberColor
                )
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(spacing.xs))
                Text("Book Appt")
            }
            FilledTonalButton(
                onClick = { onNavigateToSessions(clientId) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = M3Primary.copy(alpha = 0.10f),
                    contentColor = M3Primary
                )
            ) {
                Icon(
                    Icons.Default.EventNote,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(spacing.xs))
                Text("Sessions")
            }
        }
    }
}

@Composable
internal fun FinancialSummaryCard(
    financialSummary: FinancialSummary,
    clientId: Long,
    onNavigateToTransactions: (Long) -> Unit
) {
    val spacing = DasurvTheme.spacing
    M3ListCard {
        Column(modifier = Modifier.padding(spacing.xl)) {
            Text(
                "Financials",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = M3OnSurface
            )
            Spacer(modifier = Modifier.height(spacing.lg))

            // Balance hero
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (financialSummary.balance > 0.01)
                    M3RedContainer
                else M3PrimaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (financialSummary.balance > 0.01) "Outstanding" else "Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (financialSummary.balance > 0.01)
                            M3RedColor
                        else M3Primary
                    )
                    Text(
                        "$${financialSummary.balance.formatCurrency()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (financialSummary.balance > 0.01)
                            M3RedColor
                        else M3Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.md))

            ChargedPaidRow(
                charged = financialSummary.totalCharged,
                paid = financialSummary.totalPaid
            )

            Spacer(modifier = Modifier.height(spacing.md))

            FilledTonalButton(
                onClick = { onNavigateToTransactions(clientId) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = M3GreenColor.copy(alpha = 0.10f),
                    contentColor = M3GreenColor
                )
            ) {
                Text("View All Transactions")
            }
        }
    }
}

@Composable
internal fun AppointmentsList(
    appointments: List<Appointment>,
    onNavigateToAppointmentDetail: (Long) -> Unit
) {
    val spacing = DasurvTheme.spacing
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
                Column(modifier = Modifier.padding(spacing.lg)) {
                    Text(
                        text = appointmentDateFormat.format(Date(appointment.scheduledDateTime)),
                        style = MaterialTheme.typography.titleSmall,
                        color = M3OnSurface
                    )
                    if (appointment.procedureType.isNotBlank()) {
                        Text(
                            appointment.procedureType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = M3OnSurface
                        )
                    }
                    Text(
                        "${appointment.durationMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = M3OnSurfaceVariant
                    )
                }
            }
            if (index < appointments.lastIndex) {
                M3ListDivider()
            }
        }
    }
}

@Composable
fun SessionsList(
    sessions: List<Session>,
    onNavigateToSession: (Long) -> Unit
) {
    val spacing = DasurvTheme.spacing

    if (sessions.isEmpty()) {
        DasurvEmptyState(
            icon = Icons.Default.EventNote,
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
                    Column(modifier = Modifier.padding(spacing.lg)) {
                        Text(
                            text = sessionDateFormat.format(Date(session.date)),
                            style = MaterialTheme.typography.titleSmall,
                            color = M3OnSurface
                        )
                        if (session.procedure.isNotBlank()) {
                            Text(
                                session.procedure,
                                style = MaterialTheme.typography.bodyMedium,
                                color = M3OnSurface
                            )
                        }
                        if (session.totalCost > 0) {
                            Text(
                                "$${session.totalCost.formatCurrency()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = M3Primary
                            )
                        }
                    }
                }
                if (index < sessions.lastIndex) {
                    M3ListDivider()
                }
            }
        }
    }
}
