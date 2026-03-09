package com.dasurv.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3CyanColor
import com.dasurv.ui.component.M3CyanContainer
import com.dasurv.ui.component.M3GreenColor
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.component.M3ListRow
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3PrimaryContainer
import com.dasurv.ui.component.M3SectionHeader
import com.dasurv.data.model.SearchResults
import java.text.SimpleDateFormat
import java.util.*

internal fun LazyListScope.searchResultItems(
    searchResults: SearchResults,
    dateFormat: SimpleDateFormat,
    onNavigateToClient: (Long) -> Unit,
    onNavigateToAppointmentDetail: (Long) -> Unit,
    onNavigateToSession: (Long) -> Unit,
) {
    if (searchResults.clients.isNotEmpty()) {
        item { M3SectionHeader("Clients (${searchResults.clients.size})", M3Primary) }
        item {
            M3ListCard {
                searchResults.clients.forEachIndexed { index, client ->
                    Column(modifier = Modifier.clickable { onNavigateToClient(client.id) }) {
                        M3ListRow(
                            icon = Icons.Default.Person,
                            iconTint = M3Primary,
                            iconBg = M3PrimaryContainer,
                            label = client.name,
                            description = client.phone.ifBlank { client.email }
                        )
                    }
                    if (index < searchResults.clients.lastIndex) M3ListDivider()
                }
            }
        }
    }

    if (searchResults.appointments.isNotEmpty()) {
        item { M3SectionHeader("Appointments (${searchResults.appointments.size})", M3AmberColor) }
        item {
            M3ListCard {
                searchResults.appointments.forEachIndexed { index, appt ->
                    Column(modifier = Modifier.clickable { onNavigateToAppointmentDetail(appt.id) }) {
                        M3ListRow(
                            icon = Icons.Default.CalendarMonth,
                            iconTint = M3AmberColor,
                            iconBg = M3AmberColor.copy(alpha = 0.1f),
                            label = appt.procedureType.ifBlank { "Appointment" },
                            description = dateFormat.format(Date(appt.scheduledDateTime))
                        )
                    }
                    if (index < searchResults.appointments.lastIndex) M3ListDivider()
                }
            }
        }
    }

    if (searchResults.sessions.isNotEmpty()) {
        item { M3SectionHeader("Sessions (${searchResults.sessions.size})", M3GreenColor) }
        item {
            M3ListCard {
                searchResults.sessions.forEachIndexed { index, session ->
                    Column(modifier = Modifier.clickable { onNavigateToSession(session.id) }) {
                        M3ListRow(
                            icon = Icons.Default.EventAvailable,
                            iconTint = M3GreenColor,
                            iconBg = M3GreenColor.copy(alpha = 0.1f),
                            label = session.procedure.ifBlank { "Session" },
                            description = dateFormat.format(Date(session.date))
                        )
                    }
                    if (index < searchResults.sessions.lastIndex) M3ListDivider()
                }
            }
        }
    }

    if (searchResults.equipment.isNotEmpty()) {
        item { M3SectionHeader("Equipment (${searchResults.equipment.size})", M3CyanColor) }
        item {
            M3ListCard {
                searchResults.equipment.forEachIndexed { index, eq ->
                    M3ListRow(
                        icon = if (eq.type == "consumable") Icons.Default.Healing else Icons.Default.Build,
                        iconTint = M3CyanColor,
                        iconBg = M3CyanContainer,
                        label = eq.name,
                        description = eq.brand.ifBlank { eq.category }
                    )
                    if (index < searchResults.equipment.lastIndex) M3ListDivider()
                }
            }
        }
    }
}
