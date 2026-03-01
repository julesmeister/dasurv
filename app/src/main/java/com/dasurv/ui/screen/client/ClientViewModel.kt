package com.dasurv.ui.screen.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.Client
import com.dasurv.data.model.FinancialSummary
import com.dasurv.data.repository.AppointmentRepository
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.SessionRepository
import com.dasurv.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val sessionRepository: SessionRepository,
    private val appointmentRepository: AppointmentRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val filteredClients = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) clientRepository.getAllClients()
        else clientRepository.searchClients(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedClient = MutableStateFlow<Client?>(null)
    val selectedClient: StateFlow<Client?> = _selectedClient

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun loadClient(id: Long) {
        viewModelScope.launch {
            _selectedClient.value = clientRepository.getClientById(id)
        }
    }

    fun getSessionsForClient(clientId: Long) = sessionRepository.getSessionsForClient(clientId)

    fun getAppointmentsForClient(clientId: Long) = appointmentRepository.getAppointmentsForClient(clientId)

    fun getFinancialSummary(clientId: Long): Flow<FinancialSummary> =
        transactionRepository.getFinancialSummary(clientId)

    fun saveClient(client: Client, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (client.id == 0L) {
                clientRepository.insertClient(client)
            } else {
                clientRepository.updateClient(client)
            }
            onSuccess()
        }
    }

    fun deleteClient(client: Client, onSuccess: () -> Unit) {
        viewModelScope.launch {
            clientRepository.deleteClient(client)
            onSuccess()
        }
    }
}
