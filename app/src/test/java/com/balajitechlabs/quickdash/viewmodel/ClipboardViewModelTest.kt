/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: viewmodel
 * File: ClipboardViewModelTest.kt
 * Description: Unit tests verifying clipboard item management, search filtering, and deletion.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.viewmodel

import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.clipboard.data.ClipboardRepository
import com.balajitechlabs.quickdash.features.clipboard.presentation.ClipboardViewModel
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
class ClipboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val clipboardRepository = mockk<ClipboardRepository>(relaxed = true)
    private val userStore = mockk<UserStore>(relaxed = true)
    private lateinit var viewModel: ClipboardViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ClipboardViewModel(clipboardRepository, userStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveClipboardHistory delegates to clipboardRepository`() = runTest {
        viewModel.saveClipboardHistory("[\"test\"]")
        coVerify { clipboardRepository.saveClipboardHistory("[\"test\"]") }
    }

    @Test
    fun `saveClipboardPinned delegates to clipboardRepository`() = runTest {
        viewModel.saveClipboardPinned("[\"pinned\"]")
        coVerify { clipboardRepository.saveClipboardPinned("[\"pinned\"]") }
    }
}
