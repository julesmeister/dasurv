package com.dasurv.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.model.SearchResults
import com.dasurv.data.repository.AppointmentRepository
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.EquipmentRepository
import com.dasurv.data.repository.SessionRepository
import com.dasurv.util.DefaultSubscribePolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val appointmentRepository: AppointmentRepository,
    private val sessionRepository: SessionRepository,
    private val equipmentRepository: EquipmentRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val searchResults: StateFlow<SearchResults> = _query
        .debounce(300)
        .flatMapLatest { q ->
            if (q.length < 2) {
                flowOf(SearchResults())
            } else {
                combine(
                    clientRepository.searchClients(q),
                    appointmentRepository.searchAppointments(q),
                    sessionRepository.searchSessions(q),
                    equipmentRepository.searchEquipment(q)
                ) { clients, appointments, sessions, equipment ->
                    SearchResults(
                        clients = clients.take(10),
                        appointments = appointments.take(10),
                        sessions = sessions.take(10),
                        equipment = equipment.take(10)
                    )
                }
            }
        }.stateIn(viewModelScope, DefaultSubscribePolicy, SearchResults())

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }
}
