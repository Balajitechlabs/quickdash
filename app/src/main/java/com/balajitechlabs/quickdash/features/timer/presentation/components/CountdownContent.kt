/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/timer/presentation/components
 * File: CountdownContent.kt
 * Description: Countdown timer interface featuring presets, custom duration entry, and system alarm triggers.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.timer.presentation.components

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.utils.AppLogger
import com.balajitechlabs.quickdash.features.timer.presentation.QuickTimerViewModel
import com.balajitechlabs.quickdash.features.timer.presentation.TimerAlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.util.Random

private val presetTimers = listOf(
    "1 min" to 60_000L,
    "3 min" to 180_000L,
    "5 min" to 300_000L,
    "10 min" to 600_000L,
    "15 min" to 900_000L,
    "30 min" to 1_800_000L
)

@Composable
fun CountdownContent(
    viewModel: QuickTimerViewModel,
    scope: CoroutineScope,
    isFloating: Boolean
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var totalMs by remember { mutableLongStateOf(300_000L) }
    var remainingMs by remember { mutableLongStateOf(300_000L) }
    var isRunning by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    var customInput by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    val timerId = remember { Random().nextInt(99999) + 1000 }
    val alarmIntent = remember(timerId) {
        PendingIntent.getBroadcast(
            context,
            timerId,
            Intent(context, TimerAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val triggerTime = SystemClock.elapsedRealtime() + remainingMs
            try {
                val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }

                if (canScheduleExact) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    } else {
                        alarmManager.set(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    }
                }
            } catch (e: SecurityException) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    } else {
                        alarmManager.set(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    }
                } catch (ex: Exception) {
                    AppLogger.e("CountdownContent", "Alarm scheduling failed", ex)
                }
            } catch (e: Exception) {
                AppLogger.e("CountdownContent", "Unexpected alarm scheduling error", e)
            }

            val startRemaining = remainingMs
            val startTime = SystemClock.elapsedRealtime()
            while (isRunning && remainingMs > 0L) {
                val elapsed = SystemClock.elapsedRealtime() - startTime
                remainingMs = (startRemaining - elapsed).coerceAtLeast(0L)
                if (remainingMs == 0L) {
                    isRunning = false
                    finished = true
                    val entry = TimerHistoryEntry(
                        type = "Timer",
                        durationMs = totalMs,
                        timestamp = System.currentTimeMillis(),
                        detail = formatCountdown(totalMs) + " Preset"
                    )
                    addTimerHistoryEntry(viewModel, scope, entry)
                }
                delay(100L)
            }
        } else {
            alarmManager.cancel(alarmIntent)
        }
    }

    val progress = if (totalMs > 0) remainingMs.toFloat() / totalMs.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isFloating) 8.dp else 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            items(presetTimers.size) { idx ->
                val (label, ms) = presetTimers[idx]
                FilterChip(
                    selected = totalMs == ms && !showCustomInput,
                    onClick = {
                        com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true, 16L)
                        isRunning = false
                        finished = false
                        totalMs = ms
                        remainingMs = ms
                        showCustomInput = false
                    },
                    label = { Text(label, fontSize = 12.sp) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
            item {
                FilterChip(
                    selected = showCustomInput,
                    onClick = { showCustomInput = !showCustomInput },
                    label = { Text("Custom", fontSize = 12.sp) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        if (showCustomInput) {
            OutlinedTextField(
                value = customInput,
                onValueChange = { customInput = it },
                label = { Text("MM:SS or MM") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                trailingIcon = {
                    TextButton(onClick = {
                        val parts = customInput.split(":")
                        val ms = when (parts.size) {
                            2 -> (parts[0].toLongOrNull() ?: 0L) * 60_000L + (parts[1].toLongOrNull() ?: 0L) * 1_000L
                            1 -> (parts[0].toLongOrNull() ?: 0L) * 60_000L
                            else -> 0L
                        }
                        if (ms > 0) {
                            isRunning = false
                            finished = false
                            totalMs = ms
                            remainingMs = ms
                            showCustomInput = false
                        }
                    }) {
                        Text("Set")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .size(if (isFloating) 140.dp else 180.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 6.dp,
                color = if (finished) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatCountdown(remainingMs),
                    fontSize = if (isFloating) 22.sp else 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (finished) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (finished) {
                    Text(
                        text = "Done",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true, 18L)
                    isRunning = false
                    finished = false
                    remainingMs = totalMs
                },
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reset",
                    modifier = Modifier.size(24.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playHeavyVibration(context, true, 26L)
                    if (finished) {
                        finished = false
                        remainingMs = totalMs
                    } else {
                        isRunning = !isRunning
                    }
                },
                containerColor = when {
                    finished -> MaterialTheme.colorScheme.error
                    isRunning -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.primary
                },
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Filled.Pause else if (finished) Icons.Filled.Refresh else Icons.Filled.PlayArrow,
                    contentDescription = "Toggle timer",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
