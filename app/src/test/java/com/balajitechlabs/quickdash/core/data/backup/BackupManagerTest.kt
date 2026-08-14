package com.balajitechlabs.quickdash.core.data.backup

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Assert.assertThrows
import org.junit.Test
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class BackupManagerTest {

    private val gson = Gson()

    private fun deriveTestKey(passphrase: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, 100_000, 256)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    @Test
    fun `test BackupPayload json serialization and deserialization`() {
        val metadata = BackupMetadata(
            version = 1,
            appVersion = "5.2.1",
            appVersionCode = 521,
            timestamp = 1723630000000L,
            deviceName = "Google Pixel 9 Pro",
            isEncrypted = true
        )
        val note = NoteBackupItem(
            id = "note-123",
            text = "Test encrypted note content",
            timestamp = 1723630000000L,
            isPinned = true,
            isArchived = false
        )
        val payload = BackupPayload(
            metadata = metadata,
            stringPreferences = mapOf("theme_mode" to "AMOLED", "default_upi_id" to "user@upi"),
            booleanPreferences = mapOf("dynamic_color" to true, "bubble_enabled" to true),
            intPreferences = mapOf("haptic_level" to 2),
            longPreferences = mapOf("total_app_opens" to 42L),
            floatPreferences = mapOf("corner_radius" to 16.0f),
            notes = listOf(note)
        )

        val json = gson.toJson(payload)
        assertThat(json).isNotEmpty()

        val parsed = gson.fromJson(json, BackupPayload::class.java)
        assertThat(parsed.metadata.appVersion).isEqualTo("5.2.1")
        assertThat(parsed.metadata.appVersionCode).isEqualTo(521)
        assertThat(parsed.metadata.isEncrypted).isTrue()
        assertThat(parsed.stringPreferences["default_upi_id"]).isEqualTo("user@upi")
        assertThat(parsed.booleanPreferences["dynamic_color"]).isTrue()
        assertThat(parsed.notes).hasSize(1)
        assertThat(parsed.notes[0].text).isEqualTo("Test encrypted note content")
        assertThat(parsed.notes[0].isPinned).isTrue()
    }

    @Test
    fun `test BackupMetadata default values`() {
        val metadata = BackupMetadata()
        assertThat(metadata.version).isEqualTo(1)
        assertThat(metadata.appVersion).isEqualTo("5.2.1")
        assertThat(metadata.appVersionCode).isEqualTo(521)
        assertThat(metadata.timestamp).isGreaterThan(0L)
    }

    @Test
    fun `test AES-256-GCM encryption and decryption with PBKDF2 passphrase`() {
        val password = "MySuperSecretBackupPassword2026!"
        val plainText = "{\"test\": \"sensitive_user_backup_data\"}"
        val plainBytes = plainText.toByteArray(Charsets.UTF_8)

        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }

        // Encrypt
        val encryptKey = deriveTestKey(password.toCharArray(), salt)
        val encryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, GCMParameterSpec(128, iv))
        val cipherText = encryptCipher.doFinal(plainBytes)

        assertThat(cipherText).isNotNull()
        assertThat(cipherText).isNotEqualTo(plainBytes)

        // Decrypt with correct password
        val decryptKey = deriveTestKey(password.toCharArray(), salt)
        val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        decryptCipher.init(Cipher.DECRYPT_MODE, decryptKey, GCMParameterSpec(128, iv))
        val decryptedBytes = decryptCipher.doFinal(cipherText)

        assertThat(String(decryptedBytes, Charsets.UTF_8)).isEqualTo(plainText)
    }

    @Test
    fun `test AES-256-GCM decryption with wrong passphrase fails`() {
        val correctPassword = "CorrectPassword123"
        val wrongPassword = "WrongPassword456"
        val plainText = "Secret Payload"

        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }

        val encryptKey = deriveTestKey(correctPassword.toCharArray(), salt)
        val encryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, GCMParameterSpec(128, iv))
        val cipherText = encryptCipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Attempt decrypt with wrong key
        val wrongKey = deriveTestKey(wrongPassword.toCharArray(), salt)
        val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        decryptCipher.init(Cipher.DECRYPT_MODE, wrongKey, GCMParameterSpec(128, iv))

        assertThrows(Exception::class.java) {
            decryptCipher.doFinal(cipherText)
        }
    }
}
