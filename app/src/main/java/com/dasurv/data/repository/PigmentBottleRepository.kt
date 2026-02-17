package com.dasurv.data.repository

import com.dasurv.data.local.dao.PigmentBottleDao
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.PigmentBottleUsage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PigmentBottleRepository @Inject constructor(
    private val pigmentBottleDao: PigmentBottleDao
) {
    fun getAllBottles(): Flow<List<PigmentBottle>> = pigmentBottleDao.getAllBottles()

    suspend fun getBottleById(id: Long): PigmentBottle? = pigmentBottleDao.getBottleById(id)

    fun getBottlesByBrand(brand: String): Flow<List<PigmentBottle>> =
        pigmentBottleDao.getBottlesByBrand(brand)

    fun getInStockBottles(): Flow<List<PigmentBottle>> = pigmentBottleDao.getInStockBottles()

    suspend fun insertBottle(bottle: PigmentBottle): Long = pigmentBottleDao.insertBottle(bottle)

    suspend fun updateBottle(bottle: PigmentBottle) = pigmentBottleDao.updateBottle(bottle)

    suspend fun deleteBottle(bottle: PigmentBottle) = pigmentBottleDao.deleteBottle(bottle)

    suspend fun getOpenBottleCountForEquipment(equipmentId: Long): Int =
        pigmentBottleDao.getOpenBottleCountForEquipment(equipmentId)

    fun getBottlesByEquipmentId(equipmentId: Long): Flow<List<PigmentBottle>> =
        pigmentBottleDao.getBottlesByEquipmentId(equipmentId)

    // Usage
    fun getUsageForBottle(bottleId: Long): Flow<List<PigmentBottleUsage>> =
        pigmentBottleDao.getUsageForBottle(bottleId)

    fun getUsageForClient(clientId: Long): Flow<List<PigmentBottleUsage>> =
        pigmentBottleDao.getUsageForClient(clientId)

    fun getUsageForSession(sessionId: Long): Flow<List<PigmentBottleUsage>> =
        pigmentBottleDao.getUsageForSession(sessionId)

    suspend fun insertUsage(usage: PigmentBottleUsage): Long = pigmentBottleDao.insertUsage(usage)

    suspend fun deleteUsage(usage: PigmentBottleUsage) = pigmentBottleDao.deleteUsage(usage)
}
