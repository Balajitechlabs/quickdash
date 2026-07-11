package com.balajitechlabs.quickdash.features.settings.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<String>
    val dynamicColor: Flow<Boolean>
    val appLanguage: Flow<String>
    val secureMode: Flow<Boolean>
    val hapticEnabled: Flow<Boolean>
    val biometricLock: Flow<Boolean>
    val shakeToOpen: Flow<Boolean>
    
    suspend fun saveThemeMode(mode: String)
    suspend fun saveDynamicColor(enabled: Boolean)
    suspend fun saveAppLanguage(lang: String)
    suspend fun saveSecureMode(enabled: Boolean)
    suspend fun saveHapticEnabled(enabled: Boolean)
    suspend fun saveBiometricLock(enabled: Boolean)
    suspend fun saveShakeToOpen(enabled: Boolean)
}
