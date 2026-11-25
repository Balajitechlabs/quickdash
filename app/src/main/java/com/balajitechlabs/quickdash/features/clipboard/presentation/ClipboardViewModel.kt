/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/clipboard/presentation
 * File: ClipboardViewModel.kt
 * Description: ViewModel orchestrating clipboard item queries, pinning, manual deletion, and atomic updates.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.clipboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.clipboard.data.ClipboardRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class ClipboardViewModel @Inject constructor(
    private val clipboardRepository: ClipboardRepository,
    private val userStore: UserStore
) : ViewModel() {

    private val clipboardMutex = Mutex()
    private val gson = Gson()
    private val listType = object : TypeToken<MutableList<String>>() {}.type

    val clipboardHistory: Flow<String> = clipboardRepository.clipboardHistory
    val clipboardPinned: Flow<String> = clipboardRepository.clipboardPinned
    val tabBiometricLock: Flow<Boolean> = userStore.tabBiometricLock

    fun saveClipboardHistory(json: String) {
        viewModelScope.launch {
            clipboardMutex.withLock {
                clipboardRepository.saveClipboardHistory(json)
            }
        }
    }

    fun addClipboardItem(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            clipboardMutex.withLock {
                val currentJson = clipboardHistory.first()
                val list: MutableList<String> = try {
                    gson.fromJson(currentJson, listType) ?: mutableListOf()
                } catch (_: Exception) {
                    mutableListOf()
                }
                if (!list.contains(text)) {
                    list.add(0, text)
                    clipboardRepository.saveClipboardHistory(gson.toJson(list.take(30)))
                }
            }
        }
    }

    fun removeClipboardItem(text: String) {
        viewModelScope.launch {
            clipboardMutex.withLock {
                val currentJson = clipboardHistory.first()
                val list: MutableList<String> = try {
                    gson.fromJson(currentJson, listType) ?: mutableListOf()
                } catch (_: Exception) {
                    mutableListOf()
                }
                if (list.remove(text)) {
                    clipboardRepository.saveClipboardHistory(gson.toJson(list))
                }
            }
        }
    }

    fun saveClipboardPinned(json: String) {
        viewModelScope.launch {
            clipboardMutex.withLock {
                clipboardRepository.saveClipboardPinned(json)
            }
        }
    }
}
