package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized responsive dimensions and breakpoints.
 *
 * Usage: val dims = responsiveDimensions()
 *        LazyVerticalGrid(columns = GridCells.Fixed(dims.gridColumns), ...)
 */
@Immutable
data class ResponsiveDimensions(
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val isLandscape: Boolean,
    val isCompact: Boolean,    // < 600dp width (phones portrait)
    val isMedium: Boolean,     // 600–839dp width (tablets portrait / phones landscape)
    val isExpanded: Boolean,   // >= 840dp width (tablets landscape)
    val gridColumns: Int,
    val gridCardAspectRatio: Float,
    val gridIconSize: Dp,
    val compactIconSize: Dp,
    val drawerWidth: Dp,
    val floatingMaxHeightFraction: Float,
    val topBarHeight: Dp,
    val hPad: Dp,
    val vPad: Dp
)

@Composable
fun responsiveDimensions(): ResponsiveDimensions {
    val config = LocalConfiguration.current
    val w = config.screenWidthDp
    val h = config.screenHeightDp
    val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val isCompact = w < 600
    val isMedium = w in 600..839
    val isExpanded = w >= 840

    val gridColumns = when {
        w >= 840 -> 4
        w >= 600 -> 3
        else -> 2
    }

    val gridCardAspectRatio = when {
        isLandscape && isExpanded -> 1.4f
        isLandscape -> 1.2f
        isExpanded -> 1.0f
        else -> 0.9f
    }

    val gridIconSize = when {
        w >= 840 -> 56.dp
        w >= 600 -> 52.dp
        else -> 44.dp
    }

    val compactIconSize = if (w >= 600) 44.dp else 38.dp

    val drawerWidth = (w * 0.82f).dp.coerceIn(280.dp, 400.dp)

    val floatingMaxHeightFraction = when {
        isLandscape && isCompact -> 0.80f
        isLandscape -> 0.85f
        isCompact -> 0.88f
        else -> 0.92f
    }

    val topBarHeight = if (isCompact) 48.dp else 56.dp

    val hPad = (10f * w / 411f).dp.coerceIn(8.dp, 20.dp)
    val vPad = if (isCompact) 6.dp else 10.dp

    return ResponsiveDimensions(
        screenWidthDp = w,
        screenHeightDp = h,
        isLandscape = isLandscape,
        isCompact = isCompact,
        isMedium = isMedium,
        isExpanded = isExpanded,
        gridColumns = gridColumns,
        gridCardAspectRatio = gridCardAspectRatio,
        gridIconSize = gridIconSize,
        compactIconSize = compactIconSize,
        drawerWidth = drawerWidth,
        floatingMaxHeightFraction = floatingMaxHeightFraction,
        topBarHeight = topBarHeight,
        hPad = hPad,
        vPad = vPad
    )
}
