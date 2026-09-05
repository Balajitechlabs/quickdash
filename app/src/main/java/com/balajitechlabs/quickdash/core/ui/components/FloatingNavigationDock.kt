/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/components
 * File: FloatingNavigationDock.kt
 * Description: Compact floating dock with icon shortcuts for rapid navigation between active tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.balajitechlabs.quickdash.core.ui.QuickDashUiState
import com.balajitechlabs.quickdash.core.ui.playClickVibration
import com.balajitechlabs.quickdash.core.utils.UpdateManager
import com.balajitechlabs.quickdash.core.utils.UpdateState

@Composable
fun BoxScope.FloatingNavigationDock(
    uiState: QuickDashUiState,
    updateState: UpdateState,
    hapticEnabled: Boolean,
    onNavigateToTab: (Int) -> Unit,
    onBackToHome: () -> Unit
) {
    if (uiState == QuickDashUiState.Onboarding) return

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(96.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.45f),
                        Color.Black.copy(alpha = 0.85f),
                        Color.Black.copy(alpha = 0.95f)
                    )
                )
            )
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        renderEffect = RenderEffect.createBlurEffect(
                            20f, 20f, Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                } else Modifier
            )
            .zIndex(8f)
    )

    val isTopLevelScreen = uiState == QuickDashUiState.Dashboard ||
            uiState == QuickDashUiState.Settings ||
            uiState == QuickDashUiState.About

    if (isTopLevelScreen) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .zIndex(10f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EssentialsFloatingToolbar(
                selectedTab = when (uiState) {
                    QuickDashUiState.Settings -> 0
                    QuickDashUiState.About -> 2
                    else -> 1
                },
                onSelectTab = onNavigateToTab
            )

            val isUpdateAvailable = updateState is UpdateState.UpdateAvailable ||
                    updateState is UpdateState.Downloading ||
                    updateState is UpdateState.ReadyToInstall

            AnimatedVisibility(
                visible = isUpdateAvailable,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "update_logo_pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )
                val downloadRotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "download_spin"
                )

                Surface(
                    onClick = {
                        playClickVibration(context, hapticEnabled)
                        val current = UpdateManager.updateState
                        if (current is UpdateState.ReadyToInstall) {
                            UpdateManager.installApk(context, current.fileName)
                        } else if (current is UpdateState.UpdateAvailable) {
                            UpdateManager.showUpdateSheet = true
                        }
                    },
                    shape = CircleShape,
                    color = Color(0xFF38393F),
                    border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.7f)),
                    shadowElevation = 8.dp,
                    tonalElevation = 0.dp,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    if (updateState is UpdateState.Downloading) {
                                        rotationZ = downloadRotation
                                    } else {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                    }
                                }
                                .clip(CircleShape)
                                .background(Color(0xFF000000))
                                .border(1.dp, Color(0xFF44474F).copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (updateState) {
                                    is UpdateState.ReadyToInstall -> Icons.Rounded.CheckCircle
                                    is UpdateState.Downloading -> Icons.Rounded.CloudDownload
                                    else -> Icons.Rounded.Download
                                },
                                contentDescription = "Update Available",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        Surface(
            onClick = {
                playClickVibration(context, hapticEnabled)
                onBackToHome()
            },
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF38393F),
            shadowElevation = 8.dp,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
                .height(46.dp)
                .zIndex(10f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Back",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}
