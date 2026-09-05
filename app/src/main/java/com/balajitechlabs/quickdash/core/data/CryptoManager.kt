/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: CryptoManager.kt
 * Description: Manages AES-256-GCM Keystore encryption, unique IV generation, and tamper detection for sensitive data.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor() {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val encryptCipher get() = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }

    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(128, iv))
        }
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry("secret", null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    "secret",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                .setBlockModes(BLOCK_MODE)
                .setEncryptionPaddings(PADDING)
                .setUserAuthenticationRequired(false)
                .setRandomizedEncryptionRequired(true)
                .build()
            )
        }.generateKey()
    }

    fun encrypt(bytes: ByteArray): String {
        val cipher = encryptCipher
        val iv = cipher.iv
        val encrypted = cipher.doFinal(bytes)
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedString: String): ByteArray {
        val combined = Base64.decode(encryptedString, Base64.DEFAULT)
        val iv = combined.copyOfRange(0, 12)
        val encrypted = combined.copyOfRange(12, combined.size)
        val cipher = getDecryptCipherForIv(iv)
        return cipher.doFinal(encrypted)
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}
