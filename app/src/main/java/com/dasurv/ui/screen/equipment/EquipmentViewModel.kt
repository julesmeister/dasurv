package com.dasurv.ui.screen.equipment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.EquipmentUsage
import com.dasurv.data.repository.EquipmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EquipmentViewModel @Inject constructor(
    private val equipmentRepository: EquipmentRepository
) : ViewModel() {

    val equipment = equipmentRepository.getAllEquipment()

    private val _selectedEquipment = MutableStateFlow<Equipment?>(null)
    val selectedEquipment: StateFlow<Equipment?> = _selectedEquipment

    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter: StateFlow<String?> = _typeFilter

    fun setTypeFilter(type: String?) {
        _typeFilter.value = type
    }

    fun loadEquipment(id: Long) {
        viewModelScope.launch {
            _selectedEquipment.value = equipmentRepository.getEquipmentById(id)
        }
    }

    fun saveEquipment(equipment: Equipment, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (equipment.id == 0L) {
                equipmentRepository.insertEquipment(equipment)
            } else {
                equipmentRepository.updateEquipment(equipment)
            }
            onSuccess()
        }
    }

    fun deleteEquipment(equipment: Equipment, onSuccess: () -> Unit) {
        viewModelScope.launch {
            equipmentRepository.deleteEquipment(equipment)
            onSuccess()
        }
    }

    fun logUsage(equipmentId: Long, quantity: Double, notes: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            equipmentRepository.insertUsage(
                EquipmentUsage(
                    equipmentId = equipmentId,
                    quantityUsed = quantity,
                    notes = notes
                )
            )
            // Deduct from stock
            val eq = equipmentRepository.getEquipmentById(equipmentId)
            if (eq != null) {
                equipmentRepository.updateEquipment(
                    eq.copy(stockQuantity = (eq.stockQuantity - quantity.toInt()).coerceAtLeast(0))
                )
            }
            onSuccess()
        }
    }
}
