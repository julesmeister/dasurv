package com.dasurv.ui.screen.camera

import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.model.DualLipAnalysis
import com.dasurv.data.model.Pigment

/**
 * Transient holder for passing pigment summary data to the summary screen
 * when no database record exists (e.g. demo photos).
 * Cleared after consumption.
 */
object PigmentSummaryData {
    var photoUri: String? = null
    var dualAnalysis: DualLipAnalysis? = null
    var upperPigment: Pigment? = null
    var lowerPigment: Pigment? = null
    var upperIntensity: Float = 0.6f
    var lowerIntensity: Float = 0.6f
    var captureType: CaptureType = CaptureType.BEFORE
    var followUpInterval: String? = null

    fun populate(
        photoUri: String?,
        dualAnalysis: DualLipAnalysis?,
        upperPigment: Pigment?,
        lowerPigment: Pigment?,
        upperIntensity: Float,
        lowerIntensity: Float,
        captureType: CaptureType = CaptureType.BEFORE,
        followUpInterval: String? = null
    ) {
        this.photoUri = photoUri
        this.dualAnalysis = dualAnalysis
        this.upperPigment = upperPigment
        this.lowerPigment = lowerPigment
        this.upperIntensity = upperIntensity
        this.lowerIntensity = lowerIntensity
        this.captureType = captureType
        this.followUpInterval = followUpInterval
    }

    fun clear() {
        photoUri = null
        dualAnalysis = null
        upperPigment = null
        lowerPigment = null
        upperIntensity = 0.6f
        lowerIntensity = 0.6f
        captureType = CaptureType.BEFORE
        followUpInterval = null
    }
}
