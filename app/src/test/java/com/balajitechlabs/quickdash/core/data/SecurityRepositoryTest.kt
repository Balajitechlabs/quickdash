/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: core/data
 * File: SecurityRepositoryTest.kt
 * Description: Unit tests verifying real SecurityRepository encryption/decryption flow with DataStore.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SecurityRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var cryptoManager: CryptoManager
    private lateinit var repository: SecurityRepository

    @Before
    fun setup() {
        val appDir = tempFolder.newFolder("app_files")
        context = mockk(relaxed = true) {
            every { applicationContext } returns this
            every { filesDir } returns appDir
        }
        cryptoManager = mockk {
            every { encrypt(any()) } answers {
                val bytes = firstArg<ByteArray>()
                "enc:" + bytes.decodeToString()
            }
            every { decrypt(any()) } answers {
                val encoded = firstArg<String>()
                encoded.removePrefix("enc:").encodeToByteArray()
            }
        }
        repository = SecurityRepository(context, cryptoManager)
    }

    @Test
    fun `saveGithubAccessToken encrypts and emits token`() = runTest {
        repository.saveGithubAccessToken("ghp_test_token")

        verify { cryptoManager.encrypt("ghp_test_token".encodeToByteArray()) }
        assertThat(repository.githubAccessToken.first()).isEqualTo("ghp_test_token")
    }

    @Test
    fun `saveServerCredentials encrypts and emits json`() = runTest {
        val creds = """{"host":"example.com","user":"admin"}"""
        repository.saveServerCredentials(creds)

        verify { cryptoManager.encrypt(creds.encodeToByteArray()) }
        assertThat(repository.serverCredentials.first()).isEqualTo(creds)
    }
}
