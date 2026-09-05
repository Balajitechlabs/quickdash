/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/onboarding/presentation
 * File: WelcomeOnboardingScreen.kt
 * Description: Modern step-based onboarding screen orchestrating page progression and state persistence.
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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.WelcomeOnboardingDoneStep
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.WelcomeOnboardingIntroStep
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.WelcomeOnboardingPermissionsStep
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.WelcomeOnboardingPreferencesStep
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.WelcomeOnboardingSplashStep
import kotlinx.coroutines.launch

private enum class OnboardingStep {
    SPLASH, WELCOME, PERMISSIONS, PREFERENCES, DONE
}

@Composable
fun WelcomeOnboardingScreen(onFinishOnboarding: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userStore = remember { UserStore(context) }

    var step by remember { mutableStateOf(OnboardingStep.SPLASH) }

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

    val themeMode by userStore.themeMode.collectAsStateWithLifecycle(initialValue = "SYSTEM")
    var selectedTheme by remember(themeMode) { mutableStateOf(themeMode) }

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
                OnboardingStep.SPLASH -> WelcomeOnboardingSplashStep(onContinue = { step = OnboardingStep.WELCOME })
                OnboardingStep.WELCOME -> WelcomeOnboardingIntroStep(
                    onContinue = { step = OnboardingStep.PERMISSIONS }
                )
                OnboardingStep.PERMISSIONS -> WelcomeOnboardingPermissionsStep(
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
                OnboardingStep.PREFERENCES -> WelcomeOnboardingPreferencesStep(
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
                OnboardingStep.DONE -> WelcomeOnboardingDoneStep(
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
