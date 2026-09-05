/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data/migration
 * File: DataMigrationManager.kt
 * Description: Manages legacy preference migrations, schema version increments, and automated data upgrades.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data.migration


import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.balajitechlabs.quickdash.core.data.HistoryRepository
import com.balajitechlabs.quickdash.core.data.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class DataMigrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val historyRepository: HistoryRepository
) {
    companion object {
        private const val TAG = "DataMigrationManager"
        val HAS_MIGRATED_HISTORY_KEY = booleanPreferencesKey("has_migrated_history")
        val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history")
        val WIFI_HISTORY_KEY = stringPreferencesKey("wifi_history")
        val QR_HISTORY_KEY = stringPreferencesKey("qr_history")
    }

    suspend fun migrateLegacyHistoryIfNeeded(): Boolean {
        val dataStore = context.dataStore
        val preferences = dataStore.data.first()
        val hasMigrated = preferences[HAS_MIGRATED_HISTORY_KEY] ?: false

        if (hasMigrated) {
            return true
        }

        try {
            // Migrate Search History
            val searchHistoryJson = preferences[SEARCH_HISTORY_KEY]
            if (!searchHistoryJson.isNullOrBlank() && searchHistoryJson != "[]") {
                val list: List<String> = try {
                    Gson().fromJson<List<String>>(
                        searchHistoryJson,
                        object : TypeToken<List<String>>() {}.type
                    ) ?: emptyList()
                } catch (e: Exception) { emptyList() }
                
                for (query in list.reversed()) {
                    historyRepository.addSearchHistory(query)
                }
            }

            // Migrate Wi-Fi History
            val wifiHistoryJson = preferences[WIFI_HISTORY_KEY]
            if (!wifiHistoryJson.isNullOrBlank() && wifiHistoryJson != "[]") {
                try {
                    val array = JSONArray(wifiHistoryJson)
                    for (i in array.length() - 1 downTo 0) {
                        val obj = array.getJSONObject(i)
                        historyRepository.addWifiHistory(
                            ssid = obj.optString("ssid", ""),
                            password = obj.optString("password", ""),
                            securityType = obj.optString("securityType", "WPA/WPA2")
                        )
                    }
                } catch (e: Exception) { Log.e(TAG, "Failed to migrate Wi-Fi history", e) }
            }

            // Migrate QR History
            val qrHistoryJson = preferences[QR_HISTORY_KEY]
            if (!qrHistoryJson.isNullOrBlank() && qrHistoryJson != "[]") {
                try {
                    val array = JSONArray(qrHistoryJson)
                    for (i in array.length() - 1 downTo 0) {
                        val obj = array.getJSONObject(i)
                        historyRepository.saveQrHistoryItem(
                            amount = obj.optString("amount", ""),
                            note = obj.optString("note", ""),
                            upiId = obj.optString("upiId", ""),
                            targetApp = obj.optString("targetApp", ""),
                            category = obj.optString("category", "General")
                        )
                    }
                } catch (e: Exception) { Log.e(TAG, "Failed to migrate QR history", e) }
            }

            // Mark migration as complete and delete old strings to save space
            dataStore.edit { prefs ->
                prefs[HAS_MIGRATED_HISTORY_KEY] = true
                prefs.remove(SEARCH_HISTORY_KEY)
                prefs.remove(WIFI_HISTORY_KEY)
                prefs.remove(QR_HISTORY_KEY)
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to migrate legacy history", e)
            return false
        }
    }
}
