/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/components
 * File: AccentWheelDialog.kt
 * Description: Color picker dialog allowing granular selection of custom theme accent hues and seed colors.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Custom RGB Accent Wheel Dialog (`AccentWheelDialog.kt`).
 * Interactive color picker allowing custom floating bubble ring and toolbar glow customization.
 */
@Composable
fun AccentWheelDialog(
    initialColor: Color = MaterialTheme.colorScheme.primary,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var red by remember { mutableFloatStateOf(initialColor.red) }
    var green by remember { mutableFloatStateOf(initialColor.green) }
    var blue by remember { mutableFloatStateOf(initialColor.blue) }

    val currentColor = Color(red, green, blue)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Custom Accent Color Picker",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Red: ${(red * 255).toInt()}", style = MaterialTheme.typography.bodySmall)
                Slider(value = red, onValueChange = { red = it }, modifier = Modifier.fillMaxWidth())

                Text("Green: ${(green * 255).toInt()}", style = MaterialTheme.typography.bodySmall)
                Slider(value = green, onValueChange = { green = it }, modifier = Modifier.fillMaxWidth())

                Text("Blue: ${(blue * 255).toInt()}", style = MaterialTheme.typography.bodySmall)
                Slider(value = blue, onValueChange = { blue = it }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onColorSelected(currentColor)
                onDismiss()
            }) {
                Text("APPLY ACCENT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
