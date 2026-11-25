/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/calculator/presentation
 * File: QuickCalculatorViewModel.kt
 * Description: ViewModel holding calculator state, mode toggles, and history for QuickCalculatorScreen.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.calculator.presentation

import androidx.lifecycle.ViewModel
import com.balajitechlabs.quickdash.features.calculator.domain.CalcKey
import com.balajitechlabs.quickdash.features.calculator.domain.CalculatorEngine
import com.balajitechlabs.quickdash.features.calculator.domain.CalculatorState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class QuickCalculatorViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private val _isScientific = MutableStateFlow(false)
    val isScientific: StateFlow<Boolean> = _isScientific.asStateFlow()

    private val _showHistory = MutableStateFlow(false)
    val showHistory: StateFlow<Boolean> = _showHistory.asStateFlow()

    fun onKey(key: CalcKey) {
        _state.update { current -> CalculatorEngine.reduce(current, key) }
    }

    fun setScientific(scientific: Boolean) {
        _isScientific.value = scientific
    }

    fun setShowHistory(show: Boolean) {
        _showHistory.value = show
    }

    fun clearHistory() {
        _state.update { it.copy(history = emptyList()) }
        _showHistory.value = false
    }

    fun selectHistoryEntry(entry: String) {
        val parts = entry.split("=")
        if (parts.size == 2) {
            val value = parts[1].trim()
            _state.update { it.copy(display = value, expression = value, justEvaluated = true) }
        }
        _showHistory.value = false
    }
}
