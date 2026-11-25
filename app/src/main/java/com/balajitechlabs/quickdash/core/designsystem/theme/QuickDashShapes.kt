/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: core/designsystem/theme
 * File: QuickDashShapes.kt
 * Description: Material 3 Expressive shape scale and semantic container shapes.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive shape system for QuickDash.
 * Uses generous corner radii for cards, sheets, and interactive controls.
 */
val QuickDashShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object QuickDashShapeTokens {
    val Card = RoundedCornerShape(20.dp)
    val CardCompact = RoundedCornerShape(16.dp)
    val Pill = CircleShape
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val Dialog = RoundedCornerShape(28.dp)
    val Input = RoundedCornerShape(16.dp)
}
