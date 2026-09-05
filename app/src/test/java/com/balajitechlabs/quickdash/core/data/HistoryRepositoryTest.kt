/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: HistoryRepositoryTest.kt
 * Description: Unit tests for history repository querying, persistence, and clear operations.
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
class HistoryRepositoryTest {

    private val historyRepository = mockk<HistoryRepository>(relaxed = true)

    @Before
    fun setup() {
        val emptyPrefs = HistoryPreferences.getDefaultInstance()
        coEvery { historyRepository.historyPreferencesFlow } returns flowOf(emptyPrefs)
    }

    @Test
    fun `addSearchHistory saves and reads back`() = runTest {
        val query = "test query"
        val updatedPrefs = emptyPrefs().toBuilder().addSearchHistory(query).build()
        coEvery { historyRepository.historyPreferencesFlow } returns flowOf(updatedPrefs)

        historyRepository.addSearchHistory(query)

        coVerify { historyRepository.addSearchHistory(query) }
        val prefs = historyRepository.historyPreferencesFlow.first()
        assertThat(prefs.searchHistoryList).containsExactly(query)
    }

    @Test
    fun `clearSearchHistory empties the list`() = runTest {
        historyRepository.clearSearchHistory()
        coVerify { historyRepository.clearSearchHistory() }
    }

    @Test
    fun `addWifiHistory saves credentials`() = runTest {
        historyRepository.addWifiHistory("HomeWiFi", "password123", "WPA2")
        coVerify { historyRepository.addWifiHistory("HomeWiFi", "password123", "WPA2") }
    }

    @Test
    fun `removeWifiHistoryEntry removes specific entry`() = runTest {
        historyRepository.removeWifiHistoryEntry("WiFiA")
        coVerify { historyRepository.removeWifiHistoryEntry("WiFiA") }
    }

    @Test
    fun `saveQrHistoryItem persists QR data`() = runTest {
        historyRepository.saveQrHistoryItem("100", "test", "user@upi", "GPay", "General")
        coVerify { historyRepository.saveQrHistoryItem("100", "test", "user@upi", "GPay", "General") }
    }

    @Test
    fun `default HistoryPreferences has empty histories`() {
        val defaultPrefs = HistoryPreferences.getDefaultInstance()
        assertThat(defaultPrefs.searchHistoryList).isEmpty()
        assertThat(defaultPrefs.notificationHistory).isEmpty()
    }

    @Test
    fun `adding search item builds correct list ordering`() {
        val builder = HistoryPreferences.newBuilder()
        val list = mutableListOf("query1", "query2")
        list.remove("query1")
        list.add(0, "query1")

        builder.clearSearchHistory().addAllSearchHistory(list)
        val result = builder.build()

        assertThat(result.searchHistoryList).containsExactly("query1", "query2").inOrder()
    }

    private fun emptyPrefs() = HistoryPreferences.getDefaultInstance()
}
