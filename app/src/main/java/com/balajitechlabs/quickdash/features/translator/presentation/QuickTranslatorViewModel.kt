/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/translator/presentation
 * File: QuickTranslatorViewModel.kt
 * Description: ViewModel managing translation state, language selection, and translation history.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.translator.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.features.translator.data.TranslationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickTranslatorViewModel @Inject constructor(
    private val translationRepository: TranslationRepository
) : ViewModel() {

    private val _translatedResult = MutableStateFlow("")
    val translatedResult: StateFlow<String> = _translatedResult.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    fun translate(
        text: String,
        fromCode: String,
        toCode: String,
        onComplete: (String) -> Unit
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isTranslating.value = true
            try {
                val result = translationRepository.translate(text, fromCode, toCode)
                _translatedResult.value = result
                onComplete(result)
            } finally {
                _isTranslating.value = false
            }
        }
    }
}
