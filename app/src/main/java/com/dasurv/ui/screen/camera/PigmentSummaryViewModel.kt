package com.dasurv.ui.screen.camera

import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.local.entity.LipPhoto
import com.dasurv.data.local.entity.LipZone
import com.dasurv.data.model.DualLipAnalysis
import com.dasurv.data.model.LipColorAnalysis
import com.dasurv.data.model.LipColorCategory
import com.dasurv.data.model.Pigment
import com.dasurv.data.repository.LipPhotoRepository
import com.dasurv.data.repository.PigmentRepository
import com.dasurv.util.ColorMatcher
import com.dasurv.util.HealingGuide
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@HiltViewModel
class PigmentSummaryViewModel @Inject constructor(
    private val lipPhotoRepository: LipPhotoRepository,
    private val pigmentRepository: PigmentRepository,
    private val colorMatcher: ColorMatcher,
    private val healingGuide: HealingGuide
) : ViewModel() {

    private val _photoUri = MutableStateFlow<String?>(null)
    val photoUri: StateFlow<String?> = _photoUri

    private val _dualAnalysis = MutableStateFlow<DualLipAnalysis?>(null)
    val dualAnalysis: StateFlow<DualLipAnalysis?> = _dualAnalysis

    private val _upperPigment = MutableStateFlow<Pigment?>(null)
    val upperPigment: StateFlow<Pigment?> = _upperPigment

    private val _lowerPigment = MutableStateFlow<Pigment?>(null)
    val lowerPigment: StateFlow<Pigment?> = _lowerPigment

    private val _upperPrediction = MutableStateFlow<HealingGuide.HealingPrediction?>(null)
    val upperPrediction: StateFlow<HealingGuide.HealingPrediction?> = _upperPrediction

    private val _lowerPrediction = MutableStateFlow<HealingGuide.HealingPrediction?>(null)
    val lowerPrediction: StateFlow<HealingGuide.HealingPrediction?> = _lowerPrediction

    private val _blendedUpperHex = MutableStateFlow<String?>(null)
    val blendedUpperHex: StateFlow<String?> = _blendedUpperHex

    private val _blendedLowerHex = MutableStateFlow<String?>(null)
    val blendedLowerHex: StateFlow<String?> = _blendedLowerHex

    private val _captureType = MutableStateFlow(CaptureType.BEFORE)
    val captureType: StateFlow<CaptureType> = _captureType

    private val _followUpInterval = MutableStateFlow<String?>(null)
    val followUpInterval: StateFlow<String?> = _followUpInterval

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadPhoto(photoId: Long) {
        if (photoId <= 0) {
            loadFromTransientData()
        } else {
            loadFromDatabase(photoId)
        }
    }

    private fun loadFromTransientData() {
        viewModelScope.launch {
            _isLoading.value = true

            val data = PigmentSummaryData
            _photoUri.value = data.photoUri
            _dualAnalysis.value = data.dualAnalysis
            _upperPigment.value = data.upperPigment
            _lowerPigment.value = data.lowerPigment
            _captureType.value = data.captureType
            _followUpInterval.value = data.followUpInterval

            computePredictions(data.dualAnalysis, data.upperIntensity, data.lowerIntensity)

            PigmentSummaryData.clear()
            _isLoading.value = false
        }
    }

    private fun loadFromDatabase(photoId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val lipPhoto = lipPhotoRepository.getPhotoById(photoId) ?: run {
                _isLoading.value = false
                return@launch
            }
            _photoUri.value = lipPhoto.photoUri
            _captureType.value = lipPhoto.captureType
            _followUpInterval.value = lipPhoto.followUpInterval

            val dual = reconstructAnalysis(lipPhoto)
            _dualAnalysis.value = dual

            val savedPigments = lipPhotoRepository.getPigmentsForPhotoOnce(photoId)
            val allPigments = pigmentRepository.getAllPigments()

            for (saved in savedPigments) {
                val matched = allPigments.find {
                    it.name == saved.pigmentName && it.brand.displayName == saved.pigmentBrand
                } ?: continue
                when (saved.lipZone) {
                    LipZone.UPPER -> _upperPigment.value = matched
                    LipZone.LOWER -> _lowerPigment.value = matched
                }
            }

            computePredictions(dual, 0.6f, 0.6f)
            _isLoading.value = false
        }
    }

    private fun computePredictions(dual: DualLipAnalysis?, upperIntensity: Float, lowerIntensity: Float) {
        _upperPigment.value?.let { pigment ->
            dual?.upperLip?.let { analysis ->
                _blendedUpperHex.value = colorMatcher.blendPigmentResult(
                    analysis.dominantColorHex, pigment.colorHex, upperIntensity
                )
                _upperPrediction.value = healingGuide.predict(
                    analysis.dominantColorHex, pigment, analysis.category, upperIntensity
                )
            }
        }

        _lowerPigment.value?.let { pigment ->
            dual?.lowerLip?.let { analysis ->
                _blendedLowerHex.value = colorMatcher.blendPigmentResult(
                    analysis.dominantColorHex, pigment.colorHex, lowerIntensity
                )
                _lowerPrediction.value = healingGuide.predict(
                    analysis.dominantColorHex, pigment, analysis.category, lowerIntensity
                )
            }
        }
    }

    private fun reconstructAnalysis(lipPhoto: LipPhoto): DualLipAnalysis? {
        fun parseCategory(cat: String?) = try {
            LipColorCategory.valueOf(cat!!)
        } catch (_: Exception) {
            LipColorCategory.MEDIUM_PINK
        }

        val upper = if (lipPhoto.upperLipColorHex != null && lipPhoto.upperLipCategory != null) {
            LipColorAnalysis(
                lipPhoto.upperLipColorHex, parseCategory(lipPhoto.upperLipCategory),
                lipPhoto.upperLipHue ?: 0f, lipPhoto.upperLipSaturation ?: 0f, lipPhoto.upperLipValue ?: 0f
            )
        } else null

        val lower = if (lipPhoto.lowerLipColorHex != null && lipPhoto.lowerLipCategory != null) {
            LipColorAnalysis(
                lipPhoto.lowerLipColorHex, parseCategory(lipPhoto.lowerLipCategory),
                lipPhoto.lowerLipHue ?: 0f, lipPhoto.lowerLipSaturation ?: 0f, lipPhoto.lowerLipValue ?: 0f
            )
        } else null

        return if (upper != null || lower != null) DualLipAnalysis(upper, lower) else null
    }
}
