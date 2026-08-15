package com.balajitechlabs.quickdash.core.data.backup

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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
            appVersion = "5.2.2",
            appVersionCode = 522,
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
        assertThat(parsed.metadata.appVersion).isEqualTo("5.2.2")
        assertThat(parsed.metadata.appVersionCode).isEqualTo(522)
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
        assertThat(metadata.isEncrypted).isFalse()
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

    @Test
    fun `test QDBK binary envelope header format verification`() {
        val magic = BackupManager.MAGIC_HEADER
        val out = ByteArrayOutputStream()
        out.write(magic)
        out.write(byteArrayOf(BackupManager.CURRENT_FORMAT_VERSION, BackupManager.FLAG_PLAINTEXT))
        val testPayload = "{\"test\": true}".toByteArray(Charsets.UTF_8)
        out.write(testPayload)

        val bytes = out.toByteArray()
        assertThat(bytes.size).isAtLeast(6)
        assertThat(bytes[0]).isEqualTo(0x51.toByte()) // 'Q'
        assertThat(bytes[1]).isEqualTo(0x44.toByte()) // 'D'
        assertThat(bytes[2]).isEqualTo(0x42.toByte()) // 'B'
        assertThat(bytes[3]).isEqualTo(0x4B.toByte()) // 'K'
        assertThat(bytes[4]).isEqualTo(1.toByte())    // Version 1
        assertThat(bytes[5]).isEqualTo(0.toByte())    // Plaintext flag

        val content = String(bytes.copyOfRange(6, bytes.size), Charsets.UTF_8)
        assertThat(content).isEqualTo("{\"test\": true}")
    }

    @Test
    fun `test encrypted QDBK envelope roundtrip structure`() {
        val magic = BackupManager.MAGIC_HEADER
        val salt = ByteArray(BackupManager.SALT_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(BackupManager.IV_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        val password = "StrongPassphrase@2026"
        val payload = "{\"notes\": [{\"id\":\"1\", \"text\":\"hello\"}]}"

        val key = deriveTestKey(password.toCharArray(), salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))

        val out = ByteArrayOutputStream()
        out.write(magic)
        out.write(byteArrayOf(BackupManager.CURRENT_FORMAT_VERSION, BackupManager.FLAG_ENCRYPTED))
        out.write(salt)
        out.write(iv)
        out.write(ciphertext)

        val fullFile = out.toByteArray()
        assertThat(fullFile[5]).isEqualTo(BackupManager.FLAG_ENCRYPTED)

        // Read and decrypt envelope
        val inStream = ByteArrayInputStream(fullFile)
        val header = ByteArray(6)
        inStream.read(header)
        val inSalt = ByteArray(BackupManager.SALT_SIZE_BYTES)
        inStream.read(inSalt)
        val inIv = ByteArray(BackupManager.IV_SIZE_BYTES)
        inStream.read(inIv)
        val inCiphertext = inStream.readBytes()

        val decryptKey = deriveTestKey(password.toCharArray(), inSalt)
        val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        decryptCipher.init(Cipher.DECRYPT_MODE, decryptKey, GCMParameterSpec(128, inIv))
        val decrypted = String(decryptCipher.doFinal(inCiphertext), Charsets.UTF_8)

        assertThat(decrypted).isEqualTo(payload)
    }
}
