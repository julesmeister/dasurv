package com.dasurv.data.repository

import com.dasurv.data.local.dao.SessionTemplateDao
import com.dasurv.data.local.entity.SessionTemplate
import com.dasurv.data.local.entity.SessionTemplateEquipment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionTemplateRepository @Inject constructor(
    private val sessionTemplateDao: SessionTemplateDao
) {
    fun getAllTemplates(): Flow<List<SessionTemplate>> = sessionTemplateDao.getAllTemplates()

    suspend fun getTemplateById(id: Long): SessionTemplate? = sessionTemplateDao.getTemplateById(id)

    suspend fun insertTemplate(template: SessionTemplate): Long = sessionTemplateDao.insertTemplate(template)

    suspend fun updateTemplate(template: SessionTemplate) = sessionTemplateDao.updateTemplate(template)

    suspend fun deleteTemplate(template: SessionTemplate) = sessionTemplateDao.deleteTemplate(template)

    suspend fun getEquipmentForTemplate(templateId: Long): List<SessionTemplateEquipment> =
        sessionTemplateDao.getEquipmentForTemplate(templateId)

    suspend fun insertTemplateEquipment(item: SessionTemplateEquipment): Long =
        sessionTemplateDao.insertTemplateEquipment(item)

    suspend fun deleteEquipmentForTemplate(templateId: Long) =
        sessionTemplateDao.deleteEquipmentForTemplate(templateId)
}
