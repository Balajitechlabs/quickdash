/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/capture/presentation/dialogs
 * File: ScreenRecorderPermissionDialog.kt
 * Description: Dialog requesting MediaProjection and microphone permissions before recording.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.capture.presentation.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScreenRecorderPermissionDialog(
    recordAudio: Boolean,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Start Screen Recording?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("QuickDash will:")
                Text("• Capture everything on your screen")
                if (recordAudio) Text("• Record audio from your microphone")
                Text("• Save the recording to Movies/QuickDash in your gallery")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Android will ask for screen capture permission on the next step.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
