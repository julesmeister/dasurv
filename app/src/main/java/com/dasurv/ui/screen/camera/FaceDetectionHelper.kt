package com.dasurv.ui.screen.camera

import android.graphics.Bitmap
import com.dasurv.data.model.DualLipAnalysis
import com.dasurv.util.LipColorAnalyzer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import java.io.Closeable

internal class FaceDetectionHelper(
    private val lipColorAnalyzer: LipColorAnalyzer
) : Closeable {

    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setMinFaceSize(0.1f)
            .build()
        FaceDetection.getClient(options)
    }

    override fun close() {
        try { detector.close() } catch (_: Exception) {}
    }

    /**
     * Detect a face in the bitmap and optionally run lip-color analysis.
     * @param existingAnalysis if non-null, lip-color analysis is skipped.
     * @return (face, dualAnalysis) — either may be null.
     */
    suspend fun detectFaceAndAnalyze(
        bitmap: Bitmap,
        existingAnalysis: DualLipAnalysis?
    ): Pair<Face?, DualLipAnalysis?> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val faces = detector.process(inputImage).await()

            if (faces.isEmpty()) return Pair(null, existingAnalysis)

            val face = faces[0]

            if (existingAnalysis != null) {
                return Pair(face, existingAnalysis)
            }

            // Run lip color analysis
            val dual = lipColorAnalyzer.analyzeDualLipColor(bitmap, face)
            if (dual != null) {
                Pair(face, dual)
            } else {
                // Fallback to landmark-based
                val single = lipColorAnalyzer.analyzeLipColor(bitmap, face)
                if (single != null) {
                    Pair(face, DualLipAnalysis(upperLip = single, lowerLip = single))
                } else {
                    Pair(face, null)
                }
            }
        } catch (_: Exception) {
            Pair(null, existingAnalysis)
        }
    }
}
