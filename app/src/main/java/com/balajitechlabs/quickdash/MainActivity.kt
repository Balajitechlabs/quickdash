package com.balajitechlabs.quickdash

import com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity
import com.balajitechlabs.quickdash.core.utils.AppLogger

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.net.Uri
import android.os.Build
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.services.FloatingBubbleService
import com.balajitechlabs.quickdash.core.ui.QuickDashApp
import com.balajitechlabs.quickdash.core.ui.theme.QuickDashTheme
import com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.balajitechlabs.quickdash.features.broadcast.data.TelegramPollerWorker
import java.util.concurrent.TimeUnit
import com.balajitechlabs.quickdash.features.onboarding.presentation.WelcomeOnboardingScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import com.balajitechlabs.quickdash.MainViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    private var isAuthenticated by mutableStateOf(false)
    private var isAuthRequired by mutableStateOf(false)

    private val closeAppReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.balajitechlabs.quickdash.CLOSE_APP") {
                finishAndRemoveTask()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Apply saved locale — do NOT use runBlocking on the main thread here.
        // DataStore IO on the main thread causes an ANR / deadlock in release builds.
        // We launch async and accept that the first frame may use the system locale;
        // this is invisible to the user since the Activity hasn't rendered yet.
        lifecycleScope.launch {
            try {
                val langCode = mainViewModel.appLanguage.first()
                if (langCode.isNotBlank()) {
                    val locale = Locale.forLanguageTag(langCode)
                    Locale.setDefault(locale)
                    val config = resources.configuration
                    config.setLocale(locale)
                    @Suppress("DEPRECATION")
                    resources.updateConfiguration(config, resources.displayMetrics)
                }
            } catch (_: Exception) { /* keep system locale */ }
        }

        val shortcutAction = intent?.action
        
        val searchShortcut = androidx.core.content.pm.ShortcutInfoCompat.Builder(this, "shortcut_search")
            .setShortLabel("Search")
            .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(this, R.drawable.ic_search))
            .setIntent(Intent(this, MainActivity::class.java).apply {
                action = "com.balajitechlabs.quickdash.ACTION_QUICK_SEARCH"
            })
            .build()
        
        val notesShortcut = androidx.core.content.pm.ShortcutInfoCompat.Builder(this, "shortcut_notes")
            .setShortLabel("New Note")
            .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(this, R.drawable.ic_note))
            .setIntent(Intent(this, MainActivity::class.java).apply {
                action = "com.balajitechlabs.quickdash.ACTION_QUICK_NOTES"
            })
            .build()
            
        val wifiShortcut = androidx.core.content.pm.ShortcutInfoCompat.Builder(this, "shortcut_wifi")
            .setShortLabel("Wi-Fi Share")
            .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(this, R.drawable.ic_shortcut_wifi))
            .setIntent(Intent(this, MainActivity::class.java).apply {
                action = "com.balajitechlabs.quickdash.ACTION_SHOW_QR"
            })
            .build()

        val calcShortcut = androidx.core.content.pm.ShortcutInfoCompat.Builder(this, "shortcut_calculator")
            .setShortLabel("Calculator")
            .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(this, R.drawable.ic_calculator))
            .setIntent(Intent(this, MainActivity::class.java).apply {
                action = "com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR"
            })
            .build()

        val timerShortcut = androidx.core.content.pm.ShortcutInfoCompat.Builder(this, "shortcut_timer")
            .setShortLabel("Timer")
            .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(this, R.drawable.ic_timer))
            .setIntent(Intent(this, MainActivity::class.java).apply {
                action = "com.balajitechlabs.quickdash.ACTION_QUICK_TIMER"
            })
            .build()

        androidx.core.content.pm.ShortcutManagerCompat.addDynamicShortcuts(this, listOf(searchShortcut, notesShortcut, wifiShortcut, calcShortcut, timerShortcut))
        
        // Google Play Store APIs: In-App Updates & In-App Reviews (throttled)
        checkForPlayAppUpdate()
        lifecycleScope.launch {
            val opens = mainViewModel.settingsRepository.totalAppOpens.first()
            // Only show review dialog: after 10+ opens AND every 20 opens (avoids Google suppression)
            if (opens >= 10L && opens % 20L == 0L) {
                requestPlayInAppReview()
            }
        }
        
        // Enqueue the Telegram Poller to check for broadcasts every 15 minutes
        val pollerRequest = PeriodicWorkRequestBuilder<TelegramPollerWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "telegram_poller",
            ExistingPeriodicWorkPolicy.KEEP,
            pollerRequest
        )
        
        // Also trigger it IMMEDIATELY once on app launch so you don't have to wait 15 mins for replies
        val oneTimeRequest = androidx.work.OneTimeWorkRequestBuilder<TelegramPollerWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "telegram_poller_immediate",
            androidx.work.ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )

        val filter = IntentFilter("com.balajitechlabs.quickdash.CLOSE_APP")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(closeAppReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(closeAppReceiver, filter)
        }

        lifecycleScope.launch {
            mainViewModel.secureMode.collect { secure ->
                if (secure) {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }

        lifecycleScope.launch {
            val onboarded = mainViewModel.isOnboardingComplete.first()
            val style = mainViewModel.launchStyle.first()
            val isShortcut = intent?.action != null && intent.action != Intent.ACTION_MAIN

            if (onboarded && style == "FLOATING_DIALOG" && !isShortcut) {
                val dialogIntent = Intent(this@MainActivity, FloatingDialogActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(dialogIntent)
                finish()
                return@launch
            }

            val locked = mainViewModel.isAppLocked.first()
            if (locked) {
                isAuthRequired = true
                showBiometricPrompt()
            } else {
                isAuthenticated = true
            }
            
            // Analytics Ping
            val hasReported = mainViewModel.settingsRepository.hasReportedInstall.first()
            if (!hasReported) {
                val model = Build.MODEL
                val version = BuildConfig.VERSION_NAME
                val count = mainViewModel.settingsRepository.totalAppOpens.first()
                if (count > 0L) {
                    TelegramTracker.sendBroadcastBotMessage("📲 <b>User Returned!</b>\nApp opened for the ${count + 1}th time.")
                }
                mainViewModel.settingsRepository.setHasReportedInstall()
            }

            // Clean up legacy/default demo profiles if present
            val currentPayee = mainViewModel.settingsRepository.payeeName.first()
            if (currentPayee == "BalajiTechLabs") {
                mainViewModel.settingsRepository.savePayeeName("")
            }
            val currentIds = mainViewModel.settingsRepository.upiIds.first()
            if (currentIds == listOf("9344456571@kotakbank") || currentIds.contains("9344456571@kotakbank")) {
                mainViewModel.settingsRepository.saveUpiIds(emptyList())
                mainViewModel.settingsRepository.saveDefaultUpiId("")
            }

            
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val lastActive = mainViewModel.settingsRepository.lastActiveDate.first()
            
            if (lastActive != today) {
                val count = mainViewModel.settingsRepository.totalAppOpens.first()
                TelegramTracker.sendBroadcastBotMessage("🌅 <b>Daily Active User!</b>\nApp opened today (Total Opens: $count).")
                mainViewModel.settingsRepository.setLastActiveDate(today)
            }

            // Secure Mode — handled by the dedicated lifecycleScope.launch block above (line 158)
            // Removed duplicate collector that caused race condition with FLAG_SECURE
        }

        // Fetch and register FCM and OneSignal Diagnostics
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result ?: ""
                    lifecycleScope.launch {
                        mainViewModel.settingsRepository.saveFcmToken(token)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        setContent {
            val isMigrationComplete by mainViewModel.isMigrationComplete.collectAsState(initial = false)
            if (!isMigrationComplete) {
                QuickDashTheme {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                return@setContent
            }

            val themeMode by mainViewModel.themeMode.collectAsState(initial = "SYSTEM")
            val dynamicColor by mainViewModel.dynamicColor.collectAsState(initial = false)
            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK", "AMOLED" -> true
                else -> isSystemInDarkTheme()
            }

            val shortcutAction = intent?.action
            val notificationTitle = intent?.getStringExtra("title")
            val notificationMessage = intent?.getStringExtra("message")
            val notificationImageUrl = intent?.getStringExtra("imageUrl")
            val notificationIsPoll = intent?.getBooleanExtra("isPoll", false) ?: false

            androidx.compose.animation.Crossfade(
                targetState = themeMode,
                animationSpec = androidx.compose.animation.core.tween(400)
            ) { currentThemeMode ->
                QuickDashTheme(themeMode = currentThemeMode, darkTheme = isDarkTheme, dynamicColor = dynamicColor) {
                val lastSeenVersion by mainViewModel.lastSeenVersion.collectAsState(initial = "")

                androidx.compose.runtime.LaunchedEffect(lastSeenVersion) {
                    if (lastSeenVersion != BuildConfig.VERSION_NAME) {
                        mainViewModel.saveLastSeenVersion(BuildConfig.VERSION_NAME)
                    }
                }

                val isOnboardingComplete by mainViewModel.isOnboardingComplete.collectAsState(initial = true)

                if (!isOnboardingComplete) {
                    WelcomeOnboardingScreen(
                        onFinishOnboarding = {
                            lifecycleScope.launch {
                                mainViewModel.setOnboardingComplete()
                                val style = mainViewModel.launchStyle.first()
                                if (style == "FLOATING_DIALOG") {
                                    val dialogIntent = Intent(this@MainActivity, com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    }
                                    startActivity(dialogIntent)
                                    finish()
                                }
                            }
                        }
                    )
                } else if (isAuthenticated) {
                    QuickDashApp(
                        mainViewModel = mainViewModel,
                        shortcutAction = shortcutAction,
                        notificationTitle = notificationTitle,
                        notificationMessage = notificationMessage,
                        notificationImageUrl = notificationImageUrl,
                        notificationIsPoll = notificationIsPoll,
                        themeMode = themeMode,
                        dynamicColor = dynamicColor,
                        onToggleDynamicColor = { enabled ->
                            lifecycleScope.launch {
                                mainViewModel.saveDynamicColor(enabled)
                            }
                        },
                        onChangeThemeMode = { nextMode ->
                            lifecycleScope.launch {
                                mainViewModel.saveThemeMode(nextMode)
                            }
                        },
                        onQrShown = { maxBrightness() },
                        onRestoreBrightness = { restoreBrightness() },
                    )
                }
            }
        }
    }

    // Overlay / Bubble Service logic
        lifecycleScope.launch {
            mainViewModel.bubbleEnabled.collect { enabled ->
                try {
                    if (enabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (Settings.canDrawOverlays(this@MainActivity)) {
                                startService(Intent(this@MainActivity, FloatingBubbleService::class.java))
                            }
                        } else {
                            startService(Intent(this@MainActivity, FloatingBubbleService::class.java))
                        }
                    } else {
                        stopService(Intent(this@MainActivity, FloatingBubbleService::class.java))
                    }
                } catch (e: Exception) {
                    com.balajitechlabs.quickdash.core.utils.AppLogger.e("MainActivity", "Failed to start/stop FloatingBubbleService", e)
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
                    // Close the app on any terminal biometric error so the user
                    // is never left staring at a blank screen with no way out.
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,    // User tapped "Cancel"
                        BiometricPrompt.ERROR_USER_CANCELED,       // User dismissed
                        BiometricPrompt.ERROR_LOCKOUT,             // Too many attempts
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT,   // Permanently locked
                        BiometricPrompt.ERROR_NO_BIOMETRICS,       // No fingerprints enrolled
                        BiometricPrompt.ERROR_HW_NOT_PRESENT,      // No biometric hardware
                        BiometricPrompt.ERROR_HW_UNAVAILABLE -> {  // Hardware unavailable
                            finish()
                        }
                        // For other transient errors (e.g. sensor dirty), do nothing — the
                        // prompt stays visible and the user can try again.
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("QuickDash Locked")
            .setSubtitle("Authenticate to access your dashboard")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        saveClipboardData()
    }

    override fun onResume() {
        super.onResume()
        com.balajitechlabs.quickdash.core.utils.UpdateManager.checkForUpdates(this)
        
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.addPrimaryClipChangedListener(clipboardListener)
            saveClipboardData()
        } catch (e: Exception) {
            AppLogger.e("MainActivity", "Failed to add primary clip changed listener", e)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.removePrimaryClipChangedListener(clipboardListener)
        } catch (e: Exception) {
            AppLogger.e("MainActivity", "Failed to remove primary clip listener", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(closeAppReceiver)
        } catch (e: Exception) {
            AppLogger.e("MainActivity", "Failed to unregister closeAppReceiver", e)
        }
    }

    private fun saveClipboardData() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip()) {
                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                if (!text.isNullOrBlank()) {
                    lifecycleScope.launch {
                        val historyJson = mainViewModel.clipboardHistory.first()
                        val gson = Gson()
                        val listType = object : TypeToken<List<String>>() {}.type
                        val list: MutableList<String> = try {
                            gson.fromJson(historyJson, listType) ?: mutableListOf()
                        } catch (e: Exception) {
                            mutableListOf()
                        }
                        
                        if (!list.contains(text)) {
                            list.add(0, text)
                            mainViewModel.saveClipboardHistory(gson.toJson(list.take(20)))
                            android.widget.Toast.makeText(this@MainActivity, "Saved to QuickDash Clipboard", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AppLogger.e("MainActivity", "Clipboard read error or worker flow launch failure", e)
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

    private fun checkForPlayAppUpdate() {
        try {
            // Using KTX-compatible coroutines API (com.google.android.play:app-update-ktx)
            val appUpdateManager = com.google.android.play.core.appupdate.AppUpdateManagerFactory.create(this)
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                val isUpdateAvailable = appUpdateInfo.updateAvailability() ==
                    com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
                val isFlexible = appUpdateInfo.isUpdateTypeAllowed(
                    com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
                )
                if (isUpdateAvailable && isFlexible) {
                    @Suppress("DEPRECATION")
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        this,
                        com.google.android.play.core.appupdate.AppUpdateOptions.defaultOptions(
                            com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
                        ),
                        9901
                    )
                }
            }.addOnFailureListener { e ->
                AppLogger.e("MainActivity", "In-App Update check failed", e)
            }
        } catch (e: Exception) {
            AppLogger.e("MainActivity", "In-App Update check skipped", e)
        }
    }

    private fun requestPlayInAppReview() {
        try {
            // Uses com.google.android.play:review-ktx (already in build.gradle.kts)
            val manager = com.google.android.play.core.review.ReviewManagerFactory.create(this)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    manager.launchReviewFlow(this, task.result)
                } else {
                    AppLogger.e("MainActivity", "In-App Review request failed: ${task.exception?.message}")
                }
            }
        } catch (e: Exception) {
            AppLogger.e("MainActivity", "In-App Review request skipped", e)
        }
    }
}
