/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/onboarding/presentation
 * File: QuickDashWelcomeScreen.kt
 * Description: Welcome splash screen introducing core capabilities of the floating productivity suite.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.onboarding.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.balajitechlabs.quickdash.core.shizuku.ShizukuHelper
import com.balajitechlabs.quickdash.core.ui.playClickVibration
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.QuickDashPermissionsContent
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.QuickDashWelcomeHeroStep

enum class QuickDashWelcomeStep {
    WELCOME,
    PERMISSIONS
}

@Composable
fun QuickDashWelcomeScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    hapticEnabled: Boolean = true
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(QuickDashWelcomeStep.WELCOME) }

    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var notificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        )
    }
    var cameraGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var micGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var exactAlarmGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
                alarmManager?.canScheduleExactAlarms() ?: true
            } else true
        )
    }
    var shizukuGranted by remember {
        mutableStateOf(ShizukuHelper.isAvailable && ShizukuHelper.isPermissionGranted)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
    }
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationGranted = granted
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                overlayGranted = Settings.canDrawOverlays(context)
                notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else {
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
                }
                cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
                    alarmManager?.canScheduleExactAlarms() ?: true
                } else true
                shizukuGranted = ShizukuHelper.isAvailable && ShizukuHelper.isPermissionGranted
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF000000)
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState == QuickDashWelcomeStep.PERMISSIONS) {
                    (slideInHorizontally { it } + fadeIn(tween(300))) togetherWith
                            (slideOutHorizontally { -it } + fadeOut(tween(250)))
                } else {
                    (slideInHorizontally { -it } + fadeIn(tween(300))) togetherWith
                            (slideOutHorizontally { it } + fadeOut(tween(250)))
                }
            },
            label = "welcome_flow"
        ) { step ->
            when (step) {
                QuickDashWelcomeStep.WELCOME -> {
                    QuickDashWelcomeHeroStep(
                        hapticEnabled = hapticEnabled,
                        onContinue = {
                            playClickVibration(context, hapticEnabled)
                            currentStep = QuickDashWelcomeStep.PERMISSIONS
                        }
                    )
                }
                QuickDashWelcomeStep.PERMISSIONS -> {
                    QuickDashPermissionsContent(
                        overlayGranted = overlayGranted,
                        notificationGranted = notificationGranted,
                        cameraGranted = cameraGranted,
                        micGranted = micGranted,
                        exactAlarmGranted = exactAlarmGranted,
                        shizukuGranted = shizukuGranted,
                        hapticEnabled = hapticEnabled,
                        onRequestOverlay = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            context.startActivity(intent)
                        },
                        onRequestNotification = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        },
                        onRequestCamera = { cameraLauncher.launch(Manifest.permission.CAMERA) },
                        onRequestMic = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        onRequestExactAlarm = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        },
                        onRequestShizuku = {
                            if (!ShizukuHelper.isAvailable) {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                                if (launchIntent != null) {
                                    context.startActivity(launchIntent)
                                } else {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api")
                                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                    context.startActivity(intent)
                                }
                            } else {
                                ShizukuHelper.requestPermission(9001)
                            }
                        },
                        onBack = {
                            playClickVibration(context, hapticEnabled)
                            currentStep = QuickDashWelcomeStep.WELCOME
                        },
                        onFinish = {
                            playClickVibration(context, hapticEnabled)
                            onFinishOnboarding()
                        }
                    )
                }
            }
        }
    }
}
