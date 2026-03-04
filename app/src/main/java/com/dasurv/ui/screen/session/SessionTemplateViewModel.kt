package com.dasurv.ui.screen.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.SessionTemplate
import com.dasurv.data.local.entity.SessionTemplateEquipment
import com.dasurv.data.repository.SessionTemplateRepository
import com.dasurv.util.DefaultSubscribePolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionTemplateViewModel @Inject constructor(
    private val templateRepository: SessionTemplateRepository
) : ViewModel() {

    val allTemplates: StateFlow<List<SessionTemplate>> = templateRepository.getAllTemplates()
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    private val _selectedTemplate = MutableStateFlow<SessionTemplate?>(null)
    val selectedTemplate: StateFlow<SessionTemplate?> = _selectedTemplate

    fun loadTemplate(id: Long) {
        viewModelScope.launch {
            _selectedTemplate.value = templateRepository.getTemplateById(id)
        }
    }

    fun clearSelectedTemplate() {
        _selectedTemplate.value = null
    }

    fun saveTemplate(
        template: SessionTemplate,
        equipmentItems: List<Pair<Long, Int>>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val id = if (template.id == 0L) {
                templateRepository.insertTemplate(template)
            } else {
                templateRepository.updateTemplate(template)
                template.id
            }
            // Replace equipment list
            templateRepository.deleteEquipmentForTemplate(id)
            for ((equipmentId, quantity) in equipmentItems) {
                templateRepository.insertTemplateEquipment(
                    SessionTemplateEquipment(
                        templateId = id,
                        equipmentId = equipmentId,
                        quantity = quantity
                    )
                )
            }
            onSuccess()
        }
    }

    fun deleteTemplate(template: SessionTemplate, onSuccess: () -> Unit) {
        viewModelScope.launch {
            templateRepository.deleteTemplate(template)
            onSuccess()
        }
    }

    suspend fun getEquipmentForTemplate(templateId: Long): List<SessionTemplateEquipment> =
        templateRepository.getEquipmentForTemplate(templateId)
}
