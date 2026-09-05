/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: UserStoreBase.kt
 * Description: Foundational preferences store defining companion keys, app lifecycle metrics, and JSON backup and restore.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.balajitechlabs.quickdash.core.data.prefs.PreferencesKeys
import com.balajitechlabs.quickdash.core.utils.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

open class UserStoreBase(val context: Context) {

    init {
        EncryptedPrefsHelper.init(context)
    }

    val analyticsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ANALYTICS_ENABLED_KEY] ?: true
    }

    suspend fun isAnalyticsEnabled(): Boolean {
        return analyticsEnabled.first()
    }

    suspend fun saveAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANALYTICS_ENABLED_KEY] = enabled
        }
    }

    val hasReportedInstall: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_REPORTED_INSTALL_KEY] ?: false
    }

    suspend fun setHasReportedInstall() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_REPORTED_INSTALL_KEY] = true
        }
    }

    val lastActiveDate: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_ACTIVE_DATE_KEY] ?: ""
    }

    suspend fun setLastActiveDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_ACTIVE_DATE_KEY] = date
        }
    }

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_ONBOARDING_COMPLETE_KEY] ?: false
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETE_KEY] = true
        }
    }

    val lastTelegramUpdateId: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_TELEGRAM_UPDATE_ID_KEY] ?: 0L
    }

    suspend fun setLastTelegramUpdateId(updateId: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_TELEGRAM_UPDATE_ID_KEY] = updateId
        }
    }

    val totalAppOpens: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TOTAL_APP_OPENS_KEY] ?: 0L
    }

    suspend fun incrementAppOpens() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.TOTAL_APP_OPENS_KEY] ?: 0L
            preferences[PreferencesKeys.TOTAL_APP_OPENS_KEY] = current + 1
        }
    }

    val totalQrGenerated: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TOTAL_QR_GENERATED_KEY] ?: 0L
    }

    suspend fun incrementQrGenerated() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.TOTAL_QR_GENERATED_KEY] ?: 0L
            preferences[PreferencesKeys.TOTAL_QR_GENERATED_KEY] = current + 1
        }
    }

    val totalNotesSaved: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TOTAL_NOTES_SAVED_KEY] ?: 0L
    }

    suspend fun incrementNotesSaved() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.TOTAL_NOTES_SAVED_KEY] ?: 0L
            preferences[PreferencesKeys.TOTAL_NOTES_SAVED_KEY] = current + 1
        }
    }

    val lastSeenVersion: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_SEEN_VERSION_KEY] ?: ""
    }

    suspend fun saveLastSeenVersion(version: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SEEN_VERSION_KEY] = version
        }
    }

    val includePreRelease: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.INCLUDE_PRE_RELEASE_KEY] ?: false
    }

    suspend fun saveIncludePreRelease(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_PRE_RELEASE_KEY] = enabled
        }
    }

    suspend fun exportToJson(): String {
        val json = JSONObject()
        json.put("magic", "QuickDash_Backup_v1")

        val prefs = context.dataStore.data.first()
        prefs[PreferencesKeys.UPI_IDS_KEY]?.let { json.put("upi_ids", it) }
        prefs[PreferencesKeys.UPI_ID_KEY]?.let { json.put("upi_id", it) }
        prefs[PreferencesKeys.DEFAULT_UPI_ID_KEY]?.let { json.put("default_upi_id", it) }
        prefs[PreferencesKeys.PAYEE_NAME_KEY]?.let { json.put("payee_name", it) }
        prefs[PreferencesKeys.RECENT_AMOUNTS_KEY]?.let { json.put("recent_amounts", it) }
        prefs[PreferencesKeys.SHOW_UPI_ID_KEY]?.let { json.put("show_upi_id", it) }
        prefs[PreferencesKeys.THEME_MODE_KEY]?.let { json.put("theme_mode", it) }
        prefs[PreferencesKeys.DYNAMIC_COLOR_KEY]?.let { json.put("dynamic_color", it) }
        prefs[PreferencesKeys.CHAT_DEFAULT_CODE_KEY]?.let { json.put("chat_default_code", it) }
        prefs[PreferencesKeys.CHAT_DEFAULT_ISO_KEY]?.let { json.put("chat_default_iso", it) }
        prefs[PreferencesKeys.CHAT_PAUSE_HISTORY_KEY]?.let { json.put("chat_pause_history", it) }

        return json.toString(4)
    }

    suspend fun importFromJson(jsonString: String): Boolean {
        return try {
            val json = JSONObject(jsonString)
            if (json.optString("magic") != "QuickDash_Backup_v1") {
                return false
            }
            context.dataStore.edit { preferences ->
                if (json.has("upi_ids")) preferences[PreferencesKeys.UPI_IDS_KEY] = json.getString("upi_ids")
                if (json.has("upi_id")) preferences[PreferencesKeys.UPI_ID_KEY] = json.getString("upi_id")
                if (json.has("default_upi_id")) preferences[PreferencesKeys.DEFAULT_UPI_ID_KEY] = json.getString("default_upi_id")
                if (json.has("payee_name")) preferences[PreferencesKeys.PAYEE_NAME_KEY] = json.getString("payee_name")
                if (json.has("recent_amounts")) preferences[PreferencesKeys.RECENT_AMOUNTS_KEY] = json.getString("recent_amounts")
                if (json.has("show_upi_id")) preferences[PreferencesKeys.SHOW_UPI_ID_KEY] = json.getBoolean("show_upi_id")
                if (json.has("theme_mode")) preferences[PreferencesKeys.THEME_MODE_KEY] = json.getString("theme_mode")
                if (json.has("dynamic_color")) preferences[PreferencesKeys.DYNAMIC_COLOR_KEY] = json.getBoolean("dynamic_color")
                if (json.has("chat_default_code")) preferences[PreferencesKeys.CHAT_DEFAULT_CODE_KEY] = json.getString("chat_default_code")
                if (json.has("chat_default_iso")) preferences[PreferencesKeys.CHAT_DEFAULT_ISO_KEY] = json.getString("chat_default_iso")
                if (json.has("chat_pause_history")) preferences[PreferencesKeys.CHAT_PAUSE_HISTORY_KEY] = json.getBoolean("chat_pause_history")
            }
            true
        } catch (e: Exception) {
            AppLogger.e("UserStoreBase", "JSON import failed", e)
            false
        }
    }
}
