package com.balajitechlabs.quickdash.features.timer.presentation

import androidx.compose.animation.AnimatedContent
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.balajitechlabs.quickdash.core.data.UserStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private fun formatMs(ms: Long): String {
    val hours = ms / 3_600_000L
    val minutes = (ms % 3_600_000L) / 60_000L
    val seconds = (ms % 60_000L) / 1_000L
    val centis = (ms % 1_000L) / 10L
    return if (hours > 0) "%02d:%02d:%02d.%02d".format(hours, minutes, seconds, centis)
    else "%02d:%02d.%02d".format(minutes, seconds, centis)
}

private fun formatCountdown(ms: Long): String {
    val h = ms / 3_600_000L
    val m = (ms % 3_600_000L) / 60_000L
    val s = (ms % 60_000L) / 1_000L
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}


data class TimerHistoryEntry(
    val type: String, // "Stopwatch" or "Timer"
    val durationMs: Long,
    val timestamp: Long,
    val detail: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTimerScreen(userStore: UserStore, isFloating: Boolean = false) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SegmentedButton(
                selected = selectedTab == 0,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedTab = 0
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                label = { Text("Stopwatch") },
                icon = { SegmentedButtonDefaults.Icon(active = selectedTab == 0) }
            )
            SegmentedButton(
                selected = selectedTab == 1,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedTab = 1
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                label = { Text("Timer") },
                icon = { SegmentedButtonDefaults.Icon(active = selectedTab == 1) }
            )
            SegmentedButton(
                selected = selectedTab == 2,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedTab = 2
                },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                label = { Text("History") },
                icon = { SegmentedButtonDefaults.Icon(active = selectedTab == 2) }
            )
        }

        AnimatedContent(targetState = selectedTab, label = "tab") { tab ->
            when (tab) {
                0 -> StopwatchContent(userStore = userStore, scope = scope, isFloating = isFloating)
                1 -> CountdownContent(userStore = userStore, scope = scope, isFloating = isFloating)
                else -> TimerHistoryContent(userStore = userStore, isFloating = isFloating)
            }
        }
    }
}

@Composable
private fun StopwatchContent(userStore: UserStore, scope: kotlinx.coroutines.CoroutineScope, isFloating: Boolean) {
    val haptic = LocalHapticFeedback.current
    var isRunning by remember { mutableStateOf(false) }
    var elapsed by remember { mutableLongStateOf(0L) }
    var laps by remember { mutableStateOf(listOf<Long>()) }
    var lastLapTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = android.os.SystemClock.elapsedRealtime() - elapsed
            while (isRunning) {
                elapsed = android.os.SystemClock.elapsedRealtime() - startTime
                delay(16L)
            }
        }
    }

    val bestLap = laps.minOrNull()
    val worstLap = laps.maxOrNull()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (isFloating) 8.dp else 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Arc progress ring
        val angle = ((elapsed % 60_000L) / 60_000f) * 360f
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

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lap / Reset
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                            addTimerHistoryEntry(userStore, scope, entry)
                        }
                        elapsed = 0L; laps = emptyList(); lastLapTime = 0L
                    }
                },
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Flag else Icons.Default.Refresh,
                    contentDescription = if (isRunning) "Lap" else "Reset",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Start / Pause
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isRunning = !isRunning
                },
                containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = if (isRunning) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Lap list
        if (laps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp)) {
                itemsIndexed(laps.reversed()) { i, lapMs ->
                    val lapIdx = laps.size - i
                    val isBest = lapMs == bestLap && laps.size > 1
                    val isWorst = lapMs == worstLap && laps.size > 1
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lap $lapIdx", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            formatMs(lapMs),
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

private val presetTimers = listOf(
    "1 min" to 60_000L,
    "3 min" to 180_000L,
    "5 min" to 300_000L,
    "10 min" to 600_000L,
    "15 min" to 900_000L,
    "30 min" to 1_800_000L,
)

@Composable
private fun CountdownContent(userStore: UserStore, scope: kotlinx.coroutines.CoroutineScope, isFloating: Boolean) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current
    var totalMs by remember { mutableLongStateOf(300_000L) }
    var remainingMs by remember { mutableLongStateOf(300_000L) }
    var isRunning by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    // Text input for custom time (MM:SS)
    var customInput by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager }
    val timerId = remember { java.util.Random().nextInt(99999) + 1000 }
    val alarmIntent = remember(timerId) {
        android.app.PendingIntent.getBroadcast(
            context,
            timerId,
            android.content.Intent(context, TimerAlarmReceiver::class.java),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val triggerTime = android.os.SystemClock.elapsedRealtime() + remainingMs
            try {
                val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }

                if (canScheduleExact) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    } else {
                        alarmManager.setExact(
                            android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(
                            android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    } else {
                        alarmManager.set(
                            android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    }
                }
            } catch (e: SecurityException) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(
                            android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    } else {
                        alarmManager.set(
                            android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerTime,
                            alarmIntent
                        )
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val startRemaining = remainingMs
            val startTime = android.os.SystemClock.elapsedRealtime()
            while (isRunning && remainingMs > 0L) {
                val elapsed = android.os.SystemClock.elapsedRealtime() - startTime
                remainingMs = (startRemaining - elapsed).coerceAtLeast(0L)
                if (remainingMs == 0L) {
                    isRunning = false; finished = true
                    val entry = TimerHistoryEntry(
                        type = "Timer",
                        durationMs = totalMs,
                        timestamp = System.currentTimeMillis(),
                        detail = formatCountdown(totalMs) + " Preset"
                    )
                    addTimerHistoryEntry(userStore, scope, entry)
                }
                delay(100L)
            }
        } else {
            alarmManager.cancel(alarmIntent)
        }
    }

    val progress = if (totalMs > 0) remainingMs.toFloat() / totalMs.toFloat() else 0f

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (isFloating) 8.dp else 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Preset chips
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            items(presetTimers.size) { idx ->
                val (label, ms) = presetTimers[idx]
                FilterChip(
                    selected = totalMs == ms && !showCustomInput,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isRunning = false; finished = false
                        totalMs = ms; remainingMs = ms; showCustomInput = false
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                trailingIcon = {
                    TextButton(onClick = {
                        val parts = customInput.split(":")
                        val ms = when (parts.size) {
                            2 -> (parts[0].toLongOrNull() ?: 0L) * 60_000L + (parts[1].toLongOrNull() ?: 0L) * 1_000L
                            1 -> (parts[0].toLongOrNull() ?: 0L) * 60_000L
                            else -> 0L
                        }
                        if (ms > 0) {
                            isRunning = false; finished = false
                            totalMs = ms; remainingMs = ms; showCustomInput = false
                        }
                    }) { Text("Set") }
                }
            )
        }

        // Countdown display
        Box(
            modifier = Modifier.size(if (isFloating) 140.dp else 180.dp).padding(8.dp),
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
                    Text("Done! 🎉", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isRunning = false; finished = false; remainingMs = totalMs
                },
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(24.dp))
            }

            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (finished) { finished = false; remainingMs = totalMs } else isRunning = !isRunning
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
                    imageVector = if (isRunning) Icons.Default.Pause else if (finished) Icons.Default.Refresh else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun TimerHistoryContent(userStore: UserStore, isFloating: Boolean) {
    val historyJson by userStore.timerHistory.collectAsState(initial = "[]")
    val gson = remember { Gson() }
    val listType = remember { object : TypeToken<List<TimerHistoryEntry>>() {}.type }
    val entries = remember(historyJson) {
        try { gson.fromJson<List<TimerHistoryEntry>>(historyJson, listType) ?: emptyList() }
        catch (_: Exception) { emptyList() }
    }
    val scope = rememberCoroutineScope()
    var showClearAllConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isFloating) 8.dp else 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                    imageVector = Icons.Default.History,
                    contentDescription = null,
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
                modifier = Modifier.fillMaxWidth().heightIn(max = if (isFloating) 200.dp else 300.dp)
            ) {
                items(entries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (entry.type == "Stopwatch") Icons.Default.Flag else Icons.Default.Timer,
                                contentDescription = entry.type,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val durationText = if (entry.type == "Stopwatch") formatMs(entry.durationMs) else formatCountdown(entry.durationMs)
                                Text(
                                    text = "${entry.type}: $durationText",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val dateText = remember(entry.timestamp) {
                                    val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                    sdf.format(java.util.Date(entry.timestamp))
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
                                    scope.launch { userStore.saveTimerHistory(gson.toJson(newList)) }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
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
                        userStore.saveTimerHistory("[]")
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

private fun addTimerHistoryEntry(
    userStore: UserStore,
    scope: kotlinx.coroutines.CoroutineScope,
    entry: TimerHistoryEntry
) {
    scope.launch {
        try {
            val gson = Gson()
            val listType = object : TypeToken<List<TimerHistoryEntry>>() {}.type
            val currentJson = userStore.timerHistory.first()
            val currentList = gson.fromJson<List<TimerHistoryEntry>>(currentJson, listType) ?: emptyList()
            val newList = (listOf(entry) + currentList).take(50)
            userStore.saveTimerHistory(gson.toJson(newList))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}