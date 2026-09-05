/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/dialogs
 * File: BlogClearFeedDialog.kt
 * Description: Confirmation dialog for clearing cached developer blog announcements.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun BlogClearFeedDialog(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Clear Feed") },
        text = { Text("Are you sure you want to clear the feed and reset all cached messages, polls, and votes?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Clear Feed", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
