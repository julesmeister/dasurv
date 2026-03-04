package com.dasurv.data.model

import com.dasurv.data.local.entity.Appointment
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.Session

data class SearchResults(
    val clients: List<Client> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val sessions: List<Session> = emptyList(),
    val equipment: List<Equipment> = emptyList()
) {
    val isEmpty: Boolean
        get() = clients.isEmpty() && appointments.isEmpty() && sessions.isEmpty() && equipment.isEmpty()

    val totalCount: Int
        get() = clients.size + appointments.size + sessions.size + equipment.size
}
