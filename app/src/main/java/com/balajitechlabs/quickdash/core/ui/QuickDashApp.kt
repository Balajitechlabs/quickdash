package com.balajitechlabs.quickdash.core.ui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
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
import com.balajitechlabs.quickdash.features.dashboard.presentation.DashboardScreen
import com.balajitechlabs.quickdash.features.settings.presentation.BlogPostsScreen
import com.balajitechlabs.quickdash.features.qr.presentation.EnterAmountScreen
import com.balajitechlabs.quickdash.features.chat.presentation.QuickChatScreen
import com.balajitechlabs.quickdash.features.insta.presentation.QuickSocialScreen
import com.balajitechlabs.quickdash.features.notes.presentation.QuickNotesScreen
import com.balajitechlabs.quickdash.features.search.presentation.QuickSearchScreen
import com.balajitechlabs.quickdash.features.wifi.presentation.QuickWifiScreen
import com.balajitechlabs.quickdash.features.dashboard.presentation.QuickTool
import com.balajitechlabs.quickdash.features.settings.presentation.SystemLogsScreen
import com.balajitechlabs.quickdash.features.qr.presentation.SetupScreen
import com.balajitechlabs.quickdash.features.qr.presentation.ShowQrScreen
import com.balajitechlabs.quickdash.features.qr.presentation.PaymentTargetApp
import com.balajitechlabs.quickdash.features.settings.presentation.SettingsScreen
import com.balajitechlabs.quickdash.features.onboarding.presentation.OnboardingScreen
import com.balajitechlabs.quickdash.core.utils.QRCodeGenerator
import com.balajitechlabs.quickdash.core.utils.UpdateManager
import com.balajitechlabs.quickdash.core.utils.UpdateState
import com.balajitechlabs.quickdash.core.ui.components.WhatsNewDialog
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

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
    data object Wifi : QuickDashUiState
    data object Hotspot : QuickDashUiState
    data object ApiPanel : QuickDashUiState
    data object Clipboard : QuickDashUiState
    data object Calculator : QuickDashUiState
    data object Timer : QuickDashUiState
    data object FirebaseSetup : QuickDashUiState
    data object BlogPosts : QuickDashUiState
}

@Composable
fun QuickDashApp(
    userStore: UserStore,
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
    onDismiss: () -> Unit = {},
    onConvertToFullScreen: (() -> Unit)? = null
) {
    val savedUpiIds by userStore.upiIds.collectAsState(initial = emptyList())
    val defaultPaymentApp by userStore.defaultPaymentApp.collectAsState(initial = "ANY")
    val qrHistoryJson by userStore.qrHistory.collectAsState(initial = "[]")
    
    // Choose active IDs based on mode
    val activeIds = savedUpiIds
    
    val savedDefaultUpiId by userStore.defaultUpiId.collectAsState(initial = null)
    val activeDefaultId = savedDefaultUpiId ?: savedUpiIds.firstOrNull() ?: ""

    val savedPayeeName by userStore.payeeName.collectAsState(initial = null)
    val recentAmounts by userStore.recentAmounts.collectAsState(initial = emptyList())
    val showUpiId by userStore.showUpiId.collectAsState(initial = true)
    
    val bubbleEnabled by userStore.bubbleEnabled.collectAsState(initial = true)
    val emojiHeaderVal by userStore.emojiHeader.collectAsState(initial = "🚀")

    val scope = rememberCoroutineScope()
    val qrColorVal = MaterialTheme.colorScheme.primary.toArgb()
    val qrSecondaryColorVal = MaterialTheme.colorScheme.secondary.toArgb()
    val appContext = LocalContext.current
    
    val navigationStack = remember { mutableStateListOf<QuickDashUiState>(QuickDashUiState.Dashboard) }
    val uiState = navigationStack.lastOrNull() ?: QuickDashUiState.Dashboard

    var backPressedTime by remember { mutableStateOf(0L) }
    androidx.activity.compose.BackHandler {
        if (navigationStack.size > 1) {
            navigationStack.removeLast()
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressedTime < 2000) {
                (appContext as? android.app.Activity)?.finishAndRemoveTask()
            } else {
                backPressedTime = currentTime
                android.widget.Toast.makeText(appContext, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    var showChatSettings by remember { mutableStateOf(false) }
    var selectingCountry by remember { mutableStateOf(false) }
    var processedShortcut by remember(shortcutAction) { mutableStateOf(shortcutAction) }

    val barcodeScanner = remember { com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(appContext) }
    val triggerScanQr = remember {
        {
            barcodeScanner.startScan()
                .addOnSuccessListener { barcode ->
                    var rawValue = (barcode.rawValue ?: "").trim()
                    rawValue = rawValue.replace(Regex("[\\p{Cc}\\p{Cf}]"), "")
                    if (rawValue.startsWith("upi://") || rawValue.startsWith("gpay://") || rawValue.startsWith("phonepe://") || rawValue.startsWith("paytmmp://") || rawValue.startsWith("bhim://")) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawValue)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            appContext.startActivity(intent)
                        } catch (e: Exception) {
                            val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            cm.setPrimaryClip(android.content.ClipData.newPlainText("Scanned UPI", rawValue))
                            android.widget.Toast.makeText(appContext, "Failed to launch payment app. Copied link.", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } else if (rawValue.startsWith("http://") || rawValue.startsWith("https://")) {
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
                    } else {
                        val cm = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("Scanned QR", rawValue))
                        android.widget.Toast.makeText(appContext, "Scanned: $rawValue (Copied)", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    android.widget.Toast.makeText(appContext, "Scan cancelled or failed", android.widget.Toast.LENGTH_SHORT).show()
                }
            Unit
        }
    }

    val navigateTo: (QuickDashUiState) -> Unit = { state ->
        if (navigationStack.lastOrNull() != state) {
            navigationStack.add(state)
        }
    }

    LaunchedEffect(Unit) {
        val isOnboardingComplete = userStore.isOnboardingComplete.first()
        if (!isOnboardingComplete) {
            navigationStack.clear()
            navigationStack.add(QuickDashUiState.Onboarding)
        }
    }

    var showNotificationPreview by remember { mutableStateOf(false) }

    LaunchedEffect(processedShortcut) {
        val action = processedShortcut ?: return@LaunchedEffect
        navigationStack.clear()
        navigationStack.add(QuickDashUiState.Dashboard)
        val targetState = when (action) {
            "com.balajitechlabs.quickdash.ACTION_VIEW_NOTIFICATION" -> {
                showNotificationPreview = true
                QuickDashUiState.Dashboard
            }
            "com.balajitechlabs.quickdash.ACTION_QUICK_UPI", "com.balajitechlabs.quickdash.ACTION_SHOW_QR" -> {
                if (activeIds.isNotEmpty()) {
                    QuickDashUiState.EnterAmount(activeIds, activeDefaultId ?: activeIds.first())
                } else {
                    QuickDashUiState.Setup(isManaging = false)
                }
            }
            "com.balajitechlabs.quickdash.ACTION_SCAN_QR" -> {
                triggerScanQr()
                QuickDashUiState.Dashboard
            }
            "com.balajitechlabs.quickdash.ACTION_QUICK_CHAT" -> QuickDashUiState.WhatsApp
            "com.balajitechlabs.quickdash.ACTION_QUICK_INSTA" -> QuickDashUiState.Instagram
            "com.balajitechlabs.quickdash.ACTION_QUICK_NOTES" -> QuickDashUiState.Notes
            "com.balajitechlabs.quickdash.ACTION_QUICK_SEARCH" -> QuickDashUiState.Search
            "com.balajitechlabs.quickdash.ACTION_QUICK_SETTINGS" -> QuickDashUiState.Settings
            "com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR" -> QuickDashUiState.Calculator
            "com.balajitechlabs.quickdash.ACTION_QUICK_TIMER" -> QuickDashUiState.Timer
            "com.balajitechlabs.quickdash.ACTION_QUICK_CLIPBOARD" -> QuickDashUiState.Clipboard
            "com.balajitechlabs.quickdash.ACTION_QUICK_WIFI" -> QuickDashUiState.Wifi
            else -> QuickDashUiState.Dashboard
        }
        if (targetState != QuickDashUiState.Dashboard) {
            navigationStack.add(targetState)
        }
        processedShortcut = null
    }


    QuickDashContent(
        userStore = userStore,
        uiState = uiState,
        isFloating = isFloating,
        recentAmounts = recentAmounts,
        upiIds = activeIds,
        defaultUpiId = activeDefaultId,
        showUpiId = showUpiId,
        themeMode = themeMode,
        dynamicColor = dynamicColor,
        hapticEnabled = userStore.hapticEnabled.collectAsState(initial = true).value,
        onToggleDynamicColor = onToggleDynamicColor,
        onChangeThemeMode = onChangeThemeMode,
        payeeName = savedPayeeName,
        showChatSettings = showChatSettings,
        onToggleChatSettings = { showChatSettings = it },
        selectingCountry = selectingCountry,
        onToggleSelectingCountry = { selectingCountry = it },
        defaultPaymentApp = defaultPaymentApp,
        qrHistoryJson = qrHistoryJson,
        onClearQrHistory = { scope.launch { userStore.clearQrHistory() } },
        onScanQr = triggerScanQr,
        onSaveUpiIds = { ids, name, defaultId ->
            scope.launch {
                userStore.saveUpiIds(ids)
                userStore.saveDefaultUpiId(defaultId)
                userStore.savePayeeName(name)
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
                scope.launch { userStore.saveRecentAmount(amount) }
            }

            val payScheme = targetApp.schemePrefix
            val uriBuilder = Uri.parse(payScheme).buildUpon()
                .appendQueryParameter("pa", selectedId)
                .apply {
                    if (amount.isNotBlank()) {
                        appendQueryParameter("am", amount)
                    }
                }.appendQueryParameter("cu", "INR")

            if (!savedPayeeName.isNullOrBlank()) {
                uriBuilder.appendQueryParameter("pn", savedPayeeName)
            }
            if (note.isNotBlank()) {
                uriBuilder.appendQueryParameter("tn", note)
            }
            val payURL = uriBuilder.build().toString()

            scope.launch {
                userStore.saveQrHistoryItem(amount, note, selectedId, targetApp.name, category)
                val bitmap = withContext(Dispatchers.Default) {
                    val useEmoji = userStore.qrUseEmojiOverlay.first()
                    QRCodeGenerator.generateQRCode(
                        context = appContext,
                        text = payURL,
                        width = 1024,
                        height = 1024,
                        qrColor = qrColorVal,
                        centerEmoji = if (useEmoji) emojiHeaderVal else null,
                        qrGradientColors = if (useGradient) Pair(qrColorVal, qrSecondaryColorVal) else null,
                        useCircularDots = useCircularDots
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
                    else QuickDashUiState.EnterAmount(activeIds, activeDefaultId ?: activeIds.first())
                }
                QuickTool.WHATSAPP -> QuickDashUiState.WhatsApp
                QuickTool.INSTAGRAM -> QuickDashUiState.Instagram
                QuickTool.NOTES -> QuickDashUiState.Notes
                QuickTool.SEARCH -> QuickDashUiState.Search
                QuickTool.WIFI -> QuickDashUiState.Wifi
                QuickTool.CLIPBOARD -> QuickDashUiState.Clipboard
                QuickTool.CALCULATOR -> QuickDashUiState.Calculator
                QuickTool.TIMER -> QuickDashUiState.Timer
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
                userStore.setOnboardingComplete()
                navigationStack.removeLast()
                navigationStack.add(QuickDashUiState.Dashboard)
            }
        },
        bubbleEnabled = bubbleEnabled,
        onToggleBubble = { enabled ->
            scope.launch {
                userStore.setBubbleEnabled(enabled)
            }
        },
        onConvertToFullScreen = onConvertToFullScreen,
        onNavigateToSystemLogs = { navigateTo(QuickDashUiState.SystemLogs) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickDashContent(
    userStore: UserStore,
    uiState: QuickDashUiState,
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
    onToolSelected: (QuickTool) -> Unit = {},
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onGenerateWifiQr: (String) -> Unit = {},
    onOnboardingComplete: () -> Unit = {},
    bubbleEnabled: Boolean = true,
    onToggleBubble: (Boolean) -> Unit = {},
    onConvertToFullScreen: (() -> Unit)? = null,
    onNavigateToSystemLogs: () -> Unit = {}
) {
    if (uiState is QuickDashUiState.Onboarding) {
        OnboardingScreen(userStore = userStore, onComplete = onOnboardingComplete)
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val blogDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showWhatsNewOnLaunch by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val lastSeen = userStore.lastSeenVersion.first()
        val currentVersion = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "2.2.6"
        } catch (e: Exception) {
            "2.2.6"
        }
        if (lastSeen != currentVersion) {
            showWhatsNewOnLaunch = true
        }

        // Clipboard auto-clean interval execution check
        val interval = userStore.clipboardAutocleanInterval.first()
        val customDelay = userStore.clipboardClearDelay.first()
        val lastClean = userStore.lastClipboardCleanTime.first()
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
            userStore.saveClipboardHistory("[]")
            userStore.saveLastClipboardCleanTime(now)
        }
    }
    val emojiHeaderVal by userStore.emojiHeader.collectAsState(initial = "🚀")
    val qrUseEmojiOverlay by userStore.qrUseEmojiOverlay.collectAsState(initial = false)
    val confettiEnabled by userStore.confettiEnabled.collectAsState(initial = true)
    var triggerEmojiConfetti by remember { mutableStateOf(false) }
    var emojiConfettiKey by remember { mutableStateOf(0) }
    var settingsConfettiType by remember { mutableStateOf<String?>(null) }
    var settingsConfettiKey by remember { mutableStateOf(0) }
    var showFullScreenPrompt by remember { mutableStateOf(false) }

    val rawPostsJson by userStore.firebaseBlogPosts.collectAsState(initial = "[]")
    val hiddenJson by userStore.hiddenNotifications.collectAsState(initial = "[]")
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
        ModalNavigationDrawer(
            drawerState = blogDrawerState,
            gesturesEnabled = drawerState.isClosed,
            scrimColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxHeight().width(320.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                ) {
                    BlogPostsScreen(userStore = userStore)
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            scrimColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(320.dp),
                        drawerContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        drawerShape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            MaterialTheme.colorScheme.surface
                                        )
                                    )
                                )
                        ) {
                            com.balajitechlabs.quickdash.features.settings.presentation.SettingsScreen(
                                userStore = userStore,
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
                                onBackToHome = { scope.launch { drawerState.close() } },
                                onNavigateToSystemLogs = {
                                    scope.launch { drawerState.close() }
                                    onNavigateToSystemLogs()
                                },
                                onManageUpiIds = {
                                    scope.launch { drawerState.close() }
                                    onManageUpiIds()
                                }
                            )
                        }
                    }
                }
            }
        ) {
        val borderWidth = com.balajitechlabs.quickdash.core.ui.theme.LocalBorderWidth.current
        val showShadow = com.balajitechlabs.quickdash.core.ui.theme.LocalShowShadow.current
        val cardShape = MaterialTheme.shapes.medium

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = if (isFloating) Modifier.fillMaxWidth().wrapContentHeight().animateContentSize() else Modifier.fillMaxSize()
            ) {
                Surface(
                    modifier = if (isFloating) {
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .then(if (showShadow) Modifier.shadow(8.dp, cardShape) else Modifier)
                            .blur(if (drawerState.isOpen) 12.dp else 0.dp)
                    } else {
                        Modifier.fillMaxSize().blur(if (drawerState.isOpen) 12.dp else 0.dp)
                    },
            tonalElevation = 6.dp,
            shadowElevation = if (showShadow) 8.dp else 0.dp,
            shape = if (isFloating) cardShape else RoundedCornerShape(0.dp),
            border = if (isFloating) BorderStroke(borderWidth.dp, MaterialTheme.colorScheme.outlineVariant) else null,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(
                        start = if (isFloating) 12.dp else 24.dp,
                        end = if (isFloating) 12.dp else 24.dp,
                        top = if (isFloating) 8.dp else 12.dp,
                        bottom = if (isFloating) 8.dp else 16.dp
                    )
                    .fillMaxWidth()
                    .then(if (isFloating) Modifier.fillMaxHeight() else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // ── Top Bar (M3 TopAppBar style) ──────────────────
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.align(Alignment.CenterStart).wrapContentWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (uiState != QuickDashUiState.Dashboard) {
                            FilledTonalIconButton(
                                onClick = onBackToHome,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_back),
                                    contentDescription = "Back",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { scope.launch { blogDrawerState.open() } },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (activeNotificationCount > 0) {
                                                Badge {
                                                    Text(activeNotificationCount.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Notifications",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                UpdateTag()
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 52.dp)
                    ) {
                        Text(
                            text = when (uiState) {
                                QuickDashUiState.Onboarding -> "Welcome to QuickDash"
                                QuickDashUiState.WhatsApp -> if (selectingCountry) "Select Country" else if (showChatSettings) "Chat Settings" else "Quick Chat"
                                QuickDashUiState.Instagram -> "Quick Social Access"
                                QuickDashUiState.Settings -> "Settings"
                                QuickDashUiState.SystemLogs -> "System Logs"
                                QuickDashUiState.Notes -> "Quick Notes"
                                QuickDashUiState.Search -> "Quick Search"
                                QuickDashUiState.Wifi -> "Quick Wi-Fi"
                                QuickDashUiState.Clipboard -> "Clipboard"
                                QuickDashUiState.Calculator -> "Calculator"
                                QuickDashUiState.Timer -> "Timer"
                                is QuickDashUiState.Setup,
                                is QuickDashUiState.EnterAmount,
                                is QuickDashUiState.ShowQr -> "Quick Collect"
                                else -> "QuickDash"
                            },
                            style = if (isFloating) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (uiState == QuickDashUiState.Dashboard) {
                            Text(
                                text = emojiHeaderVal,
                                style = if (isFloating) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .clickable {
                                        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
                                        audioManager?.playSoundEffect(android.media.AudioManager.FX_KEY_CLICK, 0.3f)
                                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                        if (vibrator != null && vibrator.hasVibrator()) {
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                                vibrator.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_CLICK))
                                            } else {
                                                vibrator.vibrate(20L)
                                            }
                                        }
                                        triggerEmojiConfetti = true
                                        emojiConfettiKey++
                                    }
                                    .padding(start = 8.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.align(Alignment.CenterEnd).wrapContentWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (uiState == QuickDashUiState.Dashboard) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isFloating && onConvertToFullScreen != null) {
                                    IconButton(
                                        onClick = { showFullScreenPrompt = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_arrow_up_update),
                                            contentDescription = "Convert to Full Screen",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                FilledTonalIconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_settings),
                                        contentDescription = "Settings",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else if (uiState == QuickDashUiState.WhatsApp && !showChatSettings) {
                            FilledTonalIconButton(
                                onClick = { onToggleChatSettings(true) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_tools),
                                    contentDescription = "Chat Settings",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Animated Screen Content ──────────────────────
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        val springSpec = spring<Float>(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                        val specInt = spring<androidx.compose.ui.unit.IntOffset>(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                        if (targetState != QuickDashUiState.Dashboard && initialState == QuickDashUiState.Dashboard) {
                            // Premium Slide in from Right + Scale + Fade
                            slideInHorizontally(
                                animationSpec = specInt,
                                initialOffsetX = { it }
                            ) + scaleIn(
                                animationSpec = springSpec,
                                initialScale = 0.92f
                            ) + fadeIn(animationSpec = springSpec) togetherWith
                                slideOutHorizontally(
                                    animationSpec = specInt,
                                    targetOffsetX = { -it }
                                ) + scaleOut(
                                    animationSpec = springSpec,
                                    targetScale = 0.92f
                                ) + fadeOut(animationSpec = springSpec)
                        } else if (targetState == QuickDashUiState.Dashboard && initialState != QuickDashUiState.Dashboard) {
                            // Premium Slide in from Left (Back) + Scale + Fade
                            slideInHorizontally(
                                animationSpec = specInt,
                                initialOffsetX = { -it }
                            ) + scaleIn(
                                animationSpec = springSpec,
                                initialScale = 0.92f
                            ) + fadeIn(animationSpec = springSpec) togetherWith
                                slideOutHorizontally(
                                    animationSpec = specInt,
                                    targetOffsetX = { it }
                                ) + scaleOut(
                                    animationSpec = springSpec,
                                    targetScale = 0.92f
                                ) + fadeOut(animationSpec = springSpec)
                        } else {
                            fadeIn(animationSpec = springSpec) + scaleIn(animationSpec = springSpec, initialScale = 0.95f) togetherWith 
                                fadeOut(animationSpec = springSpec) + scaleOut(animationSpec = springSpec, targetScale = 0.95f)
                        }
                    },
                    label = "screenTransition",
                    modifier = Modifier.fillMaxWidth()
                ) { state ->
                    when (state) {
                        QuickDashUiState.Onboarding -> {
                            // Handled top-level now, but fallback here just in case
                            OnboardingScreen(
                                userStore = userStore,
                                onComplete = onOnboardingComplete
                            )
                        }
                        QuickDashUiState.Dashboard ->
                            DashboardScreen(
                                hapticEnabled = hapticEnabled,
                                isFloating = isFloating,
                                onToolSelected = onToolSelected
                            )
                        is QuickDashUiState.Setup ->
                            SetupScreen(
                                upiIds = upiIds,
                                defaultUpiId = defaultUpiId,
                                payeeName = payeeName,
                                onSaveUpiIds = onSaveUpiIds
                            )
                        is QuickDashUiState.EnterAmount ->
                            EnterAmountScreen(
                                recentAmounts = recentAmounts,
                                upiIds = state.upiIds,
                                defaultUpiId = state.defaultUpiId,
                                defaultPaymentApp = defaultPaymentApp,
                                qrHistoryJson = qrHistoryJson,
                                onClearQrHistory = onClearQrHistory,
                                onScanQr = onScanQr,
                                onGenerateQr = onGenerateQr,
                                onManageUpiIds = onManageUpiIds
                            )
                        is QuickDashUiState.ShowQr -> {
                            val confettiType by userStore.confettiType.collectAsState(initial = "Default")
                            val hapticLevel by userStore.hapticLevel.collectAsState(initial = "Crisp")
                            ShowQrScreen(
                                amount = state.amount,
                                qrBitmap = state.qrBitmap,
                                upiId = state.upiId,
                                payeeName = state.payeeName,
                                showUpiId = showUpiId,
                                payUrl = state.payUrl,
                                confettiType = confettiType,
                                hapticLevel = hapticLevel,
                                onQrShown = onQrShown,
                                onRestoreBrightness = onRestoreBrightness,
                                onDismiss = onDismiss
                            )
                        }
                        QuickDashUiState.WhatsApp ->
                            QuickChatScreen(
                                userStore = userStore,
                                showSettings = showChatSettings,
                                onToggleSettings = onToggleChatSettings,
                                selectingCountry = selectingCountry,
                                onToggleSelectingCountry = onToggleSelectingCountry,
                                onDismiss = onDismiss
                            )
                        QuickDashUiState.Instagram ->
                            QuickSocialScreen(
                                userStore = userStore,
                                onDismiss = onDismiss
                            )
                        QuickDashUiState.Settings ->
                            SettingsScreen(
                                userStore = userStore,
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
                                onManageUpiIds = onManageUpiIds
                            )
                        QuickDashUiState.SystemLogs ->
                            SystemLogsScreen(onDismiss = onBackToHome)
                        QuickDashUiState.Notes ->
                            QuickNotesScreen(userStore = userStore, isFloating = isFloating, onDismiss = onBackToHome)
                        QuickDashUiState.Search ->
                            QuickSearchScreen(userStore = userStore, onDismiss = onBackToHome)
                        QuickDashUiState.Wifi ->
                            QuickWifiScreen(
                                userStore = userStore,
                                isFloating = isFloating,
                                onDismiss = onBackToHome
                            )
                        QuickDashUiState.Clipboard ->
                            com.balajitechlabs.quickdash.features.clipboard.presentation.ClipboardScreen(
                                userStore = userStore,
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
                                userStore = userStore,
                                isFloating = isFloating
                            )
                        else -> {
                            // Phase 4+ screens placeholders
                        }
                    }
                }

                if (!isFloating && uiState == QuickDashUiState.Dashboard) {
                    val appContext = LocalContext.current
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://razorpay.me/@balajitechlabs"))
                            appContext.startActivity(intent)
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Support Me", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    
                    Text(
                        text = "balajitechlabs",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
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
                shapes = listOf(nl.dionsegijn.konfetti.core.models.Shape.DrawableShape(emojiDrawable, tint = false)),
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
    if (showWhatsNewOnLaunch) {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName ?: "2.2.6"
        
        LaunchedEffect(Unit) {
            settingsConfettiType = "Corner"
            settingsConfettiKey++
        }

        WhatsNewDialog(
            versionName = versionName,
            onDismiss = {
                showWhatsNewOnLaunch = false
                scope.launch {
                    userStore.saveLastSeenVersion(versionName)
                }
            }
        )
    }
    if (showFullScreenPrompt) {
        AlertDialog(
            onDismissRequest = { showFullScreenPrompt = false },
            title = { Text("Display Options", fontWeight = FontWeight.Bold) },
            text = { Text("Would you like to expand QuickDash to full screen or continue in the floating dialog window?") },
            confirmButton = {
                Button(
                    onClick = {
                        showFullScreenPrompt = false
                        onConvertToFullScreen?.invoke()
                    }
                ) {
                    Text("Convert to full screen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFullScreenPrompt = false }
                ) {
                    Text("Stay in floating dialog")
                }
            }
        )
    }
}
}
}
}
}
}


@Composable
fun UpdateTag() {
    val context = LocalContext.current
    val currentVersionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "v${packageInfo.versionName}"
        } catch (e: Exception) {
            "v2.1.8"
        }
    }

    val state = UpdateManager.updateState
    val hasLocalApk = UpdateManager.hasLocalApk

    when (state) {
        UpdateState.Idle -> {
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
        is UpdateState.UpdateAvailable -> {
            Surface(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        UpdateManager.startDownload(
                            context,
                            state.apkUrl,
                            state.versionName
                        )
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
