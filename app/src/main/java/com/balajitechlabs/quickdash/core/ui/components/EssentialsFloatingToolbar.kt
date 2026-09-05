/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/components
 * File: EssentialsFloatingToolbar.kt
 * Description: Spring-animated pill toolbar anchored above navigation bars for high-speed tab switching.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.playClickVibration

private data class EssentialToolbarTab(
    val label: String,
    val icon: ImageVector
)

/**
 * Authentic EssentialX (sameerasw.com) Floating Bottom Navigation Toolbar.
 * Features:
 * - Solid surfaceContainerHigh (#38393F) pill container with #44474F border
 * - Pitch black (#000000) pop-out background pill for active tab with spring expansion
 * - Crisp white icons & text for active tab, high-contrast onSurfaceVariant for unselected tabs
 *
 * Tabs: [0] Settings · [1] Home · [2] About
 * Developer: balajitechlabs
 */
@Composable
fun EssentialsFloatingToolbar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val context = LocalContext.current
    val tabs = listOf(
        EssentialToolbarTab(label = "Settings", icon = Icons.Rounded.Settings),
        EssentialToolbarTab(label = "Home", icon = Icons.Rounded.Home),
        EssentialToolbarTab(label = "About", icon = Icons.Rounded.Info)
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF38393F),
        border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.7f)),
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index

                val itemWidth by animateDpAsState(
                    targetValue = if (isSelected) 44.dp else 40.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "item_width_$index"
                )

                val labelWidth by animateDpAsState(
                    targetValue = if (isSelected) 72.dp else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "label_width_$index"
                )

                IconButton(
                    onClick = {
                        playClickVibration(context, hapticEnabled)
                        onSelectTab(index)
                    },
                    modifier = Modifier
                        .width(itemWidth + labelWidth)
                        .height(44.dp)
                        .then(
                            if (isSelected) {
                                Modifier.border(1.dp, Color(0xFF44474F).copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                            } else Modifier
                        ),
                    shape = RoundedCornerShape(22.dp),
                    colors = if (isSelected) {
                        IconButtonDefaults.filledIconButtonColors(
                            contentColor = Color.White,
                            containerColor = Color(0xFF000000)
                        )
                    } else {
                        IconButtonDefaults.iconButtonColors(
                            contentColor = Color(0xFFC5C6D0),
                            containerColor = Color.Transparent
                        )
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) Color.White else Color(0xFFC5C6D0),
                            modifier = Modifier.size(22.dp)
                        )

                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                color = Color.White,
                                modifier = Modifier.basicMarquee()
                            )
                        }
                    }
                }

                if (index < tabs.size - 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Essentials Floating Toolbar - Dark",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
fun EssentialsFloatingToolbarPreview() {
    MaterialTheme {
        EssentialsFloatingToolbar(
            selectedTab = 1,
            onSelectTab = {}
        )
    }
}
