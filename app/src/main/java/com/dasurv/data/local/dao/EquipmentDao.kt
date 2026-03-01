package com.dasurv.data.local.dao

import androidx.room.*
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.EquipmentUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment ORDER BY category, name ASC")
    fun getAllEquipment(): Flow<List<Equipment>>

    @Query("SELECT * FROM equipment WHERE id = :id")
    suspend fun getEquipmentById(id: Long): Equipment?

    @Query("SELECT * FROM equipment WHERE category = :category")
    fun getEquipmentByCategory(category: String): Flow<List<Equipment>>

    @Query("SELECT * FROM equipment WHERE type = :type ORDER BY category, name ASC")
    fun getByType(type: String): Flow<List<Equipment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: Equipment): Long

    @Query("SELECT * FROM equipment WHERE id IN (:ids)")
    suspend fun getEquipmentByIds(ids: List<Long>): List<Equipment>

    @Update
    suspend fun updateEquipment(equipment: Equipment)

    @Update
    suspend fun updateEquipmentBatch(equipment: List<Equipment>)

    @Delete
    suspend fun deleteEquipment(equipment: Equipment)

    // Equipment usage (standalone)
    @Query("SELECT * FROM equipment_usage WHERE equipmentId = :equipmentId ORDER BY date DESC")
    fun getUsageForEquipment(equipmentId: Long): Flow<List<EquipmentUsage>>

    @Insert
    suspend fun insertUsage(usage: EquipmentUsage): Long

    @Delete
    suspend fun deleteUsage(usage: EquipmentUsage)

    @Transaction
    suspend fun insertUsageAndDeductStock(usage: EquipmentUsage) {
        insertUsage(usage)
        val eq = getEquipmentById(usage.equipmentId) ?: return
        updateEquipment(eq.copy(stockQuantity = (eq.stockQuantity - usage.quantityUsed.toInt()).coerceAtLeast(0)))
    }
}
