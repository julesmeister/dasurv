package com.dasurv.data.repository

import com.dasurv.data.local.dao.EquipmentDao
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.EquipmentUsage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentRepository @Inject constructor(
    private val equipmentDao: EquipmentDao
) {
    fun getAllEquipment(): Flow<List<Equipment>> = equipmentDao.getAllEquipment()

    suspend fun getEquipmentById(id: Long): Equipment? = equipmentDao.getEquipmentById(id)

    fun getEquipmentByCategory(category: String): Flow<List<Equipment>> =
        equipmentDao.getEquipmentByCategory(category)

    fun getByType(type: String): Flow<List<Equipment>> = equipmentDao.getByType(type)

    suspend fun insertEquipment(equipment: Equipment): Long =
        equipmentDao.insertEquipment(equipment)

    suspend fun getEquipmentByIds(ids: List<Long>): List<Equipment> =
        equipmentDao.getEquipmentByIds(ids)

    suspend fun updateEquipment(equipment: Equipment) = equipmentDao.updateEquipment(equipment)

    suspend fun updateEquipmentBatch(equipment: List<Equipment>) =
        equipmentDao.updateEquipmentBatch(equipment)

    suspend fun deleteEquipment(equipment: Equipment) = equipmentDao.deleteEquipment(equipment)

    // Standalone usage
    fun getUsageForEquipment(equipmentId: Long): Flow<List<EquipmentUsage>> =
        equipmentDao.getUsageForEquipment(equipmentId)

    suspend fun insertUsage(usage: EquipmentUsage): Long = equipmentDao.insertUsage(usage)

    suspend fun insertUsageAndDeductStock(usage: EquipmentUsage) =
        equipmentDao.insertUsageAndDeductStock(usage)

    suspend fun deleteUsage(usage: EquipmentUsage) = equipmentDao.deleteUsage(usage)
}
