package com.balajitechlabs.quickdash.features.dashboard.presentation

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.biometric.BiometricPrompt
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.ui.QuickDashApp
import com.balajitechlabs.quickdash.core.ui.theme.QuickDashTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import java.util.Locale

class FloatingDialogActivity : FragmentActivity() {
    private lateinit var userStore: UserStore
    private var isAuthenticated by mutableStateOf(false)
    private var isAuthRequired by mutableStateOf(false)
    private var isFullScreen by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userStore = UserStore(this)

        val displayMetrics = resources.displayMetrics
        val isLargeScreen = resources.configuration.smallestScreenWidthDp >= 600
        isFullScreen = isLargeScreen

        val lp = window.attributes
        if (isFullScreen) {
            lp.width = android.view.WindowManager.LayoutParams.MATCH_PARENT
            lp.height = android.view.WindowManager.LayoutParams.MATCH_PARENT
        } else {
            lp.width = (displayMetrics.widthPixels * 0.85f).toInt().coerceAtMost(800)
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT
        }
        window.attributes = lp

        // Force app locale configuration context from Datastore
        val langCode = runBlocking { userStore.appLanguage.first() }
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        lifecycleScope.launch {
            val locked = userStore.isAppLocked.first()
            if (locked) {
                isAuthRequired = true
                showBiometricPrompt()
            } else {
                isAuthenticated = true
            }

            // Secure Mode Setup
            userStore.secureMode.collect { secure ->
                if (secure) {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }

        setContent {
            val themeMode by userStore.themeMode.collectAsState(initial = "SYSTEM")
            val dynamicColor by userStore.dynamicColor.collectAsState(initial = false)
            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            QuickDashTheme(themeMode = themeMode, darkTheme = isDarkTheme, dynamicColor = dynamicColor) {
                if (isAuthenticated) {
                    Box(
                        modifier = if (isFullScreen) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        val targetAction = intent?.getStringExtra("launch_section") ?: intent?.action
                        QuickDashApp(
                            userStore = userStore,
                            shortcutAction = targetAction,
                            themeMode = themeMode,
                            dynamicColor = dynamicColor,
                            isFloating = !isFullScreen,
                            onToggleDynamicColor = { enabled ->
                                lifecycleScope.launch {
                                    userStore.saveDynamicColor(enabled)
                                }
                            },
                            onChangeThemeMode = { nextMode ->
                                lifecycleScope.launch {
                                    userStore.saveThemeMode(nextMode)
                                }
                            },
                            onQrShown = { maxBrightness() },
                            onRestoreBrightness = { restoreBrightness() },
                            onDismiss = { finish() },
                            onConvertToFullScreen = {
                                isFullScreen = true
                                val lp = window.attributes
                                lp.width = android.view.WindowManager.LayoutParams.MATCH_PARENT
                                lp.height = android.view.WindowManager.LayoutParams.MATCH_PARENT
                                window.attributes = lp
                            }
                        )
                    }
                }
            }
        }
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
            .setSubtitle("Authenticate to access your floating dashboard")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        saveClipboardData()
    }

    override fun onResume() {
        super.onResume()
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.addPrimaryClipChangedListener(clipboardListener)
            saveClipboardData()
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.removePrimaryClipChangedListener(clipboardListener)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun saveClipboardData() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip()) {
                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                if (!text.isNullOrBlank()) {
                    lifecycleScope.launch {
                        val historyJson = userStore.clipboardHistory.first()
                        val gson = Gson()
                        val listType = object : TypeToken<List<String>>() {}.type
                        val list: MutableList<String> = try {
                            gson.fromJson(historyJson, listType) ?: mutableListOf()
                        } catch (e: Exception) {
                            mutableListOf()
                        }
                        
                        if (!list.contains(text)) {
                            list.add(0, text)
                            userStore.saveClipboardHistory(gson.toJson(list.take(20)))
                            android.widget.Toast.makeText(this@FloatingDialogActivity, "Saved to QuickDash Clipboard", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore clipboard read errors
        }
    }

    private fun maxBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 1.0f
        window.attributes = layoutParams
    }

    private fun restoreBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness =
            android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams
    }
}
