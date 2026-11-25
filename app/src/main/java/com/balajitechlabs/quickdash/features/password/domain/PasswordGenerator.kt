/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/password/domain
 * File: PasswordGenerator.kt
 * Description: Pure-Kotlin cryptographically secure random password generator and SHA-256 hash utility.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.password.domain

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordGenerator {

    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#\$%^&*()_+-=[]{}|;:,.<>?"

    private val secureRandom = SecureRandom()

    fun generate(
        length: Int,
        includeNumbers: Boolean,
        includeSymbols: Boolean
    ): String {
        val safeLength = length.coerceIn(4, 128)
        val pool = buildString {
            append(UPPERCASE)
            append(LOWERCASE)
            if (includeNumbers) append(NUMBERS)
            if (includeSymbols) append(SYMBOLS)
        }
        return buildString(safeLength) {
            repeat(safeLength) {
                append(pool[secureRandom.nextInt(pool.length)])
            }
        }
    }

    fun sha256(input: String): String {
        if (input.isEmpty()) return ""
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
