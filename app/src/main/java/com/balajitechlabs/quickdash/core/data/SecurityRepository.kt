/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: SecurityRepository.kt
 * Description: Repository coordinating biometric authentication status, app lock state, and secure window mode.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_settings")

@Singleton
class SecurityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) {
    private val dataStore = context.securityDataStore

    companion object {
        val GITHUB_ACCESS_TOKEN_KEY = stringPreferencesKey("github_access_token")
        val SERVER_CREDENTIALS_KEY = stringPreferencesKey("server_credentials")
    }

    val githubAccessToken: Flow<String> = dataStore.data.map { preferences ->
        preferences[GITHUB_ACCESS_TOKEN_KEY]?.let { decrypt(it) } ?: ""
    }

    suspend fun saveGithubAccessToken(token: String) {
        dataStore.edit { preferences ->
            preferences[GITHUB_ACCESS_TOKEN_KEY] = encrypt(token)
        }
    }

    val serverCredentials: Flow<String> = dataStore.data.map { preferences ->
        preferences[SERVER_CREDENTIALS_KEY]?.let { decrypt(it) } ?: "{}"
    }

    suspend fun saveServerCredentials(json: String) {
        dataStore.edit { preferences ->
            preferences[SERVER_CREDENTIALS_KEY] = encrypt(json)
        }
    }

    private fun encrypt(value: String): String {
        return try {
            cryptoManager.encrypt(value.toByteArray())
        } catch (e: Exception) {
            Log.e("QuickDash", "Error occurred: ${e.message}", e)
            ""
        }
    }

    private fun decrypt(value: String): String {
        return try {
            if (value.isBlank()) return ""
            String(cryptoManager.decrypt(value))
        } catch (e: Exception) {
            Log.e("QuickDash", "Error occurred: ${e.message}", e)
            ""
        }
    }
}
