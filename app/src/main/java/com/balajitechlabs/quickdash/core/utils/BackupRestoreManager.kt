package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import com.balajitechlabs.quickdash.core.data.dataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.balajitechlabs.quickdash.core.data.database.AppDatabase
import com.balajitechlabs.quickdash.core.data.database.NoteEntity
import com.balajitechlabs.quickdash.core.data.EncryptedPrefsHelper
import com.google.gson.reflect.TypeToken

object BackupRestoreManager {

    private val gson = Gson()

    suspend fun getBackupJsonString(context: Context): String = withContext(Dispatchers.IO) {
        val preferences = context.dataStore.data.first()
        val dataMap = mutableMapOf<String, Any>()
        
        for ((key, value) in preferences.asMap()) {
            dataMap[key.name] = value
        }

        // Include Room DB Notes
        val notes = AppDatabase.getDatabase(context).noteDao().getAllNotesSync()
        dataMap["ROOM_NOTES"] = notes

        // Include EncryptedPrefsHelper content
        val encryptedKeys = listOf("notes_history", "clipboard_history", "clipboard_pinned", "wifi_password")
        val encryptedMap = mutableMapOf<String, String>()
        for (k in encryptedKeys) {
            val v = EncryptedPrefsHelper.getString(k, null)
            if (v != null) {
                encryptedMap[k] = v
            }
        }
        dataMap["ENCRYPTED_PREFS"] = encryptedMap

        gson.toJson(dataMap)
    }

    suspend fun restoreFromJsonString(context: Context, jsonString: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            val dataMap: Map<String, Any> = gson.fromJson(jsonString, mapType)

            context.dataStore.edit { preferences ->
                for ((keyName, value) in dataMap) {
                    if (keyName == "ROOM_NOTES" || keyName == "ENCRYPTED_PREFS") continue
                    when {
                        value is Boolean -> preferences[booleanPreferencesKey(keyName)] = value
                        value is String -> preferences[stringPreferencesKey(keyName)] = value
                        value is Number -> {
                            when (keyName) {
                                "corner_radius", "border_width", "haptic_duration", "font_scale" -> {
                                    preferences[androidx.datastore.preferences.core.floatPreferencesKey(keyName)] = value.toFloat()
                                }
                                "last_clipboard_clean_time", "clipboard_clear_delay", "last_telegram_update_id" -> {
                                    preferences[androidx.datastore.preferences.core.longPreferencesKey(keyName)] = value.toLong()
                                }
                                else -> {
                                    preferences[stringPreferencesKey(keyName)] = value.toString()
                                }
                            }
                        }
                    }
                }
            }

            // Restore encrypted prefs if present
            if (dataMap.containsKey("ENCRYPTED_PREFS")) {
                val encMapJson = gson.toJson(dataMap["ENCRYPTED_PREFS"])
                val encType = object : TypeToken<Map<String, String>>() {}.type
                val encMap: Map<String, String>? = gson.fromJson(encMapJson, encType)
                encMap?.forEach { (k, v) ->
                    EncryptedPrefsHelper.putString(k, v)
                }
            }

            // Restore Room DB Notes if they exist in the backup
            if (dataMap.containsKey("ROOM_NOTES")) {
                val notesJson = gson.toJson(dataMap["ROOM_NOTES"])
                val listType = object : TypeToken<List<NoteEntity>>() {}.type
                val notes: List<NoteEntity> = gson.fromJson(notesJson, listType) ?: emptyList()
                if (notes.isNotEmpty()) {
                    AppDatabase.getDatabase(context).noteDao().insertAll(notes)
                }
            }
            
            AppLogger.i("Restore", "Successfully restored JSON backup")
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e("Restore", "Failed to restore JSON backup", e)
            Result.failure(e)
        }
    }

    suspend fun backupDataStore(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = getBackupJsonString(context)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))
            
            AppLogger.i("Backup", "Successfully backed up data to $uri")
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e("Backup", "Failed to backup data", e)
            Result.failure(e)
        }
    }

    suspend fun restoreDataStore(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return@withContext Result.failure(Exception("Could not open input stream"))
            
            restoreFromJsonString(context, jsonString)
        } catch (e: Exception) {
            AppLogger.e("Restore", "Failed to restore data", e)
            Result.failure(e)
        }
    }
}
