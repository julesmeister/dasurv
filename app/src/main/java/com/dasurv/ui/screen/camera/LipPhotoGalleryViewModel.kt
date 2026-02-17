package com.dasurv.ui.screen.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.local.entity.LipPhoto
import com.dasurv.data.local.entity.LipPhotoPigment
import com.dasurv.data.repository.LipPhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LipPhotoGalleryViewModel @Inject constructor(
    private val lipPhotoRepository: LipPhotoRepository
) : ViewModel() {

    private var clientId: Long = 0

    private val _photos = MutableStateFlow<List<LipPhoto>>(emptyList())
    val photos: StateFlow<List<LipPhoto>> = _photos

    private val _pigmentsByPhoto = MutableStateFlow<Map<Long, List<LipPhotoPigment>>>(emptyMap())
    val pigmentsByPhoto: StateFlow<Map<Long, List<LipPhotoPigment>>> = _pigmentsByPhoto

    private val _expandedPhotoId = MutableStateFlow<Long?>(null)
    val expandedPhotoId: StateFlow<Long?> = _expandedPhotoId

    fun loadPhotos(clientId: Long) {
        this.clientId = clientId
        viewModelScope.launch {
            lipPhotoRepository.getPhotosForClient(clientId).collect { photoList ->
                _photos.value = photoList
                // Load pigments for all photos
                val pigmentsMap = mutableMapOf<Long, List<LipPhotoPigment>>()
                for (photo in photoList) {
                    lipPhotoRepository.getPigmentsForPhoto(photo.id).collect { pigments ->
                        pigmentsMap[photo.id] = pigments
                    }
                }
                _pigmentsByPhoto.value = pigmentsMap
            }
        }
    }

    fun toggleExpanded(photoId: Long) {
        _expandedPhotoId.value = if (_expandedPhotoId.value == photoId) null else photoId
    }

    fun updateNotes(photo: LipPhoto, notes: String) {
        viewModelScope.launch {
            lipPhotoRepository.updatePhoto(photo.copy(notes = notes))
        }
    }

    fun updateCaptureType(photo: LipPhoto, type: CaptureType, followUpInterval: String? = null) {
        viewModelScope.launch {
            lipPhotoRepository.updatePhoto(
                photo.copy(captureType = type, followUpInterval = followUpInterval)
            )
        }
    }

    fun deletePhoto(photo: LipPhoto) {
        // Optimistically remove from UI immediately
        _photos.value = _photos.value.filter { it.id != photo.id }
        _pigmentsByPhoto.value = _pigmentsByPhoto.value - photo.id
        if (_expandedPhotoId.value == photo.id) _expandedPhotoId.value = null

        viewModelScope.launch {
            lipPhotoRepository.deletePigmentsForPhoto(photo.id)
            lipPhotoRepository.deletePhoto(photo)
            try { File(photo.photoUri).delete() } catch (_: Exception) {}
        }
    }
}
