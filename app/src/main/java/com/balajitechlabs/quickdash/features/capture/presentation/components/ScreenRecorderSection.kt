/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/capture/presentation/components
 * File: ScreenRecorderSection.kt
 * Description: Control card managing video capture, audio toggle, and recording timer.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.capture.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun ScreenRecorderSection(
    isRecording: Boolean,
    isPaused: Boolean,
    recordAudio: Boolean,
    qualityRes: String,
    elapsedSeconds: Int,
    onToggleAudio: () -> Unit,
    onToggleRecord: () -> Unit,
    onTogglePause: () -> Unit,
    onSelectQuality: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1C1C1E),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        when {
                            isRecording && !isPaused -> Color(0xFFFF3B30).copy(alpha = 0.25f)
                            isPaused -> Color(0xFFFF9500).copy(alpha = 0.25f)
                            else -> Color(0xFF2C2C2E)
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isRecording && !isPaused -> Color(0xFFFF3B30).copy(alpha = 0.6f)
                            isPaused -> Color(0xFFFF9500).copy(alpha = 0.6f)
                            else -> Color.White.copy(alpha = 0.1f)
                        },
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .scale(if (isRecording && !isPaused) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(
                            when {
                                isRecording && !isPaused -> Color(0xFFFF3B30).copy(alpha = pulseAlpha)
                                isPaused -> Color(0xFFFF9500)
                                else -> Color(0xFF48484A)
                            }
                        )
                )

                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                Text(
                    text = when {
                        isRecording && !isPaused -> "REC  ${String.format(Locale.US, "%02d:%02d", minutes, seconds)}"
                        isPaused -> "PAUSED  ${String.format(Locale.US, "%02d:%02d", minutes, seconds)}"
                        else -> "READY"
                    },
                    color = when {
                        isRecording && !isPaused -> Color.White
                        isPaused -> Color(0xFFFF9500)
                        else -> Color(0xFF8E8E93)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF2C2C2E))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleAudio,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (recordAudio) MaterialTheme.colorScheme.primaryContainer.copy(0.9f) else Color(0xFF3A3A3C))
                ) {
                    Icon(
                        if (recordAudio) Icons.Filled.Mic else Icons.Filled.MicOff,
                        "Toggle Mic",
                        tint = if (recordAudio) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF636366)
                    )
                }

                IconButton(
                    onClick = onToggleRecord,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isRecording)
                                    listOf(Color(0xFFFF453A), Color(0xFFBF0000))
                                else
                                    listOf(Color(0xFF34C759), Color(0xFF248A3D))
                            )
                        )
                ) {
                    Icon(
                        if (isRecording) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
                        if (isRecording) "Stop Recording" else "Start Recording",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(
                    onClick = onTogglePause,
                    enabled = isRecording,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isPaused) Color(0xFFFF9500).copy(0.3f) else if (isRecording) Color(0xFF3A3A3C) else Color.Transparent)
                ) {
                    Icon(
                        if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        "Pause/Resume",
                        tint = when {
                            !isRecording -> Color(0xFF636366)
                            isPaused -> Color(0xFFFF9500)
                            else -> Color.White
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Recording Resolution",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFC5C6D0)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("720p HD", "1080p FHD", "4K Ultra").forEach { res ->
                        FilterChip(
                            selected = qualityRes == res,
                            onClick = { if (!isRecording) onSelectQuality(res) },
                            enabled = !isRecording,
                            label = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        res,
                                        fontSize = 11.sp,
                                        fontWeight = if (qualityRes == res) FontWeight.Bold else FontWeight.Medium,
                                        color = if (qualityRes == res) Color.White else Color(0xFF8E8E93),
                                        maxLines = 1
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color(0xFF2C2C2E)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (isRecording) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Screen is being recorded · $qualityRes",
                    color = Color(0xFF8E8E93),
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "Tap stop icon to finish. Saved to Movies/QuickDash",
                    color = Color(0xFF636366),
                    fontSize = 10.sp
                )
            }
        }
    }
}
