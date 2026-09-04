/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui
 * File: QuickDashApp.kt
 * Description: Root application UI composable, floating navigation dock with dynamic update pill,
 *              fullscreen overlay tools, backstack navigation, and update bottom sheet integration.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui

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

private const val TAG = "QuickDashApp"

sealed interface QuickDashUiState {
    data object Onboarding : QuickDashUiState
    data object Dashboard : QuickDashUiState
    data class Setup(val isManaging: Boolean) : QuickDashUiState
    data class EnterAmount(val upiIds: List<String>, val defaultUpiId: String) : QuickDashUiState
    data class ShowQr(
        val amount: String, val qrBitmap: android.graphics.Bitmap, val upiId: String, val payeeName: String, val payUrl: String
    ) : QuickDashUiState

    data object WhatsApp : QuickDashUiState // Represents Chat in PRD
    data object Instagram : QuickDashUiState // Represents Insta in PRD
    data object Settings : QuickDashUiState
    data object SystemLogs : QuickDashUiState
    data object Notes : QuickDashUiState
    data object Search : QuickDashUiState
    data object Web : QuickDashUiState
    data object Wifi : QuickDashUiState
    data object Hotspot : QuickDashUiState
    data object ApiPanel : QuickDashUiState
    data object Clipboard : QuickDashUiState
    data object Calculator : QuickDashUiState
    data object Timer : QuickDashUiState
    data object Converter : QuickDashUiState
    data object Translator : QuickDashUiState
    data object Capture : QuickDashUiState
    data object FirebaseSetup : QuickDashUiState
    data object BlogPosts : QuickDashUiState
    data object Pomodoro : QuickDashUiState
    data object Password : QuickDashUiState
    data object VoiceMemos : QuickDashUiState
    data object Reminders : QuickDashUiState
    data object QrScanner : QuickDashUiState
    data object About : QuickDashUiState
    data object ContactQr : QuickDashUiState
    data object BubbleCustomizer : QuickDashUiState
}

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
                            android.widget.Toast.makeText(appContext, "Wi-Fi: $ssid (Password copied)", android.widget.Toast.LENGTH_LONG).show()
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
                            android.widget.Toast.makeText(appContext, "Chat Contact Loaded", android.widget.Toast.LENGTH_SHORT).show()
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
                                android.widget.Toast.makeText(appContext, "Failed to launch payment app. Copied UPI link.", android.widget.Toast.LENGTH_LONG).show()
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
                                android.widget.Toast.makeText(appContext, "Scanned: $rawValue (Copied)", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }

                        // 5. Plain Text / Clipboard fallback
                        else -> {
                            val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            cm.setPrimaryClip(android.content.ClipData.newPlainText("Scanned Content", rawValue))
                            android.widget.Toast.makeText(appContext, "Saved to QuickDash Clipboard", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onError = { err ->
                    android.widget.Toast.makeText(appContext, err, android.widget.Toast.LENGTH_SHORT).show()
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
        onToggleNotificationPopup = { showNotificationPopup = it },
        onNavigateToBubbleCustomizer = { navigateTo(QuickDashUiState.BubbleCustomizer) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuickDashContent(
    mainViewModel: com.balajitechlabs.quickdash.MainViewModel,
    uiState: QuickDashUiState,
    usePaypal: Boolean = false,
    onTogglePaypal: (Boolean) -> Unit = {},
    isFloating: Boolean = false,
    recentAmounts: List<String> = emptyList(),
    upiIds: List<String> = emptyList(),
    defaultUpiId: String? = null,
    payeeName: String? = null,
    showUpiId: Boolean = true,
    themeMode: String = "SYSTEM",
    dynamicColor: Boolean = false,
    hapticEnabled: Boolean = true,
    onChangeThemeMode: (String) -> Unit = {},
    onToggleDynamicColor: (Boolean) -> Unit = {},
    showChatSettings: Boolean = false,
    onToggleChatSettings: (Boolean) -> Unit = {},
    selectingCountry: Boolean = false,
    onToggleSelectingCountry: (Boolean) -> Unit = {},
    defaultPaymentApp: String = "ANY",
    qrHistoryJson: String = "[]",
    onClearQrHistory: () -> Unit = {},
    onScanQr: () -> Unit = {},
    onSaveUpiIds: (List<String>, String, String) -> Unit,
    onGenerateQr: (String, String, String, PaymentTargetApp, String, Boolean, Boolean) -> Unit,
    onManageUpiIds: () -> Unit,
    onBackToHome: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onNavigateTo: (QuickDashUiState) -> Unit = {},
    onToolSelected: (QuickTool) -> Unit = {},
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onGenerateWifiQr: (String) -> Unit = {},
    onOnboardingComplete: () -> Unit = {},
    bubbleEnabled: Boolean = true,
    onToggleBubble: (Boolean) -> Unit = {},
    onNavigateToSystemLogs: () -> Unit = {},
    showNotificationPopup: Boolean = false,
    onToggleNotificationPopup: (Boolean) -> Unit = {},
    onNavigateToTab: (Int) -> Unit = {},
    onNavigateToBubbleCustomizer: () -> Unit = {}
) {


    var showSettingsPopup by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val updateState = UpdateManager.updateState


    LaunchedEffect(Unit) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersion = packageInfo.versionName ?: "5.2.3"
            mainViewModel.userStore.saveLastSeenVersion(currentVersion)
        } catch (_: Exception) {}

        // Clipboard auto-clean interval execution check
        val interval = mainViewModel.userStore.clipboardAutocleanInterval.first()
        val customDelay = mainViewModel.userStore.clipboardClearDelay.first()
        val lastClean = mainViewModel.userStore.lastClipboardCleanTime.first()
        val now = System.currentTimeMillis()
        var shouldClean = false
        
        if (interval != "OFF") {
            val intervalMs = when (interval) {
                "1H" -> 3600000L
                "12H" -> 43200000L
                "1D" -> 86400000L
                else -> 0L
            }
            if (intervalMs > 0 && now - lastClean >= intervalMs) {
                shouldClean = true
            }
        }
        if (customDelay > 0 && now - lastClean >= customDelay) {
            shouldClean = true
        }
        if (shouldClean) {
            mainViewModel.userStore.saveClipboardHistory("[]")
            mainViewModel.userStore.saveLastClipboardCleanTime(now)
        }
    }
    val emojiHeaderVal by mainViewModel.userStore.emojiHeader.collectAsStateWithLifecycle(initialValue = "")
    val qrUseEmojiOverlay by mainViewModel.userStore.qrUseEmojiOverlay.collectAsStateWithLifecycle(initialValue = false)
    val confettiEnabled by mainViewModel.userStore.confettiEnabled.collectAsStateWithLifecycle(initialValue = true)
    var triggerEmojiConfetti by remember { mutableStateOf(false) }
    var emojiConfettiKey by remember { mutableStateOf(0) }
    var settingsConfettiType by remember { mutableStateOf<String?>(null) }
    var settingsConfettiKey by remember { mutableStateOf(0) }
    var showTipsSheet by remember { mutableStateOf(false) }

    val rawPostsJson by mainViewModel.userStore.firebaseBlogPosts.collectAsStateWithLifecycle(initialValue = "[]")
    val hiddenJson by mainViewModel.userStore.hiddenNotifications.collectAsStateWithLifecycle(initialValue = "[]")
    val activeNotificationCount = remember(rawPostsJson, hiddenJson) {
        try {
            val gson = com.google.gson.Gson()
            val postsType = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
            val posts: List<Map<String, Any>> = gson.fromJson(rawPostsJson, postsType) ?: emptyList()
            val hiddenType = object : com.google.gson.reflect.TypeToken<Set<String>>() {}.type
            val hidden: Set<String> = gson.fromJson(hiddenJson, hiddenType) ?: emptySet()
            posts.count { post ->
                val ts = (post["timestamp"] as? Number)?.toLong() ?: 0L
                val key = "${ts}_${post["title"]}"
                !hidden.contains(key)
            }
        } catch (_: Exception) { 0 }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val borderWidth = com.balajitechlabs.quickdash.core.ui.theme.LocalBorderWidth.current
            val showShadow = com.balajitechlabs.quickdash.core.ui.theme.LocalShowShadow.current
            val cardShape = MaterialTheme.shapes.medium

            val targetWindowHeight = when (uiState) {
                QuickDashUiState.Dashboard, QuickDashUiState.Settings, QuickDashUiState.About -> 660.dp
                QuickDashUiState.WhatsApp, QuickDashUiState.Instagram -> 490.dp
                QuickDashUiState.Timer, QuickDashUiState.Wifi, QuickDashUiState.Hotspot -> 460.dp
                QuickDashUiState.Calculator -> 520.dp
                QuickDashUiState.Notes, QuickDashUiState.Reminders, QuickDashUiState.Clipboard -> 580.dp
                is QuickDashUiState.ShowQr, is QuickDashUiState.EnterAmount, is QuickDashUiState.Setup -> 540.dp
                QuickDashUiState.Translator, QuickDashUiState.Converter -> 520.dp
                QuickDashUiState.VoiceMemos, QuickDashUiState.Password, QuickDashUiState.Pomodoro -> 500.dp
                else -> 540.dp
            }
            val animatedHeight by androidx.compose.animation.core.animateDpAsState(
                targetValue = targetWindowHeight,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "window_height"
            )

            var dragOffsetY by remember { mutableFloatStateOf(0f) }
            val animatedOffsetY by androidx.compose.animation.core.animateFloatAsState(
                targetValue = dragOffsetY,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "window_drag_offset"
            )

            val isWelcome = uiState == QuickDashUiState.Onboarding
            Surface(
                    modifier = if (isFloating && !isWelcome) {
                        Modifier
                            .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                            .fillMaxWidth()
                            .height(animatedHeight)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                    } else {
                        Modifier.fillMaxSize()
                    },
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    shape = if (isFloating && !isWelcome) RoundedCornerShape(28.dp) else RoundedCornerShape(0.dp),
                    border = if (isFloating && !isWelcome) BorderStroke(1.dp, Color(0xFF38393F)) else null,
                    color = Color(0xFF000000),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (!isFloating || isWelcome) Modifier.statusBarsPadding().navigationBarsPadding() else Modifier)
                                .padding(
                                    start = if (isFloating && !isWelcome) 12.dp else 20.dp,
                                    end = if (isFloating && !isWelcome) 12.dp else 20.dp,
                                    top = if (isFloating && !isWelcome) 6.dp else 12.dp,
                                    bottom = 0.dp
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            // ── Top Drag Capsule & Edge Minimize Handle (EssentialX Floating Window) ──
                            if (isFloating && !isWelcome) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp, bottom = 6.dp)
                                        .pointerInput(Unit) {
                                            detectVerticalDragGestures(
                                                onDragEnd = {
                                                    if (dragOffsetY > 100f) {
                                                        if (android.provider.Settings.canDrawOverlays(context)) {
                                                            try {
                                                                val sIntent = Intent(context, com.balajitechlabs.quickdash.core.services.FloatingBubbleService::class.java)
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                    context.startForegroundService(sIntent)
                                                                } else {
                                                                    context.startService(sIntent)
                                                                }
                                                            } catch (_: Exception) {}
                                                            onDismiss()
                                                        } else {
                                                            android.widget.Toast.makeText(context, "Enable 'Display over other apps' to minimize into a bubble", android.widget.Toast.LENGTH_LONG).show()
                                                            try {
                                                                val overlayIntent = Intent(
                                                                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                                    android.net.Uri.parse("package:${context.packageName}")
                                                                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                                                context.startActivity(overlayIntent)
                                                            } catch (_: Exception) {}
                                                            onDismiss()
                                                        }
                                                    } else {
                                                        dragOffsetY = 0f
                                                    }
                                                },
                                                onDragCancel = { dragOffsetY = 0f },
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(4.dp)
                                            .background(Color(0xFF555860), RoundedCornerShape(2.dp))
                                    )
                                }
                            }

                            val dashboardGridState = rememberLazyGridState()
                            val dashboardListState = rememberLazyListState()

                            AnimatedContent(
                                targetState = uiState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                transitionSpec = {
                                    val isForward = targetState != QuickDashUiState.Dashboard
                                    if (isForward) {
                                        (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)) { width -> width / 4 } + fadeIn(animationSpec = tween(180)))
                                            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)) { width -> -width / 4 } + fadeOut(animationSpec = tween(150)))
                                    } else {
                                        (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)) { width -> -width / 4 } + fadeIn(animationSpec = tween(180)))
                                            .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)) { width -> width / 4 } + fadeOut(animationSpec = tween(150)))
                                    }
                                },
                                label = "quickdash_screen_transition"
                            ) { targetState ->
                                when (targetState) {
                                    QuickDashUiState.Dashboard ->
                                        SpotlightLauncher(
                                            hapticEnabled = hapticEnabled,
                                            isFloating = isFloating,
                                            mainViewModel = mainViewModel,
                                            listState = dashboardListState,
                                            onToolSelected = onToolSelected
                                        )
                                    is QuickDashUiState.Setup ->
                                        SetupScreen(
                                            upiIds = upiIds,
                                            defaultUpiId = defaultUpiId,
                                            payeeName = payeeName,
                                            usePaypal = usePaypal,
                                            onSaveUpiIds = onSaveUpiIds
                                        )
                                    is QuickDashUiState.EnterAmount ->
                                        EnterAmountScreen(
                                            recentAmounts = recentAmounts,
                                            upiIds = upiIds,
                                            defaultUpiId = defaultUpiId ?: "",
                                            defaultPaymentApp = defaultPaymentApp,
                                            usePaypal = usePaypal,
                                            isFloating = isFloating,
                                            qrHistoryJson = qrHistoryJson,
                                            onClearQrHistory = onClearQrHistory,
                                            onScanQr = onScanQr,
                                            onGenerateQr = onGenerateQr,
                                            onManageUpiIds = onManageUpiIds
                                        )
                                    is QuickDashUiState.ShowQr -> {
                                        val state = targetState
                                        val confettiTypeFlow = mainViewModel.userStore.confettiType.collectAsStateWithLifecycle(initialValue = "Default")
                                        val hapticLevelFlow = mainViewModel.userStore.hapticLevel.collectAsStateWithLifecycle(initialValue = "Crisp")
                                        ShowQrScreen(
                                            amount = state.amount,
                                            qrBitmap = state.qrBitmap,
                                            upiId = state.upiId,
                                            payeeName = state.payeeName,
                                            showUpiId = showUpiId,
                                            payUrl = state.payUrl,
                                            usePaypal = usePaypal,
                                            confettiType = confettiTypeFlow.value,
                                            hapticLevel = hapticLevelFlow.value,
                                            isFloating = isFloating,
                                            onQrShown = onQrShown,
                                            onRestoreBrightness = onRestoreBrightness,
                                            onDismiss = onDismiss
                                        )
                                    }
                                    QuickDashUiState.WhatsApp ->
                                        QuickChatScreen(
                                            showSettings = showChatSettings,
                                            onToggleSettings = onToggleChatSettings,
                                            selectingCountry = selectingCountry,
                                            onToggleSelectingCountry = onToggleSelectingCountry,
                                            onDismiss = onDismiss
                                        )
                                    QuickDashUiState.Instagram ->
                                        QuickSocialScreen(
                                            mainViewModel = mainViewModel,
                                            onDismiss = onDismiss
                                        )
                                    QuickDashUiState.Settings ->
                                        SettingsScreen(
                                            themeMode = themeMode,
                                            dynamicColor = dynamicColor,
                                            bubbleEnabled = bubbleEnabled,
                                            onChangeThemeMode = onChangeThemeMode,
                                            onToggleDynamicColor = onToggleDynamicColor,
                                            onToggleBubble = onToggleBubble,
                                            onTriggerConfetti = {
                                                settingsConfettiType = it
                                                settingsConfettiKey++
                                            },
                                            onBackToHome = onBackToHome,
                                            onNavigateToSystemLogs = onNavigateToSystemLogs,
                                            onManageUpiIds = onManageUpiIds,
                                            onNavigateToBubbleCustomizer = onNavigateToBubbleCustomizer
                                        )
                                    QuickDashUiState.BubbleCustomizer ->
                                        com.balajitechlabs.quickdash.features.customizer.presentation.BubbleCustomizerScreen()
                                    QuickDashUiState.SystemLogs ->
                                        SystemLogsScreen(onDismiss = onBackToHome)
                                    QuickDashUiState.Notes ->
                                        QuickNotesScreen(mainViewModel = mainViewModel, isFloating = isFloating, onDismiss = onBackToHome)
                                    QuickDashUiState.Search -> {
                                        QuickSearchScreen(mainViewModel = mainViewModel, onDismiss = onBackToHome)
                                    }
                                    QuickDashUiState.Web -> {
                                        QuickWebScreen(onClose = onBackToHome)
                                    }
                                    QuickDashUiState.Wifi -> {
                                        QuickWifiScreen(
                                            isFloating = isFloating,
                                            onDismiss = onBackToHome
                                        )
                                    }
                                    QuickDashUiState.Clipboard ->
                                        com.balajitechlabs.quickdash.features.clipboard.presentation.ClipboardScreen(
                                            isFloating = isFloating,
                                            onTriggerConfetti = {
                                                settingsConfettiType = "Default"
                                                settingsConfettiKey++
                                            },
                                            onDismiss = onBackToHome
                                        )
                                    QuickDashUiState.Calculator ->
                                        com.balajitechlabs.quickdash.features.calculator.presentation.QuickCalculatorScreen(
                                            isFloating = isFloating
                                        )
                                    QuickDashUiState.Timer ->
                                        com.balajitechlabs.quickdash.features.timer.presentation.QuickTimerScreen(
                                            isFloating = isFloating
                                        )
                                    QuickDashUiState.Converter -> QuickConverterScreen()
                                    QuickDashUiState.Translator -> QuickTranslatorScreen()
                                    QuickDashUiState.Capture -> QuickCaptureScreen(isFloating = isFloating)
                                    QuickDashUiState.Pomodoro -> QuickPomodoroScreen(isFloating = isFloating)
                                    QuickDashUiState.Password -> QuickPasswordScreen(isFloating = isFloating)
                                    QuickDashUiState.VoiceMemos -> QuickVoiceMemosScreen(isFloating = isFloating)
                                    QuickDashUiState.Reminders -> QuickRemindersScreen()
                                    QuickDashUiState.QrScanner -> QuickQrScannerScreen()
                                    QuickDashUiState.Onboarding -> com.balajitechlabs.quickdash.features.onboarding.presentation.QuickDashWelcomeScreen(
                                        onFinishOnboarding = onOnboardingComplete,
                                        hapticEnabled = hapticEnabled
                                    )
                                    QuickDashUiState.BlogPosts -> BlogPostsScreen()
                                    QuickDashUiState.About -> com.balajitechlabs.quickdash.features.about.presentation.AboutScreen()
                                    QuickDashUiState.ContactQr -> com.balajitechlabs.quickdash.features.qr.presentation.QuickContactQrScreen(isFloating = isFloating, onBack = onBackToHome)
                                    else -> {
                                        // Future screen placeholders
                                    }
                                }
                            }
                        }

                        // ── Floating Bottom Navigation Bar with Companion Tip FAB (EssentialX Style) ──
                        if (uiState != QuickDashUiState.Onboarding) {
                            // Translucent gradient dock with progressive hardware blur
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(96.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.45f),
                                                Color.Black.copy(alpha = 0.85f),
                                                Color.Black.copy(alpha = 0.95f)
                                            )
                                        )
                                    )
                                    .then(
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            Modifier.graphicsLayer {
                                                renderEffect = RenderEffect.createBlurEffect(
                                                    20f, 20f, Shader.TileMode.CLAMP
                                                ).asComposeRenderEffect()
                                            }
                                        } else Modifier
                                    )
                                    .zIndex(8f)
                            )

                            val isTopLevelScreen = uiState == QuickDashUiState.Dashboard || 
                                                    uiState == QuickDashUiState.Settings || 
                                                    uiState == QuickDashUiState.About

                            if (isTopLevelScreen) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp)
                                        .zIndex(10f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    com.balajitechlabs.quickdash.core.ui.components.EssentialsFloatingToolbar(
                                        selectedTab = when (uiState) {
                                            QuickDashUiState.Settings -> 0
                                            QuickDashUiState.About -> 2
                                            else -> 1
                                        },
                                        onSelectTab = onNavigateToTab
                                    )

                                    // ── Floating Update Pill on Right (ONLY visible when update is available) ──
                                    val isUpdateAvailable = updateState is UpdateState.UpdateAvailable ||
                                                            updateState is UpdateState.Downloading ||
                                                            updateState is UpdateState.ReadyToInstall

                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = isUpdateAvailable,
                                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                                    ) {
                                         val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "update_logo_pulse")
                                         val pulseScale by infiniteTransition.animateFloat(
                                             initialValue = 1f,
                                             targetValue = 1.15f,
                                             animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                                 animation = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                                 repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                                             ),
                                             label = "pulse_scale"
                                         )
                                         val downloadRotation by infiniteTransition.animateFloat(
                                             initialValue = 0f,
                                             targetValue = 360f,
                                             animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                                 animation = androidx.compose.animation.core.tween(1200, easing = androidx.compose.animation.core.LinearEasing),
                                                 repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                                             ),
                                             label = "download_spin"
                                         )

                                         Surface(
                                             onClick = {
                                                 playClickVibration(context, hapticEnabled)
                                                 val current = UpdateManager.updateState
                                                 if (current is UpdateState.ReadyToInstall) {
                                                     UpdateManager.installApk(context, current.fileName)
                                                 } else if (current is UpdateState.UpdateAvailable) {
                                                     UpdateManager.showUpdateSheet = true
                                                 }
                                             },
                                             shape = CircleShape,
                                             color = Color(0xFF38393F),
                                             border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.7f)),
                                             shadowElevation = 8.dp,
                                             tonalElevation = 0.dp,
                                             modifier = Modifier.size(52.dp)
                                         ) {
                                             Box(
                                                 modifier = Modifier
                                                     .fillMaxSize()
                                                     .padding(6.dp),
                                                 contentAlignment = Alignment.Center
                                             ) {
                                                 Box(
                                                     modifier = Modifier
                                                         .fillMaxSize()
                                                         .graphicsLayer {
                                                             if (updateState is UpdateState.Downloading) {
                                                                 rotationZ = downloadRotation
                                                             } else {
                                                                 scaleX = pulseScale
                                                                 scaleY = pulseScale
                                                             }
                                                         }
                                                         .clip(CircleShape)
                                                         .background(Color(0xFF000000))
                                                         .border(1.dp, Color(0xFF44474F).copy(alpha = 0.6f), CircleShape),
                                                     contentAlignment = Alignment.Center
                                                 ) {
                                                     Icon(
                                                         imageVector = when (updateState) {
                                                             is UpdateState.ReadyToInstall -> Icons.Rounded.CheckCircle
                                                             is UpdateState.Downloading -> Icons.Rounded.CloudDownload
                                                             else -> Icons.Rounded.Download
                                                         },
                                                         contentDescription = "Update Available",
                                                         tint = Color.White,
                                                         modifier = Modifier.size(22.dp)
                                                     )
                                                 }
                                             }
                                         }
                                    }
                                }
                            } else {
                                // ── Floating Back Pill for Sub-Tools (EssentialX Style) ──
                                Surface(
                                    onClick = {
                                        playClickVibration(context, hapticEnabled)
                                        onBackToHome()
                                    },
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color(0xFF38393F),
                                    shadowElevation = 8.dp,
                                    tonalElevation = 0.dp,
                                    border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 14.dp)
                                        .height(46.dp)
                                        .zIndex(10f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Back",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        if (showTipsSheet) {
                            com.balajitechlabs.quickdash.core.ui.components.QuickDashTipsSheet(
                                onDismissRequest = { showTipsSheet = false }
                            )
                        }
                    }
                }

                // ─── Settings Popup Dialog ────────────────────────────────────────
        if (showSettingsPopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { showSettingsPopup = false },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.90f)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { /* consume clicks inside */ },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Popup Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { showSettingsPopup = false }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "✕",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        // Settings content
                        Box(modifier = Modifier.weight(1f)) {
                            com.balajitechlabs.quickdash.features.settings.presentation.SettingsScreen(
                                themeMode = themeMode,
                                dynamicColor = dynamicColor,
                                bubbleEnabled = bubbleEnabled,
                                onChangeThemeMode = onChangeThemeMode,
                                onToggleDynamicColor = onToggleDynamicColor,
                                onToggleBubble = onToggleBubble,
                                onTriggerConfetti = {
                                    settingsConfettiType = it
                                    settingsConfettiKey++
                                },
                                onBackToHome = { showSettingsPopup = false },
                                onNavigateToSystemLogs = {
                                    showSettingsPopup = false
                                    onNavigateToSystemLogs()
                                },
                                onManageUpiIds = {
                                    showSettingsPopup = false
                                    onManageUpiIds()
                                },
                                onNavigateToBubbleCustomizer = {
                                    showSettingsPopup = false
                                    onNavigateToBubbleCustomizer()
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showNotificationPopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { onToggleNotificationPopup(false) },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.82f)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { /* consume clicks inside popup */ },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { onToggleNotificationPopup(false) }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = "✕",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        // Notifications list
                        Box(modifier = Modifier.weight(1f)) {
                            BlogPostsScreen()
                        }
                    }
                }
            }
        }


        if (triggerEmojiConfetti && confettiEnabled) {
            val context = LocalContext.current
            val emojiDrawable = remember(emojiHeaderVal) {
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 48f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val bounds = android.graphics.Rect()
                paint.getTextBounds(emojiHeaderVal, 0, emojiHeaderVal.length, bounds)
                val width = (bounds.width() + 10).coerceAtLeast(64)
                val height = (bounds.height() + 10).coerceAtLeast(64)
                val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                val x = width / 2f
                val y = height / 2f - (paint.descent() + paint.ascent()) / 2f
                canvas.drawText(emojiHeaderVal, x, y, paint)
                android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
            }
            
            val party = nl.dionsegijn.konfetti.core.Party(
                speed = 10f,
                maxSpeed = 30f,
                damping = 0.9f,
                angle = 0,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                shapes = listOf(nl.dionsegijn.konfetti.core.models.Shape.Circle, nl.dionsegijn.konfetti.core.models.Shape.Square),
                emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 500, java.util.concurrent.TimeUnit.MILLISECONDS).max(50),
                position = nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.3)
            )
            
            nl.dionsegijn.konfetti.compose.KonfettiView(
                modifier = Modifier.fillMaxSize().zIndex(200f),
                parties = listOf(party),
                updateListener = object : nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(system: nl.dionsegijn.konfetti.core.PartySystem, activeSystems: Int) {
                        if (activeSystems == 0) {
                            triggerEmojiConfetti = false
                        }
                    }
                }
            )
        }

        if (settingsConfettiType != null && confettiEnabled) {
            key(settingsConfettiKey) {
                val type = settingsConfettiType
                val partyList = when (type) {
                    "Right" -> listOf(
                        nl.dionsegijn.konfetti.core.Party(
                            speed = 25f,
                            maxSpeed = 45f,
                            damping = 0.9f,
                            angle = 180,
                            spread = 60,
                            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                            size = listOf(nl.dionsegijn.konfetti.core.models.Size(32, 6f), nl.dionsegijn.konfetti.core.models.Size(42, 8f)),
                            emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 300, java.util.concurrent.TimeUnit.MILLISECONDS).max(100),
                            position = nl.dionsegijn.konfetti.core.Position.Relative(1.0, 0.5)
                        )
                    )
                    "Corner" -> listOf(
                        nl.dionsegijn.konfetti.core.Party(
                            speed = 25f,
                            maxSpeed = 40f,
                            damping = 0.9f,
                            angle = -45,
                            spread = 40,
                            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                            size = listOf(nl.dionsegijn.konfetti.core.models.Size(32, 6f), nl.dionsegijn.konfetti.core.models.Size(42, 8f)),
                            emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 100, java.util.concurrent.TimeUnit.MILLISECONDS).max(80),
                            position = nl.dionsegijn.konfetti.core.Position.Relative(0.0, 0.8)
                        ),
                        nl.dionsegijn.konfetti.core.Party(
                            speed = 25f,
                            maxSpeed = 40f,
                            damping = 0.9f,
                            angle = -135,
                            spread = 40,
                            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                            size = listOf(nl.dionsegijn.konfetti.core.models.Size(32, 6f), nl.dionsegijn.konfetti.core.models.Size(42, 8f)),
                            emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 100, java.util.concurrent.TimeUnit.MILLISECONDS).max(80),
                            position = nl.dionsegijn.konfetti.core.Position.Relative(1.0, 0.8)
                        )
                    )
                    "Export" -> listOf(
                        nl.dionsegijn.konfetti.core.Party(
                            speed = 5f,
                            maxSpeed = 25f,
                            damping = 0.9f,
                            angle = 90,
                            spread = 80,
                            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                            size = listOf(nl.dionsegijn.konfetti.core.models.Size(32, 6f), nl.dionsegijn.konfetti.core.models.Size(42, 8f)),
                            emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 1000, java.util.concurrent.TimeUnit.MILLISECONDS).max(100),
                            position = nl.dionsegijn.konfetti.core.Position.Relative(0.0, 0.0).between(nl.dionsegijn.konfetti.core.Position.Relative(1.0, 0.0))
                        )
                    )
                    else -> listOf(
                        nl.dionsegijn.konfetti.core.Party(
                            speed = 0f,
                            maxSpeed = 30f,
                            damping = 0.9f,
                            angle = 0,
                            spread = 360,
                            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                            size = listOf(nl.dionsegijn.konfetti.core.models.Size(32, 6f), nl.dionsegijn.konfetti.core.models.Size(42, 8f)),
                            emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 200, java.util.concurrent.TimeUnit.MILLISECONDS).max(100),
                            position = nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.5)
                        )
                    )
                }

        nl.dionsegijn.konfetti.compose.KonfettiView(
            modifier = Modifier.fillMaxSize().zIndex(300f),
            parties = partyList,
            updateListener = object : nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener {
                override fun onParticleSystemEnded(system: nl.dionsegijn.konfetti.core.PartySystem, activeSystems: Int) {
                    if (activeSystems == 0) {
                        settingsConfettiType = null
                    }
                }
            }
        )
    }
}

    if (UpdateManager.showUpdateSheet && updateState is UpdateState.UpdateAvailable) {
        AppUpdateBottomSheet(
            updateState = updateState,
            onDismissRequest = { UpdateManager.showUpdateSheet = false },
            onStartDownload = {
                UpdateManager.showUpdateSheet = false
                UpdateManager.startDownload(context, updateState.apkUrl, updateState.versionName, updateState.sha256)
            }
        )
    }

    if (updateState is UpdateState.Downloading) {
        DownloadingProgressDialog(state = updateState)
    }

    if (updateState is UpdateState.ReadyToInstall) {
        ReadyToInstallDialog(
            state = updateState,
            onInstall = { UpdateManager.installApk(context, updateState.fileName) },
            onDismiss = {
                UpdateManager.dismissInstall()
            }
        )
    }
}
}
}

@Composable


fun UpdateTag(onShowUpdateDialog: () -> Unit = {}) {
    val context = LocalContext.current
    val currentVersionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${packageInfo.versionName ?: com.balajitechlabs.quickdash.BuildConfig.VERSION_NAME}"
        } catch (e: Exception) {
            "v${com.balajitechlabs.quickdash.BuildConfig.VERSION_NAME}"
        }
    }

    val state = UpdateManager.updateState
    val hasLocalApk = UpdateManager.hasLocalApk

    when (state) {
        is UpdateState.Idle,
        is UpdateState.UpToDate -> {
            if (hasLocalApk) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            UpdateManager.deleteDownloadedApks(context)
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "APK",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "Delete APKs",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            UpdateManager.checkForUpdates(context, manual = true)
                        }
                ) {
                    Text(
                        text = currentVersionName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        UpdateState.Checking -> {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Checking…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        is UpdateState.Error -> {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        UpdateManager.checkForUpdates(context, manual = true)
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Retry ↺",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        is UpdateState.UpdateAvailable -> {
            Surface(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onShowUpdateDialog()
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "v${state.versionName}",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_cloud_download),
                        contentDescription = "Update Available",
                        tint = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        is UpdateState.Downloading -> {
            Surface(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "v${state.versionName}",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CircularProgressIndicator(
                        progress = { state.progress / 100f },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        is UpdateState.ReadyToInstall -> {
            Surface(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        UpdateManager.installApk(context, state.fileName)
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "v${state.versionName}",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle_fill),
                        contentDescription = "Install Update",
                        tint = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentModeSwitcherButton(
    usePaypal: Boolean,
    onTogglePaypal: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val userStore = remember { UserStore(context) }
    val hapticEnabled by userStore.hapticEnabled.collectAsStateWithLifecycle(initialValue = true)

    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
        modifier = Modifier
            .size(width = 44.dp, height = 32.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .clickable {
                playClickVibration(context, hapticEnabled)
                onTogglePaypal(!usePaypal)
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            AnimatedContent(
                targetState = usePaypal,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                },
                label = "paymentModeTransition"
            ) { activePaypal ->
                Icon(
                    painter = painterResource(if (activePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay),
                    contentDescription = "Switch Payment Mode",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

fun playClickVibration(context: Context, hapticEnabled: Boolean) {
    if (!hapticEnabled) return
    try {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            (context.getSystemService(android.os.VibratorManager::class.java))?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && vibrator.areAllPrimitivesSupported(android.os.VibrationEffect.Composition.PRIMITIVE_TICK)) {
                vibrator.vibrate(
                    android.os.VibrationEffect.startComposition()
                        .addPrimitive(android.os.VibrationEffect.Composition.PRIMITIVE_TICK, 0.8f)
                        .compose()
                )
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                vibrator.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(12L)
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to play click vibration", e)
    }
}

fun playExplosionVibration(context: Context, hapticEnabled: Boolean) {
    if (!hapticEnabled) return
    try {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            (context.getSystemService(android.os.VibratorManager::class.java))?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                vibrator.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 15, 80, 20), -1)
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to play explosion vibration", e)
    }
}

@Composable
private fun NavigationPillItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
        label = "navPillColor"
    )
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (badgeCount > 0) {
                BadgedBox(badge = { Badge { Text(badgeCount.toString()) } }) {
                    Icon(imageVector = icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(20.dp))
                }
            } else {
                Icon(imageVector = icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(20.dp))
            }
            if (selected) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
            }
        }
    }
}

