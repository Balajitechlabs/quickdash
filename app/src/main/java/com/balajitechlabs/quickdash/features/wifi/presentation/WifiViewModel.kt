package com.balajitechlabs.quickdash.features.wifi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WifiViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    // UI state flows
    val wifiSsid: StateFlow<String> = settingsRepository.wifiSsid
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
        
    val wifiPassword: StateFlow<String> = settingsRepository.wifiPassword
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
        
    val wifiHistoryJson: StateFlow<String> = settingsRepository.wifiHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "[]")
        
    val emojiHeader: StateFlow<String> = settingsRepository.emojiHeader
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "🚀")
        
    val qrUseEmojiOverlay: StateFlow<Boolean> = settingsRepository.qrUseEmojiOverlay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val wifiHotspotMode: StateFlow<Boolean> = settingsRepository.wifiHotspotMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val serverCredentials: StateFlow<String> = settingsRepository.serverCredentials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "{}")

    fun saveWifiHotspotMode(mode: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveWifiHotspotMode(mode)
        }
    }

    fun saveServerCredentials(json: String) {
        viewModelScope.launch {
            settingsRepository.saveServerCredentials(json)
        }
    }

    fun saveWifiCredentials(ssid: String, password: String) {
        viewModelScope.launch {
            settingsRepository.saveWifiCredentials(ssid, password)
        }
    }

    fun addWifiHistory(ssid: String, password: String, encryptionType: String) {
        viewModelScope.launch {
            settingsRepository.addWifiHistory(ssid, password, encryptionType)
        }
    }

    fun clearWifiHistory() {
        viewModelScope.launch {
            settingsRepository.clearWifiHistory()
        }
    }

    fun removeWifiHistoryEntry(ssid: String) {
        viewModelScope.launch {
            settingsRepository.removeWifiHistoryEntry(ssid)
        }
    }
}
