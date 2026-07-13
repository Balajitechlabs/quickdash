package com.balajitechlabs.quickdash.features.onboarding.presentation.steps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.components.getCustomShape
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.LivePreviewCard
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.OnboardingScaffold

@Composable
fun ShapeStep(
    currentShape: String,
    currentRadius: Float,
    currentBorder: Float,
    onShapeSelected: (String) -> Unit,
    onRadiusChanged: (Float) -> Unit,
    onBorderChanged: (Float) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val shapes = listOf("Rounded", "Cut", "Squircle", "Smooth", "Octagon", "Pentagon")

    OnboardingScaffold(
        stepTitle = "Customize Corners",
        stepSubtitle = "Adjust the shape, radius, and border of cards and buttons.",
        currentStep = 4,
        totalSteps = 7,
        showBack = true,
        showSkip = false,
        onBack = onBack
    ) {
        // Shape grid - 3x2
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            shapes.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { style ->
                        val isSelected = currentShape == style
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "shapeScale"
                        )

                        val shape = remember(style, currentRadius) {
                            getCustomShape(style, currentRadius)
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .scale(scale)
                                .clickable { onShapeSelected(style) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = shape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = if (isSelected) 0.15f else 0.06f),
                                    border = BorderStroke(currentBorder.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (isSelected) 0.6f else 0.2f)),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "Q",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // Fill remaining space
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Corner Radius slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Corner Radius",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${currentRadius.toInt()}dp",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = currentRadius,
            onValueChange = onRadiusChanged,
            valueRange = 4f..32f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Border Width slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Border Width",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${"%.1f".format(currentBorder)}dp",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = currentBorder,
            onValueChange = onBorderChanged,
            valueRange = 0f..4f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LivePreviewCard(
            themeIsDark = true,
            themeLabel = "Preview",
            shapeStyle = currentShape,
            cornerRadius = currentRadius,
            borderWidth = currentBorder,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            fontScale = 1f
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Continue",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
