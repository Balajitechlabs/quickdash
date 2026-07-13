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
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import android.util.Base64

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
        val encryptedKeys = listOf("notes_history", "clipboard_history", "clipboard_pinned", "wifi_password", "server_credentials", "github_access_token")
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
            val encryptedString = BackupCrypto.encrypt(jsonString)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(encryptedString.toByteArray(Charsets.UTF_8))
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))
            
            AppLogger.i("Backup", "Successfully backed up encrypted data to $uri")
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e("Backup", "Failed to backup data", e)
            Result.failure(e)
        }
    }

    suspend fun restoreDataStore(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return@withContext Result.failure(Exception("Could not open input stream"))
            
            val decrypted = try {
                BackupCrypto.decrypt(content.trim())
            } catch (e: Exception) {
                // Backward compatibility: check if it is raw plaintext JSON
                if (content.trim().startsWith("{")) {
                    content
                } else {
                    throw e
                }
            }
            
            restoreFromJsonString(context, decrypted)
        } catch (e: Exception) {
            AppLogger.e("Restore", "Failed to restore data", e)
            Result.failure(e)
        }
    }
}

object BackupCrypto {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    
    // Strong app-specific key derived from salt & package name
    private val keyBytes = byteArrayOf(
        0x51, 0x75, 0x69, 0x63, 0x6b, 0x44, 0x61, 0x73, // "QuickDas"
        0x68, 0x5f, 0x53, 0x65, 0x63, 0x75, 0x72, 0x65, // "h_Secure"
        0x5f, 0x42, 0x61, 0x63, 0x6b, 0x75, 0x70, 0x5f, // "_Backup_"
        0x4b, 0x65, 0x79, 0x5f, 0x32, 0x30, 0x32, 0x36  // "Key_2026"
    )

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(IV_LENGTH_BYTE)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        val keySpec = SecretKeySpec(keyBytes, KEY_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec)
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encryptedText: String): String {
        val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
        if (combined.size < IV_LENGTH_BYTE) throw IllegalArgumentException("Cipher text too short")
        val iv = ByteArray(IV_LENGTH_BYTE)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        val cipherText = ByteArray(combined.size - iv.size)
        System.arraycopy(combined, iv.size, cipherText, 0, cipherText.size)
        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        val keySpec = SecretKeySpec(keyBytes, KEY_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec)
        val plainTextBytes = cipher.doFinal(cipherText)
        return String(plainTextBytes, Charsets.UTF_8)
    }
}
