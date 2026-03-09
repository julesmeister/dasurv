package com.dasurv.ui.screen.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.Appointment
import com.dasurv.data.local.entity.AppointmentStatus
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.RecurrenceType
import com.dasurv.data.local.entity.Session
import com.dasurv.data.local.entity.Staff
import com.dasurv.data.model.AppointmentWithClient
import com.dasurv.data.model.CalendarDay
import com.dasurv.data.model.CalendarMonth
import com.dasurv.data.repository.AppointmentRepository
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.SessionRepository
import com.dasurv.data.repository.StaffRepository
import com.dasurv.util.AppointmentAlarmScheduler
import com.dasurv.util.DefaultSubscribePolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val clientRepository: ClientRepository,
    private val sessionRepository: SessionRepository,
    private val staffRepository: StaffRepository,
    private val alarmScheduler: AppointmentAlarmScheduler
) : ViewModel() {

    val activeStaff: StateFlow<List<Staff>> = staffRepository.getActiveStaff()
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val currentYear: StateFlow<Int> = _currentYear
    val currentMonth: StateFlow<Int> = _currentMonth

    private val _selectedDayOfMonth = MutableStateFlow<Int?>(null)
    val selectedDayOfMonth: StateFlow<Int?> = _selectedDayOfMonth

    private val _selectedAppointment = MutableStateFlow<Appointment?>(null)
    val selectedAppointment: StateFlow<Appointment?> = _selectedAppointment

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun clearSnackbar() { _snackbarMessage.value = null }

    val clients: StateFlow<List<Client>> = clientRepository.getAllClients()
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthAppointments: StateFlow<List<Appointment>> = combine(_currentYear, _currentMonth) { year, month ->
        val cal = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val endTime = cal.timeInMillis
        Pair(startTime, endTime)
    }.flatMapLatest { (start, end) ->
        appointmentRepository.getAppointmentsInRange(start, end)
    }.stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    val calendarMonth: StateFlow<CalendarMonth> = combine(
        _currentYear, _currentMonth, monthAppointments
    ) { year, month, appointments ->
        buildCalendarMonth(year, month, appointments)
    }.stateIn(viewModelScope, DefaultSubscribePolicy, buildCalendarMonth(
        _currentYear.value, _currentMonth.value, emptyList()
    ))

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDayAppointments: StateFlow<List<AppointmentWithClient>> = combine(
        _selectedDayOfMonth, monthAppointments, clientRepository.getAllClients()
    ) { day, appointments, allClients ->
        if (day == null) return@combine emptyList()
        val clientMap = allClients.associateBy { it.id }
        val cal = Calendar.getInstance()
        appointments.filter { appt ->
            cal.timeInMillis = appt.scheduledDateTime
            cal.get(Calendar.DAY_OF_MONTH) == day
        }.map { appt ->
            AppointmentWithClient(appt, clientMap[appt.clientId]?.name ?: "Unknown")
        }
    }.stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    fun navigateMonth(delta: Int) {
        val cal = Calendar.getInstance().apply {
            set(_currentYear.value, _currentMonth.value, 1)
            add(Calendar.MONTH, delta)
        }
        _currentYear.value = cal.get(Calendar.YEAR)
        _currentMonth.value = cal.get(Calendar.MONTH)
        _selectedDayOfMonth.value = null
    }

    fun selectDay(day: Int) {
        _selectedDayOfMonth.value = if (_selectedDayOfMonth.value == day) null else day
    }

    fun goToLatestAppointment() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val appointment = appointmentRepository.getNextAppointment(now)
                ?: appointmentRepository.getLatestAppointment()
            if (appointment == null) {
                _snackbarMessage.value = "No appointments yet"
                return@launch
            }
            val cal = Calendar.getInstance().apply { timeInMillis = appointment.scheduledDateTime }
            _currentYear.value = cal.get(Calendar.YEAR)
            _currentMonth.value = cal.get(Calendar.MONTH)
            _selectedDayOfMonth.value = cal.get(Calendar.DAY_OF_MONTH)
        }
    }

    fun loadAppointment(id: Long) {
        viewModelScope.launch {
            _selectedAppointment.value = appointmentRepository.getAppointmentById(id)
        }
    }

    fun saveAppointment(appointment: Appointment, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val isNew = appointment.id == 0L
            val id = if (isNew) {
                appointmentRepository.insertAppointment(appointment)
            } else {
                appointmentRepository.updateAppointment(appointment)
                appointment.id
            }
            if (appointment.reminderEnabled && appointment.status == AppointmentStatus.SCHEDULED) {
                val triggerAt = appointment.scheduledDateTime - appointment.reminderMinutesBefore * 60_000L
                if (triggerAt > System.currentTimeMillis()) {
                    alarmScheduler.scheduleReminder(id, triggerAt)
                }
            } else {
                alarmScheduler.cancelReminder(id)
            }
            _snackbarMessage.value = if (isNew) "Appointment created" else "Appointment updated"
            onSuccess(id)
        }
    }

    fun saveRecurringAppointment(appointment: Appointment, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val id = appointmentRepository.createRecurringSeries(appointment)
            if (appointment.reminderEnabled && appointment.status == AppointmentStatus.SCHEDULED) {
                val triggerAt = appointment.scheduledDateTime - appointment.reminderMinutesBefore * 60_000L
                if (triggerAt > System.currentTimeMillis()) {
                    alarmScheduler.scheduleReminder(id, triggerAt)
                }
            }
            _snackbarMessage.value = "Recurring appointment created"
            onSuccess(id)
        }
    }

    fun deleteAppointmentSeries(appointment: Appointment, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Delete children first
            appointmentRepository.deleteRecurringSeries(appointment.id)
            // Delete parent
            alarmScheduler.cancelReminder(appointment.id)
            appointmentRepository.deleteAppointment(appointment)
            _snackbarMessage.value = "Series deleted"
            onSuccess()
        }
    }

    fun deleteAppointment(appointment: Appointment, onSuccess: () -> Unit) {
        viewModelScope.launch {
            alarmScheduler.cancelReminder(appointment.id)
            appointmentRepository.deleteAppointment(appointment)
            _snackbarMessage.value = "Appointment deleted"
            onSuccess()
        }
    }

    fun updateAppointmentStatus(appointment: Appointment, status: AppointmentStatus, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            appointmentRepository.updateAppointment(appointment.copy(status = status))
            if (status != AppointmentStatus.SCHEDULED) {
                alarmScheduler.cancelReminder(appointment.id)
            }
            _snackbarMessage.value = "Status updated to ${status.name.lowercase().replaceFirstChar { it.uppercase() }}"
            onSuccess()
        }
    }

    fun convertToSession(appointment: Appointment, onSessionCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val session = Session(
                clientId = appointment.clientId,
                date = appointment.scheduledDateTime,
                procedure = appointment.procedureType,
                notes = appointment.notes
            )
            val sessionId = sessionRepository.insertSession(session)
            appointmentRepository.updateAppointment(
                appointment.copy(status = AppointmentStatus.COMPLETED, sessionId = sessionId)
            )
            alarmScheduler.cancelReminder(appointment.id)
            _snackbarMessage.value = "Session started"
            onSessionCreated(sessionId)
        }
    }

    fun getClientName(clientId: Long): Flow<String> = flow {
        val client = clientRepository.getClientById(clientId)
        emit(client?.name ?: "Unknown")
    }

    private fun buildCalendarMonth(year: Int, month: Int, appointments: List<Appointment>): CalendarMonth {
        val cal = Calendar.getInstance()
        val today = Calendar.getInstance()

        cal.set(year, month, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        cal.add(Calendar.MONTH, -1)
        val daysInPrevMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val groupCal = Calendar.getInstance()
        val appointmentsByDay = appointments.groupBy { appt ->
            groupCal.timeInMillis = appt.scheduledDateTime
            groupCal.get(Calendar.DAY_OF_MONTH)
        }

        val days = mutableListOf<CalendarDay>()

        // Previous month days
        for (i in firstDayOfWeek - 1 downTo 0) {
            days.add(CalendarDay(daysInPrevMonth - i, isCurrentMonth = false))
        }

        // Current month days
        for (day in 1..daysInMonth) {
            val isToday = year == today.get(Calendar.YEAR) &&
                    month == today.get(Calendar.MONTH) &&
                    day == today.get(Calendar.DAY_OF_MONTH)
            days.add(CalendarDay(day, isCurrentMonth = true, isToday = isToday,
                appointments = appointmentsByDay[day] ?: emptyList()))
        }

        // Fill remaining cells to get 42 (6 rows x 7 columns)
        var nextDay = 1
        while (days.size < 42) {
            days.add(CalendarDay(nextDay++, isCurrentMonth = false))
        }

        return CalendarMonth(year, month, days)
    }
}
