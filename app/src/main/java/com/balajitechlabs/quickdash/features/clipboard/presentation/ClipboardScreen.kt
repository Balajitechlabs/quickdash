/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/clipboard/presentation
 * File: ClipboardScreen.kt
 * Description: Clipboard manager screen displaying captured clips, search, auto-clean options, and copy actions.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.clipboard.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.core.security.IncognitoManager
import com.balajitechlabs.quickdash.core.ui.components.EmptyStateCard
import com.balajitechlabs.quickdash.features.clipboard.presentation.components.ClipboardItemCard
import com.balajitechlabs.quickdash.features.clipboard.presentation.components.ClipboardLockView
import com.balajitechlabs.quickdash.features.clipboard.presentation.components.isSensitive
import com.balajitechlabs.quickdash.features.clipboard.presentation.dialogs.ClipboardClearDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

private const val TAG = "ClipboardScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardScreen(
    viewModel: ClipboardViewModel = hiltViewModel(),
    isFloating: Boolean = false,
    onTriggerConfetti: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gson = remember { Gson() }

    val clipboardJson by viewModel.clipboardHistory.collectAsStateWithLifecycle(initialValue = "[]")
    val pinnedJson by viewModel.clipboardPinned.collectAsStateWithLifecycle(initialValue = "[]")

    LaunchedEffect(Unit) {
        try {
            val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipManager != null && clipManager.hasPrimaryClip()) {
                val clipText = clipManager.primaryClip?.getItemAt(0)?.text?.toString()
                if (!clipText.isNullOrBlank()) {
                    viewModel.addClipboardItem(clipText)
                }
            }
        } catch (_: Exception) {}
    }

    val listType = object : TypeToken<List<String>>() {}.type
    val clipboardItems = remember(clipboardJson) {
        try {
            gson.fromJson<List<String>>(clipboardJson, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
    val pinnedItems = remember(pinnedJson) {
        try {
            gson.fromJson<List<String>>(pinnedJson, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    var selectedFilter by remember { mutableStateOf("All") }
    val filteredItems = remember(clipboardItems, selectedFilter, pinnedItems) {
        when (selectedFilter) {
            "Pinned" -> pinnedItems
            "Links" -> clipboardItems.filter { it.contains("http://") || it.contains("https://") || it.contains("www.") }
            "Phones" -> clipboardItems.filter { it.matches(Regex(".*[0-9]{7,15}.*")) }
            "Emails" -> clipboardItems.filter { it.contains("@") && it.contains(".") }
            else -> clipboardItems
        }
    }

    var revealedItems by remember { mutableStateOf(setOf<String>()) }
    val isTabLocked by viewModel.tabBiometricLock.collectAsStateWithLifecycle(initialValue = false)
    var isUnlocked by remember { mutableStateOf(false) }
    var showClearAllConfirmation by remember { mutableStateOf(false) }

    if (isTabLocked && !isUnlocked) {
        ClipboardLockView(
            context = context,
            onUnlocked = { isUnlocked = true }
        )
        return
    }

    Column(modifier = Modifier.fillMaxWidth().imePadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Clipboard History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (IncognitoManager.isIncognitoActive) {
                    Text(
                        "Incognito Active — History Paused",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (clipboardItems.isNotEmpty()) {
                    Text(
                        "${clipboardItems.size} item${if (clipboardItems.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (clipboardItems.isNotEmpty()) {
                FilledTonalIconButton(
                    onClick = { showClearAllConfirmation = true },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear all", modifier = Modifier.size(20.dp))
                }
            }
        }

        if (!isFloating) {
            val filters = listOf("All", "Pinned", "Links", "Phones", "Emails")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(filters, key = { it }) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (selectedFilter == filter) ({
                            Icon(Icons.Filled.FilterList, null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                        }) else null
                    )
                }
            }
        }

        if (filteredItems.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Filled.ContentCopy,
                title = if (clipboardItems.isEmpty()) "Nothing copied yet" else "No $selectedFilter items found",
                subtitle = if (clipboardItems.isEmpty()) "Text copied while the app is running will appear here automatically." else "Try a different filter to see your clipboard history.",
                actionLabel = if (clipboardItems.isNotEmpty() && selectedFilter != "All") "Show All" else null,
                onActionClick = { selectedFilter = "All" },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        val displayItems = if (isFloating) filteredItems.take(5) else filteredItems
        if (filteredItems.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                if (selectedFilter == "All" && pinnedItems.isNotEmpty() && !isFloating) {
                    item {
                        Text(
                            text = "Pinned Items",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp).animateItem()
                        )
                    }
                    itemsIndexed(pinnedItems, key = { index, item -> "pinned:$index:$item" }) { _, item ->
                        Box(modifier = Modifier.animateItem()) {
                            ClipboardItemCard(
                                item = item,
                                isPinned = true,
                                sensitive = isSensitive(item),
                                revealed = revealedItems.contains(item),
                                onToggleReveal = {
                                    revealedItems = if (revealedItems.contains(item)) revealedItems - item else revealedItems + item
                                },
                                onTogglePin = {
                                    val newList = pinnedItems.filter { it != item }
                                    coroutineScope.launch {
                                        viewModel.saveClipboardPinned(gson.toJson(newList))
                                    }
                                },
                                onShare = {
                                    try {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, item)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Clipboard Item"))
                                        onTriggerConfetti()
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to share clipboard item", e)
                                    }
                                },
                                onCopy = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("QuickDash", item))
                                },
                                onDelete = {
                                    val newList = clipboardItems.toMutableList().apply { remove(item) }
                                    viewModel.saveClipboardHistory(gson.toJson(newList))
                                    val newPinned = pinnedItems.filter { it != item }
                                    viewModel.saveClipboardPinned(gson.toJson(newPinned))
                                },
                                context = context
                            )
                        }
                    }
                    item {
                        Text(
                            text = "Recent History",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp).animateItem()
                        )
                    }
                }

                items(displayItems, key = { it }) { item ->
                    @Suppress("DEPRECATION")
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                viewModel.removeClipboardItem(item)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier.animateItem(),
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    ) {
                        ClipboardItemCard(
                            item = item,
                            isPinned = pinnedItems.contains(item),
                            sensitive = isSensitive(item),
                            revealed = revealedItems.contains(item),
                            onToggleReveal = {
                                revealedItems = if (revealedItems.contains(item)) revealedItems - item else revealedItems + item
                            },
                            onTogglePin = {
                                val isPinned = pinnedItems.contains(item)
                                val newList = if (isPinned) pinnedItems.filter { it != item } else pinnedItems + item
                                coroutineScope.launch {
                                    viewModel.saveClipboardPinned(gson.toJson(newList))
                                }
                            },
                            onShare = {
                                try {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, item)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Clipboard Item"))
                                    onTriggerConfetti()
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to share clipboard item", e)
                                }
                            },
                            onCopy = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("QuickDash", item))
                            },
                            onDelete = {
                                val newList = clipboardItems.toMutableList().apply { remove(item) }
                                viewModel.saveClipboardHistory(gson.toJson(newList))
                                if (pinnedItems.contains(item)) {
                                    val newPinned = pinnedItems.filter { it != item }
                                    viewModel.saveClipboardPinned(gson.toJson(newPinned))
                                }
                            },
                            context = context
                        )
                    }
                }
            }
        }
    }

    if (showClearAllConfirmation) {
        ClipboardClearDialog(
            onConfirm = {
                coroutineScope.launch {
                    viewModel.saveClipboardHistory("[]")
                }
                showClearAllConfirmation = false
            },
            onDismissRequest = { showClearAllConfirmation = false }
        )
    }
}
