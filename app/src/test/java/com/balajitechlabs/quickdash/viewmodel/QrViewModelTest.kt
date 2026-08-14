package com.balajitechlabs.quickdash.viewmodel

import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.qr.presentation.QrViewModel
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
class QrViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val userStore = mockk<UserStore>(relaxed = true)
    private lateinit var viewModel: QrViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = QrViewModel(userStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveLaunchStyle delegates to userStore`() = runTest {
        viewModel.saveLaunchStyle("FULL_SCREEN")
        coVerify { userStore.saveLaunchStyle("FULL_SCREEN") }
    }

    @Test
    fun `saveFavoriteTools delegates to userStore`() = runTest {
        viewModel.saveFavoriteTools("[\"qr\"]")
        coVerify { userStore.saveFavoriteTools("[\"qr\"]") }
    }
}
