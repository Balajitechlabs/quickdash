package com.balajitechlabs.quickdash.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object RemoteConfigManager {
    private val _configValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val configValues = _configValues.asStateFlow()

    init {
        try {
            val clazz = Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfig")
            val getInstance = clazz.getMethod("getInstance")
            val remoteConfig = getInstance.invoke(null)

            val builderClazz = Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings\$Builder")
            val builder = builderClazz.getDeclaredConstructor().newInstance()
            val setMinInterval = builderClazz.getMethod("setMinimumFetchIntervalInSeconds", Long::class.javaPrimitiveType)
            setMinInterval.invoke(builder, 3600L)
            val buildMethod = builderClazz.getMethod("build")
            val settings = buildMethod.invoke(builder)

            val setConfigSettings = clazz.getMethod("setConfigSettingsAsync", settings.javaClass)
            setConfigSettings.invoke(remoteConfig, settings)
        } catch (_: Throwable) {
            // Remote config not active in FOSS build
        }
    }

    fun fetchAndActivate() {
        try {
            val clazz = Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfig")
            val getInstance = clazz.getMethod("getInstance")
            val remoteConfig = getInstance.invoke(null)
            val fetchMethod = clazz.getMethod("fetchAndActivate")
            val task = fetchMethod.invoke(remoteConfig) as? com.google.android.gms.tasks.Task<*>
            task?.addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    try {
                        val getAllMethod = clazz.getMethod("getAll")
                        val all = getAllMethod.invoke(remoteConfig) as? Map<*, *>
                        if (all != null) {
                            _configValues.value = all.entries.associate { (k, v) ->
                                k.toString() to (v?.javaClass?.getMethod("asString")?.invoke(v)?.toString() ?: "")
                            }
                        }
                    } catch (_: Throwable) { }
                }
            }
        } catch (_: Throwable) {
            // Not present in FOSS build
        }
    }
}