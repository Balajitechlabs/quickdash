package com.balajitechlabs.quickdash.viewmodel

import com.balajitechlabs.quickdash.MainViewModel
import com.balajitechlabs.quickdash.core.data.HistoryRepository
import com.balajitechlabs.quickdash.core.data.UserStore
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
    private val userStore = mockk<UserStore>(relaxed = true)
    private val historyRepository = mockk<HistoryRepository>(relaxed = true)

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { dataMigrationManager.migrateLegacyHistoryIfNeeded() } returns true
        coEvery { userStore.appLanguage } returns flowOf("en")
        coEvery { userStore.totalAppOpens } returns flowOf(5L)
        coEvery { userStore.isOnboardingComplete } returns flowOf(true)
        coEvery { userStore.launchStyle } returns flowOf("FULL_SCREEN")
        coEvery { userStore.isAppLocked } returns flowOf(false)

        viewModel = MainViewModel(dataMigrationManager, userStore, historyRepository)
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
    fun `incrementTotalAppOpens delegates to userStore`() = runTest {
        viewModel.incrementTotalAppOpens()
        coVerify { userStore.incrementAppOpens() }
    }
}
