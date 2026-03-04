package com.dasurv.ui.screen.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.PigmentBottleUsage
import com.dasurv.data.local.entity.Session
import com.dasurv.data.local.entity.SessionEquipment
import com.dasurv.data.local.entity.Staff
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
    private val staffRepository: StaffRepository
) : ViewModel() {

    private val _sessionId = MutableStateFlow<Long?>(null)

    private val _selectedSession = MutableStateFlow<Session?>(null)
    val selectedSession: StateFlow<Session?> = _selectedSession

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

    fun loadSession(id: Long) {
        _sessionId.value = id
        viewModelScope.launch {
            _selectedSession.value = sessionRepository.getSessionById(id)
        }
    }

    fun deleteSession(session: Session, onSuccess: () -> Unit) {
        viewModelScope.launch {
            sessionRepository.deleteSession(session)
            onSuccess()
        }
    }
}
