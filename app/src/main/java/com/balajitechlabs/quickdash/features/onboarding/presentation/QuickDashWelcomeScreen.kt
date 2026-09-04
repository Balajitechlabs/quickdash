/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: Onboarding & Welcome Experience
 * File: QuickDashWelcomeScreen.kt
 * Description: Dia-style animated welcome screen for fresh installs with vibrant aura,
 *              Reddit & Telegram community links, and interactive Permissions step.
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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.shizuku.ShizukuHelper
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import com.balajitechlabs.quickdash.core.ui.playClickVibration

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

    // Live permission states
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

    // Refresh permission statuses on resume
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
                    WelcomeContent(
                        hapticEnabled = hapticEnabled,
                        onContinue = {
                            playClickVibration(context, hapticEnabled)
                            currentStep = QuickDashWelcomeStep.PERMISSIONS
                        }
                    )
                }
                QuickDashWelcomeStep.PERMISSIONS -> {
                    PermissionsContent(
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
                                    openUrl(context, "https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api")
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

@Composable
private fun WelcomeContent(
    hapticEnabled: Boolean,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "dia_welcome_anim")

    // Breathing logo scale
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    // Rotating colorful gradient aura
    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_rotation"
    )

    val auraColors = listOf(
        Color(0xFFB0C6FF),
        Color(0xFFFF80AB),
        Color(0xFF80D8FF),
        Color(0xFFFFD180),
        Color(0xFFB0C6FF)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Dia Browser macOS Inspired Animated App Logo ──
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(150.dp)
                    .scale(logoScale)
            ) {
                // Colorful pulsating rotating gradient aura glow
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(auraRotation)
                        .clip(CircleShape)
                        .background(Brush.sweepGradient(auraColors))
                )

                // Inner dark halo
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF000000))
                )

                // Crisp App Logo
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "QuickDash Logo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Hero Title & Tagline ──
            Text(
                text = "QuickDash",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your floating productivity companion",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp
                ),
                color = Color(0xFFC5C6D0),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Community Pods (Telegram & Reddit) ──
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
                // Telegram Community Card
                CommunityRowItem(
                    title = "Telegram Community",
                    subtitle = "Join our community for updates & betas",
                    iconBadgeColor = Color(0xFF1E3A5F),
                    iconTint = Color(0xFF80D8FF),
                    iconRes = R.drawable.ic_telegram,
                    onClick = {
                        playClickVibration(context, hapticEnabled)
                        openUrl(context, "https://t.me/+FYlt5cBA29Q0ZWJl")
                    }
                )

                // Reddit Community Card
                CommunityRowItem(
                    title = "Reddit Community",
                    subtitle = "r/balajitechlabs — Share tips & feature discussions",
                    iconBadgeColor = Color(0xFF422618),
                    iconTint = Color(0xFFFF9E80),
                    imageVector = Icons.Rounded.Forum,
                    onClick = {
                        playClickVibration(context, hapticEnabled)
                        openUrl(context, "https://www.reddit.com/r/balajitechlabs/")
                    }
                )
            }
        }

        // ── Continue to Permissions Button ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 16.dp)
        ) {
            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38393F),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFF44474F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Continue to Permissions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionsContent(
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
    onFinish: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
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

            // ── Permissions List Container ──
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
                // Overlay
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

                // Notifications
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

                // Camera
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

                // Microphone
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

                // Exact Alarms
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

                // Shizuku (Optional)
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

        // ── Navigation Bottom Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            @Suppress("DEPRECATION")
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF44474F)),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
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

@Composable
private fun PermissionRowItem(
    title: String,
    subtitle: String,
    iconBadgeColor: Color,
    iconTint: Color,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    isRequired: Boolean = false,
    isOptional: Boolean = false,
    onGrant: () -> Unit
) {
    Surface(
        color = Color(0xFF38393F),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBadgeColor)
                    .border(BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                    if (isRequired) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "*",
                            color = Color(0xFFFF8A80),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFFC5C6D0)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1B3B22),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Granted",
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Granted",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF81C784)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onGrant,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRequired) MaterialTheme.colorScheme.primary else Color(0xFF2A2B30),
                        contentColor = if (isRequired) Color.Black else Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (isOptional) "Optional" else "Grant",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityRowItem(
    title: String,
    subtitle: String,
    iconBadgeColor: Color,
    iconTint: Color,
    iconRes: Int? = null,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF38393F),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBadgeColor),
                contentAlignment = Alignment.Center
            ) {
                if (iconRes != null) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                } else if (imageVector != null) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = Color(0xFFC5C6D0)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = null,
                tint = Color(0xFFC5C6D0),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
