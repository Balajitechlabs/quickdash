/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/components
 * File: GlassmorphismBlur.kt
 * Description: Provides dynamic real-time backdrop blur and translucent glassmorphism surfaces across Compose layouts.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Glassmorphism Hardware Blur Shader (`GlassmorphismBlur.kt`).
 * Applies real-time RenderEffect hardware blur on Android 12+ (API 31+) with fallback for legacy devices.
 */
fun Modifier.glassmorphicBlur(
    blurRadiusDp: Float = 20f,
    backgroundColor: Color = Color.Black.copy(alpha = 0.4f)
): Modifier = this.then(
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier
            .graphicsLayer {
                renderEffect = RenderEffect
                    .createBlurEffect(blurRadiusDp, blurRadiusDp, Shader.TileMode.CLAMP)
                    .asComposeRenderEffect()
            }
            .background(backgroundColor)
    } else {
        Modifier.background(backgroundColor)
    }
)
