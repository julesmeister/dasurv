package com.dasurv.data.local.dao

import androidx.room.*
import com.dasurv.data.local.entity.SessionTemplate
import com.dasurv.data.local.entity.SessionTemplateEquipment
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionTemplateDao {
    @Query("SELECT * FROM session_templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<SessionTemplate>>

    @Query("SELECT * FROM session_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): SessionTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: SessionTemplate): Long

    @Update
    suspend fun updateTemplate(template: SessionTemplate)

    @Delete
    suspend fun deleteTemplate(template: SessionTemplate)

    // Template equipment junction
    @Query("SELECT * FROM session_template_equipment WHERE templateId = :templateId")
    suspend fun getEquipmentForTemplate(templateId: Long): List<SessionTemplateEquipment>

    @Insert
    suspend fun insertTemplateEquipment(item: SessionTemplateEquipment): Long

    @Query("DELETE FROM session_template_equipment WHERE templateId = :templateId")
    suspend fun deleteEquipmentForTemplate(templateId: Long)
}
