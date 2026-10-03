/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/about/presentation/components
 * File: AboutLegalCard.kt
 * Description: Card displaying license terms, privacy policy link, and copyright notice.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.about.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer

@Composable
fun AboutLegalCard(
    context: Context,
    onShowLicense: () -> Unit,
    modifier: Modifier = Modifier
) {
    RoundedCardContainer(
        containerColor = Color(0xFF1E2024),
        cornerRadius = 20.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Legal & Open Source",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Licensed under GNU GPL v3.0 • Inspired by PocketOps (IIXII™)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                        onShowLicense()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("License", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = {
                        com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                        val privacyUrl = "https://balajitechlab.com/privacy"
                        val fallbackUrl = "https://quickdash.balajitechlab.com"
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        } catch (_: Exception) {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Privacy", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = {
                        com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                        val contributorsUrl = "https://github.com/Balajitechlabs/quickdash/graphs/contributors"
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(contributorsUrl))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Team & Credits", fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Developed & Maintained by ||BTL||™ (balajitechlabs) & Mohith (@mohith-dev-m1)\nOriginally Inspired by © 2026 Aakarsh Singhal (L192) / IIXII™",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
