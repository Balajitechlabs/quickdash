/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui
 * File: QuickDashContent.kt
 * Description: Main screen content dispatcher routing between dashboard, settings, about, and active floating tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.MainViewModel
import com.balajitechlabs.quickdash.core.services.FloatingBubbleService
import com.balajitechlabs.quickdash.core.ui.components.AppUpdateBottomSheet
import com.balajitechlabs.quickdash.core.ui.components.ConfettiOverlay
import com.balajitechlabs.quickdash.core.ui.components.DownloadingProgressDialog
import com.balajitechlabs.quickdash.core.ui.components.FloatingNavigationDock
import com.balajitechlabs.quickdash.core.ui.components.NotificationPopupDialog
import com.balajitechlabs.quickdash.core.ui.components.QuickDashTipsSheet
import com.balajitechlabs.quickdash.core.ui.components.ReadyToInstallDialog
import com.balajitechlabs.quickdash.core.ui.theme.LocalBorderWidth
import com.balajitechlabs.quickdash.core.ui.theme.LocalShowShadow
import com.balajitechlabs.quickdash.core.utils.UpdateManager
import com.balajitechlabs.quickdash.core.utils.UpdateState
import com.balajitechlabs.quickdash.features.about.presentation.AboutScreen
import com.balajitechlabs.quickdash.features.calculator.presentation.QuickCalculatorScreen
import com.balajitechlabs.quickdash.features.chat.presentation.QuickChatScreen
import com.balajitechlabs.quickdash.features.clipboard.presentation.ClipboardScreen
import com.balajitechlabs.quickdash.features.customizer.presentation.BubbleCustomizerScreen
import com.balajitechlabs.quickdash.features.dashboard.presentation.SpotlightLauncher
import com.balajitechlabs.quickdash.features.insta.presentation.QuickSocialScreen
import com.balajitechlabs.quickdash.features.notes.presentation.QuickNotesScreen
import com.balajitechlabs.quickdash.features.onboarding.presentation.QuickDashWelcomeScreen
import com.balajitechlabs.quickdash.features.password.presentation.QuickPasswordScreen
import com.balajitechlabs.quickdash.features.pomodoro.presentation.QuickPomodoroScreen
import com.balajitechlabs.quickdash.features.qr.presentation.EnterAmountScreen
import com.balajitechlabs.quickdash.features.qr.presentation.PaymentTargetApp
import com.balajitechlabs.quickdash.features.qr.presentation.QuickContactQrScreen
import com.balajitechlabs.quickdash.features.qr.presentation.QuickQrScannerScreen
import com.balajitechlabs.quickdash.features.qr.presentation.SetupScreen
import com.balajitechlabs.quickdash.features.qr.presentation.ShowQrScreen
import com.balajitechlabs.quickdash.features.reminders.presentation.QuickRemindersScreen
import com.balajitechlabs.quickdash.features.search.presentation.QuickSearchScreen
import com.balajitechlabs.quickdash.features.search.presentation.QuickWebScreen
import com.balajitechlabs.quickdash.features.settings.presentation.BlogPostsScreen
import com.balajitechlabs.quickdash.features.settings.presentation.SettingsScreen
import com.balajitechlabs.quickdash.features.settings.presentation.SystemLogsScreen
import com.balajitechlabs.quickdash.features.settings.presentation.dialogs.SettingsPopupDialog
import com.balajitechlabs.quickdash.features.converter.presentation.QuickConverterScreen
import com.balajitechlabs.quickdash.features.timer.presentation.QuickTimerScreen
import com.balajitechlabs.quickdash.features.capture.presentation.QuickCaptureScreen
import com.balajitechlabs.quickdash.features.translator.presentation.QuickTranslatorScreen
import com.balajitechlabs.quickdash.features.voicememos.presentation.QuickVoiceMemosScreen
import com.balajitechlabs.quickdash.features.wifi.presentation.QuickWifiScreen
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

@Composable
fun QuickDashContent(
    mainViewModel: MainViewModel,
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
    val context = LocalContext.current
    val updateState = UpdateManager.updateState

    LaunchedEffect(Unit) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersion = packageInfo.versionName ?: "5.2.3"
            mainViewModel.userStore.saveLastSeenVersion(currentVersion)
        } catch (_: Exception) {}

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
    var settingsConfettiType by remember { mutableStateOf<String?>(null) }
    var settingsConfettiKey by remember { mutableStateOf(0) }
    var showTipsSheet by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
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
            val animatedHeight by animateDpAsState(
                targetValue = targetWindowHeight,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "window_height"
            )

            var dragOffsetY by remember { mutableFloatStateOf(0f) }
            val animatedOffsetY by animateFloatAsState(
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
                        if (isFloating && !isWelcome) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, bottom = 6.dp)
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures(
                                            onDragEnd = {
                                                if (dragOffsetY > 100f) {
                                                    if (Settings.canDrawOverlays(context)) {
                                                        try {
                                                            val sIntent = Intent(context, FloatingBubbleService::class.java)
                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                context.startForegroundService(sIntent)
                                                            } else {
                                                                context.startService(sIntent)
                                                            }
                                                        } catch (_: Exception) {}
                                                        onDismiss()
                                                    } else {
                                                        Toast.makeText(context, "Enable 'Display over other apps' to minimize into a bubble", Toast.LENGTH_LONG).show()
                                                        try {
                                                            val overlayIntent = Intent(
                                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                                Uri.parse("package:${context.packageName}")
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
                                        onToolSelected = onToolSelected,
                                        onScanQr = onScanQr
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
                                    BubbleCustomizerScreen()
                                QuickDashUiState.SystemLogs ->
                                    SystemLogsScreen(onDismiss = onBackToHome)
                                QuickDashUiState.Notes ->
                                    QuickNotesScreen(mainViewModel = mainViewModel, isFloating = isFloating, onDismiss = onBackToHome)
                                QuickDashUiState.Search ->
                                    QuickSearchScreen(mainViewModel = mainViewModel, onDismiss = onBackToHome)
                                QuickDashUiState.Web ->
                                    QuickWebScreen(onClose = onBackToHome)
                                QuickDashUiState.Wifi ->
                                    QuickWifiScreen(isFloating = isFloating, onDismiss = onBackToHome)
                                QuickDashUiState.Clipboard ->
                                    ClipboardScreen(
                                        isFloating = isFloating,
                                        onTriggerConfetti = {
                                            settingsConfettiType = "Default"
                                            settingsConfettiKey++
                                        },
                                        onDismiss = onBackToHome
                                    )
                                QuickDashUiState.Calculator ->
                                    QuickCalculatorScreen(isFloating = isFloating)
                                QuickDashUiState.Timer ->
                                    QuickTimerScreen(isFloating = isFloating)
                                QuickDashUiState.Converter -> QuickConverterScreen()
                                QuickDashUiState.Translator -> QuickTranslatorScreen()
                                QuickDashUiState.Capture -> QuickCaptureScreen(isFloating = isFloating)
                                QuickDashUiState.Pomodoro -> QuickPomodoroScreen(isFloating = isFloating)
                                QuickDashUiState.Password -> QuickPasswordScreen(isFloating = isFloating)
                                QuickDashUiState.VoiceMemos -> QuickVoiceMemosScreen(isFloating = isFloating)
                                QuickDashUiState.Reminders -> QuickRemindersScreen()
                                QuickDashUiState.QrScanner -> QuickQrScannerScreen()
                                QuickDashUiState.Onboarding -> QuickDashWelcomeScreen(
                                    onFinishOnboarding = onOnboardingComplete,
                                    hapticEnabled = hapticEnabled
                                )
                                QuickDashUiState.BlogPosts -> BlogPostsScreen()
                                QuickDashUiState.About -> AboutScreen()
                                QuickDashUiState.ContactQr -> QuickContactQrScreen(isFloating = isFloating, onBack = onBackToHome)
                                else -> {}
                            }
                        }
                    }

                    FloatingNavigationDock(
                        uiState = uiState,
                        updateState = updateState,
                        hapticEnabled = hapticEnabled,
                        onNavigateToTab = onNavigateToTab,
                        onBackToHome = onBackToHome
                    )

                    if (showTipsSheet) {
                        QuickDashTipsSheet(
                            onDismissRequest = { showTipsSheet = false }
                        )
                    }
                }
            }

            if (showSettingsPopup) {
                SettingsPopupDialog(
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
                    onDismiss = { showSettingsPopup = false },
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

            if (showNotificationPopup) {
                NotificationPopupDialog(
                    onDismiss = { onToggleNotificationPopup(false) }
                )
            }

            ConfettiOverlay(
                confettiEnabled = confettiEnabled,
                triggerEmojiConfetti = triggerEmojiConfetti,
                onEmojiConfettiEnded = { triggerEmojiConfetti = false },
                emojiHeaderVal = emojiHeaderVal,
                settingsConfettiType = settingsConfettiType,
                settingsConfettiKey = settingsConfettiKey,
                onSettingsConfettiEnded = { settingsConfettiType = null }
            )

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
