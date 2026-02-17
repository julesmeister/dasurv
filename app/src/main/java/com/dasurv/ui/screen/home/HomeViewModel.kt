package com.dasurv.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.model.AppointmentWithClient
import com.dasurv.data.repository.AppointmentRepository
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    clientRepository: ClientRepository,
    sessionRepository: SessionRepository,
    appointmentRepository: AppointmentRepository
) : ViewModel() {
    val clients = clientRepository.getAllClients()
    val recentSessions = sessionRepository.getAllSessions()

    val upcomingAppointments: StateFlow<List<AppointmentWithClient>> = combine(
        appointmentRepository.getUpcomingAppointments(System.currentTimeMillis(), 5),
        clientRepository.getAllClients()
    ) { appointments, allClients ->
        val clientMap = allClients.associateBy { it.id }
        appointments.map { appt ->
            AppointmentWithClient(appt, clientMap[appt.clientId]?.name ?: "Unknown")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
