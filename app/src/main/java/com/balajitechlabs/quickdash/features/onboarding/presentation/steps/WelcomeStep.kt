package com.balajitechlabs.quickdash.features.onboarding.presentation.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
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
            text = "Your system-wide productivity hub for fast actions, QR payments, and deep customization.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        FeatureHighlightCard(
            icon = Icons.Default.QrCode,
            title = "Quick Collect Payments",
            description = "Generate UPI & PayPal QR codes instantly with saved targets.",
            accentColor = MaterialTheme.colorScheme.primary,
            delayMillis = 0
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureHighlightCard(
            icon = Icons.Default.ContentPaste,
            title = "Smart Clipboard",
            description = "Auto-detect phone numbers, emails, and links. Pin sensitive data.",
            accentColor = MaterialTheme.colorScheme.secondary,
            delayMillis = 80
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureHighlightCard(
            icon = Icons.Default.Search,
            title = "Multi-Engine Search",
            description = "Search across Google, DuckDuckGo, Bing & custom engines instantly.",
            accentColor = MaterialTheme.colorScheme.tertiary,
            delayMillis = 160
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureHighlightCard(
            icon = Icons.Default.Send,
            title = "Direct Chat Shortcuts",
            description = "Prefill messages and jump straight into WhatsApp, Telegram & more.",
            accentColor = MaterialTheme.colorScheme.error,
            delayMillis = 240
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Get Started",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
