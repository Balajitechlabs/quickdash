/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr/presentation
 * File: QrViewModel.kt
 * Description: ViewModel managing UPI QR generation, scan result parsing, and scan history persistence.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrViewModel @Inject constructor(
    private val userStore: UserStore
) : ViewModel() {

    val launchStyle: Flow<String> = userStore.launchStyle
    val favoriteTools: Flow<String> = userStore.favoriteTools

    fun saveLaunchStyle(style: String) {
        viewModelScope.launch {
            userStore.saveLaunchStyle(style)
        }
    }

    fun saveFavoriteTools(tools: String) {
        viewModelScope.launch {
            userStore.saveFavoriteTools(tools)
        }
    }
}
