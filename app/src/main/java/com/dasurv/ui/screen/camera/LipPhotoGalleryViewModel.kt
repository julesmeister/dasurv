package com.dasurv.ui.screen.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.CaptureType
import com.dasurv.data.local.entity.LipPhoto
import com.dasurv.data.local.entity.LipPhotoPigment
import com.dasurv.data.repository.LipPhotoRepository
import com.dasurv.util.DefaultSubscribePolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import android.content.Context
import com.dasurv.util.PhotoShareHelper
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LipPhotoGalleryViewModel @Inject constructor(
    private val lipPhotoRepository: LipPhotoRepository
) : ViewModel() {

    private var clientId: Long = 0

    private val _clientId = MutableStateFlow<Long?>(null)

    val photos: StateFlow<List<LipPhoto>> = _clientId
        .filterNotNull()
        .flatMapLatest { lipPhotoRepository.getPhotosForClient(it) }
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    private val _pigmentsByPhoto = MutableStateFlow<Map<Long, List<LipPhotoPigment>>>(emptyMap())
    val pigmentsByPhoto: StateFlow<Map<Long, List<LipPhotoPigment>>> = _pigmentsByPhoto

    private val _expandedPhotoId = MutableStateFlow<Long?>(null)
    val expandedPhotoId: StateFlow<Long?> = _expandedPhotoId

    init {
        viewModelScope.launch {
            photos.collect { photoList ->
                val pigmentsMap = mutableMapOf<Long, List<LipPhotoPigment>>()
                for (photo in photoList) {
                    pigmentsMap[photo.id] = lipPhotoRepository.getPigmentsForPhotoOnce(photo.id)
                }
                _pigmentsByPhoto.value = pigmentsMap
            }
        }
    }

    fun loadPhotos(clientId: Long) {
        this.clientId = clientId
        _clientId.value = clientId
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

    fun sharePhoto(context: Context, photo: LipPhoto) {
        PhotoShareHelper.sharePhoto(context, photo.photoUri, "Dasurv Studios")
    }

    fun deletePhoto(photo: LipPhoto) {
        _pigmentsByPhoto.value = _pigmentsByPhoto.value - photo.id
        if (_expandedPhotoId.value == photo.id) _expandedPhotoId.value = null

        viewModelScope.launch {
            lipPhotoRepository.deletePigmentsForPhoto(photo.id)
            lipPhotoRepository.deletePhoto(photo)
            try { File(photo.photoUri).delete() } catch (_: Exception) {}
        }
    }
}
