/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/timer/presentation/components
 * File: TimerHistoryContent.kt
 * Description: Completed timer and stopwatch session history log with deletion and clear-all actions.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.timer.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.features.timer.presentation.QuickTimerViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimerHistoryContent(
    viewModel: QuickTimerViewModel,
    isFloating: Boolean
) {
    val historyJson by viewModel.timerHistory.collectAsStateWithLifecycle(initialValue = "[]")
    val gson = remember { Gson() }
    val listType = remember { object : TypeToken<List<TimerHistoryEntry>>() {}.type }
    val entries = remember(historyJson) {
        try {
            gson.fromJson<List<TimerHistoryEntry>>(historyJson, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
    val scope = rememberCoroutineScope()
    var showClearAllConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isFloating) 8.dp else 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${entries.size} Session${if (entries.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (entries.isNotEmpty()) {
                Text(
                    text = "Clear All",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable {
                        showClearAllConfirmation = true
                    }
                )
            }
        }

        if (entries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = "Timer history",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = "No history recorded yet",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = if (isFloating) 200.dp else 300.dp)
            ) {
                items(entries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (entry.type == "Stopwatch") Icons.Filled.Flag else Icons.Filled.Timer,
                                contentDescription = entry.type,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val durationText = if (entry.type == "Stopwatch") {
                                    formatMs(entry.durationMs)
                                } else {
                                    formatCountdown(entry.durationMs)
                                }
                                Text(
                                    text = "${entry.type}: $durationText",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val dateText = remember(entry.timestamp) {
                                    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                                    sdf.format(Date(entry.timestamp))
                                }
                                Text(
                                    text = if (entry.detail.isNotEmpty()) "${entry.detail} • $dateText" else dateText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    val newList = entries.filter { it != entry }
                                    viewModel.saveTimerHistory(gson.toJson(newList))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Delete entry",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearAllConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmation = false },
            title = { Text("Clear Timer History") },
            text = { Text("Are you sure you want to clear all timer history sessions?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.saveTimerHistory("[]")
                    }
                    showClearAllConfirmation = false
                }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
