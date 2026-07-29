package com.balajitechlabs.quickdash.features.clipboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.SettingsRepository
import com.balajitechlabs.quickdash.features.clipboard.data.ClipboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClipboardViewModel @Inject constructor(
    private val clipboardRepository: ClipboardRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val clipboardHistory: Flow<String> = clipboardRepository.clipboardHistory
    val clipboardPinned: Flow<String> = clipboardRepository.clipboardPinned
    val tabBiometricLock: Flow<Boolean> = settingsRepository.tabBiometricLock

    fun saveClipboardHistory(json: String) {
        viewModelScope.launch {
            clipboardRepository.saveClipboardHistory(json)
        }
    }

    fun saveClipboardPinned(json: String) {
        viewModelScope.launch {
            clipboardRepository.saveClipboardPinned(json)
        }
    }
}
