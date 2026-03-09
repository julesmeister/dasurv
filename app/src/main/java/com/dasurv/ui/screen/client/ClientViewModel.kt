package com.dasurv.ui.screen.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.Client
import com.dasurv.data.model.FinancialSummary
import com.dasurv.data.local.entity.ClientUpdate
import com.dasurv.data.repository.AppointmentRepository
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.ClientUpdateRepository
import com.dasurv.data.repository.SessionRepository
import com.dasurv.data.repository.TransactionRepository
import com.dasurv.util.DefaultSubscribePolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val sessionRepository: SessionRepository,
    private val appointmentRepository: AppointmentRepository,
    private val transactionRepository: TransactionRepository,
    private val clientUpdateRepository: ClientUpdateRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val filteredClients = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) clientRepository.getAllClients()
        else clientRepository.searchClients(query)
    }.stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    private val _selectedClient = MutableStateFlow<Client?>(null)
    val selectedClient: StateFlow<Client?> = _selectedClient

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun clearSnackbar() { _snackbarMessage.value = null }
    fun showSnackbar(msg: String) { _snackbarMessage.value = msg }

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
            val isNew = client.id == 0L
            if (isNew) {
                clientRepository.insertClient(client)
            } else {
                clientRepository.updateClient(client)
            }
            _snackbarMessage.value = if (isNew) "Client added" else "Client updated"
            onSuccess()
        }
    }

    fun getUpdatesForClient(clientId: Long) = clientUpdateRepository.getUpdatesForClient(clientId)

    fun saveUpdate(update: ClientUpdate, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val isNew = update.id == 0L
            if (isNew) {
                clientUpdateRepository.insertUpdate(update)
            } else {
                clientUpdateRepository.updateUpdate(update)
            }
            _snackbarMessage.value = if (isNew) "Update added" else "Update saved"
            onSuccess()
        }
    }

    fun deleteUpdate(update: ClientUpdate) {
        viewModelScope.launch {
            clientUpdateRepository.deleteUpdate(update)
            _snackbarMessage.value = "Update deleted"
        }
    }

    fun deleteClient(client: Client, onSuccess: () -> Unit) {
        viewModelScope.launch {
            clientRepository.deleteClient(client)
            onSuccess()
        }
    }
}
