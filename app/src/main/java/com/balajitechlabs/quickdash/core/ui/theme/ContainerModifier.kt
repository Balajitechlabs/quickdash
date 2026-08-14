package com.balajitechlabs.quickdash.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.core.ui.components.smoothCornerShape

/**
 * Organic M3 Expressive Squircle Container Modifier.
 * Applies continuous curvature shapes, tonal container background, border, shadow, and inner padding.
 */
@Composable
fun Modifier.container(
    shape: Shape = smoothCornerShape(24f),
    color: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 4.dp,
    clip: Boolean = true,
    padding: Dp = 0.dp
): Modifier = composed {
    this
        .then(
            if (shadowElevation > 0.dp) Modifier.shadow(
                elevation = shadowElevation,
                shape = shape,
                clip = false
            ) else Modifier
        )
        .then(
            if (clip) Modifier.clip(shape) else Modifier
        )
        .background(color, shape)
        .border(borderWidth, borderColor, shape)
        .padding(padding)
}