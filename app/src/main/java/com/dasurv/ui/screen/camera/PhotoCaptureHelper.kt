package com.dasurv.ui.screen.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.LipPhoto
import com.dasurv.data.local.entity.LipPhotoPigment
import com.dasurv.data.local.entity.LipZone
import com.dasurv.data.model.DualLipAnalysis
import com.dasurv.data.model.FollowUpInterval
import com.dasurv.data.repository.LipPhotoRepository
import com.dasurv.util.ColorMatcher
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class PhotoCaptureHelper @Inject constructor(
    private val application: Application,
    private val lipPhotoRepository: LipPhotoRepository
) {
    fun rotateBitmapForSaving(bitmap: Bitmap, rotation: Int, isFrontCamera: Boolean): Bitmap {
        val matrix = Matrix()
        if (rotation != 0) {
            matrix.postRotate(rotation.toFloat())
        }
        if (isFrontCamera) {
            // Mirror horizontally for front camera
            matrix.postScale(-1f, 1f)
        }
        return if (rotation != 0 || isFrontCamera) {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    suspend fun saveClientPhoto(
        bitmap: Bitmap,
        rotation: Int,
        isFrontCamera: Boolean,
        client: Client,
        captureType: CaptureType,
        followUpInterval: FollowUpInterval,
        customInterval: String,
        dualAnalysis: DualLipAnalysis?,
        upperRecs: List<ColorMatcher.PigmentRecommendation>,
        lowerRecs: List<ColorMatcher.PigmentRecommendation>,
        selectedUpperIdx: Int,
        selectedLowerIdx: Int
    ): Long {
        val rotatedBitmap = rotateBitmapForSaving(bitmap, rotation, isFrontCamera)
        val photoDir = File(application.filesDir, "lip_photos")
        if (!photoDir.exists()) photoDir.mkdirs()
        val file = File(photoDir, "lip_${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        val intervalStr = when (captureType) {
            CaptureType.FOLLOW_UP -> {
                if (followUpInterval == FollowUpInterval.CUSTOM) customInterval
                else followUpInterval.displayName
            }
            else -> null
        }

        val lipPhoto = LipPhoto(
            clientId = client.id,
            photoUri = file.absolutePath,
            captureType = captureType,
            followUpInterval = intervalStr,
            upperLipColorHex = dualAnalysis?.upperLip?.dominantColorHex,
            upperLipCategory = dualAnalysis?.upperLip?.category?.name,
            upperLipHue = dualAnalysis?.upperLip?.hue,
            upperLipSaturation = dualAnalysis?.upperLip?.saturation,
            upperLipValue = dualAnalysis?.upperLip?.value,
            lowerLipColorHex = dualAnalysis?.lowerLip?.dominantColorHex,
            lowerLipCategory = dualAnalysis?.lowerLip?.category?.name,
            lowerLipHue = dualAnalysis?.lowerLip?.hue,
            lowerLipSaturation = dualAnalysis?.lowerLip?.saturation,
            lowerLipValue = dualAnalysis?.lowerLip?.value
        )
        val photoId = lipPhotoRepository.insertPhoto(lipPhoto)

        // Save pigment recommendations for each zone
        if (upperRecs.isNotEmpty()) {
            val selected = upperRecs.getOrNull(selectedUpperIdx) ?: upperRecs.first()
            lipPhotoRepository.insertPigment(
                LipPhotoPigment(
                    lipPhotoId = photoId,
                    lipZone = LipZone.UPPER,
                    pigmentName = selected.pigment.name,
                    pigmentBrand = selected.pigment.brand.displayName,
                    pigmentColorHex = selected.pigment.colorHex,
                    isRecommended = true
                )
            )
        }

        if (lowerRecs.isNotEmpty()) {
            val selected = lowerRecs.getOrNull(selectedLowerIdx) ?: lowerRecs.first()
            lipPhotoRepository.insertPigment(
                LipPhotoPigment(
                    lipPhotoId = photoId,
                    lipZone = LipZone.LOWER,
                    pigmentName = selected.pigment.name,
                    pigmentBrand = selected.pigment.brand.displayName,
                    pigmentColorHex = selected.pigment.colorHex,
                    isRecommended = true
                )
            )
        }

        return photoId
    }

    suspend fun saveDemoPhoto(
        bitmap: Bitmap,
        rotation: Int,
        isFrontCamera: Boolean
    ): String {
        val rotatedBitmap = rotateBitmapForSaving(bitmap, rotation, isFrontCamera)
        val photoDir = File(application.filesDir, "demo_photos")
        if (!photoDir.exists()) photoDir.mkdirs()
        val file = File(photoDir, "demo_${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }
}
