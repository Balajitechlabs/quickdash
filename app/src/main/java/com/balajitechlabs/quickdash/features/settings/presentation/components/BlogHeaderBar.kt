/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/components
 * File: BlogHeaderBar.kt
 * Description: Header bar for developer blog posts with refresh and filter actions.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.balajitechlabs.quickdash.features.broadcast.data.TelegramPollerWorker

@Composable
fun BlogHeaderBar(
    context: Context,
    onClearFeedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                val oneTimeRequest = OneTimeWorkRequestBuilder<TelegramPollerWorker>().build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "telegram_poller_immediate",
                    ExistingWorkPolicy.REPLACE,
                    oneTimeRequest
                )
            }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh notifications",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onClearFeedClick) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = "Clear Feed",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
