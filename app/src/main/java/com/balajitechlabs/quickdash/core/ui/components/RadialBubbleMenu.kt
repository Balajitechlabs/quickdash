package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.R
import kotlin.math.roundToInt

data class RadialAction(
    val id: String,
    val title: String,
    val iconRes: Int,
    val actionIntent: String,
    val angleDegrees: Double // 0: Right, 90: Bottom, 180: Left, 270: Top
)

val DEFAULT_RADIAL_ACTIONS = listOf(
    RadialAction("upi", "UPI QR", R.drawable.ic_shortcut_upi, "com.balajitechlabs.quickdash.ACTION_QUICK_UPI", 270.0), // Top
    RadialAction("notes", "Notes", R.drawable.ic_note, "com.balajitechlabs.quickdash.ACTION_QUICK_NOTES", 0.0),       // Right
    RadialAction("calc", "Calc", R.drawable.ic_calculator, "com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR", 90.0), // Bottom
    RadialAction("timer", "Timer", R.drawable.ic_timer, "com.balajitechlabs.quickdash.ACTION_QUICK_TIMER", 180.0)      // Left
)

@Composable
fun RadialBubbleMenu(
    actions: List<RadialAction> = DEFAULT_RADIAL_ACTIONS,
    activeSectorIndex: Int = -1, // Highlighted via drag (-1 = none)
    onActionSelected: (RadialAction) -> Unit,
    onDismiss: () -> Unit
) {
    val scaleAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val radiusDp = 80.dp

    Box(
        modifier = Modifier
            .size(240.dp)
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
                .size(48.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .clickable { onDismiss() },
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Close Radial Menu",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Cardinal radial action nodes
        actions.forEachIndexed { index, action ->
            val rad = Math.toRadians(action.angleDegrees)
            val offsetX = (radiusDp.value * kotlin.math.cos(rad)).dp
            val offsetY = (radiusDp.value * kotlin.math.sin(rad)).dp

            val isSelected = activeSectorIndex == index
            val animatedNodeScale by animateFloatAsState(
                targetValue = if (isSelected) 1.20f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "node_scale_$index"
            )

            val nodeBgColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
            val nodeContentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (offsetX.toPx()).roundToInt(),
                            (offsetY.toPx()).roundToInt()
                        )
                    }
                    .graphicsLayer {
                        scaleX = animatedNodeScale
                        scaleY = animatedNodeScale
                    }
            ) {
                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(if (isSelected) 12.dp else 4.dp, CircleShape)
                        .clip(CircleShape)
                        .clickable { onActionSelected(action) },
                    color = nodeBgColor,
                    shape = CircleShape
                ) {
                    Column(
                        modifier = Modifier.size(54.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(action.iconRes),
                            contentDescription = action.title,
                            tint = nodeContentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = action.title,
                            color = nodeContentColor,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
