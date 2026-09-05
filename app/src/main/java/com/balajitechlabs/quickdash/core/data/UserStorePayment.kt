/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: UserStorePayment.kt
 * Description: Payment preferences store managing UPI VPA IDs, PayPal identifiers, payee names, and QR history.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.balajitechlabs.quickdash.core.data.prefs.PreferencesKeys
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class UserStorePayment(context: Context) : UserStoreUi(context) {

    val upiIds: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val rawIds = preferences[PreferencesKeys.UPI_IDS_KEY]
        if (!rawIds.isNullOrBlank()) {
            rawIds.split(",").filter { it.isNotBlank() }
        } else {
            val legacyId = preferences[PreferencesKeys.UPI_ID_KEY]
            if (!legacyId.isNullOrBlank()) listOf(legacyId) else emptyList()
        }
    }

    val defaultUpiId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_UPI_ID_KEY] ?: ""
    }

    val upiId: Flow<String?> = upiIds.map { it.firstOrNull() }

    suspend fun saveUpiIds(ids: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UPI_IDS_KEY] = ids.joinToString(",")
            if (ids.isNotEmpty()) {
                preferences[PreferencesKeys.UPI_ID_KEY] = ids.first()
                val currentDefault = preferences[PreferencesKeys.DEFAULT_UPI_ID_KEY]
                if (currentDefault == null || !ids.contains(currentDefault)) {
                    preferences[PreferencesKeys.DEFAULT_UPI_ID_KEY] = ids.first()
                }
            } else {
                preferences.remove(PreferencesKeys.UPI_ID_KEY)
                preferences.remove(PreferencesKeys.DEFAULT_UPI_ID_KEY)
            }
        }
    }

    suspend fun saveDefaultUpiId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_UPI_ID_KEY] = id
        }
    }

    suspend fun saveUpiId(id: String) {
        saveUpiIds(listOf(id))
    }

    val usePaypal: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USE_PAYPAL_KEY] ?: false
    }

    val paypalIds: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val rawIds = preferences[PreferencesKeys.PAYPAL_IDS_KEY]
        if (!rawIds.isNullOrBlank()) {
            rawIds.split(",").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    val defaultPaypalId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_PAYPAL_ID_KEY] ?: ""
    }

    suspend fun saveUsePaypal(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_PAYPAL_KEY] = use
        }
    }

    suspend fun savePaypalIds(ids: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PAYPAL_IDS_KEY] = ids.joinToString(",")
            if (ids.isNotEmpty()) {
                val currentDefault = preferences[PreferencesKeys.DEFAULT_PAYPAL_ID_KEY]
                if (currentDefault == null || !ids.contains(currentDefault)) {
                    preferences[PreferencesKeys.DEFAULT_PAYPAL_ID_KEY] = ids.first()
                }
            } else {
                preferences.remove(PreferencesKeys.DEFAULT_PAYPAL_ID_KEY)
            }
        }
    }

    suspend fun saveDefaultPaypalId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_PAYPAL_ID_KEY] = id
        }
    }

    val payeeName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PAYEE_NAME_KEY] ?: ""
    }

    suspend fun savePayeeName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PAYEE_NAME_KEY] = name
        }
    }

    val recentAmounts: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val serialized = preferences[PreferencesKeys.RECENT_AMOUNTS_KEY] ?: "100,200,500"
        serialized.split(",").filter { it.isNotBlank() }
    }

    suspend fun saveRecentAmount(amount: String) {
        if (amount.isBlank()) return
        context.dataStore.edit { preferences ->
            val currentList = (preferences[PreferencesKeys.RECENT_AMOUNTS_KEY] ?: "100,200,500")
                .split(",")
                .filter { it.isNotBlank() }
                .toMutableList()
            currentList.remove(amount)
            currentList.add(0, amount)
            val newList = currentList.take(3)
            preferences[PreferencesKeys.RECENT_AMOUNTS_KEY] = newList.joinToString(",")
        }
    }

    val showUpiId: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_UPI_ID_KEY] ?: true
    }

    suspend fun saveShowUpiId(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_UPI_ID_KEY] = show
        }
    }

    val defaultPaymentApp: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_PAYMENT_APP_KEY] ?: "ANY"
    }

    suspend fun saveDefaultPaymentApp(app: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_PAYMENT_APP_KEY] = app
        }
    }

    val qrHistory: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.QR_HISTORY_KEY] ?: "[]"
    }

    suspend fun saveQrHistoryItem(
        amount: String,
        note: String,
        upiId: String,
        targetApp: String,
        category: String
    ) {
        val newEntry = JsonObject().apply {
            addProperty("amount", amount)
            addProperty("note", note)
            addProperty("upiId", upiId)
            addProperty("targetApp", targetApp)
            addProperty("category", category)
            addProperty("timestamp", System.currentTimeMillis())
        }
        context.dataStore.edit { preferences ->
            val current = try {
                val raw = preferences[PreferencesKeys.QR_HISTORY_KEY] ?: "[]"
                JsonParser.parseString(raw).asJsonArray
            } catch (_: Exception) {
                JsonArray()
            }

            val newArray = JsonArray()
            newArray.add(newEntry)
            current.forEach { newArray.add(it) }

            val trimmed = JsonArray()
            newArray.take(100).forEach { trimmed.add(it) }
            preferences[PreferencesKeys.QR_HISTORY_KEY] = trimmed.toString()
        }
    }

    suspend fun clearQrHistory() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.QR_HISTORY_KEY] = "[]"
        }
    }
}
