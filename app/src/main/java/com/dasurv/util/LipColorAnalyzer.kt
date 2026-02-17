package com.dasurv.util

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import com.dasurv.data.model.DualLipAnalysis
import com.dasurv.data.model.LipColorAnalysis
import com.dasurv.data.model.LipColorCategory
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceLandmark
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LipColorAnalyzer @Inject constructor() {

    fun analyzeLipColor(bitmap: Bitmap, face: Face): LipColorAnalysis? {
        val mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT)?.position ?: return null
        val mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT)?.position ?: return null
        val mouthBottom = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position ?: return null
        val noseBase = face.getLandmark(FaceLandmark.NOSE_BASE)?.position

        val centerX = ((mouthLeft.x + mouthRight.x) / 2).toInt()
        val centerY = if (noseBase != null) {
            ((noseBase.y + mouthBottom.y) / 2).toInt()
        } else {
            (mouthBottom.y - (mouthBottom.y - mouthLeft.y) * 0.3f).toInt()
        }

        val lipWidth = ((mouthRight.x - mouthLeft.x) * 0.4f).toInt()
        val lipHeight = (lipWidth * 0.4f).toInt()

        val left = (centerX - lipWidth / 2).coerceIn(0, bitmap.width - 1)
        val top = (centerY - lipHeight / 2).coerceIn(0, bitmap.height - 1)
        val right = (centerX + lipWidth / 2).coerceIn(0, bitmap.width - 1)
        val bottom = (centerY + lipHeight / 2).coerceIn(0, bitmap.height - 1)

        if (right <= left || bottom <= top) return null

        return extractDominantColor(bitmap, left, top, right, bottom)
    }

    fun analyzeDualLipColor(bitmap: Bitmap, face: Face): DualLipAnalysis? {
        val upperTop = face.getContour(FaceContour.UPPER_LIP_TOP)?.points
        val upperBottom = face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points
        val lowerTop = face.getContour(FaceContour.LOWER_LIP_TOP)?.points
        val lowerBottom = face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.points

        if (upperTop == null && lowerBottom == null) return null

        val upperAnalysis = if (upperTop != null && upperBottom != null) {
            extractColorFromContourRegion(bitmap, upperTop, upperBottom)
        } else null

        val lowerAnalysis = if (lowerTop != null && lowerBottom != null) {
            extractColorFromContourRegion(bitmap, lowerTop, lowerBottom)
        } else null

        if (upperAnalysis == null && lowerAnalysis == null) return null
        return DualLipAnalysis(upperLip = upperAnalysis, lowerLip = lowerAnalysis)
    }

    private fun extractColorFromContourRegion(
        bitmap: Bitmap,
        topPoints: List<PointF>,
        bottomPoints: List<PointF>
    ): LipColorAnalysis? {
        val allPoints = topPoints + bottomPoints
        if (allPoints.isEmpty()) return null

        val minX = allPoints.minOf { it.x }.toInt().coerceIn(0, bitmap.width - 1)
        val maxX = allPoints.maxOf { it.x }.toInt().coerceIn(0, bitmap.width - 1)
        val minY = allPoints.minOf { it.y }.toInt().coerceIn(0, bitmap.height - 1)
        val maxY = allPoints.maxOf { it.y }.toInt().coerceIn(0, bitmap.height - 1)

        if (maxX <= minX || maxY <= minY) return null

        // Sample from the center 60% of the bounding box for more accurate color
        val padX = ((maxX - minX) * 0.2f).toInt()
        val padY = ((maxY - minY) * 0.2f).toInt()
        val sampleLeft = (minX + padX).coerceIn(0, bitmap.width - 1)
        val sampleRight = (maxX - padX).coerceIn(0, bitmap.width - 1)
        val sampleTop = (minY + padY).coerceIn(0, bitmap.height - 1)
        val sampleBottom = (maxY - padY).coerceIn(0, bitmap.height - 1)

        if (sampleRight <= sampleLeft || sampleBottom <= sampleTop) return null

        return extractDominantColor(bitmap, sampleLeft, sampleTop, sampleRight, sampleBottom)
    }

    private fun extractDominantColor(
        bitmap: Bitmap, left: Int, top: Int, right: Int, bottom: Int
    ): LipColorAnalysis {
        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        var count = 0

        val step = 2
        for (x in left until right step step) {
            for (y in top until bottom step step) {
                val pixel = bitmap.getPixel(x, y)
                totalR += Color.red(pixel)
                totalG += Color.green(pixel)
                totalB += Color.blue(pixel)
                count++
            }
        }

        if (count == 0) count = 1
        val avgR = (totalR / count).toInt()
        val avgG = (totalG / count).toInt()
        val avgB = (totalB / count).toInt()

        val hsv = FloatArray(3)
        Color.RGBToHSV(avgR, avgG, avgB, hsv)

        val hex = String.format("#%02X%02X%02X", avgR, avgG, avgB)
        val category = classifyLipColor(hsv[0], hsv[1], hsv[2])

        return LipColorAnalysis(
            dominantColorHex = hex,
            category = category,
            hue = hsv[0],
            saturation = hsv[1],
            value = hsv[2]
        )
    }

    fun analyzeFromHex(hex: String): LipColorAnalysis {
        val color = Color.parseColor(hex)
        val r = Color.red(color); val g = Color.green(color); val b = Color.blue(color)
        val hsv = FloatArray(3)
        Color.RGBToHSV(r, g, b, hsv)
        return LipColorAnalysis(hex, classifyLipColor(hsv[0], hsv[1], hsv[2]), hsv[0], hsv[1], hsv[2])
    }

    private fun classifyLipColor(hue: Float, saturation: Float, value: Float): LipColorCategory {
        return when {
            value < 0.25f -> LipColorCategory.PIGMENTED
            saturation < 0.15f && value > 0.7f -> LipColorCategory.PALE
            saturation < 0.2f -> LipColorCategory.PALE_PINK

            hue in 0f..15f || hue > 340f -> when {
                value < 0.4f -> LipColorCategory.DARK_RED
                saturation > 0.6f -> LipColorCategory.RED
                else -> LipColorCategory.ROSE
            }

            hue in 15f..40f -> when {
                value < 0.5f -> LipColorCategory.BROWN
                else -> LipColorCategory.MAUVE
            }

            hue in 300f..340f -> when {
                value > 0.75f && saturation < 0.3f -> LipColorCategory.LIGHT_PINK
                value > 0.6f -> LipColorCategory.MEDIUM_PINK
                saturation > 0.5f -> LipColorCategory.DARK_PINK
                else -> LipColorCategory.MAUVE
            }

            else -> LipColorCategory.MEDIUM_PINK
        }
    }
}
