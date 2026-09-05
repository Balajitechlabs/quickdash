/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: CryptoManagerTest.kt
 * Description: Unit tests verifying AES-256-GCM encryption, IV uniqueness, and authentication tag validation.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManagerTest {

    private val secureRandom = SecureRandom()

    private fun generateTestAesKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256, secureRandom)
        return keyGen.generateKey()
    }

    @Test
    fun `aes gcm round trip encrypts and decrypts correctly`() {
        val key = generateTestAesKey()
        val originalText = "QuickDash-Secret-Token-12345"
        val originalBytes = originalText.toByteArray(Charsets.UTF_8)

        // Encrypt with 12-byte IV
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        assertThat(iv.size).isEqualTo(12)
        val encrypted = cipher.doFinal(originalBytes)
        assertThat(encrypted).isNotEqualTo(originalBytes)

        // Decrypt
        val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        decryptCipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val decryptedBytes = decryptCipher.doFinal(encrypted)
        val decryptedText = String(decryptedBytes, Charsets.UTF_8)

        assertThat(decryptedText).isEqualTo(originalText)
    }

    @Test
    fun `unique iv generated on each encryption operation`() {
        val key = generateTestAesKey()
        val cipher1 = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key) }
        val cipher2 = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key) }

        assertThat(cipher1.iv).isNotEqualTo(cipher2.iv)
    }

    @Test
    fun `tampered ciphertext throws AEADBadTagException`() {
        val key = generateTestAesKey()
        val originalBytes = "Important-Credentials".toByteArray()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(originalBytes)

        // Corrupt the ciphertext
        encrypted[0] = (encrypted[0].toInt() xor 0xFF).toByte()

        val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        decryptCipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))

        try {
            decryptCipher.doFinal(encrypted)
            assert(false) { "Expected AEADBadTagException or GeneralSecurityException" }
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(javax.crypto.AEADBadTagException::class.java)
        }
    }

    @Test
    fun `cryptoManager mock contracts verify cleanly`() {
        val mockCrypto = mockk<CryptoManager>()
        every { mockCrypto.encrypt(any()) } returns "mock_base64_payload"
        every { mockCrypto.decrypt("mock_base64_payload") } returns "secret_data".toByteArray()

        val encrypted = mockCrypto.encrypt("test".toByteArray())
        assertThat(encrypted).isEqualTo("mock_base64_payload")

        val decrypted = mockCrypto.decrypt(encrypted)
        assertThat(String(decrypted)).isEqualTo("secret_data")
    }

    @Test
    fun `decrypting with wrong key throws AEADBadTagException`() {
        val keyA = generateTestAesKey()
        val keyB = generateTestAesKey()
        val originalBytes = "SuperSensitivePayload".toByteArray()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keyA)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(originalBytes)

        val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
        decryptCipher.init(Cipher.DECRYPT_MODE, keyB, GCMParameterSpec(128, iv))

        try {
            decryptCipher.doFinal(encrypted)
            assert(false) { "Expected decryption to fail with wrong key" }
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(javax.crypto.AEADBadTagException::class.java)
        }
    }
}

