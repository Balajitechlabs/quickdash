/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui
 * File: QuickDashMotion.kt
 * Description: EssentialX-styled component for core/ui supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * QuickDash v5 Motion Design System
 * Standardized spring physics and easing specs across all animations.
 */
object QuickDashMotion {
    // Springs
    val defaultSpring = spring<Float>(
        dampingRatio = 0.7f,
        stiffness = 400f
    )
    
    val bouncySpring = spring<Float>(
        dampingRatio = 0.5f,
        stiffness = 250f
    )
    
    val stiffSpring = spring<Float>(
        dampingRatio = 0.9f,
        stiffness = 600f
    )

    val gentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Tweens
    val fastTween = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)
    val slowTween = tween<Float>(durationMillis = 400, easing = LinearOutSlowInEasing)
}
