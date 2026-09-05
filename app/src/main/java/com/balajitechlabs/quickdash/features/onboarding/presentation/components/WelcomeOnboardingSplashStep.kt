/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/onboarding/presentation/components
 * File: WelcomeOnboardingSplashStep.kt
 * Description: Branded splash screen displayed during initial first-launch experience.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.onboarding.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WelcomeOnboardingSplashStep(onContinue: () -> Unit) {
    val scale = remember { Animatable(0.25f) }
    val alpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)) }
        launch { alpha.animateTo(1f, tween(500)) }
        delay(1000)
        taglineAlpha.animateTo(1f, tween(500))
        delay(600)
        onContinue()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.alpha = alpha.value
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "QuickDash",
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "QuickDash",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer { this.alpha = alpha.value }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your floating toolkit",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer { this.alpha = taglineAlpha.value }
            )
        }
    }
}
