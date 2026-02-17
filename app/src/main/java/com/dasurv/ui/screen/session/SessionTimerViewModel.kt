package com.dasurv.ui.screen.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionTimerState(
    /** Timer overlay is visible */
    val isActive: Boolean = false,
    /** Timer is currently ticking */
    val isRunning: Boolean = false,
    /** Timer is paused (active but not ticking) */
    val isPaused: Boolean = false,
    /** Waiting for user to confirm discard */
    val showStopConfirmation: Boolean = false,
    val clientId: Long? = null,
    val clientName: String = "",
    val totalElapsed: Long = 0,
    val upperElapsed: Long = 0,
    val lowerElapsed: Long = 0,
    val isUpperRunning: Boolean = false,
    val isLowerRunning: Boolean = false,
    val isExpanded: Boolean = false
)

@HiltViewModel
class SessionTimerViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SessionTimerState())
    val state: StateFlow<SessionTimerState> = _state

    private var tickJob: Job? = null

    /** Start a new timer for the given client. */
    fun startTimer(clientId: Long) {
        _state.value = SessionTimerState(
            isActive = true,
            isRunning = true,
            clientId = clientId,
            isExpanded = true
        )
        startTicking()
        viewModelScope.launch {
            val client = clientRepository.getClientById(clientId)
            if (client != null) {
                _state.update { it.copy(clientName = client.name) }
            }
        }
    }

    /** Check if a timer is already running for a different client. */
    fun hasConflict(clientId: Long): Boolean {
        val s = _state.value
        return s.isActive && s.clientId != null && s.clientId != clientId
    }

    /** Pause or resume the timer. */
    fun pauseResumeTimer() {
        _state.update {
            if (it.isPaused) {
                it.copy(isPaused = false, isRunning = true)
            } else {
                it.copy(isPaused = true, isRunning = false)
            }
        }
    }

    /** Request stop — pauses timer and shows confirmation dialog. */
    fun requestStop() {
        _state.update {
            it.copy(isPaused = true, isRunning = false, showStopConfirmation = true)
        }
    }

    /** User cancelled the stop confirmation — resume if was running before. */
    fun cancelStop() {
        _state.update {
            it.copy(showStopConfirmation = false, isPaused = false, isRunning = true)
        }
    }

    /** User confirmed discard — clear everything. */
    fun confirmDiscard() {
        tickJob?.cancel()
        tickJob = null
        _state.value = SessionTimerState()
    }

    /** Reset timer completely (called after session save). */
    fun resetTimer() {
        tickJob?.cancel()
        tickJob = null
        _state.value = SessionTimerState()
    }

    fun toggleExpanded() {
        _state.update { it.copy(isExpanded = !it.isExpanded) }
    }

    fun toggleUpperZone() {
        _state.update { it.copy(isUpperRunning = !it.isUpperRunning) }
    }

    fun toggleLowerZone() {
        _state.update { it.copy(isLowerRunning = !it.isLowerRunning) }
    }

    /** Get current durations. Only valid when timer clientId matches. */
    fun getDurationsForClient(clientId: Long): Triple<Long, Long, Long> {
        val s = _state.value
        return if (s.isActive && s.clientId == clientId) {
            Triple(s.totalElapsed, s.upperElapsed, s.lowerElapsed)
        } else {
            Triple(0L, 0L, 0L)
        }
    }

    fun stopAndSaveDurations(sessionId: Long) {
        val s = _state.value
        val total = s.totalElapsed
        val upper = s.upperElapsed
        val lower = s.lowerElapsed
        tickJob?.cancel()
        tickJob = null
        viewModelScope.launch {
            val session = sessionRepository.getSessionById(sessionId)
            if (session != null) {
                sessionRepository.updateSession(
                    session.copy(
                        durationSeconds = total,
                        upperLipSeconds = upper,
                        lowerLipSeconds = lower
                    )
                )
            }
            _state.value = SessionTimerState()
        }
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _state.update { s ->
                    if (!s.isRunning || s.isPaused) return@update s
                    s.copy(
                        totalElapsed = s.totalElapsed + 1,
                        upperElapsed = if (s.isUpperRunning) s.upperElapsed + 1 else s.upperElapsed,
                        lowerElapsed = if (s.isLowerRunning) s.lowerElapsed + 1 else s.lowerElapsed
                    )
                }
            }
        }
    }
}
