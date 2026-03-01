package com.dasurv.data.local.dao

import androidx.room.*
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.PigmentBottleUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface PigmentBottleDao {
    // Bottle CRUD
    @Query("SELECT * FROM pigment_bottles ORDER BY pigmentBrand, pigmentName ASC")
    fun getAllBottles(): Flow<List<PigmentBottle>>

    @Query("SELECT * FROM pigment_bottles WHERE id = :id")
    suspend fun getBottleById(id: Long): PigmentBottle?

    @Query("SELECT * FROM pigment_bottles WHERE pigmentBrand = :brand ORDER BY pigmentName ASC")
    fun getBottlesByBrand(brand: String): Flow<List<PigmentBottle>>

    @Query("SELECT * FROM pigment_bottles WHERE remainingMl > 0 ORDER BY pigmentBrand, pigmentName ASC")
    fun getInStockBottles(): Flow<List<PigmentBottle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBottle(bottle: PigmentBottle): Long

    @Query("SELECT * FROM pigment_bottles WHERE id IN (:ids)")
    suspend fun getBottlesByIds(ids: List<Long>): List<PigmentBottle>

    @Update
    suspend fun updateBottle(bottle: PigmentBottle)

    @Update
    suspend fun updateBottleBatch(bottles: List<PigmentBottle>)

    @Delete
    suspend fun deleteBottle(bottle: PigmentBottle)

    @Query("SELECT COUNT(*) FROM pigment_bottles WHERE equipmentId = :equipmentId AND remainingMl > 0")
    suspend fun getOpenBottleCountForEquipment(equipmentId: Long): Int

    @Query("SELECT * FROM pigment_bottles WHERE equipmentId = :equipmentId ORDER BY purchaseDate DESC")
    fun getBottlesByEquipmentId(equipmentId: Long): Flow<List<PigmentBottle>>

    // Usage queries
    @Query("SELECT * FROM pigment_bottle_usage WHERE bottleId = :bottleId ORDER BY date DESC")
    fun getUsageForBottle(bottleId: Long): Flow<List<PigmentBottleUsage>>

    @Query("SELECT * FROM pigment_bottle_usage WHERE clientId = :clientId ORDER BY date DESC")
    fun getUsageForClient(clientId: Long): Flow<List<PigmentBottleUsage>>

    @Query("SELECT * FROM pigment_bottle_usage WHERE sessionId = :sessionId ORDER BY date DESC")
    fun getUsageForSession(sessionId: Long): Flow<List<PigmentBottleUsage>>

    @Insert
    suspend fun insertUsage(usage: PigmentBottleUsage): Long

    @Delete
    suspend fun deleteUsage(usage: PigmentBottleUsage)
}
