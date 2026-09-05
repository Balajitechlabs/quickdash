/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/onboarding/presentation/components
 * File: QuickDashPermissionsContent.kt
 * Description: Step view explaining required Android runtime permissions and system overlay access.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.onboarding.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import com.balajitechlabs.quickdash.core.ui.playClickVibration

@Composable
fun QuickDashPermissionsContent(
    overlayGranted: Boolean,
    notificationGranted: Boolean,
    cameraGranted: Boolean,
    micGranted: Boolean,
    exactAlarmGranted: Boolean,
    shizukuGranted: Boolean,
    hapticEnabled: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestCamera: () -> Unit,
    onRequestMic: () -> Unit,
    onRequestExactAlarm: () -> Unit,
    onRequestShizuku: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Permissions & Setup",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "QuickDash operates 100% on-device. Grant permissions for full floating utility functionality.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = Color(0xFFC5C6D0)
            )

            Spacer(modifier = Modifier.height(20.dp))

            RoundedCardContainer(
                cornerRadius = 22.dp,
                spacing = 2.dp,
                containerColor = Color(0xFF38393F),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f)),
                        RoundedCornerShape(22.dp)
                    )
            ) {
                PermissionRowItem(
                    title = "Display Over Other Apps",
                    subtitle = "Required for the floating bubble, radial tools & quick panels",
                    iconBadgeColor = Color(0xFF2A2B30),
                    iconTint = Color.White,
                    imageVector = Icons.Rounded.Layers,
                    isGranted = overlayGranted,
                    isRequired = true,
                    onGrant = {
                        playClickVibration(context, hapticEnabled)
                        onRequestOverlay()
                    }
                )

                PermissionRowItem(
                    title = "Notifications & Alarms",
                    subtitle = "Required for countdown timer alerts & update notifications",
                    iconBadgeColor = Color(0xFF2A2B30),
                    iconTint = Color.White,
                    imageVector = Icons.Rounded.Notifications,
                    isGranted = notificationGranted,
                    onGrant = {
                        playClickVibration(context, hapticEnabled)
                        onRequestNotification()
                    }
                )

                PermissionRowItem(
                    title = "Camera Access",
                    subtitle = "Required for scanning QR codes and barcodes",
                    iconBadgeColor = Color(0xFF2A2B30),
                    iconTint = Color.White,
                    imageVector = Icons.Rounded.CameraAlt,
                    isGranted = cameraGranted,
                    onGrant = {
                        playClickVibration(context, hapticEnabled)
                        onRequestCamera()
                    }
                )

                PermissionRowItem(
                    title = "Microphone Access",
                    subtitle = "Required for Quick Voice Memos audio recording",
                    iconBadgeColor = Color(0xFF2A2B30),
                    iconTint = Color.White,
                    imageVector = Icons.Rounded.Mic,
                    isGranted = micGranted,
                    onGrant = {
                        playClickVibration(context, hapticEnabled)
                        onRequestMic()
                    }
                )

                PermissionRowItem(
                    title = "Exact Alarms & Timers",
                    subtitle = "Ensures timers and reminders fire accurately during Doze mode",
                    iconBadgeColor = Color(0xFF2A2B30),
                    iconTint = Color.White,
                    imageVector = Icons.Rounded.Alarm,
                    isGranted = exactAlarmGranted,
                    onGrant = {
                        playClickVibration(context, hapticEnabled)
                        onRequestExactAlarm()
                    }
                )

                PermissionRowItem(
                    title = "Shizuku Privileged Access",
                    subtitle = "Optional: Unlocks saved Wi-Fi networks inspection without root",
                    iconBadgeColor = Color(0xFF2A2B30),
                    iconTint = Color.White,
                    imageVector = Icons.Rounded.Security,
                    isGranted = shizukuGranted,
                    isOptional = true,
                    onGrant = {
                        playClickVibration(context, hapticEnabled)
                        onRequestShizuku()
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF44474F)),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38393F),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFF44474F)),
                modifier = Modifier
                    .weight(3f)
                    .height(54.dp)
            ) {
                Text(
                    text = "Launch QuickDash",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}
