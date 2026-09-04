/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/clipboard
 * File: ClipboardRepository.kt
 * Description: EssentialX-styled component for features/clipboard supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.clipboard.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.balajitechlabs.quickdash.core.data.EncryptedPrefsHelper
import com.balajitechlabs.quickdash.core.data.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val CLIPBOARD_HISTORY_KEY = "clipboard_history"
        val CLIPBOARD_PINNED_KEY = "clipboard_pinned"
        val CLIPBOARD_AUTOCLEAN_INTERVAL_KEY = stringPreferencesKey("clipboard_autoclean_interval")
        val LAST_CLIPBOARD_CLEAN_TIME_KEY = longPreferencesKey("last_clipboard_clean_time")
        val CLIPBOARD_CLEAR_DELAY_KEY = longPreferencesKey("clipboard_clear_delay")
    }

    val clipboardHistory: Flow<String> = EncryptedPrefsHelper.getStringFlow(CLIPBOARD_HISTORY_KEY, "[]")
    val clipboardPinned: Flow<String> = EncryptedPrefsHelper.getStringFlow(CLIPBOARD_PINNED_KEY, "[]")

    suspend fun saveClipboardHistory(json: String) {
        EncryptedPrefsHelper.putString(CLIPBOARD_HISTORY_KEY, json)
    }

    suspend fun saveClipboardPinned(json: String) {
        EncryptedPrefsHelper.putString(CLIPBOARD_PINNED_KEY, json)
    }

    val clipboardAutocleanInterval: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CLIPBOARD_AUTOCLEAN_INTERVAL_KEY] ?: "Never"
    }

    suspend fun saveClipboardAutocleanInterval(interval: String) {
        context.dataStore.edit { preferences ->
            preferences[CLIPBOARD_AUTOCLEAN_INTERVAL_KEY] = interval
        }
    }

    val lastClipboardCleanTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_CLIPBOARD_CLEAN_TIME_KEY] ?: 0L
    }

    suspend fun saveLastClipboardCleanTime(timeMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_CLIPBOARD_CLEAN_TIME_KEY] = timeMs
        }
    }
    
    val clipboardClearDelay: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[CLIPBOARD_CLEAR_DELAY_KEY] ?: 60000L
    }

    suspend fun saveClipboardClearDelay(delayMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[CLIPBOARD_CLEAR_DELAY_KEY] = delayMs
        }
    }
}
