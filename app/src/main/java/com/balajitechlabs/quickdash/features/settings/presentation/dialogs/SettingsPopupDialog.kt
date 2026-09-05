/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/dialogs
 * File: SettingsPopupDialog.kt
 * Description: Dialog hosting quick access preferences in floating window mode.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.features.settings.presentation.SettingsScreen

@Composable
fun SettingsPopupDialog(
    themeMode: String,
    dynamicColor: Boolean,
    bubbleEnabled: Boolean,
    onChangeThemeMode: (String) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onToggleBubble: (Boolean) -> Unit,
    onTriggerConfetti: (String) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToSystemLogs: () -> Unit,
    onManageUpiIds: () -> Unit,
    onNavigateToBubbleCustomizer: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* consume clicks inside */ },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onDismiss() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Box(modifier = Modifier.weight(1f)) {
                    SettingsScreen(
                        themeMode = themeMode,
                        dynamicColor = dynamicColor,
                        bubbleEnabled = bubbleEnabled,
                        onChangeThemeMode = onChangeThemeMode,
                        onToggleDynamicColor = onToggleDynamicColor,
                        onToggleBubble = onToggleBubble,
                        onTriggerConfetti = onTriggerConfetti,
                        onBackToHome = onDismiss,
                        onNavigateToSystemLogs = {
                            onDismiss()
                            onNavigateToSystemLogs()
                        },
                        onManageUpiIds = {
                            onDismiss()
                            onManageUpiIds()
                        },
                        onNavigateToBubbleCustomizer = {
                            onDismiss()
                            onNavigateToBubbleCustomizer()
                        }
                    )
                }
            }
        }
    }
}
