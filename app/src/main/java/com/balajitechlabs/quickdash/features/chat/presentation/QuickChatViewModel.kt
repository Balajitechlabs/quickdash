/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/chat
 * File: QuickChatViewModel.kt
 * Description: EssentialX-styled component for features/chat supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickChatViewModel @Inject constructor(
    private val userStore: UserStore
) : ViewModel() {

    val chatDefaultCode: Flow<String> = userStore.chatDefaultCode
    val chatDefaultIso: Flow<String> = userStore.chatDefaultIso
    val chatHistory: Flow<List<String>> = userStore.chatHistory
    val chatPauseHistory: Flow<Boolean> = userStore.chatPauseHistory

    fun saveChatDefaultCountry(code: String, iso: String) {
        viewModelScope.launch {
            userStore.saveChatDefaultCountry(code, iso)
        }
    }

    fun saveChatNumberToHistory(number: String, flag: String) {
        viewModelScope.launch {
            userStore.saveChatNumberToHistory(number, flag)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            userStore.clearChatHistory()
        }
    }

    fun saveChatPauseHistory(pause: Boolean) {
        viewModelScope.launch {
            userStore.saveChatPauseHistory(pause)
        }
    }
}
