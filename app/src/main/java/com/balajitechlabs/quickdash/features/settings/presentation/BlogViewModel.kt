package com.balajitechlabs.quickdash.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlogViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val firebaseBlogPosts: Flow<String> = settingsRepository.firebaseBlogPosts
    val pollVotes: Flow<String> = settingsRepository.pollVotes
    val hiddenNotifications: Flow<String> = settingsRepository.hiddenNotifications
    val pinnedNotifications: Flow<String> = settingsRepository.pinnedNotifications
    val showImagePreviews: Flow<Boolean> = settingsRepository.showImagePreviews

    fun savePollVote(json: String) {
        viewModelScope.launch {
            settingsRepository.savePollVote(json)
        }
    }

    fun saveHiddenNotifications(json: String) {
        viewModelScope.launch {
            settingsRepository.saveHiddenNotifications(json)
        }
    }

    fun savePinnedNotifications(json: String) {
        viewModelScope.launch {
            settingsRepository.savePinnedNotifications(json)
        }
    }

    fun saveFirebaseBlogPosts(json: String) {
        viewModelScope.launch {
            settingsRepository.saveFirebaseBlogPosts(json)
        }
    }
}
