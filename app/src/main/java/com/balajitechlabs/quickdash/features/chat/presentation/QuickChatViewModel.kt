package com.balajitechlabs.quickdash.features.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickChatViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val chatDefaultCode: Flow<String> = settingsRepository.chatDefaultCode
    val chatDefaultIso: Flow<String> = settingsRepository.chatDefaultIso
    val chatHistory: Flow<List<String>> = settingsRepository.chatHistory
    val chatPauseHistory: Flow<Boolean> = settingsRepository.chatPauseHistory

    fun saveChatDefaultCountry(code: String, iso: String) {
        viewModelScope.launch {
            settingsRepository.saveChatDefaultCountry(code, iso)
        }
    }

    fun saveChatNumberToHistory(number: String, flag: String) {
        viewModelScope.launch {
            settingsRepository.saveChatNumberToHistory(number, flag)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            settingsRepository.clearChatHistory()
        }
    }

    fun saveChatPauseHistory(pause: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveChatPauseHistory(pause)
        }
    }
}
