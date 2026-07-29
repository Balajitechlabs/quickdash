package com.balajitechlabs.quickdash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.HistoryRepository
import com.balajitechlabs.quickdash.core.data.SettingsRepository
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
    val settingsRepository: SettingsRepository,
    val historyRepository: HistoryRepository
) : ViewModel() {

    private val _isMigrationComplete = MutableStateFlow(false)
    val isMigrationComplete: StateFlow<Boolean> = _isMigrationComplete

    val appLanguage: Flow<String> = settingsRepository.appLanguage
    val totalAppOpens: Flow<Long> = settingsRepository.totalAppOpens
    val isOnboardingComplete: Flow<Boolean> = settingsRepository.isOnboardingComplete
    val launchStyle: Flow<String> = settingsRepository.launchStyle
    val isAppLocked: Flow<Boolean> = settingsRepository.isAppLocked
    val hasReportedInstall: Flow<Boolean> = settingsRepository.hasReportedInstall
    val payeeName: Flow<String?> = settingsRepository.payeeName
    val upiIds: Flow<List<String>> = settingsRepository.upiIds
    val lastActiveDate: Flow<String> = settingsRepository.lastActiveDate
    val secureMode: Flow<Boolean> = settingsRepository.secureMode
    val themeMode: Flow<String> = settingsRepository.themeMode
    val dynamicColor: Flow<Boolean> = settingsRepository.dynamicColor
    val lastSeenVersion: Flow<String> = settingsRepository.lastSeenVersion
    val bubbleEnabled: Flow<Boolean> = settingsRepository.bubbleEnabled
    
    val clipboardHistory: Flow<String> = settingsRepository.clipboardHistory

    init {
        viewModelScope.launch {
            dataMigrationManager.migrateLegacyHistoryIfNeeded()
            _isMigrationComplete.value = true
        }
    }
    
    fun setHasReportedInstall() {
        viewModelScope.launch {
            settingsRepository.setHasReportedInstall()
        }
    }
    
    fun incrementTotalAppOpens() {
        viewModelScope.launch {
            settingsRepository.incrementAppOpens()
        }
    }
    
    fun setLastActiveDate(date: String) {
        viewModelScope.launch {
            settingsRepository.setLastActiveDate(date)
        }
    }

    fun saveFcmToken(token: String) {
        viewModelScope.launch {
            settingsRepository.saveFcmToken(token)
        }
    }

    fun saveLastSeenVersion(version: String) {
        viewModelScope.launch {
            settingsRepository.saveLastSeenVersion(version)
        }
    }

    fun setOnboardingComplete() {
        viewModelScope.launch {
            settingsRepository.setOnboardingComplete()
        }
    }

    fun saveDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveDynamicColor(enabled)
        }
    }

    fun saveThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.saveThemeMode(mode)
        }
    }

    fun saveClipboardHistory(json: String) {
        viewModelScope.launch {
            settingsRepository.saveClipboardHistory(json)
        }
    }
}
