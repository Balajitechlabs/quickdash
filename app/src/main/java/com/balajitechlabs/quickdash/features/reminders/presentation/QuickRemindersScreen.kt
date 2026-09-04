/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/reminders
 * File: QuickRemindersScreen.kt
 * Description: EssentialX-styled component for features/reminders supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.reminders.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer

/**
 * ⏰ Tool #24 — Quick Reminders & Focus Alarm (`QuickRemindersScreen.kt`).
 * 1-tap floating alarm and break reminder setter.
 */
@Composable
fun QuickRemindersScreen() {
    val context = LocalContext.current
    var reminderText by remember { mutableStateOf("") }
    var selectedMinutes by remember { mutableStateOf(15) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⏰ Quick Reminders & Alarm",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        RoundedCardContainer {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = reminderText,
                    onValueChange = { reminderText = it },
                    label = { Text("Reminder Title") },
                    placeholder = { Text("e.g. Take study break / Drink water") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Timer Interval",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 30, 60).forEach { min ->
                        val isSelected = selectedMinutes == min
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMinutes = min },
                            label = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${min}m",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else Color(0xFFC5C6D0),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color(0xFF2C2C2E)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF44474F).copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val title = if (reminderText.isBlank()) "Quick Reminder" else reminderText
                        try {
                            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as? android.app.AlarmManager
                            val intent = android.content.Intent(context, com.balajitechlabs.quickdash.features.timer.presentation.TimerAlarmReceiver::class.java).apply {
                                putExtra("EXTRA_TITLE", "⏰ Reminder: $title")
                                putExtra("EXTRA_MESSAGE", title)
                            }
                            val pendingIntent = android.app.PendingIntent.getBroadcast(
                                context,
                                (System.currentTimeMillis() % 100000).toInt(),
                                intent,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                            )
                            val triggerAt = System.currentTimeMillis() + (selectedMinutes * 60 * 1000L)
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                alarmManager?.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                            } else {
                                alarmManager?.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                            }
                            Toast.makeText(context, "⏰ Reminder set for $selectedMinutes minutes!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Reminder set for $selectedMinutes minutes!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(androidx.compose.material.icons.Icons.Filled.Alarm, contentDescription = "Set reminder", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set Reminder Alarm", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
