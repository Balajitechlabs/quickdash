/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: app
 * File: MainViewModel.kt
 * Description: Central ViewModel orchestrating UI state, UserStore data collection, tool interactions, and update checks.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.HistoryRepository
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.data.migration.DataMigrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val dataMigrationManager: DataMigrationManager,
    val userStore: UserStore,
    val historyRepository: HistoryRepository
) : ViewModel() {

    private val _isMigrationComplete = MutableStateFlow(false)
    val isMigrationComplete: StateFlow<Boolean> = _isMigrationComplete

    val appLanguage: Flow<String> = userStore.appLanguage
    val totalAppOpens: Flow<Long> = userStore.totalAppOpens
    val isOnboardingComplete: Flow<Boolean> = userStore.isOnboardingComplete
    val launchStyle: Flow<String> = userStore.launchStyle
    val isAppLocked: Flow<Boolean> = userStore.isAppLocked
    val hasReportedInstall: Flow<Boolean> = userStore.hasReportedInstall
    val payeeName: Flow<String?> = userStore.payeeName
    val upiIds: Flow<List<String>> = userStore.upiIds
    val lastActiveDate: Flow<String> = userStore.lastActiveDate
    val secureMode: Flow<Boolean> = userStore.secureMode
    val themeMode: Flow<String> = userStore.themeMode
    val dynamicColor: Flow<Boolean> = userStore.dynamicColor
    val lastSeenVersion: Flow<String> = userStore.lastSeenVersion
    val bubbleEnabled: Flow<Boolean> = userStore.bubbleEnabled
    
    val clipboardHistory: Flow<String> = userStore.clipboardHistory

    init {
        viewModelScope.launch {
            dataMigrationManager.migrateLegacyHistoryIfNeeded()
            _isMigrationComplete.value = true
        }
    }
    
    fun setHasReportedInstall() {
        viewModelScope.launch {
            userStore.setHasReportedInstall()
        }
    }
    
    fun incrementTotalAppOpens() {
        viewModelScope.launch {
            userStore.incrementAppOpens()
        }
    }
    
    fun setLastActiveDate(date: String) {
        viewModelScope.launch {
            userStore.setLastActiveDate(date)
        }
    }

    fun saveFcmToken(token: String) {
        viewModelScope.launch {
            userStore.saveFcmToken(token)
        }
    }

    fun saveLastSeenVersion(version: String) {
        viewModelScope.launch {
            userStore.saveLastSeenVersion(version)
        }
    }

    fun setOnboardingComplete() {
        viewModelScope.launch {
            userStore.setOnboardingComplete()
        }
    }

    fun saveDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userStore.saveDynamicColor(enabled)
        }
    }

    fun saveThemeMode(mode: String) {
        viewModelScope.launch {
            userStore.saveThemeMode(mode)
        }
    }

    fun saveClipboardHistory(json: String) {
        viewModelScope.launch {
            userStore.saveClipboardHistory(json)
        }
    }
}
