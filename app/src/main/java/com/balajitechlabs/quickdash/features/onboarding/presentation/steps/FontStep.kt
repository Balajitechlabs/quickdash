package com.balajitechlabs.quickdash.features.onboarding.presentation.steps

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.theme.getGoogleFontFamily
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.LivePreviewCard
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.OnboardingScaffold

@Composable
fun FontStep(
    currentFontFamily: String,
    currentFontScale: Float,
    shapeStyle: String,
    cornerRadius: Float,
    borderWidth: Float,
    onFontFamilySelected: (String) -> Unit,
    onFontScaleChanged: (Float) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val fontFamilies = listOf(
        "Default" to "SYSTEM",
        "Sans-Serif" to "SANSSERIF",
        "Serif" to "SERIF",
        "Monospace" to "MONOSPACE",
        "Cursive" to "CURSIVE",
        "Nunito" to "NUNITO",
        "Poppins" to "POPPINS",
        "Space Grotesk" to "SPACE_GROTESK"
    )

    val resolvedFontFamily = remember(currentFontFamily) {
        when (currentFontFamily.uppercase()) {
            "SANSSERIF" -> FontFamily.SansSerif
            "SERIF" -> FontFamily.Serif
            "MONOSPACE" -> FontFamily.Monospace
            "CURSIVE" -> FontFamily.Cursive
            "NUNITO" -> getGoogleFontFamily("Nunito")
            "POPPINS" -> getGoogleFontFamily("Poppins")
            "SPACE_GROTESK" -> getGoogleFontFamily("Space Grotesk")
            else -> FontFamily.Default
        }
    }

    OnboardingScaffold(
        stepTitle = "Typography",
        stepSubtitle = "Select a font family and adjust the text size.",
        currentStep = 5,
        totalSteps = 7,
        showBack = true,
        showSkip = false,
        onBack = onBack
    ) {
        // Font family horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fontFamilies.forEach { (label, code) ->
                val isSelected = currentFontFamily.uppercase() == code.uppercase()
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "fontScale"
                )

                val font = when (code.uppercase()) {
                    "SANSSERIF" -> FontFamily.SansSerif
                    "SERIF" -> FontFamily.Serif
                    "MONOSPACE" -> FontFamily.Monospace
                    "CURSIVE" -> FontFamily.Cursive
                    "NUNITO" -> getGoogleFontFamily("Nunito")
                    "POPPINS" -> getGoogleFontFamily("Poppins")
                    "SPACE_GROTESK" -> getGoogleFontFamily("Space Grotesk")
                    else -> FontFamily.Default
                }

                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .height(56.dp)
                        .scale(scale)
                        .clickable { onFontFamilySelected(code) },
                    shape = RoundedCornerShape(12.dp),
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
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = font,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Font scale slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Text Size",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${"%.1f".format(currentFontScale)}x",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = currentFontScale,
            onValueChange = onFontScaleChanged,
            valueRange = 0.8f..1.5f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LivePreviewCard(
            themeIsDark = true,
            themeLabel = "Preview",
            shapeStyle = shapeStyle,
            cornerRadius = cornerRadius,
            borderWidth = borderWidth,
            fontFamily = resolvedFontFamily,
            fontScale = currentFontScale
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
                text = "Finalize Setup",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
