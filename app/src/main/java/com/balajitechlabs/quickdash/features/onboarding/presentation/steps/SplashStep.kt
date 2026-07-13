package com.balajitechlabs.quickdash.features.onboarding.presentation.steps

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashStep(
    onFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val logoScale = remember { Animatable(0.3f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(30f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val shimmerProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(600))
        }

        delay(400)

        launch {
            textAlpha.animateTo(1f, animationSpec = tween(500))
            textOffsetY.animateTo(
                0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }

        delay(200)

        launch {
            subtitleAlpha.animateTo(1f, animationSpec = tween(500))
        }

        launch {
            shimmerProgress.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }

        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(logoAlpha.value)
        ) {
            Card(
                shape = RoundedCornerShape(36.dp),
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer(
                        scaleX = logoScale.value,
                        scaleY = logoScale.value
                    )
                    .shadow(24.dp, RoundedCornerShape(36.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "QuickDash Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "QuickDash",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 38.sp,
                modifier = Modifier
                    .graphicsLayer { translationY = textOffsetY.value }
                    .alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your Ultimate Productivity Hub",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "Made with \u2764 by balajitechlabs",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }
    }
}
