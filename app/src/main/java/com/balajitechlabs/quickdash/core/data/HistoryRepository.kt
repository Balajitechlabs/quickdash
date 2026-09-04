/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: HistoryRepository.kt
 * Description: EssentialX-styled component for core/data supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.historyDataStore: DataStore<HistoryPreferences> by dataStore(
    fileName = "history.pb",
    serializer = HistorySerializer
)

@Singleton
class HistoryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.historyDataStore

    val historyPreferencesFlow: Flow<HistoryPreferences> = dataStore.data
    
    val notificationHistory: Flow<String> = historyPreferencesFlow.map { preferences ->
        preferences.notificationHistory.ifBlank { "[]" }
    }

    suspend fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        dataStore.updateData { preferences ->
            val list = preferences.searchHistoryList.toMutableList()
            list.remove(query)
            list.add(0, query)
            val trimmed = list.take(50)
            preferences.toBuilder()
                .clearSearchHistory()
                .addAllSearchHistory(trimmed)
                .build()
        }
    }

    suspend fun clearSearchHistory() {
        dataStore.updateData { preferences ->
            preferences.toBuilder().clearSearchHistory().build()
        }
    }

    suspend fun addWifiHistory(ssid: String, password: String, securityType: String = "WPA/WPA2") {
        dataStore.updateData { preferences ->
            val list = preferences.wifiHistoryList.toMutableList()
            val existing = list.find { it.ssid == ssid }
            
            val count = existing?.let { it.shareCount + 1 } ?: 1
            val savedAt = existing?.savedAt ?: System.currentTimeMillis()
            
            list.remove(existing)
            
            val newEntry = WifiHistoryEntry.newBuilder()
                .setSsid(ssid)
                .setPassword(password)
                .setSecurityType(securityType)
                .setShareCount(count)
                .setLastSharedAt(System.currentTimeMillis())
                .setSavedAt(savedAt)
                .build()
                
            list.add(0, newEntry)
            val trimmed = list.take(50)
            
            preferences.toBuilder()
                .clearWifiHistory()
                .addAllWifiHistory(trimmed)
                .build()
        }
    }

    suspend fun removeWifiHistoryEntry(ssid: String) {
        dataStore.updateData { preferences ->
            val list = preferences.wifiHistoryList.filter { it.ssid != ssid }
            preferences.toBuilder()
                .clearWifiHistory()
                .addAllWifiHistory(list)
                .build()
        }
    }

    suspend fun clearWifiHistory() {
        dataStore.updateData { preferences ->
            preferences.toBuilder().clearWifiHistory().build()
        }
    }

    suspend fun saveQrHistoryItem(amount: String, note: String, upiId: String, targetApp: String, category: String) {
        dataStore.updateData { preferences ->
            val newEntry = QrHistoryEntry.newBuilder()
                .setAmount(amount)
                .setNote(note)
                .setUpiId(upiId)
                .setTargetApp(targetApp)
                .setCategory(category)
                .setTimestamp(System.currentTimeMillis())
                .build()
                
            val list = preferences.qrHistoryList.toMutableList()
            list.add(0, newEntry)
            val trimmed = list.take(100)
            
            preferences.toBuilder()
                .clearQrHistory()
                .addAllQrHistory(trimmed)
                .build()
        }
    }

    suspend fun clearQrHistory() {
        dataStore.updateData { preferences ->
            preferences.toBuilder().clearQrHistory().build()
        }
    }
    
    // Notifications and Poll Votes kept as JSON strings for backward compatibility
    suspend fun saveHiddenNotifications(json: String) {
        dataStore.updateData { it.toBuilder().setHiddenNotifications(json).build() }
    }
    
    suspend fun savePinnedNotifications(json: String) {
        dataStore.updateData { it.toBuilder().setPinnedNotifications(json).build() }
    }
    
    suspend fun saveNotificationHistory(json: String) {
        dataStore.updateData { it.toBuilder().setNotificationHistory(json).build() }
    }
    
    suspend fun savePollVote(json: String) {
        dataStore.updateData { it.toBuilder().setPollVotes(json).build() }
    }
}
