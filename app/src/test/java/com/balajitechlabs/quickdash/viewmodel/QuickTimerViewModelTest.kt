package com.balajitechlabs.quickdash.viewmodel

import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.timer.presentation.QuickTimerViewModel
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuickTimerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val userStore = mockk<UserStore>(relaxed = true)
    private lateinit var viewModel: QuickTimerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = QuickTimerViewModel(userStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveTimerHistory delegates to userStore`() = runTest {
        viewModel.saveTimerHistory("[\"timer_1\"]")
        coVerify { userStore.saveTimerHistory("[\"timer_1\"]") }
    }
}
