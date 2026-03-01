package com.dasurv.ui.screen.camera

import android.graphics.Bitmap
import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.LipZone
import com.dasurv.data.model.DualLipAnalysis
import com.dasurv.data.model.FollowUpInterval
import com.dasurv.data.model.LipColorAnalysis
import com.dasurv.data.repository.ClientRepository
import com.dasurv.util.ColorMatcher
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
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LipCameraViewModel @Inject constructor(
    private val lipColorAnalyzer: LipColorAnalyzer,
    private val colorMatcher: ColorMatcher,
    private val photoCaptureHelper: PhotoCaptureHelper,
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val faceDetectionHelper = FaceDetectionHelper(lipColorAnalyzer)

    override fun onCleared() {
        super.onCleared()
        faceDetectionHelper.close()
    }

    private val _lipAnalysis = MutableStateFlow<LipColorAnalysis?>(null)
    val lipAnalysis: StateFlow<LipColorAnalysis?> = _lipAnalysis

    private val _dualAnalysis = MutableStateFlow<DualLipAnalysis?>(null)
    val dualAnalysis: StateFlow<DualLipAnalysis?> = _dualAnalysis

    private val _recommendations = MutableStateFlow<List<ColorMatcher.PigmentRecommendation>>(emptyList())
    val recommendations: StateFlow<List<ColorMatcher.PigmentRecommendation>> = _recommendations

    private val _detectedFace = MutableStateFlow<Face?>(null)
    val detectedFace: StateFlow<Face?> = _detectedFace

    // Client selection
    private val _selectedClient = MutableStateFlow<Client?>(null)
    val selectedClient: StateFlow<Client?> = _selectedClient

    private val _clientSearchQuery = MutableStateFlow("")
    val clientSearchQuery: StateFlow<String> = _clientSearchQuery

    val clientSearchResults: StateFlow<List<Client>> = _clientSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) clientRepository.getAllClients()
            else clientRepository.searchClients("%$query%")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Capture settings
    private val _captureType = MutableStateFlow(CaptureType.BEFORE)
    val captureType: StateFlow<CaptureType> = _captureType

    private val _followUpInterval = MutableStateFlow(FollowUpInterval.IMMEDIATE_AFTER)
    val followUpInterval: StateFlow<FollowUpInterval> = _followUpInterval

    private val _customInterval = MutableStateFlow("")
    val customInterval: StateFlow<String> = _customInterval

    // AR mode
    private val _arMode = MutableStateFlow(false)
    val arMode: StateFlow<Boolean> = _arMode

    private val _selectedLipZone = MutableStateFlow(LipZone.UPPER)
    val selectedLipZone: StateFlow<LipZone> = _selectedLipZone

    // Pigment recommendations per zone
    private val _upperLipRecommendations = MutableStateFlow<List<ColorMatcher.PigmentRecommendation>>(emptyList())
    val upperLipRecommendations: StateFlow<List<ColorMatcher.PigmentRecommendation>> = _upperLipRecommendations

    private val _lowerLipRecommendations = MutableStateFlow<List<ColorMatcher.PigmentRecommendation>>(emptyList())
    val lowerLipRecommendations: StateFlow<List<ColorMatcher.PigmentRecommendation>> = _lowerLipRecommendations

    private val _selectedUpperPigmentIndex = MutableStateFlow(0)
    val selectedUpperPigmentIndex: StateFlow<Int> = _selectedUpperPigmentIndex

    private val _selectedLowerPigmentIndex = MutableStateFlow(0)
    val selectedLowerPigmentIndex: StateFlow<Int> = _selectedLowerPigmentIndex

    // Capture state
    private val _captureInProgress = MutableStateFlow(false)
    val captureInProgress: StateFlow<Boolean> = _captureInProgress

    private val _captureSuccess = MutableStateFlow(false)
    val captureSuccess: StateFlow<Boolean> = _captureSuccess

    private val _captureError = MutableStateFlow<String?>(null)
    val captureError: StateFlow<String?> = _captureError

    // Gallery mode
    private val _galleryMode = MutableStateFlow(false)
    val galleryMode: StateFlow<Boolean> = _galleryMode

    private val _galleryBitmap = MutableStateFlow<Bitmap?>(null)
    val galleryBitmap: StateFlow<Bitmap?> = _galleryBitmap

    private var lastBitmap: Bitmap? = null
    private var lastRotation: Int = 0
    private var lastIsFrontCamera: Boolean = true

    // Navigation events sealed class
    sealed class NavigationEvent {
        data class ToCaptureResult(val photoId: Long) : NavigationEvent()
        data class ToDemoResult(val path: String) : NavigationEvent()
    }

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    fun preselectClient(clientId: Long) {
        viewModelScope.launch {
            _selectedClient.value = clientRepository.getClientById(clientId)
        }
    }

    fun selectClient(client: Client?) {
        _selectedClient.value = client
    }

    fun updateClientSearch(query: String) {
        _clientSearchQuery.value = query
    }

    fun loadAllClients() {
        _clientSearchQuery.value = ""
    }

    fun setImageRotation(rotation: Int, isFrontCamera: Boolean) {
        // Ignore camera rotation callbacks when in gallery mode —
        // gallery bitmaps are already EXIF-corrected
        if (_galleryMode.value) return
        lastRotation = rotation
        lastIsFrontCamera = isFrontCamera
    }

    fun onFaceDetected(face: Face, bitmap: Bitmap) {
        _detectedFace.value = face
        lastBitmap = bitmap

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            // Try dual analysis first (contour-based)
            val dual = lipColorAnalyzer.analyzeDualLipColor(bitmap, face)
            if (dual != null) {
                _dualAnalysis.value = dual
                // Use upper lip for legacy single analysis display
                _lipAnalysis.value = dual.upperLip ?: dual.lowerLip

                dual.upperLip?.let { upper ->
                    _upperLipRecommendations.value = colorMatcher.getCorrectiveRecommendations(upper)
                }
                dual.lowerLip?.let { lower ->
                    _lowerLipRecommendations.value = colorMatcher.getCorrectiveRecommendations(lower)
                }
                // Combined recommendations
                val primary = dual.upperLip ?: dual.lowerLip
                if (primary != null) {
                    _recommendations.value = colorMatcher.getCorrectiveRecommendations(primary)
                }
            } else {
                // Fallback to landmark-based analysis
                val analysis = lipColorAnalyzer.analyzeLipColor(bitmap, face)
                if (analysis != null) {
                    _lipAnalysis.value = analysis
                    _recommendations.value = colorMatcher.getCorrectiveRecommendations(analysis)
                }
            }
        }
    }

    fun setCaptureType(type: CaptureType) {
        _captureType.value = type
    }

    fun setFollowUpInterval(interval: FollowUpInterval) {
        _followUpInterval.value = interval
    }

    fun setCustomInterval(text: String) {
        _customInterval.value = text
    }

    fun toggleArMode() {
        _arMode.value = !_arMode.value
    }

    fun setSelectedLipZone(zone: LipZone) {
        _selectedLipZone.value = zone
    }

    fun swipeToNextPigment() {
        when (_selectedLipZone.value) {
            LipZone.UPPER -> {
                val max = _upperLipRecommendations.value.size
                if (max > 0) _selectedUpperPigmentIndex.value = (_selectedUpperPigmentIndex.value + 1) % max
            }
            LipZone.LOWER -> {
                val max = _lowerLipRecommendations.value.size
                if (max > 0) _selectedLowerPigmentIndex.value = (_selectedLowerPigmentIndex.value + 1) % max
            }
        }
    }

    fun swipeToPreviousPigment() {
        when (_selectedLipZone.value) {
            LipZone.UPPER -> {
                val max = _upperLipRecommendations.value.size
                if (max > 0) _selectedUpperPigmentIndex.value = (_selectedUpperPigmentIndex.value - 1 + max) % max
            }
            LipZone.LOWER -> {
                val max = _lowerLipRecommendations.value.size
                if (max > 0) _selectedLowerPigmentIndex.value = (_selectedLowerPigmentIndex.value - 1 + max) % max
            }
        }
    }

    fun getSelectedUpperPigmentHex(): String? {
        val recs = _upperLipRecommendations.value
        val idx = _selectedUpperPigmentIndex.value
        return recs.getOrNull(idx)?.pigment?.colorHex
    }

    fun getSelectedLowerPigmentHex(): String? {
        val recs = _lowerLipRecommendations.value
        val idx = _selectedLowerPigmentIndex.value
        return recs.getOrNull(idx)?.pigment?.colorHex
    }

    fun capturePhoto() {
        val client = _selectedClient.value ?: return
        val bitmap = lastBitmap ?: return
        if (_captureInProgress.value) return

        _captureInProgress.value = true
        viewModelScope.launch {
            try {
                val photoId = photoCaptureHelper.saveClientPhoto(
                    bitmap = bitmap,
                    rotation = lastRotation,
                    isFrontCamera = lastIsFrontCamera,
                    client = client,
                    captureType = _captureType.value,
                    followUpInterval = _followUpInterval.value,
                    customInterval = _customInterval.value,
                    dualAnalysis = _dualAnalysis.value,
                    upperRecs = _upperLipRecommendations.value,
                    lowerRecs = _lowerLipRecommendations.value,
                    selectedUpperIdx = _selectedUpperPigmentIndex.value,
                    selectedLowerIdx = _selectedLowerPigmentIndex.value
                )

                _navigationEvent.value = NavigationEvent.ToCaptureResult(photoId)
                _captureSuccess.value = true
            } catch (e: Exception) {
                _captureError.value = e.message ?: "Capture failed"
            } finally {
                _captureInProgress.value = false
            }
        }
    }

    fun captureDemoPhoto() {
        val bitmap = lastBitmap ?: return
        if (_captureInProgress.value) return

        _captureInProgress.value = true
        viewModelScope.launch {
            try {
                val path = photoCaptureHelper.saveDemoPhoto(
                    bitmap = bitmap,
                    rotation = lastRotation,
                    isFrontCamera = lastIsFrontCamera
                )
                _navigationEvent.value = NavigationEvent.ToDemoResult(path)
                _captureSuccess.value = true
            } catch (e: Exception) {
                _captureError.value = e.message ?: "Capture failed"
            } finally {
                _captureInProgress.value = false
            }
        }
    }

    fun dismissCaptureSuccess() {
        _captureSuccess.value = false
    }

    fun dismissCaptureError() {
        _captureError.value = null
    }

    fun analyzeGalleryPhoto(bitmap: Bitmap) {
        _galleryMode.value = true
        _galleryBitmap.value = bitmap
        // Gallery bitmaps are already EXIF-corrected by loadBitmapFromUri,
        // so reset rotation to avoid double-rotating when saving
        lastRotation = 0
        lastIsFrontCamera = false
        clearAnalysis()

        viewModelScope.launch {
            val (face, _) = faceDetectionHelper.detectFaceAndAnalyze(bitmap, null)
            if (face != null) {
                onFaceDetected(face, bitmap)
            }
        }
    }

    fun exitGalleryMode() {
        _galleryMode.value = false
        _galleryBitmap.value = null
        clearAnalysis()
    }

    fun clearAnalysis() {
        _lipAnalysis.value = null
        _dualAnalysis.value = null
        _recommendations.value = emptyList()
        _upperLipRecommendations.value = emptyList()
        _lowerLipRecommendations.value = emptyList()
        _detectedFace.value = null
        lastBitmap = null
    }
}
