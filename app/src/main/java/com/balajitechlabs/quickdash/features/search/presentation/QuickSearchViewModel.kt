/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/search/presentation
 * File: QuickSearchViewModel.kt
 * Description: ViewModel managing debounced search suggestions and search history persistence.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.search.data.SearchSuggestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuickSearchViewModel @Inject constructor(
    private val suggestionRepository: SearchSuggestionRepository,
    private val userStore: UserStore
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")

    val suggestions: StateFlow<List<String>> = queryFlow
        .debounce(200L)
        .distinctUntilChanged()
        .mapLatest { q -> suggestionRepository.fetchSuggestions(q) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onQueryChanged(query: String) {
        queryFlow.value = query
    }

    fun recordSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            userStore.addSearchHistory(query)
        }
    }
}
