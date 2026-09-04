/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui
 * File: WhatsNewDialog.kt
 * Description: EssentialX-styled component for core/ui supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewDialog(
    versionName: String = "5.2.1",
    onDismiss: () -> Unit
) {
    var isInverted by remember { mutableStateOf(false) }

    // Resolve base colors based on current system theme
    val isSystemDark = isSystemInDarkTheme()
    // Normal: follows system theme. Inverted: opposite of system theme.
    val darkColors = isInverted == !isSystemDark

    val backgroundColor by animateColorAsState(
        targetValue = if (darkColors) Color(0xFF121212) else Color(0xFFFFFFFF),
        animationSpec = tween(durationMillis = 300),
        label = "dialogBgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (darkColors) Color(0xFFE0E0E0) else Color(0xFF212121),
        animationSpec = tween(durationMillis = 300),
        label = "dialogTextColor"
    )
    val primaryColor by animateColorAsState(
        targetValue = if (darkColors) Color(0xFF80CAFF) else Color(0xFF1E88E5),
        animationSpec = tween(durationMillis = 300),
        label = "dialogPrimaryColor"
    )
    val cardColor by animateColorAsState(
        targetValue = if (darkColors) Color(0xFF1E1E1E) else Color(0xFFF5F5F5),
        animationSpec = tween(durationMillis = 300),
        label = "dialogCardColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (darkColors) Color(0xFF333333) else Color(0xFFE0E0E0),
        animationSpec = tween(durationMillis = 300),
        label = "dialogBorderColor"
    )

    val items = listOf(
        "Radial Quick-Wheel" to "Long-press or swipe on the floating bubble to instantly summon top 4 tools with spring physics.",
        "Glance Material 3 Widgets" to "Responsive home screen widgets (1x1 Squircle, 2x1 Quick Bar, 2x2 Tool Hub) with dynamic tinting.",
        "Encrypted Backup and Restore" to "Export and import settings & notes with AES-256-GCM encryption and PBKDF2 passphrase protection.",
        "In-App Update Engine" to "Streamlined update checker, download progress indicator, and automatic package installer trigger.",
        "Architecture and Stability" to "Target SDK 37 (Android 16), Unified UserStore DataStore migration, and comprehensive test suite."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = if (darkColors) Color.Black else Color.White
                )
            ) {
                Text("Got it", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { isInverted = !isInverted },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
            ) {
                Icon(
                    imageVector = Icons.Filled.InvertColors,
                    contentDescription = "Invert Colors",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Invert", fontSize = 13.sp)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Changelog",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                Surface(
                    color = primaryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "v$versionName",
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            val maxH = (androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp * 0.4f).dp.coerceIn(200.dp, 400.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .maxHeight(maxH)
                    .verticalScroll(rememberScrollState())
            ) {
                items.forEachIndexed { index, (title, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(cardColor, RoundedCornerShape(12.dp))
                            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${index + 1}.",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = backgroundColor
    )
}

// Simple Helper for checking Dark Theme inside Dialog
@Composable
fun isSystemInDarkTheme(): Boolean {
    return androidx.compose.foundation.isSystemInDarkTheme()
}

// Extension to cap height of column
private fun Modifier.maxHeight(max: androidx.compose.ui.unit.Dp): Modifier = this.layout { measurable, constraints ->
    val cap = max.roundToPx()
    val childConstraints = constraints.copy(maxHeight = cap.coerceAtMost(constraints.maxHeight))
    val placeable = measurable.measure(childConstraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(0, 0)
    }
}