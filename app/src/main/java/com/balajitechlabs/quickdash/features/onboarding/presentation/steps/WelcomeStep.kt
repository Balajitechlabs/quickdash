/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/onboarding/presentation/steps
 * File: WelcomeStep.kt
 * Description: Opening introduction step detailing the floating productivity suite vision.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.onboarding.presentation.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.FeatureHighlightCard

@Composable
fun WelcomeStep(
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Welcome to QuickDash",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your on-demand floating productivity suite. Turn it on with 1 tap from Control Center or Home Screen Widgets.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        FeatureHighlightCard(
            icon = Icons.Filled.ChatBubbleOutline,
            title = "Customizable Radial Bubble Wheel",
            description = "Long-press the bubble (350ms) to launch your favorite 4 shortcuts. Customize slots directly in Settings.",
            accentColor = MaterialTheme.colorScheme.primary,
            delayMillis = 0
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureHighlightCard(
            icon = Icons.Filled.Widgets,
            title = "1-Tap Quick Tile & Glance Widgets",
            description = "Toggle the floating bubble instantly from your Control Center shade or Material You home screen widget.",
            accentColor = MaterialTheme.colorScheme.secondary,
            delayMillis = 80
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureHighlightCard(
            icon = Icons.Filled.QrCode,
            title = "Minimalist Quick Collect",
            description = "Generate universal UPI & PayPal payment QR codes instantly in a compact floating card.",
            accentColor = MaterialTheme.colorScheme.tertiary,
            delayMillis = 160
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureHighlightCard(
            icon = Icons.Filled.AutoAwesome,
            title = "On-Device Offline Privacy",
            description = "Zero telemetry, zero tracking. Translation, text extraction (OCR), and AES-256 encrypted backups run 100% on-device.",
            accentColor = MaterialTheme.colorScheme.error,
            delayMillis = 240
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Continue Setup Step-by-Step",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
