package com.dasurv.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.Client
import com.dasurv.data.model.AppointmentWithClient
import com.dasurv.data.repository.AppointmentRepository
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.SessionRepository
import com.dasurv.util.DefaultSubscribePolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    clientRepository: ClientRepository,
    sessionRepository: SessionRepository,
    appointmentRepository: AppointmentRepository
) : ViewModel() {
    val clients: StateFlow<List<Client>> = clientRepository.getAllClients()
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    val sessionCount: StateFlow<Int> = sessionRepository.getSessionCount()
        .stateIn(viewModelScope, DefaultSubscribePolicy, 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val upcomingAppointments: StateFlow<List<AppointmentWithClient>> = clients
        .flatMapLatest { allClients ->
            val clientMap = allClients.associateBy { it.id }
            // Use current time at subscription — re-evaluated when WhileSubscribed restarts
            appointmentRepository.getUpcomingAppointments(System.currentTimeMillis(), 5)
                .map { appointments ->
                    appointments.map { appt ->
                        AppointmentWithClient(appt, clientMap[appt.clientId]?.name ?: "Unknown")
                    }
                }
        }.stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())
}
