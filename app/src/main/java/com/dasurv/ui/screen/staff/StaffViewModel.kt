package com.dasurv.ui.screen.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.Staff
import com.dasurv.data.repository.StaffRepository
import com.dasurv.util.DefaultSubscribePolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val staffRepository: StaffRepository
) : ViewModel() {

    val allStaff: StateFlow<List<Staff>> = staffRepository.getAllStaff()
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    private val _selectedStaff = MutableStateFlow<Staff?>(null)
    val selectedStaff: StateFlow<Staff?> = _selectedStaff

    fun loadStaff(id: Long) {
        viewModelScope.launch {
            _selectedStaff.value = staffRepository.getStaffById(id)
        }
    }

    fun clearSelectedStaff() {
        _selectedStaff.value = null
    }

    fun saveStaff(staff: Staff, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (staff.id == 0L) {
                staffRepository.insertStaff(staff)
            } else {
                staffRepository.updateStaff(staff)
            }
            onSuccess()
        }
    }

    fun deleteStaff(staff: Staff, onSuccess: () -> Unit) {
        viewModelScope.launch {
            staffRepository.deleteStaff(staff)
            onSuccess()
        }
    }
}
