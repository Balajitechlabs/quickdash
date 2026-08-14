package com.balajitechlabs.quickdash.features.timer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickTimerViewModel @Inject constructor(
    private val userStore: UserStore
) : ViewModel() {

    val timerHistory: Flow<String> = userStore.timerHistory

    fun saveTimerHistory(json: String) {
        viewModelScope.launch {
            userStore.saveTimerHistory(json)
        }
    }
}
