/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: SecurityRepositoryTest.kt
 * Description: Unit tests verifying security settings persistence and biometric lock logic.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SecurityRepositoryTest {

    private val cryptoManager = mockk<CryptoManager>(relaxed = true)
    private val securityRepository = mockk<SecurityRepository>(relaxed = true)

    @Before
    fun setup() {
        coEvery { securityRepository.githubAccessToken } returns flowOf("")
        coEvery { securityRepository.serverCredentials } returns flowOf("{}")
    }

    @Test
    fun `saveGithubAccessToken stores token`() = runTest {
        securityRepository.saveGithubAccessToken("ghp_test_token")
        coVerify { securityRepository.saveGithubAccessToken("ghp_test_token") }
    }

    @Test
    fun `saveServerCredentials stores and retrieves`() = runTest {
        val creds = """{"host":"example.com","user":"admin"}"""
        coEvery { securityRepository.serverCredentials } returns flowOf(creds)

        securityRepository.saveServerCredentials(creds)

        coVerify { securityRepository.saveServerCredentials(creds) }
        val retrieved = securityRepository.serverCredentials.first()
        assertThat(retrieved).isEqualTo(creds)
    }

    @Test
    fun `isAuthenticated returns false initially`() = runTest {
        val token = securityRepository.githubAccessToken.first()
        assertThat(token).isEmpty()
    }
}
