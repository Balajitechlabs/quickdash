/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/theme
 * File: Dimens.kt
 * Description: Canonical layout dimension constants, padding scales, and elevation metrics.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.theme

import androidx.compose.ui.unit.dp

object Dimens {
    val FloatingMaxWidthFraction = 0.85f
    val FloatingMaxWidthDp = 480.dp
    val LandscapeFloatingFraction = 0.70f
    val DrawerWidthFraction = 0.82f
    val DrawerMinWidth = 280.dp
    val DrawerMaxWidth = 400.dp
    val TopBarHeightFloating = 48.dp
    val TopBarHeightFull = 56.dp
    val GridIconSizePhone = 44.dp
    val GridIconSizeTablet = 56.dp
    val CompactIconSizePhone = 38.dp
    val TabletBreakpointDp = 840
    val FoldableBreakpointDp = 600

    // Design System Tokens
    object Spacing {
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
    }

    object Radius {
        val xs = 4.dp
        val sm = 8.dp
        val md = 16.dp
        val lg = 24.dp
        val pill = 999.dp
    }

    object Elevation {
        val flat = 0.dp
        val subtle = 2.dp
        val floating = 8.dp
    }
}
