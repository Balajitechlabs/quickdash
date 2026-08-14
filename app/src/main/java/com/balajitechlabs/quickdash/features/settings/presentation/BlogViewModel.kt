package com.balajitechlabs.quickdash.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlogViewModel @Inject constructor(
    private val userStore: UserStore
) : ViewModel() {

    val firebaseBlogPosts: Flow<String> = userStore.firebaseBlogPosts
    val pollVotes: Flow<String> = userStore.pollVotes
    val hiddenNotifications: Flow<String> = userStore.hiddenNotifications
    val pinnedNotifications: Flow<String> = userStore.pinnedNotifications
    val showImagePreviews: Flow<Boolean> = userStore.showImagePreviews

    fun savePollVote(json: String) {
        viewModelScope.launch {
            userStore.savePollVote(json)
        }
    }

    fun saveHiddenNotifications(json: String) {
        viewModelScope.launch {
            userStore.saveHiddenNotifications(json)
        }
    }

    fun savePinnedNotifications(json: String) {
        viewModelScope.launch {
            userStore.savePinnedNotifications(json)
        }
    }

    fun saveFirebaseBlogPosts(json: String) {
        viewModelScope.launch {
            userStore.saveFirebaseBlogPosts(json)
        }
    }
}
