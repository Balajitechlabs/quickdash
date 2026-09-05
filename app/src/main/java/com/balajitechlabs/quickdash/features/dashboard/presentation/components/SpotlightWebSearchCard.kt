/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/dashboard/presentation/components
 * File: SpotlightWebSearchCard.kt
 * Description: Quick search card integrating search engine queries and web navigation.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.dashboard.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SpotlightWebSearchCard(
    searchQuery: String,
    onTriggerHaptic: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onTriggerHaptic()
                    val cleanQuery = searchQuery.trim()
                    val url = if (cleanQuery.startsWith("http://") || cleanQuery.startsWith("https://")) {
                        cleanQuery
                    } else if (cleanQuery.contains(".") && !cleanQuery.contains(" ")) {
                        "https://$cleanQuery"
                    } else {
                        "https://www.google.com/search?q=${Uri.encode(cleanQuery)}"
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF38393F),
            border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = "Search Web",
                    tint = Color(0xFFB0C6FF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Search web for \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                SearchEngineChip(label = "Google", icon = Icons.Rounded.Search) {
                    onTriggerHaptic()
                    val url = "https://www.google.com/search?q=${Uri.encode(searchQuery.trim())}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            item {
                SearchEngineChip(label = "YouTube", icon = Icons.Rounded.PlayArrow) {
                    onTriggerHaptic()
                    val url = "https://www.youtube.com/results?search_query=${Uri.encode(searchQuery.trim())}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            item {
                SearchEngineChip(label = "GitHub", icon = Icons.Rounded.Code) {
                    onTriggerHaptic()
                    val url = "https://github.com/search?q=${Uri.encode(searchQuery.trim())}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            item {
                SearchEngineChip(label = "Reddit", icon = Icons.AutoMirrored.Rounded.Chat) {
                    onTriggerHaptic()
                    val url = "https://www.reddit.com/search/?q=${Uri.encode(searchQuery.trim())}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            item {
                SearchEngineChip(label = "Wikipedia", icon = Icons.AutoMirrored.Rounded.MenuBook) {
                    onTriggerHaptic()
                    val url = "https://en.wikipedia.org/wiki/Special:Search?search=${Uri.encode(searchQuery.trim())}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }
    }
}
