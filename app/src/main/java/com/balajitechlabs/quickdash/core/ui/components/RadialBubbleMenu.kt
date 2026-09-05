/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/components
 * File: RadialBubbleMenu.kt
 * Description: Radial circular layout animating tool icons outward from the floating bubble on tap.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class RadialAction(
    val id: String,
    val title: String,
    val iconRes: Int,
    val actionIntent: String,
    val angleDegrees: Double
)

@Composable
fun RadialBubbleMenu(
    actions: List<RadialAction>,
    activeSectorIndex: Int = -1,
    onActionSelected: (RadialAction) -> Unit,
    onDismiss: () -> Unit
) {
    val scaleAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    // Full-screen container so the menu is always centered on screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        if (actions.isEmpty()) {
            // Empty state when user has pinned nothing
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                        alpha = scaleAnim.value.coerceIn(0f, 1f)
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1A1C20),
                    border = BorderStroke(1.dp, Color(0xFF44474F)),
                    modifier = Modifier
                        .size(200.dp)
                        .shadow(16.dp, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Pin tools to\nuse quick access",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        } else {
            // Radial action nodes centered on screen
            val radiusDp = when (actions.size) {
                1    -> 0.dp
                2, 3 -> 90.dp
                4, 5 -> 105.dp
                else -> 115.dp
            }

            Box(
                modifier = Modifier
                    .size(320.dp)
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                        alpha = scaleAnim.value.coerceIn(0f, 1f)
                    },
                contentAlignment = Alignment.Center
            ) {
                // Center dismiss hub
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .clickable { onDismiss() },
                    color = Color(0xFF0D0D0D),
                    border = BorderStroke(1.dp, Color(0xFF44474F)),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(com.balajitechlabs.quickdash.R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Tool nodes
                actions.forEachIndexed { index, action ->
                    val rad = Math.toRadians(action.angleDegrees)
                    val offsetX = (radiusDp.value * kotlin.math.cos(rad)).dp
                    val offsetY = (radiusDp.value * kotlin.math.sin(rad)).dp

                    val isActive = activeSectorIndex == index
                    val nodeScale by animateFloatAsState(
                        targetValue = if (isActive) 1.25f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "node_scale_$index"
                    )

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (offsetX.toPx()).roundToInt(),
                                    (offsetY.toPx()).roundToInt()
                                )
                            }
                            .graphicsLayer {
                                scaleX = nodeScale
                                scaleY = nodeScale
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(58.dp)
                                .shadow(if (isActive) 20.dp else 6.dp, CircleShape)
                                .clip(CircleShape)
                                .clickable { onActionSelected(action) },
                            color = if (isActive) Color(0xFF1E1E1E) else Color(0xFF2A2B30),
                            border = BorderStroke(
                                width = if (isActive) 1.5.dp else 1.dp,
                                color = if (isActive) Color.White.copy(alpha = 0.6f) else Color(0xFF44474F)
                            ),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(action.iconRes),
                                    contentDescription = action.title,
                                    tint = if (isActive) Color.White else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
