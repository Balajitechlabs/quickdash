/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/wifi/presentation
 * File: WifiViewModel.kt
 * Description: ViewModel managing Wi-Fi credentials, QR formatting, and saved network histories.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.wifi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WifiViewModel @Inject constructor(
    private val userStore: UserStore
) : ViewModel() {
    
    // UI state flows
    val wifiSsid: StateFlow<String> = userStore.wifiSsid
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
        
    val wifiPassword: StateFlow<String> = userStore.wifiPassword
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
        
    val wifiHistoryJson: StateFlow<String> = userStore.wifiHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "[]")
        
    val emojiHeader: StateFlow<String> = userStore.emojiHeader
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
        
    val qrUseEmojiOverlay: StateFlow<Boolean> = userStore.qrUseEmojiOverlay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val wifiHotspotMode: StateFlow<Boolean> = userStore.wifiHotspotMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val serverCredentials: StateFlow<String> = userStore.serverCredentials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "{}")

    fun saveWifiHotspotMode(mode: Boolean) {
        viewModelScope.launch {
            userStore.saveWifiHotspotMode(mode)
        }
    }

    fun saveServerCredentials(json: String) {
        viewModelScope.launch {
            userStore.saveServerCredentials(json)
        }
    }

    fun saveWifiCredentials(ssid: String, password: String) {
        viewModelScope.launch {
            userStore.saveWifiCredentials(ssid, password)
        }
    }

    fun addWifiHistory(ssid: String, password: String, encryptionType: String) {
        viewModelScope.launch {
            userStore.addWifiHistory(ssid, password, encryptionType)
        }
    }

    fun clearWifiHistory() {
        viewModelScope.launch {
            userStore.clearWifiHistory()
        }
    }

    fun removeWifiHistoryEntry(ssid: String) {
        viewModelScope.launch {
            userStore.removeWifiHistoryEntry(ssid)
        }
    }
}
