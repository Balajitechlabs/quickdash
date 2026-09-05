/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/sections
 * File: SettingsFloatingWindowSection.kt
 * Description: Settings section configuring floating window size, opacity, and dock positions.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.sections

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SettingsSystemDaydream
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.services.FloatingBubbleService
import com.balajitechlabs.quickdash.core.services.ShakeDetectorService
import com.balajitechlabs.quickdash.core.ui.components.PreferenceGroup
import com.balajitechlabs.quickdash.core.ui.components.PreferenceItem
import com.balajitechlabs.quickdash.core.ui.components.StyledSwitch
import com.balajitechlabs.quickdash.core.ui.components.SwitchStyle

@Composable
fun SettingsFloatingWindowSection(
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    bubbleEnabled: Boolean,
    onToggleBubble: (Boolean) -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onNavigateToBubbleCustomizer: () -> Unit,
    shakeToOpen: Boolean,
    shakeMode: String,
    shakeSensitivity: String,
    hapticDuration: Float,
    onSaveShakeToOpen: (Boolean) -> Unit,
    onSaveShakeMode: (String) -> Unit,
    onSaveShakeSensitivity: (String) -> Unit,
    onSaveHapticDuration: (Float) -> Unit,
    onFeedback: () -> Unit,
    activeSwitchStyle: SwitchStyle
) {
    val context = LocalContext.current

    PreferenceGroup(
        title = "Floating Window & Bubble",
        expanded = expanded,
        onHeaderClick = onHeaderClick
    ) {
        PreferenceItem(
            title = "Quick Bubble",
            subtitle = "System-wide floating bubble on top of any app",
            iconVector = Icons.Default.ChatBubbleOutline,
            trailing = {
                StyledSwitch(
                    style = activeSwitchStyle,
                    checked = bubbleEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (Settings.canDrawOverlays(context)) {
                                onToggleBubble(true)
                                context.startService(Intent(context, FloatingBubbleService::class.java))
                            } else {
                                onRequestOverlayPermission()
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        } else {
                            onToggleBubble(false)
                            context.stopService(Intent(context, FloatingBubbleService::class.java))
                        }
                    }
                )
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        PreferenceItem(
            title = "Bubble Appearance",
            subtitle = "Customize icon size, transparency, glow & quick tools",
            iconVector = Icons.Default.Palette,
            onClick = {
                onFeedback()
                onNavigateToBubbleCustomizer()
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        PreferenceItem(
            title = "Shake to Open QuickDash",
            subtitle = "Shake your phone in any app (e.g. WhatsApp) to launch QuickDash",
            iconVector = Icons.Default.ScreenRotation,
            trailing = {
                StyledSwitch(
                    style = activeSwitchStyle,
                    checked = shakeToOpen,
                    onCheckedChange = { enabled ->
                        onFeedback()
                        onSaveShakeToOpen(enabled)
                        val serviceIntent = Intent(context, ShakeDetectorService::class.java)
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent)
                            } else {
                                context.startService(serviceIntent)
                            }
                        } else {
                            context.stopService(serviceIntent)
                        }
                    }
                )
            }
        )
        if (shakeToOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Shake Trigger Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SINGLE" to "Single Shake", "DOUBLE" to "Double Shake").forEach { (mode, label) ->
                        val isSelected = shakeMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onFeedback()
                                onSaveShakeMode(mode)
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = Color(0xFF2A2B30),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Shake Sensitivity",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LOW" to "Low", "MEDIUM" to "Medium", "HIGH" to "High").forEach { (sens, label) ->
                        val isSelected = shakeSensitivity == sens
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onFeedback()
                                onSaveShakeSensitivity(sens)
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = Color(0xFF2A2B30),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Vibration Intensity on Shake: ${hapticDuration.toInt()}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Slider(
                    value = hapticDuration,
                    onValueChange = onSaveHapticDuration,
                    valueRange = 10f..80f,
                    steps = 7,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        PreferenceItem(
            title = "Quick Settings Tile",
            subtitle = "Add QuickDash tile to system notification shade",
            iconVector = Icons.Default.SettingsSystemDaydream,
            onClick = {
                if (Build.VERSION.SDK_INT >= 33) {
                    try {
                        val manager = context.getSystemService(Context.STATUS_BAR_SERVICE) as StatusBarManager
                        val componentName = ComponentName(
                            context,
                            "com.balajitechlabs.quickdash.core.services.QuickTileService"
                        )
                        manager.requestAddTileService(
                            componentName,
                            "QuickDash Hub",
                            Icon.createWithResource(context, R.mipmap.ic_launcher_round),
                            { executor -> executor.run() },
                            { _ -> }
                        )
                    } catch (_: Exception) {}
                } else {
                    Toast.makeText(context, "Pull down top notification shade, tap Edit to add QuickDash Tile", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}
