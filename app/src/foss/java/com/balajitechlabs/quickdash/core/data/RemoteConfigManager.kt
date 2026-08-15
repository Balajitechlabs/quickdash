package com.balajitechlabs.quickdash.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 🌿 FOSS Edition RemoteConfigManager.
 * Standalone offline configuration provider without Firebase dependencies.
 */
object RemoteConfigManager {
    private val _configValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val configValues = _configValues.asStateFlow()

    fun fetchAndActivate() {
        // FOSS: Standalone mode
    }
}
