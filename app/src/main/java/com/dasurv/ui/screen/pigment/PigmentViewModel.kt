package com.dasurv.ui.screen.pigment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.model.Pigment
import com.dasurv.data.model.PigmentBrand
import com.dasurv.data.repository.PigmentRepository
import com.dasurv.util.ColorMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PigmentViewModel @Inject constructor(
    private val pigmentRepository: PigmentRepository,
    private val colorMatcher: ColorMatcher
) : ViewModel() {

    private val _selectedBrand = MutableStateFlow<PigmentBrand?>(null)
    val selectedBrand: StateFlow<PigmentBrand?> = _selectedBrand

    private val _pigments = MutableStateFlow<List<Pigment>>(emptyList())
    val pigments: StateFlow<List<Pigment>> = _pigments

    init {
        viewModelScope.launch(Dispatchers.Default) {
            _pigments.value = pigmentRepository.getAllPigments()
        }
    }

    private val _desiredColorRecommendations = MutableStateFlow<List<ColorMatcher.PigmentRecommendation>>(emptyList())
    val desiredColorRecommendations: StateFlow<List<ColorMatcher.PigmentRecommendation>> = _desiredColorRecommendations

    fun selectBrand(brand: PigmentBrand?) {
        _selectedBrand.value = brand
        _pigments.value = if (brand != null) {
            pigmentRepository.getPigmentsByBrand(brand)
        } else {
            pigmentRepository.getAllPigments()
        }
    }

    fun getRecommendationsForDesiredColor(colorHex: String) {
        _desiredColorRecommendations.value = colorMatcher.getDesiredResultRecommendations(colorHex)
    }
}
