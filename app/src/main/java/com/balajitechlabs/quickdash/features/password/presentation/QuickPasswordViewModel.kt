/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/password/presentation
 * File: QuickPasswordViewModel.kt
 * Description: ViewModel managing password generation options, generated password state, and SHA-256 hashing.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.password.presentation

import androidx.lifecycle.ViewModel
import com.balajitechlabs.quickdash.features.password.domain.PasswordGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PasswordUiState(
    val length: Int = 16,
    val includeNumbers: Boolean = true,
    val includeSymbols: Boolean = true,
    val generatedPassword: String = PasswordGenerator.generate(16, includeNumbers = true, includeSymbols = true),
    val hashInput: String = "",
    val sha256Output: String = ""
)

@HiltViewModel
class QuickPasswordViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(PasswordUiState())
    val state: StateFlow<PasswordUiState> = _state.asStateFlow()

    fun regenerate() {
        _state.update { current ->
            current.copy(
                generatedPassword = PasswordGenerator.generate(
                    length = current.length,
                    includeNumbers = current.includeNumbers,
                    includeSymbols = current.includeSymbols
                )
            )
        }
    }

    fun setLength(length: Int) {
        _state.update { current ->
            current.copy(
                length = length,
                generatedPassword = PasswordGenerator.generate(
                    length = length,
                    includeNumbers = current.includeNumbers,
                    includeSymbols = current.includeSymbols
                )
            )
        }
    }

    fun setIncludeNumbers(enabled: Boolean) {
        _state.update { current ->
            current.copy(
                includeNumbers = enabled,
                generatedPassword = PasswordGenerator.generate(
                    length = current.length,
                    includeNumbers = enabled,
                    includeSymbols = current.includeSymbols
                )
            )
        }
    }

    fun setIncludeSymbols(enabled: Boolean) {
        _state.update { current ->
            current.copy(
                includeSymbols = enabled,
                generatedPassword = PasswordGenerator.generate(
                    length = current.length,
                    includeNumbers = current.includeNumbers,
                    includeSymbols = enabled
                )
            )
        }
    }

    fun onHashInputChange(text: String) {
        _state.update { current ->
            current.copy(
                hashInput = text,
                sha256Output = PasswordGenerator.sha256(text)
            )
        }
    }
}
