/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/password/domain
 * File: PasswordGeneratorTest.kt
 * Description: Unit tests verifying PasswordGenerator length bounds, character sets, and SHA-256 hashing.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.password.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PasswordGeneratorTest {

    @Test
    fun `generate returns exact requested length`() {
        val password = PasswordGenerator.generate(length = 24, includeNumbers = true, includeSymbols = true)
        assertThat(password.length).isEqualTo(24)
    }

    @Test
    fun `generate omits numbers and symbols when disabled`() {
        val password = PasswordGenerator.generate(length = 64, includeNumbers = false, includeSymbols = false)
        assertThat(password).matches("^[A-Za-z]{64}$")
    }

    @Test
    fun `sha256 returns known digest for hello`() {
        val hash = PasswordGenerator.sha256("hello")
        assertThat(hash).isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824")
    }

    @Test
    fun `sha256 returns empty string for empty input`() {
        assertThat(PasswordGenerator.sha256("")).isEmpty()
    }
}
