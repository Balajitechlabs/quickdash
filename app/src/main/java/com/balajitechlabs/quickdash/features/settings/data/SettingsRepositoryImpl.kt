package com.balajitechlabs.quickdash.features.settings.data

import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(private val userStore: UserStore) : SettingsRepository {
    
    override val themeMode: Flow<String> = userStore.themeMode
    override val dynamicColor: Flow<Boolean> = userStore.dynamicColor
    override val appLanguage: Flow<String> = userStore.appLanguage
    override val secureMode: Flow<Boolean> = userStore.secureMode
    override val hapticEnabled: Flow<Boolean> = userStore.hapticEnabled
    override val biometricLock: Flow<Boolean> = userStore.biometricLock
    override val shakeToOpen: Flow<Boolean> = userStore.shakeToOpen

    override suspend fun saveThemeMode(mode: String) {
        userStore.saveThemeMode(mode)
    }

    override suspend fun saveDynamicColor(enabled: Boolean) {
        userStore.saveDynamicColor(enabled)
    }

    override suspend fun saveAppLanguage(lang: String) {
        userStore.saveAppLanguage(lang)
    }

    override suspend fun saveSecureMode(enabled: Boolean) {
        userStore.saveSecureMode(enabled)
    }

    override suspend fun saveHapticEnabled(enabled: Boolean) {
        userStore.setHapticEnabled(enabled)
    }

    override suspend fun saveBiometricLock(enabled: Boolean) {
        userStore.saveBiometricLock(enabled)
    }

    override suspend fun saveShakeToOpen(enabled: Boolean) {
        userStore.setShakeToOpen(enabled)
    }
}
