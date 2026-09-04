/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings
 * File: SettingsScreen.kt
 * Description: Application settings center, categorized pill groups, tactile toggle switches,
 *              beta/pre-release build opt-in, backup/restore, and monochrome styling.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation

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
import com.balajitechlabs.quickdash.core.utils.BackupRestoreManager
import kotlinx.coroutines.launch
import com.balajitechlabs.quickdash.core.ui.components.WhatsNewDialog
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.view.drawToBitmap

import android.util.Log
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.balajitechlabs.quickdash.features.settings.presentation.SettingsViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
    var showWhatsNewDialog by remember { mutableStateOf(false) }
    var showFeatureRequestDialog by remember { mutableStateOf(false) }
    var showCustomSearchDialog by remember { mutableStateOf(false) }
    var showAdminMessageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showCertificateDialog by remember { mutableStateOf(false) }
    var showBubbleLearnMoreDialog by remember { mutableStateOf(false) }
    var showCustomizeBubbleDialog by remember { mutableStateOf(false) }
    val radialCustomTools by viewModel.userStore.radialCustomTools.collectAsStateWithLifecycle(initialValue = listOf("upi", "notes", "calc", "timer"))
    var userIntendedEnable by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var adminMessageText by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf("") }
    var featureRequestText by remember { mutableStateOf("") }
    var attachScreenshot by remember { mutableStateOf(false) }
    var screenshotBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
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
                screenshotBitmap = bmp
                attachScreenshot = true
            } catch (e: Exception) {
                android.util.Log.e("QuickDash", "Error occurred: ${e.message}", e)
            }
        }
    }
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val result = BackupRestoreManager.backupDataStore(context, uri)
                if (result.isSuccess) {
                    android.widget.Toast.makeText(context, "Backup Successful! 💾", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Backup Failed: ${result.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val result = BackupRestoreManager.restoreDataStore(context, uri)
                if (result.isSuccess) {
                    android.widget.Toast.makeText(context, "Data restored successfully", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Restore failed: ${result.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_LONG).show()
                }
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
            PreferenceGroup(
                title = "Floating Window & Bubble",
                expanded = expandedGroup == "Floating Window & Bubble",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "Floating Window & Bubble") null else "Floating Window & Bubble"
                }
            ) {
                PreferenceItem(
                    title = "Quick Bubble",
                    subtitle = "System-wide floating bubble on top of any app",
                    iconVector = Icons.Default.ChatBubbleOutline,
                    trailing = {
                        StyledSwitch(
                            style = activeSwitchStyle,
                            checked = bubbleEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (Settings.canDrawOverlays(context)) {
                                        onToggleBubble(true)
                                        context.startService(Intent(context, com.balajitechlabs.quickdash.core.services.FloatingBubbleService::class.java))
                                    } else {
                                        userIntendedEnable = true
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    }
                                } else {
                                    onToggleBubble(false)
                                    context.stopService(Intent(context, com.balajitechlabs.quickdash.core.services.FloatingBubbleService::class.java))
                                }
                            }
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PreferenceItem(
                    title = "Bubble Appearance",
                    subtitle = "Customize icon size, transparency, glow & quick tools",
                    iconVector = Icons.Default.Palette,
                    onClick = {
                        triggerFeedback()
                        onNavigateToBubbleCustomizer()
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PreferenceItem(
                    title = "Shake to Open QuickDash",
                    subtitle = "Shake your phone in any app (e.g. WhatsApp) to launch QuickDash",
                    iconVector = Icons.Default.ScreenRotation,
                    trailing = {
                        StyledSwitch(
                            style = activeSwitchStyle,
                            checked = shakeToOpen,
                            onCheckedChange = { enabled ->
                                triggerFeedback()
                                coroutineScope.launch {
                                    viewModel.userStore.saveShakeToOpen(enabled)
                                    val serviceIntent = Intent(context, com.balajitechlabs.quickdash.core.services.ShakeDetectorService::class.java)
                                    if (enabled) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(serviceIntent)
                                        else context.startService(serviceIntent)
                                    } else {
                                        context.stopService(serviceIntent)
                                    }
                                }
                            }
                        )
                    }
                )
                if (shakeToOpen) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Shake Trigger Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("SINGLE" to "Single Shake", "DOUBLE" to "Double Shake").forEach { (mode, label) ->
                                val isSelected = shakeMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        triggerFeedback()
                                        coroutineScope.launch { viewModel.userStore.saveShakeMode(mode) }
                                    },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = Color(0xFF2A2B30),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Shake Sensitivity",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("LOW" to "Low", "MEDIUM" to "Medium", "HIGH" to "High").forEach { (sens, label) ->
                                val isSelected = shakeSensitivity == sens
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        triggerFeedback()
                                        coroutineScope.launch { viewModel.userStore.saveShakeSensitivity(sens) }
                                    },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = Color(0xFF2A2B30),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Vibration Intensity on Shake: ${hapticDuration.toInt()}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Slider(
                            value = hapticDuration,
                            onValueChange = { dur ->
                                coroutineScope.launch { viewModel.userStore.saveHapticDuration(dur) }
                            },
                            valueRange = 10f..80f,
                            steps = 7,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PreferenceItem(
                    title = "Quick Settings Tile",
                    subtitle = "Add QuickDash tile to system notification shade",
                    iconVector = Icons.Default.SettingsSystemDaydream,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            try {
                                val manager = context.getSystemService(Context.STATUS_BAR_SERVICE) as android.app.StatusBarManager
                                val componentName = android.content.ComponentName(
                                    context,
                                    "com.balajitechlabs.quickdash.core.services.QuickTileService"
                                )
                                manager.requestAddTileService(
                                    componentName,
                                    "QuickDash Hub",
                                    android.graphics.drawable.Icon.createWithResource(context, R.mipmap.ic_launcher_round),
                                    { executor -> executor.run() },
                                    { _ -> }
                                )
                            } catch (_: Exception) {}
                        } else {
                            android.widget.Toast.makeText(context, "Pull down top notification shade, tap Edit to add QuickDash Tile", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Group 2: Payment & UPI Setup
            PreferenceGroup(
                title = "Payments & UPI",
                expanded = expandedGroup == "Payments & UPI",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "Payments & UPI") null else "Payments & UPI"
                }
            ) {
                PreferenceItem(
                    title = "Manage UPI IDs",
                    subtitle = "Configure your payment UPI IDs and display name",
                    iconVector = Icons.Default.Payment,
                    onClick = {
                        triggerFeedback()
                        onManageUpiIds()
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                var payAppExpanded by remember { mutableStateOf(false) }
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Default Target Payment App",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Preselect target app when generating Quick Collect payment QRs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedButton(
                            onClick = { payAppExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (activeDefaultPaymentApp) {
                                    "ANY" -> "Any Payment App"
                                    "GPAY" -> "Google Pay"
                                    "PHONEPE" -> "PhonePe"
                                    "PAYTM" -> "Paytm"
                                    "BHIM" -> "BHIM"
                                    else -> "Any Payment App"
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = payAppExpanded,
                            onDismissRequest = { payAppExpanded = false }
                        ) {
                            listOf("ANY" to "Any Payment App", "GPAY" to "Google Pay", "PHONEPE" to "PhonePe", "PAYTM" to "Paytm", "BHIM" to "BHIM").forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        payAppExpanded = false
                                        coroutineScope.launch {
                                            viewModel.userStore.saveDefaultPaymentApp(code)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Group 3: Security & Privacy
            PreferenceGroup(
                title = "Security & Privacy",
                expanded = expandedGroup == "Security & Privacy",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "Security & Privacy") null else "Security & Privacy"
                }
            ) {
                val isAppLocked by viewModel.userStore.isAppLocked.collectAsStateWithLifecycle(initialValue = false)
                PreferenceItem(
                    title = "Biometric Lock",
                    subtitle = "Require fingerprint / face to open QuickDash",
                    iconVector = Icons.Default.Lock,
                    trailing = {
                        StyledSwitch(
                            style = activeSwitchStyle,
                            checked = isAppLocked,
                            onCheckedChange = { enabled ->
                                triggerFeedback()
                                coroutineScope.launch { viewModel.userStore.setAppLocked(enabled) }
                            }
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                val isTabLocked by viewModel.userStore.tabBiometricLock.collectAsStateWithLifecycle(initialValue = false)
                PreferenceItem(
                    title = "Lock Private Tabs",
                    subtitle = "Require authentication for Clipboard & Notes",
                    iconVector = Icons.Default.LockClock,
                    trailing = {
                        StyledSwitch(
                            style = activeSwitchStyle,
                            checked = isTabLocked,
                            onCheckedChange = { enabled ->
                                triggerFeedback()
                                coroutineScope.launch { viewModel.userStore.saveTabBiometricLock(enabled) }
                            }
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                val isSecureMode by viewModel.userStore.secureMode.collectAsStateWithLifecycle(initialValue = false)
                PreferenceItem(
                    title = "Secure Mode",
                    subtitle = "Block screenshots and hide app preview in recents",
                    iconVector = Icons.Default.Security,
                    trailing = {
                        StyledSwitch(
                            style = activeSwitchStyle,
                            checked = isSecureMode,
                            onCheckedChange = { enabled ->
                                triggerFeedback()
                                coroutineScope.launch { viewModel.userStore.saveSecureMode(enabled) }
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Group 4: Data & Backup
            PreferenceGroup(
                title = "Data Management",
                expanded = expandedGroup == "Data Management",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "Data Management") null else "Data Management"
                }
            ) {
                PreferenceItem(
                    title = "Backup Data",
                    subtitle = "Export your settings and preferences to a JSON file",
                    iconVector = Icons.Default.Upload,
                    onClick = {
                        triggerFeedback()
                        showBackupOptionsDialog = true
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PreferenceItem(
                    title = "Restore Data",
                    subtitle = "Import your settings and preferences",
                    iconVector = Icons.Default.Download,
                    onClick = {
                        restoreLauncher.launch(arrayOf("application/json", "*/*"))
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Group 5: Updates & System
            PreferenceGroup(
                title = "Updates & System",
                expanded = expandedGroup == "Updates & System",
                onHeaderClick = {
                    triggerFeedback()
                    expandedGroup = if (expandedGroup == "Updates & System") null else "Updates & System"
                }
            ) {
                PreferenceItem(
                    title = "Pre-Release (Beta) Builds",
                    subtitle = "Receive experimental builds and early feature updates from GitHub Releases",
                    iconVector = Icons.Default.SystemUpdate,
                    trailing = {
                        StyledSwitch(
                            style = activeSwitchStyle,
                            checked = includePreRelease,
                            onCheckedChange = { enabled ->
                                triggerFeedback()
                                coroutineScope.launch {
                                    viewModel.userStore.saveIncludePreRelease(enabled)
                                }
                            }
                        )
                    }
                )
            }

            val infiniteTransition = rememberInfiniteTransition(label = "heart_pulse")
            val heartScale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "heart_scale"
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
                    text = "Made with ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC5C6D0)
                )
                Text(
                    text = "❤️",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.graphicsLayer {
                        scaleX = heartScale
                        scaleY = heartScale
                    }
                )
                Text(
                    text = " by ",
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

    if (showWhatsNewDialog) {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName ?: "5.2.1"
        WhatsNewDialog(
            versionName = versionName,
            onDismiss = { showWhatsNewDialog = false }
        )
    }

    if (showStatsDialog) {
        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            title = { Text("App Statistics", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Total App Opens: $totalOpens")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total QR Codes Generated: $totalQrs")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Notes Saved: $totalNotes")
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAdminMessageDialog) {
        AlertDialog(
            onDismissRequest = { showAdminMessageDialog = false },
            title = { Text("📬 Message Admin", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Type a message below to send directly to the Admin channel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = adminMessageText,
                        onValueChange = { adminMessageText = it },
                        placeholder = { Text("Type your message here...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = customShape,
                        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminMessageText.isNotBlank()) {
                            coroutineScope.launch {
                                com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker.sendMessage(
                                    "📬 <b>Custom Message to Admin</b>\n" +
                                    "Device: ${android.os.Build.MODEL} (${android.os.Build.MANUFACTURER})\n" +
                                    "Message: $adminMessageText"
                                )
                            }
                            showAdminMessageDialog = false
                        }
                    },
                    enabled = adminMessageText.isNotBlank()
                ) {
                    Text("Send Message")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminMessageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("🐞 Report a Bug", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Describe the issue...") },
                        modifier = Modifier.fillMaxWidth().height(if (attachScreenshot) 90.dp else 130.dp),
                        shape = customShape,
                        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = if (attachScreenshot) 3 else 5
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Screenshot attachment toggle
                    Surface(
                        shape = customShape,
                        color = if (attachScreenshot) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                attachScreenshot = !attachScreenshot
                                if (attachScreenshot) {
                                    val activity = context as? android.app.Activity
                                    if (activity != null) {
                                        captureScreenshot(activity) { bmp ->
                                            screenshotBitmap = bmp
                                        }
                                    }
                                } else {
                                    screenshotBitmap = null
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (attachScreenshot) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                if (attachScreenshot) "Screenshot attached" else "Attach screenshot (optional)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (attachScreenshot) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Thumbnail preview
                    screenshotBitmap?.let { bmp ->
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Screenshot preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(customShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Gallery Upload Button
                    OutlinedButton(
                        onClick = {
                            try {
                                galleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                android.util.Log.e("QuickDash", "Error occurred: ${e.message}", e)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = customShape
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (galleryBitmap != null) "Image attached from gallery" else "Upload from gallery",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (feedbackText.isNotBlank()) {
                        val capturedBitmap = screenshotBitmap
                        val capturedText = feedbackText
                        coroutineScope.launch {
                            try {
                                val safeFeedback = capturedText
                                    .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                                val message = "<b>Bug Report</b>\nModel: ${Build.MODEL}\nReport: $safeFeedback"
                                if (capturedBitmap != null) {
                                    com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker.sendPhoto(
                                        caption = message.replace(Regex("<[^>]*>"), ""),
                                        bitmap = capturedBitmap
                                    )
                                } else {
                                    com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker.sendMessage(message)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("QuickDash", "Error occurred: ${e.message}", e)
                            }
                        }
                    }
                    showFeedbackDialog = false
                    feedbackText = ""
                    attachScreenshot = false
                    screenshotBitmap = null
                    galleryBitmap = null
                }) {
                    Text(if (attachScreenshot) "Send with Screenshot" else "Send via Telegram")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFeedbackDialog = false
                    attachScreenshot = false
                    screenshotBitmap = null
                    galleryBitmap = null
                }) { Text("Cancel") }
            }
        )
    }

    if (showFeatureRequestDialog) {
        AlertDialog(
            onDismissRequest = { showFeatureRequestDialog = false },
            title = { Text("Request a Feature", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = featureRequestText,
                    onValueChange = { featureRequestText = it },
                    label = { Text("What feature would you like to see?") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = customShape,
                    textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 5
                )
            },
            confirmButton = {
                Button(onClick = { 
                    if (featureRequestText.isNotBlank()) {
                        val currentText = featureRequestText
                        coroutineScope.launch {
                            val safeIdea = currentText.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                            com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker.sendMessage("<b>Feature Request</b>\nIdea: $safeIdea")
                        }
                    }
                    showFeatureRequestDialog = false 
                    featureRequestText = ""
                }) {
                    Text("Send Idea")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeatureRequestDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCustomSearchDialog) {
        var newEngineName by remember { mutableStateOf("") }
        var newEngineUrl by remember { mutableStateOf("") }
        val gson = remember { com.google.gson.Gson() }
        
        val customEngines: List<Map<String, String>> = remember(customSearchEnginesJson) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type
                gson.fromJson<List<Map<String, String>>>(customSearchEnginesJson, type) as? List<Map<String, String>> ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        AlertDialog(
            onDismissRequest = { showCustomSearchDialog = false },
            title = { Text("Custom Search Engines", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    if (customEngines.isNotEmpty()) {
                        Text("Existing Custom Engines:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(customEngines) { engine ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(engine["name"] ?: "", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        Text(engine["url"] ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }
                                    IconButton(onClick = {
                                        val updated = customEngines.filter { it != engine }
                                        coroutineScope.launch {
                                            viewModel.userStore.saveCustomSearchEngines(gson.toJson(updated))
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    
                    Text("Add New Engine:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newEngineName,
                        onValueChange = { newEngineName = it },
                        label = { Text("Engine Name") },
                        placeholder = { Text("e.g. GitHub Codesearch") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = customShape
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newEngineUrl,
                        onValueChange = { newEngineUrl = it },
                        label = { Text("Search URL (ends with q=)") },
                        placeholder = { Text("e.g. https://github.com/search?q=") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = customShape
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newEngineName.isNotBlank() && newEngineUrl.isNotBlank()) {
                        val newEngine = mapOf("name" to newEngineName, "url" to newEngineUrl)
                        val updated = customEngines + newEngine
                        coroutineScope.launch {
                            viewModel.userStore.saveCustomSearchEngines(gson.toJson(updated))
                        }
                        newEngineName = ""
                        newEngineUrl = ""
                    }
                }) {
                    Text("Add Engine")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomSearchDialog = false }) { Text("Close") }
            }
        )
    }
    if (showRatingDialog) {
        var selectedStars by remember { mutableStateOf(0) }
        var reviewText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("Rate QuickDash") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 1..5) {
                            IconButton(onClick = { selectedStars = i }) {
                                Icon(
                                    imageVector = if (i <= selectedStars) Icons.Default.Star else androidx.compose.material.icons.Icons.Default.StarOutline,
                                    contentDescription = "Star $i",
                                    tint = if (i <= selectedStars) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (selectedStars > 0) {
                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            label = { Text("Optional Review") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val message = "<b>New App Rating</b>\nStars: $selectedStars\nReview: ${if (reviewText.isBlank()) "None" else reviewText}\nDevice: ${Build.MODEL}"
                                com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker.sendBroadcastBotMessage(message)
                                // Record rating stat
                                viewModel.userStore.incrementAppOpens()
                            } catch (e: Exception) {
                                android.util.Log.e("QuickDash", "Error occurred: ${e.message}", e)
                            }
                        }
                        showRatingDialog = false
                        android.widget.Toast.makeText(context, "Thank you for your rating", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    enabled = selectedStars > 0
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── About Dialog ─────────────────────────────────────────────────────
    if (showAboutDialog) {
        val packageInfo2 = remember { context.packageManager.getPackageInfo(context.packageName, 0) }
        val vNameAbout = packageInfo2.versionName ?: "?"
        val vCodeAbout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo2.longVersionCode else @Suppress("DEPRECATION") packageInfo2.versionCode.toLong()
        
        val updateState = com.balajitechlabs.quickdash.core.utils.UpdateManager.updateState

        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("About QuickDash", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("QuickDash", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                "Version $vNameAbout (Build $vCodeAbout)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.clickable {
                                    showAboutDialog = false
                                    showUpdateDialog = true
                                    try { com.balajitechlabs.quickdash.core.utils.UpdateManager.checkForUpdates(context, manual = true) } catch (e: Exception) { Log.e(TAG, "Failed to check for updates", e) }
                                }
                            )
                            Text("Developed by balajitechlabs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Fork of IIXII™ Product .",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/IIXII-L192/PocketOps-app.git"))
                                    context.startActivity(intent)
                                }.padding(vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    showCertificateDialog = true
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "View Permit Certificate",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    // Interactive update status section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            when (updateState) {
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.Idle,
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.UpToDate -> {
                                    Text("Status: Up to date ✅", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Button(
                                        onClick = {
                                            showAboutDialog = false
                                            showUpdateDialog = true
                                            try { com.balajitechlabs.quickdash.core.utils.UpdateManager.checkForUpdates(context, manual = true) } catch (e: Exception) { Log.e(TAG, "Failed to check for updates", e) }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Check for Updates")
                                    }
                                }
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.Checking -> {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Text("Checking for updates...", style = MaterialTheme.typography.bodySmall)
                                }
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.Error -> {
                                    Text("Error checking updates", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    Text(updateState.message, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                                    Button(
                                        onClick = {
                                            try { com.balajitechlabs.quickdash.core.utils.UpdateManager.checkForUpdates(context, manual = true) } catch (e: Exception) { Log.e(TAG, "Failed to retry update check", e) }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Retry Check")
                                    }
                                }
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.UpdateAvailable -> {
                                    Text("New Update Available\nVersion v${updateState.versionName.removePrefix("v")}", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Button(
                                        onClick = {
                                            try { com.balajitechlabs.quickdash.core.utils.UpdateManager.startDownload(context, updateState.apkUrl, updateState.versionName) } catch (e: Exception) { Log.e(TAG, "Failed to start update download", e) }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Download v${updateState.versionName.removePrefix("v")}")
                                    }
                                }
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.Downloading -> {
                                    Text("Downloading update: ${updateState.progress}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    LinearProgressIndicator(
                                        progress = { updateState.progress / 100f },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.ReadyToInstall -> {
                                    Text("Update ready to install", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                    Button(
                                        onClick = {
                                            try { com.balajitechlabs.quickdash.core.utils.UpdateManager.installApk(context, updateState.fileName) } catch (e: Exception) { Log.e(TAG, "Failed to install APK", e) }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                                    ) {
                                        Text("Install Now")
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        "Your all-in-one floating dashboard for productivity, payments, clipboard, Wi-Fi analysis, and much more.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }

    if (showCertificateDialog) {
        AlertDialog(
            onDismissRequest = { showCertificateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Verified Permit Certificate", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.permit_certificate),
                    contentDescription = "Permit Certificate",
                    modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { showCertificateDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showBubbleLearnMoreDialog) {
        AlertDialog(
            onDismissRequest = { showBubbleLearnMoreDialog = false },
            title = {
                Text("Quick Bubble", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "A system-wide floating bubble for instant access to all QuickDash features.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Tap to open the menu, drag to reposition.\n• Double-tap the bubble to disable it completely.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Available Features:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• UPI Pay\n• Quick Chat\n• Quick Search\n• Quick Notes\n• Calculator\n• Timer\n• Settings\n• Quick Web",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showBubbleLearnMoreDialog = false }) {
                    Text("Got it")
                }
            }
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

    // ── Update Check Dialog ───────────────────────────────────────────────
    if (showUpdateDialog) {
        com.balajitechlabs.quickdash.core.ui.components.AppUpdateDialog(
            onDismiss = { showUpdateDialog = false }
        )
    }

    // Tips & Recommendations Dialog
    if (showTipsDialog) {
        val tips = listOf(
            "GitHub Rate Limit" to "Generate a Personal Access Token on GitHub (Settings → Developer Settings → Tokens) and paste it in Advanced & API Settings. Raises limit from 60 to 5,000 requests/hour.",
            "Social Link Routing" to "Social media profile links open natively in their apps when installed. On emulators, they fallback to your browser automatically.",
            "Play Protect" to "For sideloaded APKs, tap 'More details → Install anyway' on the Play Protect prompt. The Play Store version is auto-trusted.",
            "QR Scanner First Load" to "First QR scan may show a brief overlay — Google Play Services sets up the barcode engine once. Subsequent scans are instant.",
            "Backup Your Data" to "Use Data Management → Backup Data to export all settings, notes, and clipboard history as a JSON file before switching phones.",
            "Save Battery" to "Switch to AMOLED theme in Launch & Windows for true-black backgrounds that save battery on OLED displays.",
            "Manage Notifications" to "Swipe LEFT on any notification to dismiss it. Swipe RIGHT to pin it to the top of the feed for quick access.",
            "Quick Collect" to "Set your Default Target Payment App in Advanced & API Settings to pre-select GPay, PhonePe, or Paytm for faster QR generation.",
            "Custom Seed Color" to "Use the seed color picker in Appearance to create a unique color theme applied across the entire app.",
            "Timer Persistence" to "Countdown timers use AlarmManager exact alarms — they continue even in deep Doze mode when the phone is idle."
        )
        AlertDialog(
            onDismissRequest = { showTipsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFC107))
                    Text("Tips & Recommendations", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    tips.forEachIndexed { index, (title, body) ->
                        Column {
                            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (index < tips.size - 1) {
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showTipsDialog = false }) { Text("Got it! 👍") } }
        )
    }

    if (showBackupOptionsDialog) {
        BackupRestoreDialog(
            onDismissRequest = { showBackupOptionsDialog = false }
        )
    }
}




private fun captureScreenshot(activity: android.app.Activity, callback: (android.graphics.Bitmap?) -> Unit) {
    try {
        val window = activity.window
        val view = window.decorView
        val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
        val locationOfViewInWindow = IntArray(2)
        view.getLocationInWindow(locationOfViewInWindow)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            android.view.PixelCopy.request(
                window,
                android.graphics.Rect(
                    locationOfViewInWindow[0],
                    locationOfViewInWindow[1],
                    locationOfViewInWindow[0] + view.width,
                    locationOfViewInWindow[1] + view.height
                ),
                bitmap,
                { copyResult ->
                    if (copyResult == android.view.PixelCopy.SUCCESS) {
                        callback(bitmap)
                    } else {
                        try {
                            val b = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
                            val c = android.graphics.Canvas(b)
                            view.draw(c)
                            callback(b)
                        } catch (e: java.lang.Exception) {
                            callback(null)
                        }
                    }
                },
                handler
            )
        } else {
            val c = android.graphics.Canvas(bitmap)
            view.draw(c)
            callback(bitmap)
        }
    } catch (e: java.lang.Exception) {
        android.util.Log.e("QuickDash", "Error occurred: ${e.message}", e)
        callback(null)
    }
}

private fun shareBackupFile(context: android.content.Context, jsonString: String, targetPackage: String? = null) {
    try {
        val dir = java.io.File(context.cacheDir, "images")
        if (!dir.exists()) dir.mkdirs()
        val file = java.io.File(dir, "quickdash_backup.json")
        file.writeText(jsonString)
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (targetPackage != null) {
                setPackage(targetPackage)
            }
        }
        
        val chooserIntent = if (targetPackage == null) {
            android.content.Intent.createChooser(intent, "Share Backup File")
        } else {
            intent
        }
        chooserIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        android.util.Log.e("QuickDash", "Error occurred: ${e.message}", e)
        android.widget.Toast.makeText(context, "Could not open target app. Falling back to share chooser.", android.widget.Toast.LENGTH_SHORT).show()
        if (targetPackage != null) {
            shareBackupFile(context, jsonString, null)
        }
    }
}
