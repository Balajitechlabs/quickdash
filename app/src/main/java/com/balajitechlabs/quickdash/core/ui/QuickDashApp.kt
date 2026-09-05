/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui
 * File: QuickDashApp.kt
 * Description: Root composable scaffold managing the bottom navigation, floating toolbar, theme container, and screen transitions.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui


import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.mutableFloatStateOf
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import com.balajitechlabs.quickdash.core.ui.components.AppUpdateBottomSheet
import com.balajitechlabs.quickdash.core.ui.components.DownloadingProgressDialog
import com.balajitechlabs.quickdash.core.ui.components.ReadyToInstallDialog
import com.balajitechlabs.quickdash.BuildConfig
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.balajitechlabs.quickdash.features.dashboard.presentation.SpotlightLauncher
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.ui.QuickTool
import com.balajitechlabs.quickdash.features.settings.presentation.BlogPostsScreen
import com.balajitechlabs.quickdash.features.qr.presentation.EnterAmountScreen
import com.balajitechlabs.quickdash.features.chat.presentation.QuickChatScreen
import com.balajitechlabs.quickdash.features.insta.presentation.QuickSocialScreen
import com.balajitechlabs.quickdash.features.notes.presentation.QuickNotesScreen
import com.balajitechlabs.quickdash.features.search.presentation.QuickSearchScreen
import com.balajitechlabs.quickdash.features.search.presentation.QuickWebScreen
import com.balajitechlabs.quickdash.features.wifi.presentation.QuickWifiScreen
import com.balajitechlabs.quickdash.features.converter.presentation.QuickConverterScreen
import com.balajitechlabs.quickdash.features.translator.presentation.QuickTranslatorScreen
import com.balajitechlabs.quickdash.features.capture.presentation.QuickCaptureScreen
import com.balajitechlabs.quickdash.features.settings.presentation.SystemLogsScreen
import com.balajitechlabs.quickdash.features.qr.presentation.SetupScreen
import com.balajitechlabs.quickdash.features.qr.presentation.ShowQrScreen
import com.balajitechlabs.quickdash.features.qr.presentation.PaymentTargetApp
import com.balajitechlabs.quickdash.features.settings.presentation.SettingsScreen

import com.balajitechlabs.quickdash.core.utils.QRCodeGenerator
import com.balajitechlabs.quickdash.core.utils.UpdateManager
import com.balajitechlabs.quickdash.core.utils.UpdateState
import com.balajitechlabs.quickdash.features.pomodoro.presentation.QuickPomodoroScreen
import com.balajitechlabs.quickdash.features.password.presentation.QuickPasswordScreen
import com.balajitechlabs.quickdash.features.voicememos.presentation.QuickVoiceMemosScreen
import com.balajitechlabs.quickdash.features.reminders.presentation.QuickRemindersScreen
import com.balajitechlabs.quickdash.features.qr.presentation.QuickQrScannerScreen
import com.balajitechlabs.quickdash.features.onboarding.presentation.WelcomeOnboardingScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast

private const val TAG = "QuickDashApp"

@Composable
fun QuickDashApp(
    mainViewModel: com.balajitechlabs.quickdash.MainViewModel,
    shortcutAction: String? = null,
    notificationTitle: String? = null,
    notificationMessage: String? = null,
    notificationImageUrl: String? = null,
    notificationIsPoll: Boolean = false,
    themeMode: String = "SYSTEM",
    dynamicColor: Boolean = false,
    isFloating: Boolean = false,
    onToggleDynamicColor: (Boolean) -> Unit = {},
    onChangeThemeMode: (String) -> Unit = {},
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val savedUpiIds by mainViewModel.userStore.upiIds.collectAsStateWithLifecycle(initialValue = emptyList())
    val savedPaypalIds by mainViewModel.userStore.paypalIds.collectAsStateWithLifecycle(initialValue = emptyList())
    val usePaypal by mainViewModel.userStore.usePaypal.collectAsStateWithLifecycle(initialValue = false)
    val defaultPaymentApp by mainViewModel.userStore.defaultPaymentApp.collectAsStateWithLifecycle(initialValue = "ANY")
    val qrHistoryJson by mainViewModel.userStore.qrHistory.collectAsStateWithLifecycle(initialValue = "[]")
    
    // Choose active IDs based on mode
    val activeIds = if (usePaypal) savedPaypalIds else savedUpiIds
    
    val savedDefaultUpiId by mainViewModel.userStore.defaultUpiId.collectAsStateWithLifecycle(initialValue = null)
    val savedDefaultPaypalId by mainViewModel.userStore.defaultPaypalId.collectAsStateWithLifecycle(initialValue = null)
    val activeDefaultId = if (usePaypal) savedDefaultPaypalId ?: savedPaypalIds.firstOrNull() ?: "" else savedDefaultUpiId ?: savedUpiIds.firstOrNull() ?: ""

    val savedPayeeName by mainViewModel.userStore.payeeName.collectAsStateWithLifecycle(initialValue = null)
    val recentAmounts by mainViewModel.userStore.recentAmounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val showUpiId by mainViewModel.userStore.showUpiId.collectAsStateWithLifecycle(initialValue = true)
    
    val bubbleEnabled by mainViewModel.userStore.bubbleEnabled.collectAsStateWithLifecycle(initialValue = true)
    val emojiHeaderVal by mainViewModel.userStore.emojiHeader.collectAsStateWithLifecycle(initialValue = "")

    val scope = rememberCoroutineScope()
    val qrColorVal = MaterialTheme.colorScheme.primary.toArgb()
    val qrSecondaryColorVal = MaterialTheme.colorScheme.secondary.toArgb()
    val appContext = LocalContext.current
    
    val isOnboardingCompleteFlow = mainViewModel.userStore.isOnboardingComplete.collectAsStateWithLifecycle(initialValue = null)
    val isOnboardingComplete = isOnboardingCompleteFlow.value
    
    val navigationStack = remember { mutableStateListOf<QuickDashUiState>() }
    
    LaunchedEffect(isOnboardingComplete) {
        if (isOnboardingComplete == false) {
            navigationStack.clear()
            navigationStack.add(QuickDashUiState.Onboarding)
        } else if (isOnboardingComplete == true) {
            if (navigationStack.isEmpty() || navigationStack.contains(QuickDashUiState.Onboarding)) {
                navigationStack.clear()
                navigationStack.add(QuickDashUiState.Dashboard)
            }
        }
    }
    val uiState = when {
        isOnboardingComplete == false -> QuickDashUiState.Onboarding
        navigationStack.isNotEmpty() -> navigationStack.last()
        else -> QuickDashUiState.Dashboard
    }

    var showNotificationPopup by remember { mutableStateOf(false) }
    var showSettingsPopupHoisted by remember { mutableStateOf(false) }
    var backPressedTime by remember { mutableStateOf(0L) }
    androidx.activity.compose.BackHandler {
        if (showSettingsPopupHoisted) {
            showSettingsPopupHoisted = false
        } else if (showNotificationPopup) {
            showNotificationPopup = false
        } else if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.lastIndex)
        } else {
            onDismiss()
        }
    }
    
    var showChatSettings by remember { mutableStateOf(false) }
    var selectingCountry by remember { mutableStateOf(false) }
    var processedShortcut by remember(shortcutAction) { mutableStateOf(shortcutAction) }

    val triggerScanQr = remember {
        {
            com.balajitechlabs.quickdash.features.qr.utils.QrScannerHelper.startScan(
                context = appContext,
                onResult = { raw ->
                    val rawValue = raw.trim().replace(Regex("[\\p{Cc}\\p{Cf}]"), "")
                    val lower = rawValue.lowercase()

                    when {
                        // 1. Wi-Fi QR Code Routing
                        rawValue.startsWith("WIFI:", ignoreCase = true) -> {
                            val ssid = Regex("S:([^;]+)").find(rawValue)?.groupValues?.get(1) ?: "Network"
                            val pass = Regex("P:([^;]+)").find(rawValue)?.groupValues?.get(1) ?: ""
                            val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            cm.setPrimaryClip(android.content.ClipData.newPlainText("Wi-Fi Password", pass))
                            Toast.makeText(appContext, "Wi-Fi: $ssid (Password copied)", Toast.LENGTH_LONG).show()
                            navigationStack.add(QuickDashUiState.Wifi)
                        }

                        // 2. Direct Chat QR Routing (WhatsApp / Telegram)
                        lower.contains("wa.me/") || lower.contains("whatsapp://send") || lower.contains("api.whatsapp.com/") || lower.contains("t.me/") -> {
                            var phone = Regex("[?&]phone=([+0-9]+)").find(rawValue)?.groupValues?.get(1)
                            if (phone == null) {
                                phone = Regex("wa\\.me/([+0-9]+)").find(rawValue)?.groupValues?.get(1)
                            }
                            val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            cm.setPrimaryClip(android.content.ClipData.newPlainText("Scanned Contact", phone ?: rawValue))
                            Toast.makeText(appContext, "Chat Contact Loaded", Toast.LENGTH_SHORT).show()
                            navigationStack.add(QuickDashUiState.WhatsApp)
                        }

                        // 3. Payment / UPI QR Routing
                        rawValue.startsWith("upi://") || rawValue.startsWith("gpay://") || rawValue.startsWith("phonepe://") || rawValue.startsWith("paytmmp://") || rawValue.startsWith("bhim://") -> {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawValue)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                appContext.startActivity(intent)
                            } catch (e: Exception) {
                                val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("Scanned UPI", rawValue))
                                Toast.makeText(appContext, "Failed to launch payment app. Copied UPI link.", Toast.LENGTH_LONG).show()
                            }
                        }

                        // 4. Web URLs
                        rawValue.startsWith("http://") || rawValue.startsWith("https://") -> {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawValue)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                appContext.startActivity(intent)
                            } catch (e: Exception) {
                                val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                cm.setPrimaryClip(android.content.ClipData.newPlainText("Scanned Link", rawValue))
                                Toast.makeText(appContext, "Scanned: $rawValue (Copied)", Toast.LENGTH_LONG).show()
                            }
                        }

                        // 5. Plain Text / Clipboard fallback
                        else -> {
                            val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            cm.setPrimaryClip(android.content.ClipData.newPlainText("Scanned Content", rawValue))
                            Toast.makeText(appContext, "Saved to QuickDash Clipboard", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onError = { err ->
                    Toast.makeText(appContext, err, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    val navigateTo: (QuickDashUiState) -> Unit = { state ->
        if (navigationStack.lastOrNull() != state) {
            navigationStack.add(state)
        }
    }

    val onNavigateToTab: (Int) -> Unit = { index ->
        while (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.lastIndex)
        }
        when (index) {
            0 -> navigationStack.add(QuickDashUiState.Settings)
            1 -> { /* Root is Dashboard */ }
            2 -> navigationStack.add(QuickDashUiState.About)
        }
    }



    // showNotificationPopup is passed in as a parameter

    LaunchedEffect(processedShortcut, isOnboardingComplete) {
        val action = processedShortcut ?: return@LaunchedEffect
        if (action == Intent.ACTION_MAIN || action == "android.intent.action.MAIN") {
            processedShortcut = null
            return@LaunchedEffect
        }
        // Wait for onboarding state to be resolved from DataStore before processing
        if (isOnboardingComplete == null) return@LaunchedEffect
        if (isOnboardingComplete != true) {
            processedShortcut = null
            return@LaunchedEffect
        }
        navigationStack.clear()
        navigationStack.add(QuickDashUiState.Dashboard)
        val targetState = when (action) {
            "com.balajitechlabs.quickdash.ACTION_VIEW_NOTIFICATION" -> {
                showNotificationPopup = true
                QuickDashUiState.Dashboard
            }
            "com.balajitechlabs.quickdash.ACTION_QUICK_UPI", "com.balajitechlabs.quickdash.ACTION_SHOW_QR" -> {
                if (activeIds.isNotEmpty()) {
                    QuickDashUiState.EnterAmount(activeIds, activeDefaultId.ifEmpty { activeIds.first() })
                } else {
                    QuickDashUiState.Setup(isManaging = false)
                }
            }
            "com.balajitechlabs.quickdash.ACTION_SCAN_QR", "scan_qr" -> {
                scope.launch {
                    kotlinx.coroutines.delay(200)
                    triggerScanQr()
                }
                if (activeIds.isNotEmpty()) {
                    QuickDashUiState.EnterAmount(activeIds, activeDefaultId.ifEmpty { activeIds.first() })
                } else {
                    QuickDashUiState.Setup(isManaging = false)
                }
            }
            "com.balajitechlabs.quickdash.ACTION_QUICK_CHAT"       -> QuickDashUiState.WhatsApp
            "com.balajitechlabs.quickdash.ACTION_QUICK_INSTA"      -> QuickDashUiState.Instagram
            "com.balajitechlabs.quickdash.ACTION_QUICK_NOTES"      -> QuickDashUiState.Notes
            "com.balajitechlabs.quickdash.ACTION_QUICK_SEARCH",
            "com.balajitechlabs.quickdash.ACTION_QUICK_WEB"        -> QuickDashUiState.Web
            "com.balajitechlabs.quickdash.ACTION_QUICK_SETTINGS"   -> QuickDashUiState.Settings
            "com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR" -> QuickDashUiState.Calculator
            "com.balajitechlabs.quickdash.ACTION_QUICK_TIMER"      -> QuickDashUiState.Timer
            "com.balajitechlabs.quickdash.ACTION_QUICK_POMODORO"   -> QuickDashUiState.Pomodoro
            "com.balajitechlabs.quickdash.ACTION_QUICK_CLIPBOARD"  -> QuickDashUiState.Clipboard
            "com.balajitechlabs.quickdash.ACTION_QUICK_WIFI"       -> QuickDashUiState.Wifi
            "com.balajitechlabs.quickdash.ACTION_QUICK_CAPTURE"    -> QuickDashUiState.Capture
            "com.balajitechlabs.quickdash.ACTION_QUICK_PASSWORD"   -> QuickDashUiState.Password
            "com.balajitechlabs.quickdash.ACTION_QUICK_QRSCANNER"  -> QuickDashUiState.QrScanner
            "com.balajitechlabs.quickdash.ACTION_QUICK_VOICEMEMOS" -> QuickDashUiState.VoiceMemos
            "com.balajitechlabs.quickdash.ACTION_QUICK_CONVERTER"  -> QuickDashUiState.Converter
            "com.balajitechlabs.quickdash.ACTION_QUICK_TRANSLATOR" -> QuickDashUiState.Translator
            "com.balajitechlabs.quickdash.ACTION_QUICK_REMINDERS"  -> QuickDashUiState.Reminders
            "com.balajitechlabs.quickdash.ACTION_QUICK_CONTACTQR"  -> QuickDashUiState.ContactQr
            else -> QuickDashUiState.Dashboard
        }
        if (targetState != QuickDashUiState.Dashboard) {
            navigationStack.add(targetState)
        }
        processedShortcut = null
    }


    QuickDashContent(
        mainViewModel = mainViewModel,
        uiState = uiState,
        usePaypal = usePaypal,
        onTogglePaypal = { scope.launch { mainViewModel.userStore.saveUsePaypal(it) } },
        isFloating = isFloating,
        recentAmounts = recentAmounts,
        upiIds = activeIds,
        defaultUpiId = activeDefaultId,
        showUpiId = showUpiId,
        themeMode = themeMode,
        dynamicColor = dynamicColor,
        hapticEnabled = mainViewModel.userStore.hapticEnabled.collectAsStateWithLifecycle(initialValue = true).value,
        onToggleDynamicColor = onToggleDynamicColor,
        onChangeThemeMode = onChangeThemeMode,
        payeeName = savedPayeeName,
        showChatSettings = showChatSettings,
        onToggleChatSettings = { showChatSettings = it },
        selectingCountry = selectingCountry,
        onToggleSelectingCountry = { selectingCountry = it },
        defaultPaymentApp = defaultPaymentApp,
        qrHistoryJson = qrHistoryJson,
        onClearQrHistory = { scope.launch { mainViewModel.userStore.clearQrHistory() } },
        onScanQr = triggerScanQr,
        onSaveUpiIds = { ids, name, defaultId ->
            scope.launch {
                if (usePaypal) {
                    mainViewModel.userStore.savePaypalIds(ids)
                    mainViewModel.userStore.saveDefaultPaypalId(defaultId)
                } else {
                    mainViewModel.userStore.saveUpiIds(ids)
                    mainViewModel.userStore.saveDefaultUpiId(defaultId)
                }
                mainViewModel.userStore.savePayeeName(name)
                val wasManaging = (navigationStack.lastOrNull() as? QuickDashUiState.Setup)?.isManaging == true
                if (navigationStack.isNotEmpty() && navigationStack.lastOrNull() is QuickDashUiState.Setup) {
                    navigationStack.removeAt(navigationStack.lastIndex)
                }
                if (!wasManaging && ids.isNotEmpty()) {
                    navigateTo(QuickDashUiState.EnterAmount(ids, defaultId))
                }
            }
        },
        onGenerateQr = { amount, note, selectedId, targetApp, category, useCircularDots, useGradient ->
            if (amount.isNotBlank()) {
                scope.launch { mainViewModel.userStore.saveRecentAmount(amount) }
            }

            val payScheme = targetApp.schemePrefix
            
            // We manually construct the string to prevent Uri.Builder from URL-encoding the '@' symbol in the UPI ID
            var payURL = if (usePaypal) {
                if (amount.isNotBlank()) "https://paypal.me/$selectedId/$amount"
                else "https://paypal.me/$selectedId"
            } else {
                var url = "$payScheme?pa=$selectedId&cu=INR"
                if (amount.isNotBlank()) {
                    url += "&am=$amount"
                }
                url
            }
            if (!usePaypal && !savedPayeeName.isNullOrBlank()) {
                payURL += "&pn=${Uri.encode(savedPayeeName)}"
            }
            if (note.isNotBlank()) {
                payURL += "&tn=${Uri.encode(note)}"
            }

            scope.launch {
                mainViewModel.userStore.saveQrHistoryItem(amount, note, selectedId, targetApp.name, category)
                val bitmap = withContext(Dispatchers.Default) {
                    QRCodeGenerator.generateQRCode(
                        context = appContext,
                        text = payURL,
                        width = 512,
                        height = 512,
                        qrColor = android.graphics.Color.BLACK,
                        centerEmoji = null,
                        qrGradientColors = null,
                        useCircularDots = false,
                        addBrandingFooter = true
                    )
                }
                navigateTo(
                    QuickDashUiState.ShowQr(
                        amount, bitmap, selectedId, savedPayeeName ?: "", payURL
                    )
                )
            }
        },
        onManageUpiIds = { navigateTo(QuickDashUiState.Setup(isManaging = true)) },
        onBackToHome = {
            if (selectingCountry) {
                selectingCountry = false
            } else if (showChatSettings) {
                showChatSettings = false
            } else if (navigationStack.size > 1) {
                navigationStack.removeAt(navigationStack.lastIndex)
            }
        },
        onOpenSettings = { navigateTo(QuickDashUiState.Settings) },
        onToolSelected = { tool ->
            val targetState = when (tool) {
                QuickTool.UPI -> {
                    if (activeIds.isEmpty()) QuickDashUiState.Setup(isManaging = false)
                    else QuickDashUiState.EnterAmount(activeIds, activeDefaultId.ifEmpty { activeIds.first() })
                }
                QuickTool.CHAT, QuickTool.WHATSAPP -> QuickDashUiState.WhatsApp
                QuickTool.INSTAGRAM -> QuickDashUiState.Instagram
                QuickTool.NOTES -> QuickDashUiState.Notes
                QuickTool.SEARCH -> QuickDashUiState.Search
                QuickTool.WIFI -> QuickDashUiState.Wifi
                QuickTool.CLIPBOARD -> QuickDashUiState.Clipboard
                QuickTool.CALCULATOR -> QuickDashUiState.Calculator
                QuickTool.TIMER -> QuickDashUiState.Timer
                QuickTool.CONVERTER -> QuickDashUiState.Converter
                QuickTool.TRANSLATOR -> QuickDashUiState.Translator
                QuickTool.CAPTURE -> QuickDashUiState.Capture
                QuickTool.POMODORO -> QuickDashUiState.Pomodoro
                QuickTool.PASSWORD -> QuickDashUiState.Password
                QuickTool.VOICEMEMOS -> QuickDashUiState.VoiceMemos
                QuickTool.REMINDERS -> QuickDashUiState.Reminders
                QuickTool.QRSCANNER -> QuickDashUiState.QrScanner
                QuickTool.CONTACT_QR -> QuickDashUiState.ContactQr
            }
            navigateTo(targetState)
        },
        onQrShown = onQrShown,
        onRestoreBrightness = onRestoreBrightness,
        onDismiss = onDismiss,
        onGenerateWifiQr = { wifiString ->
            scope.launch {
                val bitmap = withContext(Dispatchers.Default) {
                    QRCodeGenerator.generateQRCode(appContext, wifiString, 1024, 1024)
                }
                navigateTo(
                    QuickDashUiState.ShowQr(
                        amount = "", qrBitmap = bitmap, upiId = "", payeeName = "Wi-Fi Network", payUrl = wifiString
                    )
                )
            }
        },
        onOnboardingComplete = {
            scope.launch {
                val store = mainViewModel.userStore
                store.setOnboardingComplete()
                navigationStack.clear()
                navigationStack.add(QuickDashUiState.Dashboard)
            }
        },
        bubbleEnabled = bubbleEnabled,
        onToggleBubble = { enabled ->
            scope.launch {
                mainViewModel.userStore.setBubbleEnabled(enabled)
            }
        },
        onNavigateToSystemLogs = { navigateTo(QuickDashUiState.SystemLogs) },
        onNavigateTo = { navigateTo(it) },
        onNavigateToTab = onNavigateToTab,
        showNotificationPopup = showNotificationPopup,
        onNavigateToBubbleCustomizer = { navigateTo(QuickDashUiState.BubbleCustomizer) }
    )
}

