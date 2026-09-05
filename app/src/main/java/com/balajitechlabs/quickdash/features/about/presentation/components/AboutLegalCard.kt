/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
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
                text = "Fork of IIXII™ property • PocketOps Custom Fork License",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                        onShowLicense()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View License", fontSize = 12.sp)
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
                    Text("Privacy Policy", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Forked & Maintained by ||BTL||™ (balajitechlabs)\nOriginal Property © 2026 Aakarsh (L192) / IIXII™",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
