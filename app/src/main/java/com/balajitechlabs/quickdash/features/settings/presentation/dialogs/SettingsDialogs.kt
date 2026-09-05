/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/dialogs
 * File: SettingsDialogs.kt
 * Description: Collection of standardized dialogs for resetting preferences, logs, and cache.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.dialogs

import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

private const val TAG = "SettingsDialogs"

@Composable
fun AppStatisticsDialog(
    totalOpens: Long,
    totalQrs: Long,
    totalNotes: Long,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Statistics", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Total App Opens: $totalOpens")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total QR Codes Generated: $totalQrs")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total Notes Saved: $totalNotes")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AdminMessageDialog(
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var adminMessageText by remember { mutableStateOf("") }
    val customShape = MaterialTheme.shapes.medium

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Message Admin", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Type a message below to send directly to the Admin channel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = adminMessageText,
                    onValueChange = { adminMessageText = it },
                    placeholder = { Text("Type your message here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = customShape,
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (adminMessageText.isNotBlank()) {
                        val messageToSend = adminMessageText
                        scope.launch {
                            try {
                                TelegramTracker.sendMessage(
                                    "<b>Custom Message to Admin</b>\n" +
                                        "Device: ${Build.MODEL} (${Build.MANUFACTURER})\n" +
                                        "Message: $messageToSend"
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to send message to admin", e)
                            }
                        }
                        onDismiss()
                    }
                },
                enabled = adminMessageText.isNotBlank()
            ) {
                Text("Send Message")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FeatureRequestDialog(
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var featureRequestText by remember { mutableStateOf("") }
    val customShape = MaterialTheme.shapes.medium

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request a Feature", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = featureRequestText,
                onValueChange = { featureRequestText = it },
                label = { Text("What feature would you like to see?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = customShape,
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 5
            )
        },
        confirmButton = {
            Button(onClick = {
                if (featureRequestText.isNotBlank()) {
                    val currentText = featureRequestText
                    scope.launch {
                        try {
                            val safeIdea = currentText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                            TelegramTracker.sendMessage("<b>Feature Request</b>\nIdea: $safeIdea")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to send feature request", e)
                        }
                    }
                }
                onDismiss()
            }) {
                Text("Send Idea")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CustomSearchEnginesDialog(
    customSearchEnginesJson: String,
    onSaveCustomEngines: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newEngineName by remember { mutableStateOf("") }
    var newEngineUrl by remember { mutableStateOf("") }
    val gson = remember { Gson() }
    val customShape = MaterialTheme.shapes.medium

    val customEngines: List<Map<String, String>> = remember(customSearchEnginesJson) {
        try {
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            gson.fromJson<List<Map<String, String>>>(customSearchEnginesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Search Engines", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                if (customEngines.isNotEmpty()) {
                    Text("Existing Custom Engines:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(customEngines) { engine ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(engine["name"] ?: "", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text(engine["url"] ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                                IconButton(onClick = {
                                    val updated = customEngines.filter { it != engine }
                                    onSaveCustomEngines(gson.toJson(updated))
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                Text("Add New Engine:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = newEngineName,
                    onValueChange = { newEngineName = it },
                    label = { Text("Engine Name") },
                    placeholder = { Text("e.g. GitHub Codesearch") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = customShape
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = newEngineUrl,
                    onValueChange = { newEngineUrl = it },
                    label = { Text("Search URL (ends with q=)") },
                    placeholder = { Text("e.g. https://github.com/search?q=") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = customShape
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (newEngineName.isNotBlank() && newEngineUrl.isNotBlank()) {
                    val newEngine = mapOf("name" to newEngineName, "url" to newEngineUrl)
                    val updated = customEngines + newEngine
                    onSaveCustomEngines(gson.toJson(updated))
                    newEngineName = ""
                    newEngineUrl = ""
                }
            }) {
                Text("Add Engine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun StarRatingDialog(
    onDismiss: () -> Unit
) {
    var selectedStars by remember { mutableIntStateOf(0) }
    var reviewText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate QuickDash") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { selectedStars = i }) {
                            Icon(
                                imageVector = if (i <= selectedStars) Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = "Star $i",
                                tint = if (i <= selectedStars) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (selectedStars > 0) {
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Optional Review") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                },
                enabled = selectedStars > 0
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PermitCertificateDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Verified Permit Certificate", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Image(
                painter = painterResource(R.drawable.permit_certificate),
                contentDescription = "Permit Certificate",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Fit
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun BubbleLearnMoreDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Quick Bubble", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "A system-wide floating bubble for instant access to all QuickDash features.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Tap to open the menu, drag to reposition.\n• Double-tap the bubble to disable it completely.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Available Features:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• UPI Pay\n• Quick Chat\n• Quick Search\n• Quick Notes\n• Calculator\n• Timer\n• Settings\n• Quick Web",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
fun TipsRecommendationsDialog(
    onDismiss: () -> Unit
) {
    val tips = listOf(
        "GitHub Rate Limit" to "Generate a Personal Access Token on GitHub (Settings → Developer Settings → Tokens) and paste it in Advanced & API Settings. Raises limit from 60 to 5,000 requests/hour.",
        "Social Link Routing" to "Social media profile links open natively in their apps when installed. On emulators, they fallback to your browser automatically.",
        "Play Protect" to "For sideloaded APKs, tap 'More details → Install anyway' on the Play Protect prompt. The Play Store version is auto-trusted.",
        "QR Scanner First Load" to "First QR scan may show a brief overlay — Google Play Services sets up the barcode engine once. Subsequent scans are instant.",
        "Backup Your Data" to "Use Data Management → Backup Data to export all settings, notes, and clipboard history as a JSON file before switching phones.",
        "Save Battery" to "Switch to AMOLED theme in Launch & Windows for true-black backgrounds that save battery on OLED displays.",
        "Manage Notifications" to "Swipe LEFT on any notification to dismiss it. Swipe RIGHT to pin it to the top of the feed for quick access.",
        "Quick Collect" to "Set your Default Target Payment App in Advanced & API Settings to pre-select GPay, PhonePe, or Paytm for faster QR generation.",
        "Custom Seed Color" to "Use the seed color picker in Appearance to create a unique color theme applied across the entire app.",
        "Timer Persistence" to "Countdown timers use AlarmManager exact alarms — they continue even in deep Doze mode when the phone is idle."
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFC107))
                Text("Tips & Recommendations", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tips.forEachIndexed { index, (title, body) ->
                    Column {
                        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (index < tips.size - 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } }
    )
}
