package com.balajitechlabs.quickdash.features.onboarding.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LivePreviewCard(
    themeIsDark: Boolean,
    themeLabel: String,
    shapeStyle: String,
    cornerRadius: Float,
    borderWidth: Float,
    fontFamily: FontFamily,
    fontScale: Float,
    modifier: Modifier = Modifier
) {
    val previewShape = com.balajitechlabs.quickdash.core.ui.components.getCustomShape(shapeStyle, cornerRadius)

    val bgColor = if (themeIsDark) Color(0xFF0D0D0D) else Color(0xFFFFFBFE)
    val surfaceColor = if (themeIsDark) Color(0xFF1A1A1A) else Color(0xFFF1EBF6)
    val onSurfaceColor = if (themeIsDark) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
    val primaryColor = if (themeIsDark) Color(0xFF8AB4F8) else Color(0xFF1A73E8)
    val onPrimaryColor = if (themeIsDark) Color.Black else Color.White
    val variantColor = if (themeIsDark) Color(0xFFBBB5BF) else Color(0xFF49454E)

    Card(
        shape = previewShape,
        border = BorderStroke(borderWidth.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(cornerRadius.dp * 0.5f))
                        .background(primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Q",
                        color = onPrimaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily
                    )
                }
                Text(
                    text = "QuickDash",
                    color = onSurfaceColor,
                    fontSize = (14 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = themeLabel,
                    color = primaryColor,
                    fontSize = (9 * fontScale).sp,
                    fontFamily = fontFamily
                )
            }

            HorizontalDivider(color = surfaceColor, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("QR", "Chat", "Note").forEach { label ->
                    Surface(
                        shape = RoundedCornerShape(cornerRadius.dp * 0.6f),
                        color = surfaceColor,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = label,
                            color = primaryColor,
                            fontSize = (10 * fontScale).sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = fontFamily,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(cornerRadius.dp),
                color = primaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Primary Button",
                        color = onPrimaryColor,
                        fontSize = (11 * fontScale).sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = fontFamily
                    )
                }
            }
        }
    }
}
