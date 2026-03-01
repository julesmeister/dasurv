package com.dasurv.ui.screen.tryon

import android.graphics.Bitmap
import com.dasurv.data.local.entity.Client
import com.dasurv.data.model.DualLipAnalysis
import com.dasurv.data.model.Pigment
import com.dasurv.data.model.PigmentBrand
import com.dasurv.data.repository.ClientPigmentPreferenceRepository
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.PigmentRepository
import com.dasurv.util.LipColorAnalyzer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

enum class TryOnMode {
    LIVE_CAMERA, STATIC_PHOTO
}

@HiltViewModel
class ClientTryOnViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val pigmentRepository: PigmentRepository,
    private val preferenceRepository: ClientPigmentPreferenceRepository,
    private val lipColorAnalyzer: LipColorAnalyzer
) : ViewModel() {

    private val _mode = MutableStateFlow(TryOnMode.LIVE_CAMERA)
    val mode: StateFlow<TryOnMode> = _mode

    private val _client = MutableStateFlow<Client?>(null)
    val client: StateFlow<Client?> = _client

    private val _selectedBrand = MutableStateFlow<PigmentBrand?>(null)
    val selectedBrand: StateFlow<PigmentBrand?> = _selectedBrand

    private val _allPigments = MutableStateFlow<List<Pigment>>(emptyList())

    private val _pigments = MutableStateFlow<List<Pigment>>(emptyList())
    val pigments: StateFlow<List<Pigment>> = _pigments

    private val _selectedPigment = MutableStateFlow<Pigment?>(null)
    val selectedPigment: StateFlow<Pigment?> = _selectedPigment

    private val _clientId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val favorites: StateFlow<Set<String>> = _clientId
        .filterNotNull()
        .flatMapLatest { preferenceRepository.getPreferencesForClient(it) }
        .map { prefs -> prefs.map { "${it.pigmentName}|${it.pigmentBrand}" }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly

    init {
        // Re-apply filters whenever favorites change
        viewModelScope.launch {
            favorites.collect { applyFilters() }
        }
    }

    private val _detectedFace = MutableStateFlow<Face?>(null)
    val detectedFace: StateFlow<Face?> = _detectedFace

    private val _staticBitmap = MutableStateFlow<Bitmap?>(null)
    val staticBitmap: StateFlow<Bitmap?> = _staticBitmap

    private val _imageWidth = MutableStateFlow(480)
    val imageWidth: StateFlow<Int> = _imageWidth

    private val _imageHeight = MutableStateFlow(640)
    val imageHeight: StateFlow<Int> = _imageHeight

    private val _dualAnalysis = MutableStateFlow<DualLipAnalysis?>(null)
    val dualAnalysis: StateFlow<DualLipAnalysis?> = _dualAnalysis

    private val _detectionError = MutableStateFlow<String?>(null)
    val detectionError: StateFlow<String?> = _detectionError

    fun loadClient(clientId: Long) {
        _clientId.value = clientId
        viewModelScope.launch {
            _client.value = clientRepository.getClientById(clientId)
            _allPigments.value = pigmentRepository.getAllPigments()
            applyFilters()
        }
    }

    fun selectBrand(brand: PigmentBrand?) {
        _selectedBrand.value = brand
        applyFilters()
    }

    fun selectPigment(pigment: Pigment?) {
        _selectedPigment.value = pigment
    }

    fun toggleShowFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
        applyFilters()
    }

    fun toggleFavorite(pigment: Pigment) {
        val clientId = _client.value?.id ?: return
        viewModelScope.launch {
            preferenceRepository.togglePreference(clientId, pigment)
        }
    }

    fun onFaceDetected(face: Face, bitmap: Bitmap) {
        _detectedFace.value = face
        _imageWidth.value = bitmap.width
        _imageHeight.value = bitmap.height
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val dual = lipColorAnalyzer.analyzeDualLipColor(bitmap, face)
            if (dual != null) {
                _dualAnalysis.value = dual
            }
        }
    }

    fun updateImageDimensions(width: Int, height: Int) {
        _imageWidth.value = width
        _imageHeight.value = height
    }

    fun setMode(mode: TryOnMode) {
        _mode.value = mode
        if (mode == TryOnMode.LIVE_CAMERA) {
            _staticBitmap.value = null
            _detectedFace.value = null
        }
    }

    fun analyzeStaticPhoto(bitmap: Bitmap) {
        _mode.value = TryOnMode.STATIC_PHOTO
        _staticBitmap.value = bitmap
        _detectedFace.value = null
        _imageWidth.value = bitmap.width
        _imageHeight.value = bitmap.height

        viewModelScope.launch {
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val options = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                    .setMinFaceSize(0.15f)
                    .build()
                val detector = FaceDetection.getClient(options)
                val faces = detector.process(inputImage).await()
                if (faces.isNotEmpty()) {
                    _detectedFace.value = faces[0]
                }
                detector.close()
            } catch (_: Exception) {
                _detectionError.value = "No face detected in photo"
            }
        }
    }

    fun dismissDetectionError() {
        _detectionError.value = null
    }

    fun isFavorite(pigment: Pigment): Boolean {
        return "${pigment.name}|${pigment.brand.displayName}" in favorites.value
    }

    private fun applyFilters() {
        var filtered = _allPigments.value
        val brand = _selectedBrand.value
        if (brand != null) {
            filtered = filtered.filter { it.brand == brand }
        }
        if (_showFavoritesOnly.value) {
            filtered = filtered.filter { "${it.name}|${it.brand.displayName}" in favorites.value }
        }
        _pigments.value = filtered
    }
}
