/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/theme
 * File: QuickDashMotion.kt
 * Description: Motion specifications and spring animation transition specs for predictive back and screen navigation.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings

object QuickDashMotion {
    // Spring physics presets
    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val SheetSpring = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = 380f
    )

    val GentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessVeryLow
    )

    // Standard tweens
    val FastFade = tween<Float>(durationMillis = 150)
    val MediumFade = tween<Float>(durationMillis = 250)
    val StandardTransform = tween<Float>(durationMillis = 300)

    @Composable
    fun rememberReduceMotion(): Boolean {
        val context = LocalContext.current
        return remember(context) {
            try {
                val scale = Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1.0f
                )
                scale == 0f
            } catch (_: Exception) {
                false
            }
        }
    }

    @Composable
    fun <T> adaptiveSpring(
        spec: AnimationSpec<T>,
        fallback: AnimationSpec<T> = tween(0)
    ): AnimationSpec<T> {
        return if (rememberReduceMotion()) fallback else spec
    }
}
