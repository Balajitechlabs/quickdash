/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation
 * File: SettingsViewModel.kt
 * Description: ViewModel coordinating preferences, theme modes, backup triggers, and permission queries.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val userStore: UserStore
) : ViewModel() {

    // Helper function to safely read from UserStore as StateFlow
    inline fun <reified T> getStateFlow(flow: Flow<T>, initialValue: T) = flow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialValue
    )
}
