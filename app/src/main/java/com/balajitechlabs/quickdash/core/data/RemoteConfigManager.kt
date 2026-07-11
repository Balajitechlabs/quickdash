package com.balajitechlabs.quickdash.core.data

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object RemoteConfigManager {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()
    private val _configValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val configValues = _configValues.asStateFlow()

    init {
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // 1 hour
            .build()
        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(mapOf<String, Any>())
    }

    fun fetchAndActivate() {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val all = remoteConfig.all.mapValues { it.value.asString() }
                _configValues.value = all
            }
        }
    }
}