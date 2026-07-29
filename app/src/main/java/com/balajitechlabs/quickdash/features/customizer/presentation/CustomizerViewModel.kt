package com.balajitechlabs.quickdash.features.customizer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val bubbleSizeDp: Flow<Float> = settingsRepository.bubbleSizeDp
    val bubbleOpacityAlpha: Flow<Float> = settingsRepository.bubbleOpacityAlpha
    val bubbleGlowColorHex: Flow<String> = settingsRepository.bubbleGlowColorHex
    val useDynamicWallpaperColor: Flow<Boolean> = settingsRepository.useDynamicWallpaperColor
    val soundEffectsEnabled: Flow<Boolean> = settingsRepository.soundEffectsEnabled

    fun saveBubbleSizeDp(sizeDp: Float) {
        viewModelScope.launch {
            settingsRepository.saveBubbleSizeDp(sizeDp)
        }
    }

    fun saveBubbleOpacityAlpha(alpha: Float) {
        viewModelScope.launch {
            settingsRepository.saveBubbleOpacityAlpha(alpha)
        }
    }

    fun saveBubbleGlowColorHex(colorHex: String) {
        viewModelScope.launch {
            settingsRepository.saveBubbleGlowColorHex(colorHex)
        }
    }

    fun saveUseDynamicWallpaperColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveUseDynamicWallpaperColor(enabled)
        }
    }

    fun saveSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveSoundEffectsEnabled(enabled)
        }
    }
}
