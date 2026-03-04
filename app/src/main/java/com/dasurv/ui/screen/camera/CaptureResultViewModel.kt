package com.dasurv.ui.screen.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.LipPhoto
import com.dasurv.data.local.entity.LipPhotoPigment
import com.dasurv.data.local.entity.LipZone
import com.dasurv.data.model.DualLipAnalysis
import com.dasurv.data.model.LipColorAnalysis
import com.dasurv.data.model.LipColorCategory
import com.dasurv.data.model.Pigment
import com.dasurv.data.model.PigmentBrand
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.LipPhotoRepository
import com.dasurv.data.repository.PigmentRepository
import com.dasurv.util.ColorMatcher
import com.dasurv.util.DefaultSubscribePolicy
import com.dasurv.util.LipColorAnalyzer
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CaptureResultViewModel @Inject constructor(
    private val lipPhotoRepository: LipPhotoRepository,
    private val pigmentRepository: PigmentRepository,
    private val clientRepository: ClientRepository,
    private val colorMatcher: ColorMatcher,
    private val lipColorAnalyzer: LipColorAnalyzer,
    private val application: Application
) : ViewModel() {

    private val faceDetectionHelper = FaceDetectionHelper(lipColorAnalyzer)

    private fun decodeBitmapDownsampled(path: String, maxDim: Int = 1920): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        var sampleSize = 1
        while (maxOf(options.outWidth, options.outHeight) / sampleSize > maxDim) sampleSize *= 2
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return BitmapFactory.decodeFile(path, decodeOptions)
    }

    override fun onCleared() {
        super.onCleared()
        faceDetectionHelper.close()
    }

    private val _photo = MutableStateFlow<LipPhoto?>(null)
    val photo: StateFlow<LipPhoto?> = _photo
    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap: StateFlow<Bitmap?> = _bitmap
    private val _dualAnalysis = MutableStateFlow<DualLipAnalysis?>(null)
    val dualAnalysis: StateFlow<DualLipAnalysis?> = _dualAnalysis
    private val _upperRecommendations = MutableStateFlow<List<ColorMatcher.PigmentRecommendation>>(emptyList())
    val upperRecommendations: StateFlow<List<ColorMatcher.PigmentRecommendation>> = _upperRecommendations
    private val _lowerRecommendations = MutableStateFlow<List<ColorMatcher.PigmentRecommendation>>(emptyList())
    val lowerRecommendations: StateFlow<List<ColorMatcher.PigmentRecommendation>> = _lowerRecommendations
    private val _selectedUpperPigment = MutableStateFlow<Pigment?>(null)
    val selectedUpperPigment: StateFlow<Pigment?> = _selectedUpperPigment
    private val _selectedLowerPigment = MutableStateFlow<Pigment?>(null)
    val selectedLowerPigment: StateFlow<Pigment?> = _selectedLowerPigment
    private val _activeZone = MutableStateFlow(LipZone.UPPER)
    val activeZone: StateFlow<LipZone> = _activeZone
    private val _upperIntensity = MutableStateFlow(0.6f)
    val upperIntensity: StateFlow<Float> = _upperIntensity
    private val _lowerIntensity = MutableStateFlow(0.6f)
    val lowerIntensity: StateFlow<Float> = _lowerIntensity
    private val _blendedUpperHex = MutableStateFlow<String?>(null)
    val blendedUpperHex: StateFlow<String?> = _blendedUpperHex
    private val _blendedLowerHex = MutableStateFlow<String?>(null)
    val blendedLowerHex: StateFlow<String?> = _blendedLowerHex
    private val _detectedFace = MutableStateFlow<Face?>(null)
    val detectedFace: StateFlow<Face?> = _detectedFace
    private val _allPigments = MutableStateFlow<List<Pigment>>(emptyList())
    val allPigments: StateFlow<List<Pigment>> = _allPigments
    private val _selectedBrand = MutableStateFlow<PigmentBrand?>(null)
    val selectedBrand: StateFlow<PigmentBrand?> = _selectedBrand
    private val _filteredPigments = MutableStateFlow<List<Pigment>>(emptyList())
    val filteredPigments: StateFlow<List<Pigment>> = _filteredPigments
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _upperLipScale = MutableStateFlow(1.0f)
    val upperLipScale: StateFlow<Float> = _upperLipScale
    private val _lowerLipScale = MutableStateFlow(1.0f)
    val lowerLipScale: StateFlow<Float> = _lowerLipScale
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved
    private val _arOverlayVisible = MutableStateFlow(true)
    val arOverlayVisible: StateFlow<Boolean> = _arOverlayVisible
    private val _isCropMode = MutableStateFlow(false)
    val isCropMode: StateFlow<Boolean> = _isCropMode
    private val _isColorPickerMode = MutableStateFlow(false)
    val isColorPickerMode: StateFlow<Boolean> = _isColorPickerMode

    // Client picker state for demo photos
    private val _clientSearchQuery = MutableStateFlow("")
    val clientSearchQuery: StateFlow<String> = _clientSearchQuery

    val clientSearchResults: StateFlow<List<Client>> = _clientSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) clientRepository.getAllClients()
            else clientRepository.searchClients("%$query%")
        }
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    // Tracks the demo photo path for saveDemoAsClientPhoto
    private var demoPhotoPath: String? = null

    fun loadPhoto(photoId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            val lipPhoto = lipPhotoRepository.getPhotoById(photoId) ?: run {
                _isLoading.value = false
                return@launch
            }
            _photo.value = lipPhoto
            _upperLipScale.value = lipPhoto.upperLipScale
            _lowerLipScale.value = lipPhoto.lowerLipScale
            _allPigments.value = pigmentRepository.getAllPigments()
            _filteredPigments.value = _allPigments.value

            val bmp = decodeBitmapDownsampled(lipPhoto.photoUri)
            if (bmp != null) {
                _bitmap.value = bmp
                reconstructAnalysis(lipPhoto)?.let { dual ->
                    _dualAnalysis.value = dual
                    dual.upperLip?.let { _upperRecommendations.value = colorMatcher.getCorrectiveRecommendations(it) }
                    dual.lowerLip?.let { _lowerRecommendations.value = colorMatcher.getCorrectiveRecommendations(it) }
                }
                detectFaceAndAnalyze(bmp)
                restoreSavedPigments(photoId)
            }
            _isLoading.value = false
        }
    }

    private fun reconstructAnalysis(lipPhoto: LipPhoto): DualLipAnalysis? {
        fun parseCategory(cat: String?) = try { LipColorCategory.valueOf(cat!!) } catch (_: Exception) { LipColorCategory.MEDIUM_PINK }
        val upper = if (lipPhoto.upperLipColorHex != null && lipPhoto.upperLipCategory != null) {
            LipColorAnalysis(lipPhoto.upperLipColorHex, parseCategory(lipPhoto.upperLipCategory),
                lipPhoto.upperLipHue ?: 0f, lipPhoto.upperLipSaturation ?: 0f, lipPhoto.upperLipValue ?: 0f)
        } else null
        val lower = if (lipPhoto.lowerLipColorHex != null && lipPhoto.lowerLipCategory != null) {
            LipColorAnalysis(lipPhoto.lowerLipColorHex, parseCategory(lipPhoto.lowerLipCategory),
                lipPhoto.lowerLipHue ?: 0f, lipPhoto.lowerLipSaturation ?: 0f, lipPhoto.lowerLipValue ?: 0f)
        } else null
        return if (upper != null || lower != null) DualLipAnalysis(upper, lower) else null
    }

    private suspend fun restoreSavedPigments(photoId: Long) {
        val savedPigments = lipPhotoRepository.getPigmentsForPhotoOnce(photoId)
        if (savedPigments.isEmpty()) return
        val allPigs = _allPigments.value
        for (saved in savedPigments) {
            val matched = allPigs.find { it.name == saved.pigmentName && it.brand.displayName == saved.pigmentBrand }
                ?: continue
            when (saved.lipZone) {
                LipZone.UPPER -> _selectedUpperPigment.value = matched
                LipZone.LOWER -> _selectedLowerPigment.value = matched
            }
        }
        recomputeBlendedColors()
        _isSaved.value = true
    }

    fun loadDemoPhoto(photoPath: String) {
        demoPhotoPath = photoPath
        viewModelScope.launch {
            _isLoading.value = true
            _allPigments.value = pigmentRepository.getAllPigments()
            _filteredPigments.value = _allPigments.value
            val bmp = decodeBitmapDownsampled(photoPath)
            if (bmp != null) {
                _bitmap.value = bmp
                detectFaceAndAnalyze(bmp)
            }
            _isLoading.value = false
        }
    }

    private suspend fun detectFaceAndAnalyze(bitmap: Bitmap) {
        val (face, analysis) = faceDetectionHelper.detectFaceAndAnalyze(bitmap, _dualAnalysis.value)
        if (face != null) _detectedFace.value = face
        if (analysis != null && _dualAnalysis.value == null) {
            _dualAnalysis.value = analysis
            analysis.upperLip?.let { _upperRecommendations.value = colorMatcher.getCorrectiveRecommendations(it) }
            analysis.lowerLip?.let { _lowerRecommendations.value = colorMatcher.getCorrectiveRecommendations(it) }
        }
    }

    fun setActiveZone(zone: LipZone) { _activeZone.value = zone }

    fun selectPigment(pigment: Pigment) {
        when (_activeZone.value) {
            LipZone.UPPER -> _selectedUpperPigment.value = pigment
            LipZone.LOWER -> _selectedLowerPigment.value = pigment
        }
        _isSaved.value = false
        recomputeBlendedColors()
    }

    fun clearPigment() {
        when (_activeZone.value) {
            LipZone.UPPER -> { _selectedUpperPigment.value = null; _blendedUpperHex.value = null }
            LipZone.LOWER -> { _selectedLowerPigment.value = null; _blendedLowerHex.value = null }
        }
        _isSaved.value = false
    }

    fun clearAllPigments() {
        _selectedUpperPigment.value = null; _selectedLowerPigment.value = null
        _blendedUpperHex.value = null; _blendedLowerHex.value = null
        _isSaved.value = false
    }

    fun setIntensity(value: Float) {
        when (_activeZone.value) {
            LipZone.UPPER -> _upperIntensity.value = value
            LipZone.LOWER -> _lowerIntensity.value = value
        }
        _isSaved.value = false
        recomputeBlendedColors()
    }

    private fun recomputeBlendedColors() {
        val dual = _dualAnalysis.value ?: return
        _blendedUpperHex.value = _selectedUpperPigment.value?.let { p ->
            dual.upperLip?.let { colorMatcher.blendPigmentResult(it.dominantColorHex, p.colorHex, _upperIntensity.value) }
        }
        _blendedLowerHex.value = _selectedLowerPigment.value?.let { p ->
            dual.lowerLip?.let { colorMatcher.blendPigmentResult(it.dominantColorHex, p.colorHex, _lowerIntensity.value) }
        }
    }

    fun selectBrand(brand: PigmentBrand?) {
        _selectedBrand.value = brand
        _filteredPigments.value = if (brand != null) _allPigments.value.filter { it.brand == brand } else _allPigments.value
    }

    fun setLipScale(zone: LipZone, scale: Float) {
        val clamped = scale.coerceIn(0.5f, 2.0f)
        when (zone) {
            LipZone.UPPER -> _upperLipScale.value = clamped
            LipZone.LOWER -> _lowerLipScale.value = clamped
        }
        // Persist to DB
        _photo.value?.let { photo ->
            viewModelScope.launch {
                val updated = photo.copy(
                    upperLipScale = _upperLipScale.value,
                    lowerLipScale = _lowerLipScale.value
                )
                _photo.value = updated
                lipPhotoRepository.updatePhoto(updated)
            }
        }
    }

    fun saveSelectedPigments() {
        val photoId = _photo.value?.id ?: return
        viewModelScope.launch {
            lipPhotoRepository.deletePigmentsForPhoto(photoId)
            listOf(
                _selectedUpperPigment.value to LipZone.UPPER,
                _selectedLowerPigment.value to LipZone.LOWER
            ).forEach { (pigment, zone) ->
                pigment?.let {
                    lipPhotoRepository.insertPigment(LipPhotoPigment(
                        lipPhotoId = photoId, lipZone = zone,
                        pigmentName = it.name, pigmentBrand = it.brand.displayName, pigmentColorHex = it.colorHex
                    ))
                }
            }
            _isSaved.value = true
        }
    }

    /** Save a demo photo as a client photo, returning the new photoId or -1 on failure. */
    suspend fun saveDemoAsClientPhoto(client: Client): Long {
        val bmp = _bitmap.value ?: return -1
        val srcPath = demoPhotoPath ?: return -1
        val dual = _dualAnalysis.value

        // Copy image to client's photo directory
        val clientDir = File(application.filesDir, "client_photos/${client.id}")
        clientDir.mkdirs()
        val destFile = File(clientDir, "lip_${System.currentTimeMillis()}.jpg")
        try {
            FileOutputStream(destFile).use { bmp.compress(Bitmap.CompressFormat.JPEG, 95, it) }
        } catch (_: Exception) {
            return -1
        }

        val lipPhoto = LipPhoto(
            clientId = client.id,
            photoUri = destFile.absolutePath,
            captureType = CaptureType.BEFORE,
            upperLipColorHex = dual?.upperLip?.dominantColorHex,
            upperLipCategory = dual?.upperLip?.category?.name,
            upperLipHue = dual?.upperLip?.hue,
            upperLipSaturation = dual?.upperLip?.saturation,
            upperLipValue = dual?.upperLip?.value,
            lowerLipColorHex = dual?.lowerLip?.dominantColorHex,
            lowerLipCategory = dual?.lowerLip?.category?.name,
            lowerLipHue = dual?.lowerLip?.hue,
            lowerLipSaturation = dual?.lowerLip?.saturation,
            lowerLipValue = dual?.lowerLip?.value
        )
        val photoId = lipPhotoRepository.insertPhoto(lipPhoto)
        _photo.value = lipPhoto.copy(id = photoId)

        // Save selected pigments to the new photo
        listOf(
            _selectedUpperPigment.value to LipZone.UPPER,
            _selectedLowerPigment.value to LipZone.LOWER
        ).forEach { (pigment, zone) ->
            pigment?.let {
                lipPhotoRepository.insertPigment(LipPhotoPigment(
                    lipPhotoId = photoId, lipZone = zone,
                    pigmentName = it.name, pigmentBrand = it.brand.displayName, pigmentColorHex = it.colorHex
                ))
            }
        }
        _isSaved.value = true
        return photoId
    }

    // Client picker
    fun loadAllClients() {
        _clientSearchQuery.value = ""
    }

    fun updateClientSearch(query: String) {
        _clientSearchQuery.value = query
    }

    fun toggleArOverlay() { _arOverlayVisible.value = !_arOverlayVisible.value }
    fun enterCropMode() { _isCropMode.value = true }
    fun exitCropMode() { _isCropMode.value = false }
    fun enterColorPickerMode() { _isColorPickerMode.value = true }
    fun exitColorPickerMode() { _isColorPickerMode.value = false }

    fun applyPickedColor(hexColor: String) {
        _isColorPickerMode.value = false
        val analysis = lipColorAnalyzer.analyzeFromHex(hexColor)
        val zone = _activeZone.value
        val current = _dualAnalysis.value
        _dualAnalysis.value = when (zone) {
            LipZone.UPPER -> DualLipAnalysis(analysis, current?.lowerLip)
            LipZone.LOWER -> DualLipAnalysis(current?.upperLip, analysis)
        }
        when (zone) {
            LipZone.UPPER -> _upperRecommendations.value = colorMatcher.getCorrectiveRecommendations(analysis)
            LipZone.LOWER -> _lowerRecommendations.value = colorMatcher.getCorrectiveRecommendations(analysis)
        }
        recomputeBlendedColors()
    }

    fun rotateBitmap90(clockwise: Boolean) {
        val bmp = _bitmap.value ?: return
        viewModelScope.launch {
            val matrix = android.graphics.Matrix().apply {
                postRotate(if (clockwise) 90f else -90f)
            }
            val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
            _bitmap.value = rotated
            if (rotated !== bmp) bmp.recycle()
            _detectedFace.value = null
            detectFaceAndAnalyze(rotated)
            persistBitmapAndAnalysis(rotated)
        }
    }

    fun applyCrop(cropRect: Rect) {
        val bmp = _bitmap.value ?: return
        viewModelScope.launch {
            val l = cropRect.left.coerceIn(0, bmp.width - 1)
            val t = cropRect.top.coerceIn(0, bmp.height - 1)
            val cropped = Bitmap.createBitmap(bmp, l, t,
                cropRect.width().coerceIn(1, bmp.width - l),
                cropRect.height().coerceIn(1, bmp.height - t))
            _bitmap.value = cropped
            if (cropped !== bmp) bmp.recycle()
            _detectedFace.value = null
            _isCropMode.value = false
            detectFaceAndAnalyze(cropped)
            persistBitmapAndAnalysis(cropped)
        }
    }

    private suspend fun persistBitmapAndAnalysis(bitmap: Bitmap) {
        val photo = _photo.value ?: return
        // Save bitmap to file
        try {
            FileOutputStream(File(photo.photoUri)).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
            }
        } catch (_: Exception) { }

        // Update the DB record with current analysis
        val dual = _dualAnalysis.value
        val updated = photo.copy(
            upperLipColorHex = dual?.upperLip?.dominantColorHex,
            upperLipCategory = dual?.upperLip?.category?.name,
            upperLipHue = dual?.upperLip?.hue,
            upperLipSaturation = dual?.upperLip?.saturation,
            upperLipValue = dual?.upperLip?.value,
            lowerLipColorHex = dual?.lowerLip?.dominantColorHex,
            lowerLipCategory = dual?.lowerLip?.category?.name,
            lowerLipHue = dual?.lowerLip?.hue,
            lowerLipSaturation = dual?.lowerLip?.saturation,
            lowerLipValue = dual?.lowerLip?.value,
            upperLipScale = _upperLipScale.value,
            lowerLipScale = _lowerLipScale.value
        )
        _photo.value = updated
        lipPhotoRepository.updatePhoto(updated)
    }
}
