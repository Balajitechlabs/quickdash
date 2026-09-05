/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation
 * File: SettingsScreen.kt
 * Description: Main settings screen modularly composing security, data, updates, and community sections.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation


import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.ui.components.PreferenceGroup
import com.balajitechlabs.quickdash.core.ui.theme.LocalBorderWidth
import com.balajitechlabs.quickdash.core.ui.theme.LocalCustomShape
import com.balajitechlabs.quickdash.core.ui.theme.LocalShowShadow
import com.balajitechlabs.quickdash.core.ui.components.PreferenceItem
import com.balajitechlabs.quickdash.core.ui.components.SwitchStyle
import com.balajitechlabs.quickdash.core.ui.components.SliderStyle
import com.balajitechlabs.quickdash.core.ui.components.ShapeStyle
import com.balajitechlabs.quickdash.core.ui.components.StyledSwitch
import com.balajitechlabs.quickdash.core.ui.components.StyledSlider
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.view.drawToBitmap

import android.util.Log
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.balajitechlabs.quickdash.features.settings.presentation.SettingsViewModel
import com.balajitechlabs.quickdash.features.settings.presentation.sections.SettingsCommunitySection
import com.balajitechlabs.quickdash.features.settings.presentation.sections.SettingsDataSection
import com.balajitechlabs.quickdash.features.settings.presentation.sections.SettingsFloatingWindowSection
import com.balajitechlabs.quickdash.features.settings.presentation.sections.SettingsPaymentSection
import com.balajitechlabs.quickdash.features.settings.presentation.sections.SettingsSecuritySection
import com.balajitechlabs.quickdash.features.settings.presentation.sections.SettingsUpdatesSection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.balajitechlabs.quickdash.features.settings.presentation.dialogs.AdminMessageDialog
import com.balajitechlabs.quickdash.features.settings.presentation.dialogs.AppStatisticsDialog
import com.balajitechlabs.quickdash.features.settings.presentation.dialogs.BubbleLearnMoreDialog
import com.balajitechlabs.quickdash.features.settings.presentation.dialogs.CustomSearchEnginesDialog
import com.balajitechlabs.quickdash.features.settings.presentation.dialogs.FeatureRequestDialog
import com.balajitechlabs.quickdash.features.settings.presentation.dialogs.FeedbackDialog
import com.balajitechlabs.quickdash.features.settings.presentation.dialogs.PermitCertificateDialog
import com.balajitechlabs.quickdash.features.settings.presentation.dialogs.StarRatingDialog
import com.balajitechlabs.quickdash.features.settings.presentation.dialogs.TipsRecommendationsDialog

private const val TAG = "SettingsScreen"

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    themeMode: String,
    dynamicColor: Boolean,
    bubbleEnabled: Boolean,
    onChangeThemeMode: (String) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onToggleBubble: (Boolean) -> Unit,
    onTriggerConfetti: (String) -> Unit,
    onBackToHome: () -> Unit,
    onNavigateToSystemLogs: () -> Unit = {},
    onManageUpiIds: () -> Unit = {},
    onNavigateToBubbleCustomizer: () -> Unit = {}
) {
    val userStore = viewModel.userStore
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val customShape = LocalCustomShape.current

    // Load visual settings reactively from UserStore datastore
    val seedColorHex by viewModel.userStore.seedColor.collectAsStateWithLifecycle(initialValue = "#1E88E5")
    val switchStyleStr by viewModel.userStore.switchStyle.collectAsStateWithLifecycle(initialValue = "MaterialYou")
    val sliderStyleStr by viewModel.userStore.sliderStyle.collectAsStateWithLifecycle(initialValue = "MaterialYou")

    // Resolve active styles from switchStyleStr and sliderStyleStr
    val activeSwitchStyle = remember(switchStyleStr) {
        try { SwitchStyle.valueOf(switchStyleStr) } catch(e: Exception) { SwitchStyle.MaterialYou }
    }
    val activeSliderStyle = remember(sliderStyleStr) {
        try { SliderStyle.valueOf(sliderStyleStr) } catch(e: Exception) { SliderStyle.MaterialYou }
    }
    val shapeStyleStr by viewModel.userStore.shapeStyle.collectAsStateWithLifecycle(initialValue = "Rounded")
    val cornerRadius by viewModel.userStore.cornerRadius.collectAsStateWithLifecycle(initialValue = 16f)
    val borderWidth by viewModel.userStore.borderWidth.collectAsStateWithLifecycle(initialValue = 1f)
    val fontScale by viewModel.userStore.fontScale.collectAsStateWithLifecycle(initialValue = 1f)
    val fontFamilyName by viewModel.userStore.fontFamilyKey.collectAsStateWithLifecycle(initialValue = "system")
    val showShadow by viewModel.userStore.showShadow.collectAsStateWithLifecycle(initialValue = true)
    val showToolDescriptions by viewModel.userStore.showToolDescriptions.collectAsStateWithLifecycle(initialValue = true)
    val secureMode by viewModel.userStore.secureMode.collectAsStateWithLifecycle(initialValue = false)
    val maxBrightness by viewModel.userStore.maxBrightness.collectAsStateWithLifecycle(initialValue = false)
    val showImagePreviews by viewModel.userStore.showImagePreviews.collectAsStateWithLifecycle(initialValue = true)
    val advancedThumbnail by viewModel.userStore.advancedThumbnail.collectAsStateWithLifecycle(initialValue = false)
    val emojiHeader by viewModel.userStore.emojiHeader.collectAsStateWithLifecycle(initialValue = "")
    val appLanguage by viewModel.userStore.appLanguage.collectAsStateWithLifecycle(initialValue = "en")
    val confettiType by viewModel.userStore.confettiType.collectAsStateWithLifecycle(initialValue = "Default")
    val confettiEnabled by viewModel.userStore.confettiEnabled.collectAsStateWithLifecycle(initialValue = true)
    val hapticEnabled by viewModel.userStore.hapticEnabled.collectAsStateWithLifecycle(initialValue = true)
    val biometricLock by viewModel.userStore.biometricLock.collectAsStateWithLifecycle(initialValue = false)
    val clipboardAutocleanInterval by viewModel.userStore.clipboardAutocleanInterval.collectAsStateWithLifecycle(initialValue = "OFF")
    val shakeToOpen by viewModel.userStore.shakeToOpen.collectAsStateWithLifecycle(initialValue = false)
    val shakeMode by viewModel.userStore.shakeMode.collectAsStateWithLifecycle(initialValue = "DOUBLE")
    val shakeSensitivity by viewModel.userStore.shakeSensitivity.collectAsStateWithLifecycle(initialValue = "MEDIUM")
    val customSearchEnginesJson by viewModel.userStore.customSearchEngines.collectAsStateWithLifecycle(initialValue = "[]")
    val shakeToTrigger by viewModel.userStore.shakeToTrigger.collectAsStateWithLifecycle(initialValue = false)
    val hapticDuration by viewModel.userStore.hapticDuration.collectAsStateWithLifecycle(initialValue = 25f)
    val customBackupPath by viewModel.userStore.customBackupPath.collectAsStateWithLifecycle(initialValue = null)
    val includePreRelease by viewModel.userStore.includePreRelease.collectAsStateWithLifecycle(initialValue = false)

    var localRadius by remember(cornerRadius) { mutableStateOf(cornerRadius) }
    var localBorderWidth by remember(borderWidth) { mutableStateOf(borderWidth) }
    var localFontScale by remember(fontScale) { mutableStateOf(fontScale) }

    @Suppress("DEPRECATION")
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val hapticLevel by viewModel.userStore.hapticLevel.collectAsStateWithLifecycle(initialValue = "Crisp")
    var expandedGroup by remember { mutableStateOf<String?>(null) }
    val activeDefaultPaymentApp by viewModel.userStore.defaultPaymentApp.collectAsStateWithLifecycle(initialValue = "ANY")
    val activeClipboardClearDelay by viewModel.userStore.clipboardClearDelay.collectAsStateWithLifecycle(initialValue = -1L)
    val activeGithubAccessToken by viewModel.userStore.githubAccessToken.collectAsStateWithLifecycle(initialValue = "")

    @Composable
    fun SettingsFilterChip(
        selected: Boolean,
        onClick: () -> Unit,
        label: String
    ) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(label) },
            shape = customShape,
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                selectedBorderColor = MaterialTheme.colorScheme.primary,
                borderWidth = borderWidth.dp,
                selectedBorderWidth = borderWidth.dp
            )
        )
    }

    @Suppress("DEPRECATION")
    fun triggerFeedback() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        audioManager?.playSoundEffect(android.media.AudioManager.FX_KEY_CLICK, 0.3f)
        if (hapticDuration <= 0f) return
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createOneShot(hapticDuration.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(hapticDuration.toLong())
            }
        }
    }

    val totalOpens by viewModel.userStore.totalAppOpens.collectAsStateWithLifecycle(initialValue = 0L)
    val totalQrs by viewModel.userStore.totalQrGenerated.collectAsStateWithLifecycle(initialValue = 0L)
    val totalNotes by viewModel.userStore.totalNotesSaved.collectAsStateWithLifecycle(initialValue = 0L)

    var showStatsDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showFeatureRequestDialog by remember { mutableStateOf(false) }
    var showCustomSearchDialog by remember { mutableStateOf(false) }
    var showAdminMessageDialog by remember { mutableStateOf(false) }
    var showCertificateDialog by remember { mutableStateOf(false) }
    var showBubbleLearnMoreDialog by remember { mutableStateOf(false) }
    var showCustomizeBubbleDialog by remember { mutableStateOf(false) }
    val radialCustomTools by viewModel.userStore.radialCustomTools.collectAsStateWithLifecycle(initialValue = listOf("upi", "notes", "calc", "timer"))
    var userIntendedEnable by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var galleryBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showBackupOptionsDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                galleryBitmap = bmp
            } catch (e: Exception) {
                Log.e("QuickDash", "Error occurred: ${e.message}", e)
            }
        }
    }



    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (userIntendedEnable) {
                    if (Settings.canDrawOverlays(context)) {
                        onToggleBubble(true)
                        context.startService(Intent(context, com.balajitechlabs.quickdash.core.services.FloatingBubbleService::class.java))
                    }
                    userIntendedEnable = false
                }
                if (bubbleEnabled && !Settings.canDrawOverlays(context)) {
                    onToggleBubble(false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF000000))
                .verticalScroll(rememberScrollState())
                .padding(bottom = 140.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Group 0: Dynamic Permissions
            AppPermissionsPreferenceGroup(
                expanded = expandedGroup == "App Permissions",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "App Permissions") null else "App Permissions"
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Group 1: Floating Window & Overlay
            SettingsFloatingWindowSection(
                expanded = expandedGroup == "Floating Window & Bubble",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "Floating Window & Bubble") null else "Floating Window & Bubble"
                },
                bubbleEnabled = bubbleEnabled,
                onToggleBubble = onToggleBubble,
                onRequestOverlayPermission = { userIntendedEnable = true },
                onNavigateToBubbleCustomizer = onNavigateToBubbleCustomizer,
                shakeToOpen = shakeToOpen,
                shakeMode = shakeMode,
                shakeSensitivity = shakeSensitivity,
                hapticDuration = hapticDuration,
                onSaveShakeToOpen = { enabled ->
                    coroutineScope.launch { viewModel.userStore.saveShakeToOpen(enabled) }
                },
                onSaveShakeMode = { mode ->
                    coroutineScope.launch { viewModel.userStore.saveShakeMode(mode) }
                },
                onSaveShakeSensitivity = { sens ->
                    coroutineScope.launch { viewModel.userStore.saveShakeSensitivity(sens) }
                },
                onSaveHapticDuration = { dur ->
                    coroutineScope.launch { viewModel.userStore.saveHapticDuration(dur) }
                },
                onFeedback = ::triggerFeedback,
                activeSwitchStyle = activeSwitchStyle
            )

            // Group 2: Payment & UPI Setup
            SettingsPaymentSection(
                expanded = expandedGroup == "Payments & UPI",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "Payments & UPI") null else "Payments & UPI"
                },
                onManageUpiIds = onManageUpiIds,
                activeDefaultPaymentApp = activeDefaultPaymentApp,
                onSaveDefaultPaymentApp = { code ->
                    coroutineScope.launch { viewModel.userStore.saveDefaultPaymentApp(code) }
                },
                onFeedback = ::triggerFeedback
            )

            // Group 3: Security & Privacy
            SettingsSecuritySection(
                expanded = expandedGroup == "Security & Privacy",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "Security & Privacy") null else "Security & Privacy"
                },
                viewModel = viewModel,
                coroutineScope = coroutineScope,
                activeSwitchStyle = activeSwitchStyle,
                onFeedback = { triggerFeedback() }
            )

            // Group 4: Data & Backup
            SettingsDataSection(
                expanded = expandedGroup == "Data Management",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "Data Management") null else "Data Management"
                },
                onFeedback = { triggerFeedback() },
                onOpenBackupOptions = { showBackupOptionsDialog = true }
            )

            // Group 5: Updates & System
            SettingsUpdatesSection(
                expanded = expandedGroup == "Updates & System",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "Updates & System") null else "Updates & System"
                },
                includePreRelease = includePreRelease,
                viewModel = viewModel,
                coroutineScope = coroutineScope,
                activeSwitchStyle = activeSwitchStyle,
                onFeedback = { triggerFeedback() }
            )

            // Group 6: Community & About
            SettingsCommunitySection(
                expanded = expandedGroup == "Community & About",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "Community & About") null else "Community & About"
                },
                context = context,
                onShowStats = { showStatsDialog = true },
                onShowAdminMessage = { showAdminMessageDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2A2B30),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://balajitechlab.com"))
                            context.startActivity(intent)
                        }
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.ic_developer_avatar),
                        contentDescription = "balajitechlabs avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Crafted by ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC5C6D0)
                )
                Text(
                    text = "balajitechlabs  ||BTL||™",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://balajitechlab.com"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }


    if (showStatsDialog) {
        AppStatisticsDialog(
            totalOpens = totalOpens,
            totalQrs = totalQrs,
            totalNotes = totalNotes,
            onDismiss = { showStatsDialog = false }
        )
    }

    if (showAdminMessageDialog) {
        AdminMessageDialog(
            onDismiss = { showAdminMessageDialog = false }
        )
    }

    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = {
                showFeedbackDialog = false
                galleryBitmap = null
            },
            onLaunchGallery = { galleryLauncher.launch("image/*") },
            galleryBitmap = galleryBitmap
        )
    }

    if (showFeatureRequestDialog) {
        FeatureRequestDialog(
            onDismiss = { showFeatureRequestDialog = false }
        )
    }

    if (showCustomSearchDialog) {
        CustomSearchEnginesDialog(
            customSearchEnginesJson = customSearchEnginesJson,
            onSaveCustomEngines = { newJson ->
                coroutineScope.launch {
                    viewModel.userStore.saveCustomSearchEngines(newJson)
                }
            },
            onDismiss = { showCustomSearchDialog = false }
        )
    }

    if (showRatingDialog) {
        StarRatingDialog(
            onDismiss = { showRatingDialog = false }
        )
    }

    if (showCertificateDialog) {
        PermitCertificateDialog(
            onDismiss = { showCertificateDialog = false }
        )
    }

    if (showBubbleLearnMoreDialog) {
        BubbleLearnMoreDialog(
            onDismiss = { showBubbleLearnMoreDialog = false }
        )
    }

    if (showCustomizeBubbleDialog) {
        CustomizeBubbleDialog(
            initialTools = radialCustomTools,
            onSave = { updatedTools ->
                coroutineScope.launch {
                    viewModel.userStore.saveRadialCustomTools(updatedTools)
                }
            },
            onDismiss = { showCustomizeBubbleDialog = false }
        )
    }

    if (showTipsDialog) {
        TipsRecommendationsDialog(
            onDismiss = { showTipsDialog = false }
        )
    }

    if (showBackupOptionsDialog) {
        BackupRestoreDialog(
            onDismissRequest = { showBackupOptionsDialog = false }
        )
    }
}
