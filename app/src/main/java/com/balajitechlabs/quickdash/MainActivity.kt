/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: root
 * File: MainActivity.kt
 * Description: Main application activity hosting the primary Compose entry point and Edge-to-Edge window configuration.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.balajitechlabs.quickdash.core.services.FloatingBubbleService
import com.balajitechlabs.quickdash.core.ui.theme.QuickDashTheme
import com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity
import com.balajitechlabs.quickdash.features.onboarding.presentation.QuickDashWelcomeScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private var isAuthenticated by mutableStateOf(true)
    private var isAuthRequired by mutableStateOf(false)
    private var currentAction by mutableStateOf<String?>(null)

    private var originalBrightness: Float = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        currentAction = intent?.getStringExtra("launch_section") ?: intent?.action

        // If user is already onboarded, forward to floating dialog launcher
        lifecycleScope.launch {
            val isOnboardingComplete = mainViewModel.userStore.isOnboardingComplete.first()
            if (isOnboardingComplete) {
                val dialogIntent = Intent(this@MainActivity, FloatingDialogActivity::class.java).apply {
                    action = currentAction
                    intent?.extras?.let { putExtras(it) }
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(dialogIntent)
                finish()
                return@launch
            }
        }

        // Standard, full-screen Welcome Setup Screen for fresh installs and cleared app data
        setContent {
            val themeMode by mainViewModel.userStore.themeMode.collectAsStateWithLifecycle(initialValue = "SYSTEM")
            val dynamicColor by mainViewModel.userStore.dynamicColor.collectAsStateWithLifecycle(initialValue = false)
            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK", "AMOLED" -> true
                else -> isSystemInDarkTheme()
            }
            val isOnboardingComplete by mainViewModel.userStore.isOnboardingComplete.collectAsStateWithLifecycle(initialValue = null)

            QuickDashTheme(themeMode = themeMode, darkTheme = isDarkTheme, dynamicColor = dynamicColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    if (isOnboardingComplete == false) {
                        QuickDashWelcomeScreen(
                            onFinishOnboarding = {
                                lifecycleScope.launch {
                                    mainViewModel.userStore.setOnboardingComplete()
                                    // Start floating bubble companion if enabled
                                    val bubbleEnabled = mainViewModel.userStore.bubbleEnabled.first()
                                    if (bubbleEnabled) {
                                        try {
                                            val serviceIntent = Intent(this@MainActivity, FloatingBubbleService::class.java)
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                startForegroundService(serviceIntent)
                                            } else {
                                                startService(serviceIntent)
                                            }
                                        } catch (_: Exception) { }
                                    }
                                    // Launch into the app
                                    val dialogIntent = Intent(this@MainActivity, FloatingDialogActivity::class.java).apply {
                                        action = currentAction
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    }
                                    startActivity(dialogIntent)
                                    finish()
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentAction = intent.getStringExtra("launch_section") ?: intent.action
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthenticated = true
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finish()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("QuickDash Locked")
            .setSubtitle("Authenticate to access your QuickDash")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun maxBrightness() {
        lifecycleScope.launch {
            val maxBrightEnabled = mainViewModel.userStore.maxBrightness.first()
            if (maxBrightEnabled) {
                val lp = window.attributes
                originalBrightness = lp.screenBrightness
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
                window.attributes = lp
            }
        }
    }

    private fun restoreBrightness() {
        val lp = window.attributes
        lp.screenBrightness = originalBrightness
        window.attributes = lp
    }
}
