package com.dasurv.data.repository

import com.dasurv.data.local.dao.LipPhotoDao
import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.local.entity.LipPhoto
import com.dasurv.data.local.entity.LipPhotoPigment
import com.dasurv.data.local.entity.LipZone
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LipPhotoRepository @Inject constructor(
    private val lipPhotoDao: LipPhotoDao
) {
    fun getPhotosForClient(clientId: Long): Flow<List<LipPhoto>> =
        lipPhotoDao.getPhotosForClient(clientId)

    suspend fun getPhotoById(id: Long): LipPhoto? = lipPhotoDao.getPhotoById(id)

    fun getPhotosByType(clientId: Long, type: CaptureType): Flow<List<LipPhoto>> =
        lipPhotoDao.getPhotosByType(clientId, type)

    suspend fun insertPhoto(photo: LipPhoto): Long = lipPhotoDao.insertPhoto(photo)

    suspend fun updatePhoto(photo: LipPhoto) = lipPhotoDao.updatePhoto(photo)

    suspend fun deletePhoto(photo: LipPhoto) = lipPhotoDao.deletePhoto(photo)

    fun getPigmentsForPhoto(lipPhotoId: Long): Flow<List<LipPhotoPigment>> =
        lipPhotoDao.getPigmentsForPhoto(lipPhotoId)

    fun getPigmentsForPhotoZone(lipPhotoId: Long, zone: LipZone): Flow<List<LipPhotoPigment>> =
        lipPhotoDao.getPigmentsForPhotoZone(lipPhotoId, zone)

    suspend fun insertPigment(pigment: LipPhotoPigment): Long = lipPhotoDao.insertPigment(pigment)

    suspend fun deletePigment(pigment: LipPhotoPigment) = lipPhotoDao.deletePigment(pigment)

    suspend fun deletePigmentsForPhoto(lipPhotoId: Long) = lipPhotoDao.deletePigmentsForPhoto(lipPhotoId)

    suspend fun getPigmentsForPhotoOnce(lipPhotoId: Long) = lipPhotoDao.getPigmentsForPhotoOnce(lipPhotoId)
}
