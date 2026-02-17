package com.dasurv.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.local.entity.LipPhoto
import com.dasurv.data.local.entity.LipPhotoPigment
import com.dasurv.data.local.entity.LipZone
import kotlinx.coroutines.flow.Flow

@Dao
interface LipPhotoDao {

    @Query("SELECT * FROM lip_photos WHERE clientId = :clientId ORDER BY capturedAt DESC")
    fun getPhotosForClient(clientId: Long): Flow<List<LipPhoto>>

    @Query("SELECT * FROM lip_photos WHERE id = :id")
    suspend fun getPhotoById(id: Long): LipPhoto?

    @Query("SELECT * FROM lip_photos WHERE clientId = :clientId AND captureType = :type ORDER BY capturedAt DESC")
    fun getPhotosByType(clientId: Long, type: CaptureType): Flow<List<LipPhoto>>

    @Insert
    suspend fun insertPhoto(photo: LipPhoto): Long

    @Update
    suspend fun updatePhoto(photo: LipPhoto)

    @Delete
    suspend fun deletePhoto(photo: LipPhoto)

    @Query("SELECT * FROM lip_photo_pigments WHERE lipPhotoId = :lipPhotoId")
    fun getPigmentsForPhoto(lipPhotoId: Long): Flow<List<LipPhotoPigment>>

    @Query("SELECT * FROM lip_photo_pigments WHERE lipPhotoId = :lipPhotoId AND lipZone = :zone")
    fun getPigmentsForPhotoZone(lipPhotoId: Long, zone: LipZone): Flow<List<LipPhotoPigment>>

    @Insert
    suspend fun insertPigment(pigment: LipPhotoPigment): Long

    @Delete
    suspend fun deletePigment(pigment: LipPhotoPigment)

    @Query("DELETE FROM lip_photo_pigments WHERE lipPhotoId = :lipPhotoId")
    suspend fun deletePigmentsForPhoto(lipPhotoId: Long)

    @Query("SELECT * FROM lip_photo_pigments WHERE lipPhotoId = :lipPhotoId")
    suspend fun getPigmentsForPhotoOnce(lipPhotoId: Long): List<LipPhotoPigment>
}
