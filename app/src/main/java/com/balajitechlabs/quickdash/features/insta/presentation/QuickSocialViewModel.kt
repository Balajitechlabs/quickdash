/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/insta/presentation
 * File: QuickSocialViewModel.kt
 * Description: ViewModel managing GitHub profile fetching, loading, and error state for QuickSocialScreen.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.insta.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.features.insta.data.GithubProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialGithubUiState(
    val isLoading: Boolean = false,
    val showProfile: Boolean = false,
    val error: String? = null,
    val profile: GithubProfileCache? = null
)

@HiltViewModel
class QuickSocialViewModel @Inject constructor(
    private val githubProfileRepository: GithubProfileRepository
) : ViewModel() {

    private val _githubState = MutableStateFlow(SocialGithubUiState())
    val githubState: StateFlow<SocialGithubUiState> = _githubState.asStateFlow()

    fun fetchGithubProfile(username: String) {
        viewModelScope.launch {
            _githubState.update { it.copy(isLoading = true, error = null) }
            val result = githubProfileRepository.fetchProfile(username)
            result.fold(
                onSuccess = { profile ->
                    _githubState.update {
                        it.copy(
                            isLoading = false,
                            showProfile = true,
                            error = null,
                            profile = profile
                        )
                    }
                },
                onFailure = { ex ->
                    val msg = ex.message ?: ""
                    val friendly = when {
                        msg == "RATE_LIMIT" -> "Rate limit exceeded. Add a token in settings or try again later."
                        msg == "NOT_FOUND" -> "GitHub user not found."
                        msg.startsWith("ERROR_") -> "GitHub API error: ${msg.substring(6)}"
                        else -> "Connection error. Please check your internet connection."
                    }
                    _githubState.update {
                        it.copy(isLoading = false, showProfile = false, error = friendly)
                    }
                }
            )
        }
    }

    fun dismissProfile() {
        _githubState.update { it.copy(showProfile = false) }
    }

    fun clearError() {
        _githubState.update { it.copy(error = null) }
    }
}
