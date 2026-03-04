package com.dasurv.data.repository

import com.dasurv.data.local.dao.EquipmentDao
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.EquipmentPurchase
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

    fun getLowStockEquipment(): Flow<List<Equipment>> = equipmentDao.getLowStockEquipment()

    fun getLowStockEquipmentCount(): Flow<Int> = equipmentDao.getLowStockEquipmentCount()

    fun searchEquipment(query: String): Flow<List<Equipment>> = equipmentDao.searchEquipment(query)

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

    // Purchases
    fun getAllPurchases(): Flow<List<EquipmentPurchase>> = equipmentDao.getAllPurchases()

    fun getPurchasesBetween(start: Long, end: Long): Flow<List<EquipmentPurchase>> =
        equipmentDao.getPurchasesBetween(start, end)

    suspend fun insertPurchaseAndAddStock(purchase: EquipmentPurchase) =
        equipmentDao.insertPurchaseAndAddStock(purchase)

    suspend fun deletePurchase(purchase: EquipmentPurchase) = equipmentDao.deletePurchase(purchase)

    fun getDistinctPurchaseSources(): Flow<List<String>> = equipmentDao.getDistinctPurchaseSources()
    fun getDistinctSellers(): Flow<List<String>> = equipmentDao.getDistinctSellers()
}
