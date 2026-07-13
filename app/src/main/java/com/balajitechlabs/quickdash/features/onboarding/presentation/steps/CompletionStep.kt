package com.balajitechlabs.quickdash.features.onboarding.presentation.steps

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CompletionStep(
    notifGranted: Boolean,
    locationGranted: Boolean,
    upiConfigured: Boolean,
    themeMode: String,
    shapeStyle: String,
    fontName: String,
    onLaunch: () -> Unit
) {
    val checkScale = remember { Animatable(0f) }
    val circleProgress = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val summaryAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            circleProgress.animateTo(
                1f,
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
        }
        delay(300)
        launch {
            checkScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        delay(200)
        launch {
            contentAlpha.animateTo(1f, animationSpec = tween(500))
        }
        delay(300)
        launch {
            summaryAlpha.animateTo(1f, animationSpec = tween(500))
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Animated checkmark
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { circleProgress.value },
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer { rotationZ = -90f },
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeCap = StrokeCap.Round
            )
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier
                    .size(52.dp)
                    .scale(checkScale.value)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha.value }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "QuickDash is ready to boost your productivity.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha.value }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Setup summary card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = summaryAlpha.value },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Setup Summary",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                SummaryRow("Notifications", notifGranted)
                SummaryRow("Location Access", locationGranted)
                SummaryRow("Payment ID", upiConfigured)

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                SummaryRow("Theme", true, themeLabel = themeMode)
                SummaryRow("Shape", true, themeLabel = shapeStyle)
                SummaryRow("Font", true, themeLabel = fontName)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLaunch,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Launch QuickDash",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Made with \u2764 by balajitechlabs",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryRow(
    label: String,
    granted: Boolean,
    themeLabel: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (granted) "\u2713" else "\u2013",
            color = if (granted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = themeLabel ?: if (granted) "Enabled" else "Skipped",
            style = MaterialTheme.typography.bodySmall,
            color = if (granted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}
