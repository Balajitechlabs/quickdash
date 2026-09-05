/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/timer/presentation/components
 * File: StopwatchContent.kt
 * Description: Stopwatch interface with millisecond precision, lap recording, and session history persistence.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.timer.presentation.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.features.timer.presentation.QuickTimerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

@Composable
fun StopwatchContent(
    viewModel: QuickTimerViewModel,
    scope: CoroutineScope,
    isFloating: Boolean
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isRunning by remember { mutableStateOf(false) }
    var elapsed by remember { mutableLongStateOf(0L) }
    var laps by remember { mutableStateOf(listOf<Long>()) }
    var lastLapTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = SystemClock.elapsedRealtime() - elapsed
            while (isRunning) {
                elapsed = SystemClock.elapsedRealtime() - startTime
                delay(16L)
            }
        }
    }

    val bestLap = laps.minOrNull()
    val worstLap = laps.maxOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isFloating) 8.dp else 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (isFloating) 140.dp else 180.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { (elapsed % 60_000L) / 60_000f },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = formatMs(elapsed),
                fontSize = if (isFloating) 18.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true, 18L)
                    if (isRunning) {
                        val lapDuration = elapsed - lastLapTime
                        laps = laps + lapDuration
                        lastLapTime = elapsed
                    } else {
                        if (elapsed > 0L) {
                            val count = laps.size
                            val entry = TimerHistoryEntry(
                                type = "Stopwatch",
                                durationMs = elapsed,
                                timestamp = System.currentTimeMillis(),
                                detail = if (count > 0) "$count Laps" else "Standard Run"
                            )
                            addTimerHistoryEntry(viewModel, scope, entry)
                        }
                        elapsed = 0L
                        laps = emptyList()
                        lastLapTime = 0L
                    }
                },
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Filled.Flag else Icons.Filled.Refresh,
                    contentDescription = if (isRunning) "Lap" else "Reset",
                    modifier = Modifier.size(24.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playHeavyVibration(context, true, 26L)
                    isRunning = !isRunning
                },
                containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = if (isRunning) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (laps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 160.dp)
            ) {
                itemsIndexed(laps.reversed()) { i, lapMs ->
                    val lapIdx = laps.size - i
                    val isBest = lapMs == bestLap && laps.size > 1
                    val isWorst = lapMs == worstLap && laps.size > 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lap $lapIdx",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatMs(lapMs),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                isBest -> Color(0xFF4CAF50)
                                isWorst -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    if (i < laps.size - 1) HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}
