package com.balajitechlabs.quickdash.features.customizer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizerViewModel @Inject constructor(
    private val userStore: UserStore
) : ViewModel() {

    val bubbleSizeDp: Flow<Float> = userStore.bubbleSizeDp
    val bubbleOpacityAlpha: Flow<Float> = userStore.bubbleOpacityAlpha
    val bubbleGlowColorHex: Flow<String> = userStore.bubbleGlowColorHex
    val useDynamicWallpaperColor: Flow<Boolean> = userStore.useDynamicWallpaperColor
    val soundEffectsEnabled: Flow<Boolean> = userStore.soundEffectsEnabled

    fun saveBubbleSizeDp(sizeDp: Float) {
        viewModelScope.launch {
            userStore.saveBubbleSizeDp(sizeDp)
        }
    }

    fun saveBubbleOpacityAlpha(alpha: Float) {
        viewModelScope.launch {
            userStore.saveBubbleOpacityAlpha(alpha)
        }
    }

    fun saveBubbleGlowColorHex(colorHex: String) {
        viewModelScope.launch {
            userStore.saveBubbleGlowColorHex(colorHex)
        }
    }

    fun saveUseDynamicWallpaperColor(enabled: Boolean) {
        viewModelScope.launch {
            userStore.saveUseDynamicWallpaperColor(enabled)
        }
    }

    fun saveSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userStore.saveSoundEffectsEnabled(enabled)
        }
    }
}
