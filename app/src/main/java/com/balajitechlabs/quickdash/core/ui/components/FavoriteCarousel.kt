/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/components
 * File: FavoriteCarousel.kt
 * Description: Horizontally scrollable carousel displaying user-pinned favorite tools for rapid launching.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.QuickTool
import com.balajitechlabs.quickdash.core.ui.playClickVibration
import com.balajitechlabs.quickdash.core.ui.theme.ColorUtil

import androidx.compose.ui.graphics.vector.ImageVector

data class FavoriteCarouselItem(
    val tool: QuickTool,
    val title: String,
    val iconRes: Int = 0,
    val subtitle: String = "",
    val imageVector: ImageVector? = null
)

/**
 * Authentic EssentialX (sameerasw.com) Horizontal Favorite / Pinned Feature Cards Carousel.
 * Features:
 * - 24dp rounded squircle shape
 * - Solid surfaceContainerHigh (#38393F) background
 * - Centered pastel circle badge (46dp) with vibrant icon tint
 * - 2-line title layout with medium weight
 * - Expressive spring press animation
 *
 * Developer: balajitechlabs
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteCarousel(
    items: List<FavoriteCarouselItem>,
    onItemClick: (QuickTool) -> Unit,
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true,
    onItemLongClick: ((QuickTool) -> Unit)? = null
) {
    if (items.isEmpty()) return
    val context = LocalContext.current

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.tool.name }) { item ->
            FavoriteCardItem(
                item = item,
                onClick = {
                    playClickVibration(context, hapticEnabled)
                    onItemClick(item.tool)
                },
                onLongClick = onItemLongClick?.let { callback ->
                    {
                        playClickVibration(context, hapticEnabled)
                        callback(item.tool)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteCardItem(
    item: FavoriteCarouselItem,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fav_card_scale"
    )

    val iconBg = Color(0xFF2A2B30)
    val iconTint = Color.White

    Surface(
        modifier = modifier
            .width(132.dp)
            .height(126.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(24.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageVector != null) {
                    Icon(
                        imageVector = item.imageVector,
                        contentDescription = item.title,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.title,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
