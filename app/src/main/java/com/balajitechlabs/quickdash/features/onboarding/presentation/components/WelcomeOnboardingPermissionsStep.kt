/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/onboarding/presentation/components
 * File: WelcomeOnboardingPermissionsStep.kt
 * Description: Permission onboarding step guiding users through granting critical overlay access.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.onboarding.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer

@Composable
fun WelcomeOnboardingPermissionsStep(
    hasOverlay: Boolean,
    hasNotifications: Boolean,
    hasCamera: Boolean,
    hasMic: Boolean,
    onGrantOverlay: () -> Unit,
    onGrantNotifications: () -> Unit,
    onGrantCamera: () -> Unit,
    onGrantMic: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(48.dp))
            Text(
                text = "Permissions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Grant access for the full QuickDash experience.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            RoundedCardContainer {
                WelcomePermissionListItem(
                    icon = Icons.Rounded.Layers,
                    title = "Display over other apps",
                    subtitle = "Required — enables the floating overlay",
                    isGranted = hasOverlay,
                    isRequired = true,
                    onGrant = onGrantOverlay
                )
                WelcomePermissionListItem(
                    icon = Icons.Rounded.Notifications,
                    title = "Notifications",
                    subtitle = "For reminders and alerts",
                    isGranted = hasNotifications,
                    onGrant = onGrantNotifications
                )
                WelcomePermissionListItem(
                    icon = Icons.Rounded.CameraAlt,
                    title = "Camera",
                    subtitle = "For QR scanning and capture",
                    isGranted = hasCamera,
                    onGrant = onGrantCamera
                )
                WelcomePermissionListItem(
                    icon = Icons.Rounded.Mic,
                    title = "Microphone",
                    subtitle = "For voice memos recording",
                    isGranted = hasMic,
                    onGrant = onGrantMic
                )
            }
        }

        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            StepDots(current = 1, total = 4)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, modifier = Modifier.size(18.dp))
                }
                Button(
                    onClick = onContinue,
                    modifier = Modifier.weight(3f),
                    shape = RoundedCornerShape(14.dp),
                    enabled = hasOverlay
                ) {
                    Text(if (hasOverlay) "Continue" else "Grant Overlay First")
                }
            }
        }
    }
}

@Composable
private fun WelcomePermissionListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    isRequired: Boolean = false,
    onGrant: () -> Unit
) {
    ListItem(
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isGranted) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                if (isRequired) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "Required",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = {
            if (isGranted) {
                Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            } else {
                OutlinedButton(onClick = onGrant, shape = RoundedCornerShape(10.dp)) {
                    Text("Grant", style = MaterialTheme.typography.labelMedium)
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
