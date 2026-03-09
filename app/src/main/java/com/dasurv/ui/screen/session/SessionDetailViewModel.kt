package com.dasurv.ui.screen.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.ClientUpdate
import com.dasurv.data.local.entity.PigmentBottleUsage
import com.dasurv.data.local.entity.Session
import com.dasurv.data.local.entity.SessionEquipment
import com.dasurv.data.local.entity.Staff
import com.dasurv.data.repository.ClientUpdateRepository
import com.dasurv.data.repository.EquipmentRepository
import com.dasurv.data.repository.PigmentBottleRepository
import com.dasurv.data.repository.SessionRepository
import com.dasurv.data.repository.StaffRepository
import com.dasurv.util.DefaultSubscribePolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val equipmentRepository: EquipmentRepository,
    private val pigmentBottleRepository: PigmentBottleRepository,
    private val staffRepository: StaffRepository,
    private val clientUpdateRepository: ClientUpdateRepository
) : ViewModel() {

    private val _sessionId = MutableStateFlow<Long?>(null)

    private val _selectedSession = MutableStateFlow<Session?>(null)
    val selectedSession: StateFlow<Session?> = _selectedSession

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun clearSnackbar() { _snackbarMessage.value = null }

    val allEquipment = equipmentRepository.getAllEquipment()

    val allBottles = pigmentBottleRepository.getInStockBottles()

    val activeStaff: StateFlow<List<Staff>> = staffRepository.getActiveStaff()
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    val sessionPigments = _sessionId
        .filterNotNull()
        .flatMapLatest { sessionRepository.getPigmentsForSession(it) }
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    val sessionEquipmentList: StateFlow<List<SessionEquipment>> = _sessionId
        .filterNotNull()
        .flatMapLatest { sessionRepository.getEquipmentForSession(it) }
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    val sessionBottleUsages: StateFlow<List<PigmentBottleUsage>> = _sessionId
        .filterNotNull()
        .flatMapLatest { pigmentBottleRepository.getUsageForSession(it) }
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    val sessionUpdates: StateFlow<List<ClientUpdate>> = _sessionId
        .filterNotNull()
        .flatMapLatest { clientUpdateRepository.getUpdatesForSession(it) }
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    fun loadSession(id: Long) {
        _sessionId.value = id
        viewModelScope.launch {
            _selectedSession.value = sessionRepository.getSessionById(id)
        }
    }

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

    fun deleteSession(session: Session, onSuccess: () -> Unit) {
        viewModelScope.launch {
            sessionRepository.deleteSession(session)
            onSuccess()
        }
    }
}
