package com.balajitechlabs.quickdash.core.data.backup

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.data.database.AppDatabase
import com.balajitechlabs.quickdash.core.data.database.NoteEntity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class BackupMetadata(
    val version: Int = 1,
    val appVersion: String = "5.2.1",
    val appVersionCode: Int = 521,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceName: String = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
    val isEncrypted: Boolean = false
)

data class NoteBackupItem(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isPinned: Boolean,
    val isArchived: Boolean
)

data class BackupPayload(
    val metadata: BackupMetadata,
    val stringPreferences: Map<String, String> = emptyMap(),
    val booleanPreferences: Map<String, Boolean> = emptyMap(),
    val intPreferences: Map<String, Int> = emptyMap(),
    val longPreferences: Map<String, Long> = emptyMap(),
    val floatPreferences: Map<String, Float> = emptyMap(),
    val notes: List<NoteBackupItem> = emptyList()
)

sealed class BackupResult {
    data class Success(val notesCount: Int, val preferencesCount: Int) : BackupResult()
    data class Error(val message: String, val cause: Throwable? = null) : BackupResult()
}

enum class RestoreStrategy {
    MERGE,
    REPLACE
}

class BackupManager(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val database = AppDatabase.getDatabase(context)
    private val userStore = UserStore(context)

    companion object {
        private val MAGIC_HEADER = byteArrayOf(0x51, 0x44, 0x42, 0x4B) // "QDBK"
        private const val CURRENT_FORMAT_VERSION: Byte = 1
        private const val FLAG_PLAINTEXT: Byte = 0
        private const val FLAG_ENCRYPTED: Byte = 1

        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_SIZE_BYTES = 16
        private const val IV_SIZE_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
    }

    /**
     * Export full backup into [outputStream], optionally protected with [passphrase].
     */
    suspend fun exportBackup(
        passphrase: String?,
        outputStream: OutputStream
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            // 1. Gather Preferences from DataStore
            val allPrefs = context.userStoreDataStore.data.first().asMap()
            val stringMap = mutableMapOf<String, String>()
            val boolMap = mutableMapOf<String, Boolean>()
            val intMap = mutableMapOf<String, Int>()
            val longMap = mutableMapOf<String, Long>()
            val floatMap = mutableMapOf<String, Float>()

            for ((key, value) in allPrefs) {
                when (value) {
                    is String -> stringMap[key.name] = value
                    is Boolean -> boolMap[key.name] = value
                    is Int -> intMap[key.name] = value
                    is Long -> longMap[key.name] = value
                    is Float -> floatMap[key.name] = value
                }
            }

            // 2. Gather Notes from Room
            val notes = database.noteDao().getAllNotesSync().map {
                NoteBackupItem(
                    id = it.id,
                    text = it.text,
                    timestamp = it.timestamp,
                    isPinned = it.isPinned,
                    isArchived = it.isArchived
                )
            }

            val isEncrypted = !passphrase.isNullOrBlank()
            val metadata = BackupMetadata(
                version = 1,
                timestamp = System.currentTimeMillis(),
                isEncrypted = isEncrypted
            )

            val payload = BackupPayload(
                metadata = metadata,
                stringPreferences = stringMap,
                booleanPreferences = boolMap,
                intPreferences = intMap,
                longPreferences = longMap,
                floatPreferences = floatMap,
                notes = notes
            )

            val jsonString = gson.toJson(payload)
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

            if (isEncrypted) {
                val secureRandom = SecureRandom()
                val salt = ByteArray(SALT_SIZE_BYTES).also { secureRandom.nextBytes(it) }
                val iv = ByteArray(IV_SIZE_BYTES).also { secureRandom.nextBytes(it) }

                val secretKey = deriveKey(passphrase!!.toCharArray(), salt)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
                val ciphertext = cipher.doFinal(jsonBytes)

                outputStream.write(MAGIC_HEADER)
                outputStream.write(byteArrayOf(CURRENT_FORMAT_VERSION, FLAG_ENCRYPTED))
                outputStream.write(salt)
                outputStream.write(iv)
                outputStream.write(ciphertext)
            } else {
                outputStream.write(MAGIC_HEADER)
                outputStream.write(byteArrayOf(CURRENT_FORMAT_VERSION, FLAG_PLAINTEXT))
                outputStream.write(jsonBytes)
            }

            outputStream.flush()
            val totalPrefCount = stringMap.size + boolMap.size + intMap.size + longMap.size + floatMap.size
            BackupResult.Success(notesCount = notes.size, preferencesCount = totalPrefCount)
        } catch (e: Exception) {
            BackupResult.Error("Export failed: ${e.localizedMessage ?: e.message}", e)
        }
    }

    /**
     * Inspect backup stream header to check whether encryption is active.
     */
    suspend fun inspectBackup(inputStream: InputStream): Pair<Boolean, Boolean> = withContext(Dispatchers.IO) {
        val header = ByteArray(6)
        val read = inputStream.read(header)
        if (read < 6) return@withContext Pair(false, false)

        val isQdMagic = header[0] == MAGIC_HEADER[0] &&
                header[1] == MAGIC_HEADER[1] &&
                header[2] == MAGIC_HEADER[2] &&
                header[3] == MAGIC_HEADER[3]

        if (!isQdMagic) {
            // Raw JSON fallback
            return@withContext Pair(true, false)
        }

        val isEncrypted = header[5] == FLAG_ENCRYPTED
        Pair(true, isEncrypted)
    }

    /**
     * Import backup from [inputStream], validating [passphrase] if encrypted.
     */
    suspend fun importBackup(
        passphrase: String?,
        inputStream: InputStream,
        strategy: RestoreStrategy = RestoreStrategy.MERGE
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            val allBytes = inputStream.readBytes()
            if (allBytes.size < 6) {
                return@withContext BackupResult.Error("Invalid or empty backup file.")
            }

            val isQdMagic = allBytes[0] == MAGIC_HEADER[0] &&
                    allBytes[1] == MAGIC_HEADER[1] &&
                    allBytes[2] == MAGIC_HEADER[2] &&
                    allBytes[3] == MAGIC_HEADER[3]

            val jsonString: String
            if (isQdMagic) {
                val flag = allBytes[5]
                if (flag == FLAG_ENCRYPTED) {
                    if (passphrase.isNullOrBlank()) {
                        return@withContext BackupResult.Error("This backup is encrypted. Please enter the password.")
                    }
                    if (allBytes.size < 6 + SALT_SIZE_BYTES + IV_SIZE_BYTES) {
                        return@withContext BackupResult.Error("Corrupted encrypted backup file.")
                    }

                    val salt = allBytes.copyOfRange(6, 6 + SALT_SIZE_BYTES)
                    val iv = allBytes.copyOfRange(6 + SALT_SIZE_BYTES, 6 + SALT_SIZE_BYTES + IV_SIZE_BYTES)
                    val ciphertext = allBytes.copyOfRange(6 + SALT_SIZE_BYTES + IV_SIZE_BYTES, allBytes.size)

                    val secretKey = deriveKey(passphrase.toCharArray(), salt)
                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

                    val decryptedBytes = try {
                        cipher.doFinal(ciphertext)
                    } catch (e: Exception) {
                        return@withContext BackupResult.Error("Incorrect password or corrupted backup file.", e)
                    }
                    jsonString = String(decryptedBytes, Charsets.UTF_8)
                } else {
                    jsonString = String(allBytes.copyOfRange(6, allBytes.size), Charsets.UTF_8)
                }
            } else {
                // Direct JSON format
                jsonString = String(allBytes, Charsets.UTF_8)
            }

            val payload: BackupPayload = try {
                gson.fromJson(jsonString, BackupPayload::class.java)
            } catch (e: Exception) {
                return@withContext BackupResult.Error("Failed to parse backup contents: ${e.message}", e)
            }

            // 1. Restore Preferences into DataStore
            context.userStoreDataStore.edit { prefs ->
                payload.stringPreferences.forEach { (k, v) ->
                    prefs[stringPreferencesKey(k)] = v
                }
                payload.booleanPreferences.forEach { (k, v) ->
                    prefs[booleanPreferencesKey(k)] = v
                }
                payload.intPreferences.forEach { (k, v) ->
                    prefs[intPreferencesKey(k)] = v
                }
                payload.longPreferences.forEach { (k, v) ->
                    prefs[longPreferencesKey(k)] = v
                }
                payload.floatPreferences.forEach { (k, v) ->
                    prefs[floatPreferencesKey(k)] = v
                }
            }

            // 2. Restore Notes into Room
            val noteEntities = payload.notes.map {
                NoteEntity(
                    id = it.id,
                    text = it.text,
                    timestamp = it.timestamp,
                    isPinned = it.isPinned,
                    isArchived = it.isArchived
                )
            }

            if (strategy == RestoreStrategy.REPLACE) {
                // Clear existing notes
                val existing = database.noteDao().getAllNotesSync()
                existing.forEach { database.noteDao().deleteNote(it) }
            }

            if (noteEntities.isNotEmpty()) {
                database.noteDao().insertAll(noteEntities)
            }

            val totalPrefCount = payload.stringPreferences.size +
                    payload.booleanPreferences.size +
                    payload.intPreferences.size +
                    payload.longPreferences.size +
                    payload.floatPreferences.size

            BackupResult.Success(notesCount = noteEntities.size, preferencesCount = totalPrefCount)
        } catch (e: Exception) {
            BackupResult.Error("Restore failed: ${e.localizedMessage ?: e.message}", e)
        }
    }

    private fun deriveKey(passphrase: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}

// Extension to access Context preferencesDataStore directly from BackupManager
internal val Context.userStoreDataStore by androidx.datastore.preferences.preferencesDataStore(name = "user_settings")
