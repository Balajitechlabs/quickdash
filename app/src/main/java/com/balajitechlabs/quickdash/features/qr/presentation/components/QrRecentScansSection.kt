/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr/presentation/components
 * File: QrRecentScansSection.kt
 * Description: Section displaying recently scanned QR codes and barcodes with quick action triggers.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.playClickVibration
import com.balajitechlabs.quickdash.features.qr.utils.QrActionHelper
import com.balajitechlabs.quickdash.features.qr.utils.QrPayloadParser

@Composable
fun QrRecentScansSection(
    recentScans: List<String>,
    onClearScans: () -> Unit,
    onSelectScan: (String) -> Unit,
    onCopyScan: (String) -> Unit,
    hapticEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF38393F),
        border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Scans",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
                TextButton(
                    onClick = {
                        playClickVibration(context, hapticEnabled)
                        onClearScans()
                    }
                ) {
                    Text("Clear", color = Color(0xFFC5C6D0), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentScans.take(5).forEach { scanText ->
                    val parsed = remember(scanText) { QrPayloadParser.parse(scanText) }
                    Surface(
                        onClick = {
                            playClickVibration(context, hapticEnabled)
                            onSelectScan(scanText)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF2A2B30),
                        border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = QrActionHelper.getPayloadIcon(parsed),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = parsed.summary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = parsed.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFC5C6D0)
                                )
                            }
                            IconButton(
                                onClick = {
                                    playClickVibration(context, hapticEnabled)
                                    onCopyScan(scanText)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = Color(0xFFC5C6D0),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
