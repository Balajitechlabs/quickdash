package com.balajitechlabs.quickdash

import com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity

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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.balajitechlabs.quickdash.features.broadcast.data.TelegramPollerWorker
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : FragmentActivity() {
    private lateinit var userStore: UserStore
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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        userStore = UserStore(this)
        
        // Force app locale configuration context from Datastore
        try {
            val langCode = runBlocking { userStore.appLanguage.first() }
            val locale = Locale(langCode)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        } catch (_: Exception) {
            // Fallback to system default locale
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
            .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(this, R.drawable.ic_shortcut_upi))
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
            userStore.secureMode.collect { secure ->
                if (secure) {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }

        lifecycleScope.launch {
            val onboarded = userStore.isOnboardingComplete.first()
            val style = userStore.launchStyle.first()
            val isShortcut = intent?.action != null && intent.action != Intent.ACTION_MAIN

            if (onboarded && style == "FLOATING_DIALOG" && !isShortcut) {
                val dialogIntent = Intent(this@MainActivity, FloatingDialogActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(dialogIntent)
                finish()
                return@launch
            }

            val locked = userStore.isAppLocked.first()
            if (locked) {
                isAuthRequired = true
                showBiometricPrompt()
            } else {
                isAuthenticated = true
            }
            
            // Analytics Ping
            val hasReported = userStore.hasReportedInstall.first()
            if (!hasReported) {
                val model = Build.MODEL
                val version = BuildConfig.VERSION_NAME
                val count = userStore.totalAppOpens.first()
                TelegramTracker.sendMessage("🎉 <b>New QuickDash Install!</b>\nDevice: $model\nVersion: $version\nApp Opens: $count")
                userStore.setHasReportedInstall()
            }

            // Clean up legacy/default demo profiles if present
            val currentPayee = userStore.payeeName.first()
            if (currentPayee == "BalajiTechLabs") {
                userStore.savePayeeName("")
            }
            val currentIds = userStore.upiIds.first()
            if (currentIds == listOf("9344456571@kotakbank") || currentIds.contains("9344456571@kotakbank")) {
                userStore.saveUpiIds(emptyList())
                userStore.saveDefaultUpiId("")
            }

            
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val lastActive = userStore.lastActiveDate.first()
            if (today != lastActive) {
                val model = Build.MODEL
                val version = BuildConfig.VERSION_NAME
                val count = userStore.totalAppOpens.first()
                TelegramTracker.sendMessage("📊 <b>DAU Ping</b>: User Active Today\nDevice: $model\nVersion: $version\nApp Opens: $count")
                userStore.setLastActiveDate(today)
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

        // Fetch and register FCM and OneSignal Diagnostics
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result ?: ""
                    lifecycleScope.launch {
                        userStore.saveFcmToken(token)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // OneSignal registration loop disabled
        // try {
        //     lifecycleScope.launch {
        //         var checked = 0
        //         while (checked < 10) {
        //             val id = com.onesignal.OneSignal.User.pushSubscription.id ?: ""
        //             if (id.isNotEmpty()) {
        //                 userStore.saveOnesignalId(id)
        //                 break
        //             }
        //             kotlinx.coroutines.delay(2000)
        //             checked++
        //         }
        //     }
        // } catch (e: Exception) {
        //     e.printStackTrace()
        // }

        setContent {
            val themeMode by userStore.themeMode.collectAsState(initial = "SYSTEM")
            val dynamicColor by userStore.dynamicColor.collectAsState(initial = false)
            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            val shortcutAction = intent?.action
            val notificationTitle = intent?.getStringExtra("title")
            val notificationMessage = intent?.getStringExtra("message")
            val notificationImageUrl = intent?.getStringExtra("imageUrl")
            val notificationIsPoll = intent?.getBooleanExtra("isPoll", false) ?: false

            QuickDashTheme(themeMode = themeMode, darkTheme = isDarkTheme, dynamicColor = dynamicColor) {
                val lastSeenVersion by userStore.lastSeenVersion.collectAsState(initial = "")
                var showUpdateDialog by androidx.compose.runtime.remember { mutableStateOf(false) }

                androidx.compose.runtime.LaunchedEffect(lastSeenVersion) {
                    if (lastSeenVersion.isNotEmpty() && lastSeenVersion != BuildConfig.VERSION_NAME) {
                        showUpdateDialog = true
                    } else if (lastSeenVersion.isEmpty()) {
                        userStore.saveLastSeenVersion(BuildConfig.VERSION_NAME)
                    }
                }

                if (showUpdateDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            lifecycleScope.launch { userStore.saveLastSeenVersion(BuildConfig.VERSION_NAME) }
                            showUpdateDialog = false
                        },
                        title = { Text("App Updated! 🎉") },
                        text = { Text("Are you happy with this new version of QuickDash? Let us know!") },
                        confirmButton = {
                            TextButton(onClick = {
                                lifecycleScope.launch {
                                    TelegramTracker.sendBroadcastBotMessage("🎉 User updated to v${BuildConfig.VERSION_NAME}\nHappy: Yes")
                                    userStore.saveLastSeenVersion(BuildConfig.VERSION_NAME)
                                }
                                showUpdateDialog = false
                            }) { Text("Yes") }
                        },
                        dismissButton = {
                            Row {
                                TextButton(onClick = {
                                    lifecycleScope.launch {
                                        TelegramTracker.sendBroadcastBotMessage("📉 User updated to v${BuildConfig.VERSION_NAME}\nHappy: No")
                                        userStore.saveLastSeenVersion(BuildConfig.VERSION_NAME)
                                    }
                                    showUpdateDialog = false
                                }) { Text("No") }
                                TextButton(onClick = {
                                    lifecycleScope.launch {
                                        TelegramTracker.sendBroadcastBotMessage("⏭ User updated to v${BuildConfig.VERSION_NAME}\nFeedback: Skipped")
                                        userStore.saveLastSeenVersion(BuildConfig.VERSION_NAME)
                                    }
                                    showUpdateDialog = false
                                }) { Text("Skip") }
                            }
                        }
                    )
                }

                if (isAuthenticated) {
                    QuickDashApp(
                        userStore = userStore,
                        shortcutAction = shortcutAction,
                        notificationTitle = notificationTitle,
                        notificationMessage = notificationMessage,
                        notificationImageUrl = notificationImageUrl,
                        notificationIsPoll = notificationIsPoll,
                        themeMode = themeMode,
                        dynamicColor = dynamicColor,
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
                        onDismiss = { finish() }
                    )
                }
            }
        }

        // Overlay / Bubble Service logic
        lifecycleScope.launch {
            userStore.bubbleEnabled.collect { enabled ->
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
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(closeAppReceiver)
        } catch (e: Exception) {
            // Ignore if not registered
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
                            android.widget.Toast.makeText(this@MainActivity, "Saved to QuickDash Clipboard", android.widget.Toast.LENGTH_SHORT).show()
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
