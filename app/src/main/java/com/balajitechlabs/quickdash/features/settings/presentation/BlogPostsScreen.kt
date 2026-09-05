/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation
 * File: BlogPostsScreen.kt
 * Description: Screen displaying developer announcements, update notes, and tutorials.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker
import com.balajitechlabs.quickdash.features.settings.presentation.components.BlogHeaderBar
import com.balajitechlabs.quickdash.features.settings.presentation.components.BlogPostCard
import com.balajitechlabs.quickdash.features.settings.presentation.components.BlogPostPreviewOverlay
import com.balajitechlabs.quickdash.features.settings.presentation.dialogs.BlogClearFeedDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BlogPostsScreen(viewModel: BlogViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gson = remember { Gson() }

    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15000)
            tick++
        }
    }

    val rawPostsJson by viewModel.firebaseBlogPosts.collectAsStateWithLifecycle(initialValue = "[]")
    val pollVotesJson by viewModel.pollVotes.collectAsStateWithLifecycle(initialValue = "{}")
    val hiddenJson by viewModel.hiddenNotifications.collectAsStateWithLifecycle(initialValue = "[]")
    val pinnedJson by viewModel.pinnedNotifications.collectAsStateWithLifecycle(initialValue = "[]")

    val posts = remember(rawPostsJson) {
        try {
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            gson.fromJson<List<Map<String, Any>>>(rawPostsJson, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    var showClearFeedConfirmation by remember { mutableStateOf(false) }

    val pollVotes = remember(pollVotesJson) {
        try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson<Map<String, String>>(pollVotesJson, type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    val hiddenSet = remember(hiddenJson) {
        try {
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson<Set<String>>(hiddenJson, type) ?: emptySet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    val pinnedSet = remember(pinnedJson) {
        try {
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson<Set<String>>(pinnedJson, type) ?: emptySet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    var localHiddenSet by remember { mutableStateOf(setOf<String>()) }

    val visiblePosts = remember(posts, hiddenSet, localHiddenSet, pinnedSet) {
        val combinedHidden = hiddenSet + localHiddenSet
        val filtered = posts.filter { post ->
            val ts = (post["timestamp"] as? Number)?.toLong() ?: 0L
            val key = "${ts}_${post["title"]}"
            !combinedHidden.contains(key)
        }
        filtered.sortedByDescending { post ->
            val ts = (post["timestamp"] as? Number)?.toLong() ?: 0L
            val key = "${ts}_${post["title"]}"
            if (pinnedSet.contains(key)) Long.MAX_VALUE else ts
        }
    }

    val showImagePreviews by viewModel.showImagePreviews.collectAsStateWithLifecycle(initialValue = true)
    var previewPost by remember { mutableStateOf<Map<String, Any>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        BlogHeaderBar(
            context = context,
            onClearFeedClick = { showClearFeedConfirmation = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (visiblePosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notifications found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = visiblePosts,
                    key = { post -> "${(post["timestamp"] as? Number)?.toLong() ?: 0L}_${post["title"]}" }
                ) { post ->
                    val title = post["title"] as? String ?: "Announcement"
                    val ts = (post["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    val postKey = "${ts}_${title}"
                    val isPinned = pinnedSet.contains(postKey)

                    BlogPostCard(
                        post = post,
                        isPinned = isPinned,
                        pollVotes = pollVotes,
                        showImagePreviews = showImagePreviews,
                        tick = tick,
                        context = context,
                        onDismiss = { key ->
                            localHiddenSet = localHiddenSet + key
                            coroutineScope.launch {
                                try {
                                    val newHiddenSet = hiddenSet + key
                                    viewModel.saveHiddenNotifications(gson.toJson(newHiddenSet))
                                } catch (e: Exception) {
                                    Log.e("QuickDash", "Error hiding post: ${e.message}", e)
                                }
                            }
                        },
                        onTogglePin = { key, currentlyPinned ->
                            coroutineScope.launch {
                                try {
                                    val newPinnedSet = if (currentlyPinned) pinnedSet - key else pinnedSet + key
                                    viewModel.savePinnedNotifications(gson.toJson(newPinnedSet))
                                } catch (e: Exception) {
                                    Log.e("QuickDash", "Error toggling pin: ${e.message}", e)
                                }
                            }
                        },
                        onVote = { key, option, question ->
                            coroutineScope.launch {
                                val newVotes = pollVotes.toMutableMap()
                                newVotes[key] = option
                                viewModel.savePollVote(gson.toJson(newVotes))
                                TelegramTracker.sendBroadcastBotMessage(
                                    "<b>New Poll Vote</b>\nQuestion: $question\nVote: $option"
                                )
                            }
                        },
                        onSubmitAsk = { _, responseText, question ->
                            coroutineScope.launch {
                                TelegramTracker.sendBroadcastBotMessage(
                                    "<b>User Data Response</b>\nQuestion: $question\nResponse: $responseText"
                                )
                            }
                        },
                        onLongPressPreview = { p -> previewPost = p },
                        onReleasePreview = { previewPost = null }
                    )
                }
            }
        }

        if (hiddenSet.isNotEmpty() || localHiddenSet.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    localHiddenSet = emptySet()
                    coroutineScope.launch {
                        viewModel.saveHiddenNotifications("[]")
                    }
                    Toast.makeText(context, "All notifications restored", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Restore Dismissed Notifications", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Real-time Notification Sync",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }

    previewPost?.let { post ->
        BlogPostPreviewOverlay(
            post = post,
            context = context,
            coroutineScope = coroutineScope
        )
    }

    if (showClearFeedConfirmation) {
        BlogClearFeedDialog(
            onConfirm = {
                coroutineScope.launch {
                    viewModel.saveFirebaseBlogPosts("[]")
                    viewModel.saveHiddenNotifications("[]")
                    viewModel.savePinnedNotifications("[]")
                    viewModel.savePollVote("{}")
                }
                showClearFeedConfirmation = false
            },
            onDismissRequest = { showClearFeedConfirmation = false }
        )
    }
}
