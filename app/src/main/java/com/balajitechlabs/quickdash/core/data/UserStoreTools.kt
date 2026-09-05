/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: UserStoreTools.kt
 * Description: Preferences delegate managing tool order, clipboard, notes, chat, Wi-Fi, and cloud backups.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.balajitechlabs.quickdash.core.data.prefs.PreferencesKeys
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

open class UserStoreTools(context: Context) : UserStorePayment(context) {

    val toolOrder: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TOOL_ORDER_KEY] ?: ""
    }

    val favoriteTools: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FAVORITE_TOOLS_KEY] ?: ""
    }

    suspend fun saveToolOrder(order: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOOL_ORDER_KEY] = order
        }
    }

    suspend fun saveFavoriteTools(favorites: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FAVORITE_TOOLS_KEY] = favorites
        }
    }

    val showToolDescriptions: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_TOOL_DESCRIPTIONS_KEY] ?: true
    }

    suspend fun saveShowToolDescriptions(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_TOOL_DESCRIPTIONS_KEY] = show
        }
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.APP_LANGUAGE_KEY] ?: "English" }

    suspend fun saveAppLanguage(lang: String) {
        context.dataStore.edit { it[PreferencesKeys.APP_LANGUAGE_KEY] = lang }
    }

    val pinnedToolsJson: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PINNED_TOOLS_KEY] ?: "[]"
    }

    suspend fun savePinnedToolsJson(json: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PINNED_TOOLS_KEY] = json
        }
    }

    suspend fun togglePinnedTool(toolName: String) {
        val currentJson = pinnedToolsJson.first()
        val gson = Gson()
        val type = object : TypeToken<MutableList<String>>() {}.type
        val list: MutableList<String> = try {
            gson.fromJson(currentJson, type) ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        if (list.contains(toolName)) {
            list.remove(toolName)
        } else {
            list.add(toolName)
        }
        savePinnedToolsJson(gson.toJson(list))
    }

    val radialCustomTools: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.RADIAL_CUSTOM_TOOLS_KEY] ?: "upi,notes,calc,timer"
        val list = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (list.size >= 4) list.take(4) else listOf("upi", "notes", "calc", "timer")
    }

    suspend fun saveRadialCustomTools(tools: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RADIAL_CUSTOM_TOOLS_KEY] = tools.take(4).joinToString(",")
        }
    }

    // Google Profile & Backup
    val googleProfileName: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.GOOGLE_PROFILE_NAME_KEY] ?: "" }
    suspend fun saveGoogleProfileName(name: String) {
        context.dataStore.edit { it[PreferencesKeys.GOOGLE_PROFILE_NAME_KEY] = name }
    }

    val googleProfilePhoto: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.GOOGLE_PROFILE_PHOTO_KEY] ?: "" }
    suspend fun saveGoogleProfilePhoto(url: String) {
        context.dataStore.edit { it[PreferencesKeys.GOOGLE_PROFILE_PHOTO_KEY] = url }
    }

    val driveBackupLink: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.DRIVE_BACKUP_LINK_KEY] ?: "" }
    suspend fun saveDriveBackupLink(link: String) {
        context.dataStore.edit { it[PreferencesKeys.DRIVE_BACKUP_LINK_KEY] = link }
    }

    val googleProfileEmail: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.GOOGLE_PROFILE_EMAIL_KEY] ?: "" }
    suspend fun saveGoogleProfileEmail(email: String) {
        context.dataStore.edit { it[PreferencesKeys.GOOGLE_PROFILE_EMAIL_KEY] = email }
    }

    suspend fun clearGoogleProfile() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GOOGLE_PROFILE_NAME_KEY] = ""
            preferences[PreferencesKeys.GOOGLE_PROFILE_PHOTO_KEY] = ""
            preferences[PreferencesKeys.GOOGLE_PROFILE_EMAIL_KEY] = ""
            preferences[PreferencesKeys.DRIVE_BACKUP_LINK_KEY] = ""
        }
    }

    // Quick Chat
    val chatDefaultCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CHAT_DEFAULT_CODE_KEY] ?: "91"
    }

    val chatDefaultIso: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CHAT_DEFAULT_ISO_KEY] ?: "IN"
    }

    val chatHistory: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.CHAT_HISTORY_KEY]
        if (!raw.isNullOrBlank()) {
            raw.split(";").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    val chatPauseHistory: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CHAT_PAUSE_HISTORY_KEY] ?: false
    }

    suspend fun saveChatDefaultCountry(code: String, iso: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CHAT_DEFAULT_CODE_KEY] = code
            preferences[PreferencesKeys.CHAT_DEFAULT_ISO_KEY] = iso
        }
    }

    suspend fun saveChatNumberToHistory(number: String, flag: String) {
        context.dataStore.edit { preferences ->
            val paused = preferences[PreferencesKeys.CHAT_PAUSE_HISTORY_KEY] ?: false
            if (!paused) {
                val current = (preferences[PreferencesKeys.CHAT_HISTORY_KEY] ?: "")
                    .split(";")
                    .filter { it.isNotBlank() }
                    .toMutableList()
                val entry = "$number:$flag"
                current.remove(entry)
                current.add(0, entry)
                preferences[PreferencesKeys.CHAT_HISTORY_KEY] = current.take(20).joinToString(";")
            }
        }
    }

    suspend fun clearChatHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CHAT_HISTORY_KEY)
        }
    }

    suspend fun saveChatPauseHistory(pause: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CHAT_PAUSE_HISTORY_KEY] = pause
        }
    }

    // Notes History
    val notesHistory: Flow<String> = EncryptedPrefsHelper.getStringFlow(PreferencesKeys.NOTES_HISTORY_KEY.name, "[]")

    suspend fun saveNotesHistory(json: String) {
        EncryptedPrefsHelper.putString(PreferencesKeys.NOTES_HISTORY_KEY.name, json)
    }

    // Clipboard
    val clipboardHistory: Flow<String> = EncryptedPrefsHelper.getStringFlow(PreferencesKeys.CLIPBOARD_HISTORY_KEY.name, "[]")

    suspend fun saveClipboardHistory(json: String) {
        EncryptedPrefsHelper.putString(PreferencesKeys.CLIPBOARD_HISTORY_KEY.name, json)
    }

    val clipboardPinned: Flow<String> = EncryptedPrefsHelper.getStringFlow(PreferencesKeys.CLIPBOARD_PINNED_KEY.name, "[]")

    suspend fun saveClipboardPinned(json: String) {
        EncryptedPrefsHelper.putString(PreferencesKeys.CLIPBOARD_PINNED_KEY.name, json)
    }

    suspend fun clearClipboardHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CLIPBOARD_HISTORY_KEY)
        }
    }

    val clipboardAutocleanInterval: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CLIPBOARD_AUTOCLEAN_INTERVAL_KEY] ?: "OFF"
    }

    suspend fun setClipboardAutocleanInterval(interval: String) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.CLIPBOARD_AUTOCLEAN_INTERVAL_KEY] = interval }
    }

    val clipboardClearDelay: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CLIPBOARD_CLEAR_DELAY_KEY] ?: -1L
    }

    suspend fun saveClipboardClearDelay(delayMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLIPBOARD_CLEAR_DELAY_KEY] = delayMs
        }
    }

    val lastClipboardCleanTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_CLIPBOARD_CLEAN_TIME_KEY] ?: 0L
    }

    suspend fun saveLastClipboardCleanTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_CLIPBOARD_CLEAN_TIME_KEY] = time
        }
    }

    // Timer History
    val timerHistory: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TIMER_HISTORY_KEY] ?: "[]"
    }

    suspend fun saveTimerHistory(json: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TIMER_HISTORY_KEY] = json
        }
    }

    // Wi-Fi
    val wifiSsid: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WIFI_SSID_KEY] ?: ""
    }

    val wifiPassword: Flow<String> = EncryptedPrefsHelper.getStringFlow("wifi_password", "")

    suspend fun saveWifiCredentials(ssid: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIFI_SSID_KEY] = ssid
        }
        EncryptedPrefsHelper.putString("wifi_password", password)
    }

    val wifiHotspotMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WIFI_HOTSPOT_MODE_KEY] ?: false
    }

    suspend fun saveWifiHotspotMode(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.WIFI_HOTSPOT_MODE_KEY] = enabled }
    }

    val wifiHistory: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WIFI_HISTORY_KEY] ?: "[]"
    }

    suspend fun addWifiHistory(ssid: String, password: String, securityType: String = "WPA/WPA2") {
        context.dataStore.edit { preferences ->
            val current = try {
                val raw = preferences[PreferencesKeys.WIFI_HISTORY_KEY] ?: "[]"
                JsonParser.parseString(raw).asJsonArray
            } catch (_: Exception) {
                JsonArray()
            }

            var existingEntry: JsonObject? = null
            val filtered = JsonArray()
            current.forEach { el ->
                val obj = el.asJsonObject
                if (obj.get("ssid")?.asString == ssid) {
                    existingEntry = obj
                } else {
                    filtered.add(el)
                }
            }

            val count = if (existingEntry != null) {
                (existingEntry.get("shareCount")?.asInt ?: 0) + 1
            } else {
                1
            }

            val newEntry = JsonObject().apply {
                addProperty("ssid", ssid)
                addProperty("password", password)
                addProperty("securityType", securityType)
                addProperty("shareCount", count)
                addProperty("lastSharedAt", System.currentTimeMillis())
                addProperty("savedAt", existingEntry?.get("savedAt")?.asLong ?: System.currentTimeMillis())
            }

            val newArray = JsonArray()
            newArray.add(newEntry)
            filtered.forEach { newArray.add(it) }

            val trimmed = JsonArray()
            newArray.take(50).forEach { trimmed.add(it) }
            preferences[PreferencesKeys.WIFI_HISTORY_KEY] = trimmed.toString()
        }
    }

    suspend fun removeWifiHistoryEntry(ssid: String) {
        context.dataStore.edit { preferences ->
            val current = try {
                JsonParser.parseString(preferences[PreferencesKeys.WIFI_HISTORY_KEY] ?: "[]").asJsonArray
            } catch (_: Exception) {
                JsonArray()
            }
            val filtered = JsonArray()
            current.forEach { el ->
                if (el.asJsonObject.get("ssid")?.asString != ssid) filtered.add(el)
            }
            preferences[PreferencesKeys.WIFI_HISTORY_KEY] = filtered.toString()
        }
    }

    suspend fun clearWifiHistory() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIFI_HISTORY_KEY] = "[]"
        }
    }

    // Search History
    val searchHistory: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SEARCH_HISTORY_KEY] ?: "[]"
    }

    suspend fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        context.dataStore.edit { preferences ->
            val raw = preferences[PreferencesKeys.SEARCH_HISTORY_KEY] ?: "[]"
            val list = try {
                Gson().fromJson<List<String>>(
                    raw,
                    object : TypeToken<List<String>>() {}.type
                )?.toMutableList() ?: mutableListOf()
            } catch (_: Exception) {
                mutableListOf()
            }
            list.remove(query)
            list.add(0, query)
            preferences[PreferencesKeys.SEARCH_HISTORY_KEY] = Gson().toJson(list.take(50))
        }
    }

    suspend fun clearSearchHistory() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SEARCH_HISTORY_KEY] = "[]"
        }
    }

    val customSearchEngines: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CUSTOM_SEARCH_ENGINES_KEY] ?: "[]"
    }

    suspend fun saveCustomSearchEngines(json: String) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.CUSTOM_SEARCH_ENGINES_KEY] = json }
    }

    val firebaseBlogPosts: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIREBASE_BLOG_POSTS_KEY] ?: "[]"
    }

    suspend fun saveFirebaseBlogPosts(json: String) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.FIREBASE_BLOG_POSTS_KEY] = json }
    }

    val notificationHistory: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATION_HISTORY_KEY] ?: "[]"
    }

    suspend fun saveNotificationHistory(json: String) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.NOTIFICATION_HISTORY_KEY] = json }
    }

    val hiddenNotifications: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HIDDEN_NOTIFICATIONS_KEY] ?: "[]"
    }

    suspend fun saveHiddenNotifications(json: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDDEN_NOTIFICATIONS_KEY] = json
        }
    }

    val pinnedNotifications: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PINNED_NOTIFICATIONS_KEY] ?: "[]"
    }

    suspend fun savePinnedNotifications(json: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PINNED_NOTIFICATIONS_KEY] = json
        }
    }

    val pollVotes: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.POLL_VOTES_KEY] ?: "{}"
    }

    suspend fun savePollVote(json: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.POLL_VOTES_KEY] = json
        }
    }

    val qrUseEmojiOverlay: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.QR_USE_EMOJI_OVERLAY_KEY] ?: false
    }

    suspend fun saveQrUseEmojiOverlay(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.QR_USE_EMOJI_OVERLAY_KEY] = enabled }
    }

    val customBackupPath: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CUSTOM_BACKUP_PATH_KEY]
    }

    suspend fun saveCustomBackupPath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path == null) {
                preferences.remove(PreferencesKeys.CUSTOM_BACKUP_PATH_KEY)
            } else {
                preferences[PreferencesKeys.CUSTOM_BACKUP_PATH_KEY] = path
            }
        }
    }

    val serverCredentials: Flow<String> = EncryptedPrefsHelper.getStringFlow("server_credentials", "{}")

    suspend fun saveServerCredentials(json: String) {
        EncryptedPrefsHelper.putString("server_credentials", json)
    }

    val githubAccessToken: Flow<String> = EncryptedPrefsHelper.getStringFlow("github_access_token", "")

    suspend fun saveGithubAccessToken(token: String) {
        EncryptedPrefsHelper.putString("github_access_token", token)
    }
}
