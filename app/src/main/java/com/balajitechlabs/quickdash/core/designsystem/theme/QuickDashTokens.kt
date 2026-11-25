/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: core/designsystem/theme
 * File: QuickDashTokens.kt
 * Description: Unified spacing, sizing, elevation, and responsive breakpoint tokens.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.designsystem.theme

import androidx.compose.ui.unit.dp

object QuickDashTokens {

    object Spacing {
        val xxs = 2.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
    }

    object Size {
        val iconSm = 16.dp
        val iconMd = 20.dp
        val iconLg = 24.dp
        val touchTargetMin = 48.dp
        val buttonHeight = 48.dp
        val compactButtonHeight = 40.dp
        val topBarFloating = 48.dp
        val topBarFull = 56.dp
        val gridIconPhone = 44.dp
        val gridIconTablet = 56.dp
        val compactIconPhone = 38.dp
    }

    object Breakpoint {
        const val foldableDp = 600
        const val tabletDp = 840
        const val floatingMaxWidthFraction = 0.85f
        const val landscapeFloatingFraction = 0.70f
        val floatingMaxWidth = 480.dp
        val tabletMaxWidth = 560.dp
        const val drawerWidthFraction = 0.82f
        val drawerMinWidth = 280.dp
        val drawerMaxWidth = 400.dp
    }

    object Elevation {
        val flat = 0.dp
        val subtle = 1.dp
        val card = 2.dp
        val floating = 6.dp
    }
}
