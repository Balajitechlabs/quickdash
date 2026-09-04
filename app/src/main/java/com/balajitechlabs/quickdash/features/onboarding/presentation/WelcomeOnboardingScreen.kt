/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/onboarding
 * File: WelcomeOnboardingScreen.kt
 * Description: Full-screen Arc/Raycast-style onboarding flow — logo-only splash,
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.onboarding.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class OnboardingStep {
    SPLASH, WELCOME, PERMISSIONS, PREFERENCES, DONE
}

@Composable
fun WelcomeOnboardingScreen(onFinishOnboarding: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userStore = remember { UserStore(context) }

    var step by remember { mutableStateOf(OnboardingStep.SPLASH) }

    // Permission state
    var hasOverlay by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context)
            else true
        )
    }
    var hasNotifications by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    var hasCamera by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var hasMic by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
                hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                else true
                hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasNotifications = it }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCamera = it }
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasMic = it }

    // Preferences state
    val themeMode by userStore.themeMode.collectAsStateWithLifecycle(initialValue = "SYSTEM")
    var selectedTheme by remember(themeMode) { mutableStateOf(themeMode) }

    // UPI state
    var upiId by remember { mutableStateOf("") }
    var payeeName by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally { it } + fadeIn(tween(340))) togetherWith
                            (slideOutHorizontally { -it } + fadeOut(tween(280)))
                } else {
                    (slideInHorizontally { -it } + fadeIn(tween(340))) togetherWith
                            (slideOutHorizontally { it } + fadeOut(tween(280)))
                }
            },
            label = "onboarding_step"
        ) { currentStep ->
            when (currentStep) {
                OnboardingStep.SPLASH -> SplashStep(onContinue = { step = OnboardingStep.WELCOME })
                OnboardingStep.WELCOME -> WelcomeStep(
                    onContinue = { step = OnboardingStep.PERMISSIONS }
                )
                OnboardingStep.PERMISSIONS -> PermissionsStep(
                    hasOverlay = hasOverlay,
                    hasNotifications = hasNotifications,
                    hasCamera = hasCamera,
                    hasMic = hasMic,
                    onGrantOverlay = {
                        context.startActivity(
                            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        )
                    },
                    onGrantNotifications = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    onGrantCamera = { cameraLauncher.launch(Manifest.permission.CAMERA) },
                    onGrantMic = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onBack = { step = OnboardingStep.WELCOME },
                    onContinue = { step = OnboardingStep.PREFERENCES }
                )
                OnboardingStep.PREFERENCES -> PreferencesStep(
                    selectedTheme = selectedTheme,
                    onThemeChange = {
                        selectedTheme = it
                        scope.launch { userStore.saveThemeMode(it) }
                    },
                    upiId = upiId,
                    onUpiIdChange = { upiId = it },
                    payeeName = payeeName,
                    onPayeeNameChange = { payeeName = it },
                    onBack = { step = OnboardingStep.PERMISSIONS },
                    onContinue = {
                        scope.launch {
                            if (upiId.isNotBlank()) {
                                userStore.saveUpiIds(listOf(upiId))
                                userStore.saveDefaultUpiId(upiId)
                                if (payeeName.isNotBlank()) userStore.savePayeeName(payeeName)
                            }
                            step = OnboardingStep.DONE
                        }
                    }
                )
                OnboardingStep.DONE -> DoneStep(
                    onFinish = {
                        scope.launch {
                            userStore.setOnboardingComplete()
                            onFinishOnboarding()
                        }
                    }
                )
            }
        }
    }
}

// ─── Splash — Arc-style logo-only entrance ───────────────────────────────────

@Composable
private fun SplashStep(onContinue: () -> Unit) {
    val scale = remember { Animatable(0.25f) }
    val alpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)) }
        launch { alpha.animateTo(1f, tween(500)) }
        delay(1000)
        taglineAlpha.animateTo(1f, tween(500))
        delay(600)
        onContinue()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.alpha = alpha.value
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "QuickDash",
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "QuickDash",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer { this.alpha = alpha.value }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your floating toolkit",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer { this.alpha = taglineAlpha.value }
            )
        }
    }
}

// ─── Step 1 — Welcome ────────────────────────────────────────────────────────

@Composable
private fun WelcomeStep(onContinue: () -> Unit) {
    val features = listOf(
        Triple(Icons.Rounded.Layers, "Works everywhere", "Floating overlay on any app — no switching required"),
        Triple(Icons.Rounded.Payment, "22+ productivity tools", "Calculator, UPI QR, translator, clipboard and more"),
        Triple(Icons.Rounded.Palette, "Adapts to your style", "Wallpaper-aware M3 theming with AMOLED mode"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(Modifier.height(48.dp))
            Text(
                text = "Welcome to\nQuickDash",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 44.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Set up in under a minute.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            RoundedCardContainer {
                features.forEach { (icon, title, subtitle) ->
                    ListItem(
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                            }
                        },
                        headlineContent = {
                            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                        },
                        supportingContent = {
                            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            StepDots(current = 0, total = 4)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Get Started", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

// ─── Step 2 — Permissions ────────────────────────────────────────────────────

@Composable
private fun PermissionsStep(
    hasOverlay: Boolean,
    hasNotifications: Boolean,
    hasCamera: Boolean,
    hasMic: Boolean,
    onGrantOverlay: () -> Unit,
    onGrantNotifications: () -> Unit,
    onGrantCamera: () -> Unit,
    onGrantMic: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
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
                PermissionRow(
                    icon = Icons.Rounded.Layers,
                    title = "Display over other apps",
                    subtitle = "Required — enables the floating overlay",
                    isGranted = hasOverlay,
                    isRequired = true,
                    onGrant = onGrantOverlay
                )
                PermissionRow(
                    icon = Icons.Rounded.Notifications,
                    title = "Notifications",
                    subtitle = "For reminders and alerts",
                    isGranted = hasNotifications,
                    onGrant = onGrantNotifications
                )
                PermissionRow(
                    icon = Icons.Rounded.CameraAlt,
                    title = "Camera",
                    subtitle = "For QR scanning and capture",
                    isGranted = hasCamera,
                    onGrant = onGrantCamera
                )
                PermissionRow(
                    icon = Icons.Rounded.Mic,
                    title = "Microphone",
                    subtitle = "For voice memos recording",
                    isGranted = hasMic,
                    isLast = true,
                    onGrant = onGrantMic
                )
            }
        }

        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            StepDots(current = 1, total = 4)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                @Suppress("DEPRECATION")
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Rounded.ArrowBack, null, modifier = Modifier.size(18.dp))
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
private fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    isRequired: Boolean = false,
    isLast: Boolean = false,
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

// ─── Step 3 — Preferences ────────────────────────────────────────────────────

@Composable
private fun PreferencesStep(
    selectedTheme: String,
    onThemeChange: (String) -> Unit,
    upiId: String,
    onUpiIdChange: (String) -> Unit,
    payeeName: String,
    onPayeeNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val themes = listOf("SYSTEM" to "Auto", "LIGHT" to "Light", "DARK" to "Dark", "AMOLED" to "AMOLED")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(48.dp))
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Customize how QuickDash looks and works.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Theme",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                themes.forEachIndexed { index, (key, label) ->
                    SegmentedButton(
                        selected = selectedTheme == key,
                        onClick = { onThemeChange(key) },
                        shape = SegmentedButtonDefaults.itemShape(index, themes.size),
                        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Quick Collect (optional)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            RoundedCardContainer {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextField(
                        value = upiId,
                        onValueChange = onUpiIdChange,
                        placeholder = { Text("UPI ID — name@bank") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    TextField(
                        value = payeeName,
                        onValueChange = onPayeeNameChange,
                        placeholder = { Text("Your name (shown on QR)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            StepDots(current = 2, total = 4)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                @Suppress("DEPRECATION")
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Rounded.ArrowBack, null, modifier = Modifier.size(18.dp))
                }
                Button(onClick = onContinue, modifier = Modifier.weight(3f), shape = RoundedCornerShape(14.dp)) {
                    Text(if (upiId.isBlank()) "Skip & Continue" else "Save & Continue")
                }
            }
        }
    }
}

// ─── Step 4 — Done ───────────────────────────────────────────────────────────

@Composable
private fun DoneStep(onFinish: () -> Unit) {
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)) }
        launch { alpha.animateTo(1f, tween(500)) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer { scaleX = scale.value; scaleY = scale.value; this.alpha = alpha.value }
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp))
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "You're all set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer { this.alpha = alpha.value }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "QuickDash is ready to float over any app.\nTap the bubble or Quick Settings tile anytime.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer { this.alpha = alpha.value }
        )

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { this.alpha = alpha.value },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Open QuickDash", modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(16.dp))

        StepDots(current = 3, total = 4)
    }
}

// ─── Progress dots ────────────────────────────────────────────────────────────

@Composable
private fun StepDots(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            val width by animateFloatAsState(
                targetValue = if (i == current) 20f else 6f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "dot_$i"
            )
            Box(
                modifier = Modifier
                    .width(width.dp)
                    .height(6.dp)
                    .background(
                        if (i == current) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}
