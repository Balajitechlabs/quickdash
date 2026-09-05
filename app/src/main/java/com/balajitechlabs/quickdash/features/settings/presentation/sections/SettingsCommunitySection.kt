/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/sections
 * File: SettingsCommunitySection.kt
 * Description: Settings section providing direct links to Telegram, Reddit, developer website, and support.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.sections

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.core.ui.components.PreferenceGroup
import com.balajitechlabs.quickdash.core.ui.components.PreferenceItem

@Composable
fun SettingsCommunitySection(
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    context: Context,
    onShowStats: () -> Unit,
    onShowAdminMessage: () -> Unit
) {
    PreferenceGroup(
        title = "Community & About",
        expanded = expanded,
        onHeaderClick = onHeaderClick
    ) {
        PreferenceItem(
            title = "Telegram Community",
            subtitle = "Join for direct announcements, betas & feedback",
            iconVector = Icons.AutoMirrored.Filled.Chat,
            onClick = {
                openDirectUrl(context, "https://t.me/+FYlt5cBA29Q0ZWJl")
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        PreferenceItem(
            title = "Reddit Community",
            subtitle = "r/balajitechlabs — Discussions and ideas",
            iconVector = Icons.Default.Forum,
            onClick = {
                openDirectUrl(context, "https://www.reddit.com/r/balajitechlabs/")
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        PreferenceItem(
            title = "Developer Website",
            subtitle = "balajitechlab.com — Portfolio & updates",
            iconVector = Icons.Default.Public,
            onClick = {
                openDirectUrl(context, "https://balajitechlab.com")
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        PreferenceItem(
            title = "Message Admin",
            subtitle = "Send direct feedback or feature proposals",
            iconVector = Icons.AutoMirrored.Filled.Send,
            onClick = onShowAdminMessage
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        PreferenceItem(
            title = "App Statistics",
            subtitle = "View opens, QR generations, and notes count",
            iconVector = Icons.Default.BarChart,
            onClick = onShowStats
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}

private fun openDirectUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
