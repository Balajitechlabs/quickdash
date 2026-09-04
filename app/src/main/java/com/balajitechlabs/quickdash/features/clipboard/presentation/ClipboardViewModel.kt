/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/clipboard
 * File: ClipboardViewModel.kt
 * Description: EssentialX-styled component for features/clipboard supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.clipboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.clipboard.data.ClipboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClipboardViewModel @Inject constructor(
    private val clipboardRepository: ClipboardRepository,
    private val userStore: UserStore
) : ViewModel() {

    val clipboardHistory: Flow<String> = clipboardRepository.clipboardHistory
    val clipboardPinned: Flow<String> = clipboardRepository.clipboardPinned
    val tabBiometricLock: Flow<Boolean> = userStore.tabBiometricLock

    fun saveClipboardHistory(json: String) {
        viewModelScope.launch {
            clipboardRepository.saveClipboardHistory(json)
        }
    }

    fun addClipboardItem(text: String) {
        viewModelScope.launch {
            val currentJson = clipboardHistory.first()
            val gson = com.google.gson.Gson()
            val listType = object : com.google.gson.reflect.TypeToken<MutableList<String>>() {}.type
            val list: MutableList<String> = try {
                gson.fromJson(currentJson, listType) ?: mutableListOf()
            } catch (_: Exception) { mutableListOf() }
            if (!list.contains(text)) {
                list.add(0, text)
                clipboardRepository.saveClipboardHistory(gson.toJson(list.take(30)))
            }
        }
    }

    fun removeClipboardItem(text: String) {
        viewModelScope.launch {
            val currentJson = clipboardHistory.first()
            val gson = com.google.gson.Gson()
            val listType = object : com.google.gson.reflect.TypeToken<MutableList<String>>() {}.type
            val list: MutableList<String> = try {
                gson.fromJson(currentJson, listType) ?: mutableListOf()
            } catch (_: Exception) { mutableListOf() }
            list.remove(text)
            clipboardRepository.saveClipboardHistory(gson.toJson(list))
        }
    }

    fun saveClipboardPinned(json: String) {
        viewModelScope.launch {
            clipboardRepository.saveClipboardPinned(json)
        }
    }
}
