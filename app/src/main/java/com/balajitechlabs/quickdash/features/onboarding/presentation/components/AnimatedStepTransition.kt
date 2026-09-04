/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/onboarding
 * File: AnimatedStepTransition.kt
 * Description: EssentialX-styled component for features/onboarding supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.onboarding.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedStepTransition(
    currentStep: Int,
    stepContent: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            if (targetState > initialState) {
                slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) togetherWith
                slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(200))
            } else {
                slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) togetherWith
                slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200))
            }.using(SizeTransform(clip = false))
        },
        label = "stepTransition"
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            stepContent()
        }
    }
}
