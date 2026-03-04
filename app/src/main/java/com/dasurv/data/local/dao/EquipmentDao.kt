package com.dasurv.data.local.dao

import androidx.room.*
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.EquipmentPurchase
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

    @Query("SELECT * FROM equipment WHERE minStockThreshold > 0 AND stockQuantity <= minStockThreshold AND stockQuantity > 0")
    fun getLowStockEquipment(): Flow<List<Equipment>>

    @Query("SELECT COUNT(*) FROM equipment WHERE minStockThreshold > 0 AND stockQuantity <= minStockThreshold AND stockQuantity > 0")
    fun getLowStockEquipmentCount(): Flow<Int>

    @Query("SELECT * FROM equipment WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'")
    fun searchEquipment(query: String): Flow<List<Equipment>>

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

    // Equipment purchases
    @Query("SELECT * FROM equipment_purchases ORDER BY purchaseDate DESC")
    fun getAllPurchases(): Flow<List<EquipmentPurchase>>

    @Query("SELECT * FROM equipment_purchases WHERE purchaseDate BETWEEN :start AND :end ORDER BY purchaseDate DESC")
    fun getPurchasesBetween(start: Long, end: Long): Flow<List<EquipmentPurchase>>

    @Insert
    suspend fun insertPurchase(purchase: EquipmentPurchase): Long

    @Delete
    suspend fun deletePurchase(purchase: EquipmentPurchase)

    @Transaction
    suspend fun insertPurchaseAndAddStock(purchase: EquipmentPurchase) {
        insertPurchase(purchase)
        val eq = getEquipmentById(purchase.equipmentId) ?: return
        updateEquipment(eq.copy(stockQuantity = eq.stockQuantity + purchase.quantity))
    }

    // Autocomplete suggestions for purchase source / seller
    @Query("""
        SELECT DISTINCT purchaseSource FROM equipment WHERE purchaseSource != ''
        UNION
        SELECT DISTINCT purchaseSource FROM equipment_purchases WHERE purchaseSource != ''
    """)
    fun getDistinctPurchaseSources(): Flow<List<String>>

    @Query("""
        SELECT DISTINCT seller FROM equipment WHERE seller != ''
        UNION
        SELECT DISTINCT seller FROM equipment_purchases WHERE seller != ''
    """)
    fun getDistinctSellers(): Flow<List<String>>
}
