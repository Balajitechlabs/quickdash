package com.balajitechlabs.quickdash.features.onboarding.presentation.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
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
            text = "Welcome to QuickDash v5.1.3 🚀",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Welcome to the v5.1.3 update! Featuring Android 16 readiness, Quick Settings Tile fixes, and 12 floating tools.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        FeatureHighlightCard(
            icon = Icons.Filled.QrCode,
            title = "Minimalist Quick Collect",
            description = "Generate universal UPI & PayPal payment QR codes instantly in a compact floating card.",
            accentColor = MaterialTheme.colorScheme.primary,
            delayMillis = 0
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureHighlightCard(
            icon = Icons.Filled.QrCodeScanner,
            title = "Inbuilt QR Scanner Tile",
            description = "New Quick Settings Tile to scan payment QR codes directly from your notification bar.",
            accentColor = MaterialTheme.colorScheme.secondary,
            delayMillis = 80
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureHighlightCard(
            icon = Icons.AutoMirrored.Filled.Send,
            title = "Pure Icon Social & Chat Hub",
            description = "Instant deep-links for WhatsApp, Telegram, Signal, Instagram, Facebook & X with minimalist vector icons.",
            accentColor = MaterialTheme.colorScheme.tertiary,
            delayMillis = 160
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureHighlightCard(
            icon = Icons.Filled.AutoAwesome,
            title = "On-Device Offline AI",
            description = "Private text translation and summarization running 100% locally on your phone.",
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
