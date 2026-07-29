package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ToolbarItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

/**
 * AirSync-inspired Material 3 Expressive Floating Toolbar with bouncy spring physics.
 */
@Composable
fun ExpressiveFloatingToolbar(
    items: List<ToolbarItem>,
    selectedId: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
    selectedColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColorSelected: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconColorUnselected: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.id == selectedId
                val animatedWidth by animateDpAsState(
                    targetValue = if (isSelected) 100.dp else 44.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "ToolbarItemWidth"
                )

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .size(width = animatedWidth, height = 44.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) selectedColor else Color.Transparent)
                        .clickable { onItemSelected(item.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) iconColorSelected else iconColorUnselected,
                            modifier = Modifier.size(22.dp)
                        )
                        if (isSelected) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    color = iconColorSelected
                                ),
                                modifier = Modifier.padding(start = 6.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
