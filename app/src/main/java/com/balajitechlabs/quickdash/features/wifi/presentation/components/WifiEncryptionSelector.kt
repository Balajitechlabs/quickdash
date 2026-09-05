/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/wifi/presentation/components
 * File: WifiEncryptionSelector.kt
 * Description: Horizontal scrollable chip selector for Wi-Fi encryption types and hidden network flag.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.wifi.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun WifiEncryptionSelector(
    encryptionType: String,
    onEncryptionTypeChange: (String) -> Unit,
    isHidden: Boolean,
    onHiddenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val securityOptions = listOf("WPA", "WEP", "nopass")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            securityOptions.forEach { type ->
                FilterChip(
                    selected = encryptionType == type,
                    onClick = { onEncryptionTypeChange(type) },
                    label = { Text(type) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = isHidden,
                onClick = { onHiddenChange(!isHidden) },
                label = { Text("Hidden") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (scrollState.value > 0) {
            IconButton(
                onClick = {
                    scope.launch {
                        scrollState.animateScrollTo((scrollState.value - 120).coerceAtLeast(0))
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Scroll Left",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (scrollState.value < scrollState.maxValue) {
            IconButton(
                onClick = {
                    scope.launch {
                        scrollState.animateScrollTo((scrollState.value + 120).coerceAtMost(scrollState.maxValue))
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Scroll Right",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
