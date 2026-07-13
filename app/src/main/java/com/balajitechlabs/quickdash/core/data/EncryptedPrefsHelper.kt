package com.balajitechlabs.quickdash.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

object EncryptedPrefsHelper {
    private lateinit var prefs: SharedPreferences

    @Synchronized
    fun init(context: Context) {
        if (::prefs.isInitialized) return
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            prefs = EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to normal shared preferences if keystore is corrupted or unavailable
            prefs = context.getSharedPreferences("secure_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString(key, value).apply()
    }

    suspend fun getString(key: String, default: String? = null): String? = withContext(Dispatchers.IO) {
        prefs.getString(key, default)
    }

    fun getStringFlow(key: String, defaultValue: String): Flow<String> = callbackFlow {
        // Emit current value immediately
        trySend(prefs.getString(key, defaultValue) ?: defaultValue)

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                trySend(prefs.getString(key, defaultValue) ?: defaultValue)
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        prefs.edit().remove(key).apply()
    }
}