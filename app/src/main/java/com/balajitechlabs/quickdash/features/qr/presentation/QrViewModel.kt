package com.balajitechlabs.quickdash.features.qr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val launchStyle: Flow<String> = settingsRepository.launchStyle
    val favoriteTools: Flow<String> = settingsRepository.favoriteTools

    fun saveLaunchStyle(style: String) {
        viewModelScope.launch {
            settingsRepository.saveLaunchStyle(style)
        }
    }

    fun saveFavoriteTools(tools: String) {
        viewModelScope.launch {
            settingsRepository.saveFavoriteTools(tools)
        }
    }
}
