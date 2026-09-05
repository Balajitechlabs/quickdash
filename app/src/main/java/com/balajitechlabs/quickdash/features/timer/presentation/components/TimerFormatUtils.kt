/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/timer/presentation/components
 * File: TimerFormatUtils.kt
 * Description: Time format helpers, history entry models, and asynchronous storage utilities for timers.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.timer.presentation.components

import com.balajitechlabs.quickdash.core.utils.AppLogger
import com.balajitechlabs.quickdash.features.timer.presentation.QuickTimerViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class TimerHistoryEntry(
    val type: String,
    val durationMs: Long,
    val timestamp: Long,
    val detail: String = ""
)

fun formatMs(ms: Long): String {
    val hours = ms / 3_600_000L
    val minutes = (ms % 3_600_000L) / 60_000L
    val seconds = (ms % 60_000L) / 1_000L
    val centis = (ms % 1_000L) / 10L
    return if (hours > 0) "%02d:%02d:%02d.%02d".format(hours, minutes, seconds, centis)
    else "%02d:%02d.%02d".format(minutes, seconds, centis)
}

fun formatCountdown(ms: Long): String {
    val h = ms / 3_600_000L
    val m = (ms % 3_600_000L) / 60_000L
    val s = (ms % 60_000L) / 1_000L
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

fun addTimerHistoryEntry(
    viewModel: QuickTimerViewModel,
    scope: CoroutineScope,
    entry: TimerHistoryEntry
) {
    scope.launch {
        try {
            val gson = Gson()
            val listType = object : TypeToken<List<TimerHistoryEntry>>() {}.type
            val currentJson = viewModel.timerHistory.first()
            val currentList: List<TimerHistoryEntry> = gson.fromJson(currentJson, listType) ?: emptyList()
            val newList = (listOf(entry) + currentList).take(50)
            viewModel.saveTimerHistory(gson.toJson(newList))
        } catch (e: Exception) {
            AppLogger.e("TimerFormatUtils", "Failed to add timer history entry", e)
        }
    }
}
