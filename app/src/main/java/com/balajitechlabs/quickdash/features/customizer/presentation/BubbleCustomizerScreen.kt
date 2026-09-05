/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/customizer/presentation
 * File: BubbleCustomizerScreen.kt
 * Description: Interactive preview screen for configuring floating bubble size, opacity, glow, and animations.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.customizer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Glassmorphism & Bubble Customizer Screen (`BubbleCustomizerScreen.kt`).
 * Sliders for Bubble Size (48–84dp), Opacity (30–100%), 6 Neon Swatches, and Wallpaper Sync.
 */
@Composable
fun BubbleCustomizerScreen(viewModel: CustomizerViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val bubbleSizeDp by viewModel.bubbleSizeDp.collectAsStateWithLifecycle(initialValue = 60f)
    val bubbleOpacity by viewModel.bubbleOpacityAlpha.collectAsStateWithLifecycle(initialValue = 0.9f)
    val glowColorHex by viewModel.bubbleGlowColorHex.collectAsStateWithLifecycle(initialValue = "#3DDC84")
    val useDynamicWallpaper by viewModel.useDynamicWallpaperColor.collectAsStateWithLifecycle(initialValue = true)
    val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsStateWithLifecycle(initialValue = true)

    val swatches = listOf(
        "#3DDC84", // Neon Green
        "#00E5FF", // Cyan Electric
        "#FF295D", // Neon Pink
        "#7C4DFF", // Cyber Violet
        "#FFD600", // Gold Yellow
        "#FF6D00"  // Blaze Orange
    )

    fun parseColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFF3DDC84)
        }
    }

    val activeGlowColor = parseColor(glowColorHex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.BubbleChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Bubble & Glass Customizer",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(bubbleSizeDp.dp)
                        .clip(CircleShape)
                        .background(activeGlowColor.copy(alpha = bubbleOpacity)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Smartphone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size((bubbleSizeDp * 0.5f).dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls Pod
        RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Bubble Size Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.BubbleChart, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bubble Size: ${bubbleSizeDp.toInt()} dp",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Slider(
                    value = bubbleSizeDp,
                    onValueChange = { scope.launch { viewModel.saveBubbleSizeDp(it) } },
                    valueRange = 48f..84f,
                    steps = 35
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Opacity Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Opacity, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Glass Transparency: ${(bubbleOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Slider(
                    value = bubbleOpacity,
                    onValueChange = { scope.launch { viewModel.saveBubbleOpacityAlpha(it) } },
                    valueRange = 0.3f..1.0f
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color Swatches Pod
        RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.ColorLens, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Neon Glow Swatches", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    swatches.forEach { hex ->
                        val color = parseColor(hex)
                        val isSelected = glowColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    scope.launch { viewModel.saveBubbleGlowColorHex(hex) }
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Wallpaper & Sound Toggles Pod
        RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic Wallpaper Sync", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Auto-match glow ring to phone wallpaper accent",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Switch(
                        checked = useDynamicWallpaper,
                        onCheckedChange = { scope.launch { viewModel.saveUseDynamicWallpaperColor(it) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF2A2B30),
                            uncheckedBorderColor = Color(0xFF44474F)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("System Sound Feedback", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Tactile audio clicks on bubble drag and tap",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Switch(
                        checked = soundEffectsEnabled,
                        onCheckedChange = { scope.launch { viewModel.saveSoundEffectsEnabled(it) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFF2A2B30),
                            uncheckedBorderColor = Color(0xFF44474F)
                        )
                    )
                }
            }
        }
    }
}
