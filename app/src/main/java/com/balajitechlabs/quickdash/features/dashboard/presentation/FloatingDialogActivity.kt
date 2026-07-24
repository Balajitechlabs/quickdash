package com.balajitechlabs.quickdash.features.dashboard.presentation

import com.balajitechlabs.quickdash.core.utils.AppLogger
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.launch
import java.util.Locale

class FloatingDialogActivity : FragmentActivity() {
    private lateinit var userStore: UserStore
    private var isAuthenticated by mutableStateOf(false)
    private var isAuthRequired by mutableStateOf(false)
    private var isFullScreen by mutableStateOf(false)
    private var currentAction by mutableStateOf<String?>(null)

    private val captureReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.balajitechlabs.quickdash.CAPTURE_WINDOW") {
                captureAndSaveWindowScreenshot()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userStore = UserStore(this)
        currentAction = intent?.getStringExtra("launch_section") ?: intent?.action

        val captureFilter = android.content.IntentFilter("com.balajitechlabs.quickdash.CAPTURE_WINDOW")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(captureReceiver, captureFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(captureReceiver, captureFilter)
        }

        val config = resources.configuration
        val isLargeScreen = config.smallestScreenWidthDp >= 600
        isFullScreen = isLargeScreen

        val lp = window.attributes
        lp.width = android.view.WindowManager.LayoutParams.MATCH_PARENT
        lp.height = android.view.WindowManager.LayoutParams.MATCH_PARENT
        lp.gravity = android.view.Gravity.CENTER
        lp.windowAnimations = android.R.style.Animation_Dialog
        window.attributes = lp

        // Apply saved locale — do NOT use runBlocking on the main thread (causes ANR/deadlock
        // with DataStore in release builds). Use a best-effort synchronous read from a
        // cached coroutine value, falling back to the system default locale on failure.
        lifecycleScope.launch {
            try {
                val langCode = userStore.appLanguage.first()
                if (langCode.isNotBlank()) {
                    val locale = Locale.forLanguageTag(langCode)
                    Locale.setDefault(locale)
                    val appConfig = resources.configuration
                    appConfig.setLocale(locale)
                    @Suppress("DEPRECATION")
                    resources.updateConfiguration(appConfig, resources.displayMetrics)
                }
            } catch (_: Exception) { /* fallback: keep system locale */ }
        }

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
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding() // Keep the dialog within screen safe drawing boundaries
                            .clickable(
                                interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                finish()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val config = androidx.compose.ui.platform.LocalConfiguration.current
                        val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                        val isLargeScreen = config.smallestScreenWidthDp >= 600

                        Box(
                            modifier = Modifier
                                .then(
                                    if (isFullScreen) {
                                        Modifier.fillMaxSize()
                                    } else {
                                        val widthFraction = when {
                                            isLandscape && isLargeScreen -> 0.65f
                                            isLandscape -> 0.70f
                                            isLargeScreen -> 0.75f
                                            else -> 0.90f
                                        }
                                        val maxDp = if (isLargeScreen) 560.dp else 480.dp
                                        Modifier
                                            .widthIn(max = maxDp)
                                            .fillMaxWidth(widthFraction)
                                            .wrapContentHeight()
                                    }
                                )
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            if (event.changes.size >= 4) {
                                                captureAndSaveWindowScreenshot()
                                            }
                                        }
                                    }
                                }
                                .clickable(
                                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    // Consume click events inside the card
                                }
                        ) {
                            QuickDashApp(
                                userStore = userStore,
                                shortcutAction = currentAction,
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
                                    lp.windowAnimations = android.R.style.Animation_Dialog
                                    window.attributes = lp
                                }
                            )
                        }
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
        // Share any pending crash log now that the window & task are fully ready.
        try {
            val pendingCrash = com.balajitechlabs.quickdash.core.utils.DiagnosticLogger.getPendingCrashLogFile(this)
            if (pendingCrash != null) {
                shareLogFile(pendingCrash)
            }
        } catch (_: Exception) {}

        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.addPrimaryClipChangedListener(clipboardListener)
            saveClipboardData()
        } catch (e: Exception) {
            AppLogger.e("FloatingDialogActivity", "Failed to add primary clip changed listener", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(captureReceiver)
        } catch (_: Exception) {}
    }

    override fun onPause() {
        super.onPause()
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.removePrimaryClipChangedListener(clipboardListener)
        } catch (e: Exception) {
            AppLogger.e("FloatingDialogActivity", "Failed to remove primary clip listener", e)
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
            AppLogger.e("FloatingDialogActivity", "Clipboard read error or worker flow launch failure", e)
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

    private fun shareLogFile(file: java.io.File) {
        try {
            val authority = "${packageName}.provider"
            val uri = androidx.core.content.FileProvider.getUriForFile(this, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share Diagnostic Log"))
        } catch (e: Exception) {
            AppLogger.e("FloatingDialogActivity", "Failed to share log file", e)
        }
    }

    private fun captureAndSaveWindowScreenshot() {
        try {
            val rootView = window.decorView.rootView
            rootView.isDrawingCacheEnabled = true
            val bitmap = android.graphics.Bitmap.createBitmap(rootView.drawingCache)
            rootView.isDrawingCacheEnabled = false

            val filename = "QuickDash_Window_${System.currentTimeMillis()}.png"
            val cv = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/QuickDash")
                }
            }
            val uri = contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { s ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, s)
                }
                android.widget.Toast.makeText(this, "📸 Floating Window Screenshot Saved!", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Screenshot failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
