package com.balajitechlabs.quickdash.features.onboarding.presentation.steps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.OnboardingScaffold

@Composable
fun ThemeStep(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val themes = listOf(
        Triple("Light", "LIGHT", Color(0xFFF5F5F5)) to Color(0xFF1C1B1F),
        Triple("Dark", "DARK", Color(0xFF1A1A1A)) to Color(0xFFE6E1E5),
        Triple("AMOLED Black", "AMOLED", Color.Black) to Color(0xFFE6E1E5),
        Triple("Follow System", "SYSTEM", null) to Color(0xFF8AB4F8)
    )

    OnboardingScaffold(
        stepTitle = "Choose Your Theme",
        stepSubtitle = "Pick a look that suits you. Change anytime in Settings.",
        currentStep = 3,
        totalSteps = 7,
        showBack = true,
        showSkip = false,
        onBack = onBack
    ) {
        themes.forEachIndexed { index, (theme, textColor) ->
            val (label, code, previewBg) = theme
            val isSelected = currentTheme == code

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.02f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "themeScale"
            )

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.outlineVariant,
                label = "borderColor"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clickable { onThemeSelected(code) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                     else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = borderColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Mini preview mockup
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                if (previewBg != null) Modifier.clip(RoundedCornerShape(10.dp))
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (previewBg != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = previewBg,
                                modifier = Modifier.size(48.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = textColor.copy(alpha = 0.2f),
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "Q",
                                                color = textColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // System: split view
                            Row(modifier = Modifier.size(48.dp)) {
                                Surface(
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    color = Color(0xFFF5F5F5)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("Q", fontSize = 8.sp, color = Color(0xFF1C1B1F), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    color = Color(0xFF1A1A1A)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("Q", fontSize = 8.sp, color = Color(0xFFE6E1E5), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (code == "AMOLED") {
                            Text(
                                text = "Saves battery on OLED screens",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        if (code == "SYSTEM") {
                            Text(
                                text = "Matches your device setting",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (index < themes.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

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
