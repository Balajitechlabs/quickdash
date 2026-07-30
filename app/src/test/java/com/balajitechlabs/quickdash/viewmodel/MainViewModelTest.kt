package com.balajitechlabs.quickdash.viewmodel

import com.balajitechlabs.quickdash.MainViewModel
import com.balajitechlabs.quickdash.core.data.HistoryRepository
import com.balajitechlabs.quickdash.core.data.SettingsRepository
import com.balajitechlabs.quickdash.core.data.migration.DataMigrationManager
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val dataMigrationManager = mockk<DataMigrationManager>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val historyRepository = mockk<HistoryRepository>(relaxed = true)

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { dataMigrationManager.migrateLegacyHistoryIfNeeded() } returns true
        coEvery { settingsRepository.appLanguage } returns flowOf("en")
        coEvery { settingsRepository.totalAppOpens } returns flowOf(5L)
        coEvery { settingsRepository.isOnboardingComplete } returns flowOf(true)
        coEvery { settingsRepository.launchStyle } returns flowOf("FULL_SCREEN")
        coEvery { settingsRepository.isAppLocked } returns flowOf(false)

        viewModel = MainViewModel(dataMigrationManager, settingsRepository, historyRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init runs migration manager and completes`() = runTest {
        coVerify { dataMigrationManager.migrateLegacyHistoryIfNeeded() }
        assertThat(viewModel.isMigrationComplete.value).isTrue()
    }

    @Test
    fun `incrementTotalAppOpens delegates to settingsRepository`() = runTest {
        viewModel.incrementTotalAppOpens()
        coVerify { settingsRepository.incrementAppOpens() }
    }
}
