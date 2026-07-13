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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userStore: UserStore,
    themeMode: String,
    dynamicColor: Boolean,
    bubbleEnabled: Boolean,
    onChangeThemeMode: (String) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onToggleBubble: (Boolean) -> Unit,
    onTriggerConfetti: (String) -> Unit,
    onBackToHome: () -> Unit,
    onNavigateToSystemLogs: () -> Unit = {},
    onManageUpiIds: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val customShape = LocalCustomShape.current

    // Load new visual settings reactively from UserStore datastore
    val launchStyle by userStore.launchStyle.collectAsState(initial = "FULL_SCREEN")
    val seedColorHex by userStore.seedColor.collectAsState(initial = "#1E88E5")
    val switchStyleStr by userStore.switchStyle.collectAsState(initial = "MaterialYou")
    val sliderStyleStr by userStore.sliderStyle.collectAsState(initial = "MaterialYou")

    // Resolve active styles from switchStyleStr and sliderStyleStr
    val activeSwitchStyle = remember(switchStyleStr) {
        try { SwitchStyle.valueOf(switchStyleStr) } catch(e: Exception) { SwitchStyle.MaterialYou }
    }
    val activeSliderStyle = remember(sliderStyleStr) {
        try { SliderStyle.valueOf(sliderStyleStr) } catch(e: Exception) { SliderStyle.MaterialYou }
    }
    val shapeStyleStr by userStore.shapeStyle.collectAsState(initial = "Rounded")
    val cornerRadius by userStore.cornerRadius.collectAsState(initial = 16f)
    val borderWidth by userStore.borderWidth.collectAsState(initial = 1f)
    val fontScale by userStore.fontScale.collectAsState(initial = 1f)
    val fontFamilyName by userStore.fontFamilyKey.collectAsState(initial = "system")
    val showShadow by userStore.showShadow.collectAsState(initial = true)
    val showToolDescriptions by userStore.showToolDescriptions.collectAsState(initial = true)
    val secureMode by userStore.secureMode.collectAsState(initial = false)
    val maxBrightness by userStore.maxBrightness.collectAsState(initial = false)
    val showImagePreviews by userStore.showImagePreviews.collectAsState(initial = true)
    val advancedThumbnail by userStore.advancedThumbnail.collectAsState(initial = false)
    val emojiHeader by userStore.emojiHeader.collectAsState(initial = "🚀")
    val appLanguage by userStore.appLanguage.collectAsState(initial = "en")
    val confettiType by userStore.confettiType.collectAsState(initial = "Default")
    val confettiEnabled by userStore.confettiEnabled.collectAsState(initial = true)
    val hapticEnabled by userStore.hapticEnabled.collectAsState(initial = true)
    val biometricLock by userStore.biometricLock.collectAsState(initial = false)
    val clipboardAutocleanInterval by userStore.clipboardAutocleanInterval.collectAsState(initial = "OFF")
    val shakeToOpen by userStore.shakeToOpen.collectAsState(initial = false)
    val customSearchEnginesJson by userStore.customSearchEngines.collectAsState(initial = "[]")
    val shakeToTrigger by userStore.shakeToTrigger.collectAsState(initial = false)
    val hapticDuration by userStore.hapticDuration.collectAsState(initial = 15f)
    val customBackupPath by userStore.customBackupPath.collectAsState(initial = null)

    var localRadius by remember(cornerRadius) { mutableStateOf(cornerRadius) }
    var localBorderWidth by remember(borderWidth) { mutableStateOf(borderWidth) }
    var localFontScale by remember(fontScale) { mutableStateOf(fontScale) }

    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val hapticLevel by userStore.hapticLevel.collectAsState(initial = "Crisp")
    var expandedGroup by remember { mutableStateOf<String?>("Contact & Socials") }
    val activeDefaultPaymentApp by userStore.defaultPaymentApp.collectAsState(initial = "ANY")
    val activeClipboardClearDelay by userStore.clipboardClearDelay.collectAsState(initial = -1L)
    val activeGithubAccessToken by userStore.githubAccessToken.collectAsState(initial = "")

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

    val totalOpens by userStore.totalAppOpens.collectAsState(initial = 0L)
    val totalQrs by userStore.totalQrGenerated.collectAsState(initial = 0L)
    val totalNotes by userStore.totalNotesSaved.collectAsState(initial = 0L)

    var showStatsDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showWhatsNewDialog by remember { mutableStateOf(false) }
    var showFeatureRequestDialog by remember { mutableStateOf(false) }
    var showCustomSearchDialog by remember { mutableStateOf(false) }
    var showAdminMessageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showCertificateDialog by remember { mutableStateOf(false) }
    var showBubbleLearnMoreDialog by remember { mutableStateOf(false) }
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
                e.printStackTrace()
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
                    android.widget.Toast.makeText(context, "Data Restored Successfully! 🔄", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Restore Failed: ${result.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_LONG).show()
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            var dragOffset by remember { mutableStateOf(0f) }
            val haptic = LocalHapticFeedback.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .clickable { showUpdateDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(6.dp)
                        .offset(y = dragOffset.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), shape = CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset = (dragOffset + dragAmount.y / 2f).coerceIn(-12f, 24f)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragEnd = {
                                    dragOffset = 0f
                                }
                            )
                        }
                )
            }

            // Top Header Bar inside Settings sheet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    triggerFeedback()
                    onBackToHome()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Go to Home Screen",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            val isTabletOrLandscape = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
            val column0Groups = @Composable {
                // ── Contact Section (Top) ──────────────────────────────────────
                PreferenceGroup(
            title = "Contact & Socials",
            expanded = expandedGroup == "Contact & Socials",
            onHeaderClick = {
                triggerFeedback()
                expandedGroup = if (expandedGroup == "Contact & Socials") null else "Contact & Socials"
            }
        ) {
            PreferenceItem(
                title = "Join the Channel",
                subtitle = "Quick Dash Channel",
                iconVector = Icons.Default.Send,
                onClick = {
                    triggerFeedback()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+7jh0CvLVDlFjNDU1"))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "GitHub",
                subtitle = "balajitechlabs",
                iconVector = Icons.Default.Code,
                onClick = {
                    triggerFeedback()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/balajitechlabs"))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Instagram",
                subtitle = "balajitechlabs",
                iconVector = Icons.Default.Camera,
                onClick = {
                    triggerFeedback()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/balajitechlabs"))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Send Message to Admin",
                subtitle = "Send direct messages or access Telegram bot channels",
                iconVector = Icons.Default.Message,
                onClick = {
                    triggerFeedback()
                    adminMessageText = ""
                    showAdminMessageDialog = true
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Star QuickDash on GitHub",
                subtitle = "Love QuickDash? Give us a star on our repository!",
                iconVector = Icons.Default.Star,
                onClick = {
                    triggerFeedback()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Balajitechlabs/quickdash"))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Star PocketOps on GitHub",
                subtitle = "Support the original open-source project that inspired QuickDash",
                iconVector = Icons.Default.StarBorder,
                onClick = {
                    triggerFeedback()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/IIXII-L192/PocketOps-app"))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Shared Access",
                subtitle = "60-rate limit of the tab",
                iconVector = Icons.Default.Info,
                onClick = { triggerFeedback() }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            // ── Donate Banner ──────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                        Column {
                            Text(
                                text = "Support QuickDash Development ❤️",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Keep this project free, ad-free and open-source!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Reserve Pay button (Razorpay link)
                        Button(
                            onClick = {
                                triggerFeedback()
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://razorpay.me/@balajitechlabs"))
                                    context.startActivity(intent)
                                } catch (e: Exception) { e.printStackTrace() }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF007AFF), // High contrast blue for Raycast Razorpay
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Razorpay", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        // Direct Pay support button
                        Button(
                            onClick = {
                                triggerFeedback()
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay?pa=241120067@ybl&pn=BalajiTechLabs&cu=INR"))
                                    context.startActivity(Intent.createChooser(intent, "Support via Indian UPI Apps"))
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "No UPI app found on device", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50), // High contrast green for UPI
                                contentColor = Color.White
                            )
                        ) {
                            Icon(painterResource(R.drawable.ic_upi_pay), contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Direct Pay", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // ── Group: Advanced & API Settings ───────────────────────────────
        PreferenceGroup(
            title = "Advanced & API Settings",
            expanded = expandedGroup == "Advanced & API Settings",
            onHeaderClick = {
                triggerFeedback()
                expandedGroup = if (expandedGroup == "Advanced & API Settings") null else "Advanced & API Settings"
            }
        ) {
            // 1. Default Targeted App
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
                                        userStore.saveDefaultPaymentApp(code)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 2. Clipboard auto-clear delay
            var clearDelayExpanded by remember { mutableStateOf(false) }
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Clipboard Auto-Clear Delay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Automatically clear clipboard history after a specified period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(
                        onClick = { clearDelayExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (activeClipboardClearDelay) {
                                -1L -> "Never (Keep History)"
                                60000L -> "1 Minute"
                                300000L -> "5 Minutes"
                                600000L -> "10 Minutes"
                                1800000L -> "30 Minutes"
                                else -> "Never (Keep History)"
                            }
                        )
                    }
                    DropdownMenu(
                        expanded = clearDelayExpanded,
                        onDismissRequest = { clearDelayExpanded = false }
                    ) {
                        listOf(-1L to "Never (Keep History)", 60000L to "1 Minute", 300000L to "5 Minutes", 600000L to "10 Minutes", 1800000L to "30 Minutes").forEach { (delay, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    clearDelayExpanded = false
                                    coroutineScope.launch {
                                        userStore.saveClipboardClearDelay(delay)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // 3. GitHub Personal Access Token
            var githubTokenInput by remember(activeGithubAccessToken) { mutableStateOf(activeGithubAccessToken) }
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "GitHub Access Token (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Enter a Personal Access Token to increase GitHub API rate limits (from 60 to 5000/hr).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = githubTokenInput,
                    onValueChange = { 
                        githubTokenInput = it
                        coroutineScope.launch {
                            userStore.saveGithubAccessToken(it.trim())
                        }
                    },
                    placeholder = { Text("ghp_xxxxxxxxxxxxxxxxxxxx") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ── Group 1: Launch & Window Setup ─────────────────────────────
        PreferenceGroup(
            title = "Launch & Windows",
            expanded = expandedGroup == "Launch & Windows",
            onHeaderClick = {
                triggerFeedback()
                expandedGroup = if (expandedGroup == "Launch & Windows") null else "Launch & Windows"
            }
        ) {
            PreferenceItem(
                title = "Launch Format",
                subtitle = "App Icon action: $launchStyle",
                iconVector = Icons.Default.Launch
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsFilterChip(
                    selected = launchStyle == "FULL_SCREEN",
                    onClick = {
                        coroutineScope.launch {
                            userStore.saveLaunchStyle("FULL_SCREEN")
                            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            context.startActivity(intent)
                            if (context is android.app.Activity) {
                                context.finish()
                            }
                        }
                    },
                    label = "Full Screen"
                )
                SettingsFilterChip(
                    selected = launchStyle == "FLOATING_DIALOG",
                    onClick = {
                        coroutineScope.launch {
                            userStore.saveLaunchStyle("FLOATING_DIALOG")
                            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            context.startActivity(intent)
                            if (context is android.app.Activity) {
                                context.finish()
                            }
                        }
                    },
                    label = "Floating Dialog"
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Quick Settings Tile",
                subtitle = "Tap here to add or manage in system tray",
                iconVector = Icons.Default.SettingsSystemDaydream,
                onClick = {
                    if (Build.VERSION.SDK_INT >= 33) {
                        try {
                            val manager = context.getSystemService(Context.STATUS_BAR_SERVICE) as android.app.StatusBarManager
                            val componentName = android.content.ComponentName(
                                context,
                                "com.balajitechlabs.quickdash.core.quicktile.QuickTileService"
                            )
                            manager.requestAddTileService(
                                componentName,
                                "QuickDash",
                                android.graphics.drawable.Icon.createWithResource(context, R.mipmap.ic_launcher_round),
                                { executor -> executor.run() },
                                { result -> }
                            )
                        } catch (e: Exception) {
                            // API fallback
                        }
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Quick Bubble",
                subtitle = "System-wide overlay bubble for fast toggle",
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showBubbleLearnMoreDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Learn More",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Shake to Open",
                subtitle = "Shake your phone to instantly launch QuickDash",
                iconVector = Icons.Default.Vibration,
                trailing = {
                    StyledSwitch(
                        style = activeSwitchStyle,
                        checked = shakeToOpen,
                        onCheckedChange = { enabled ->
                            triggerFeedback()
                            coroutineScope.launch {
                                userStore.setShakeToOpen(enabled)
                            }
                            // Toggle service via application
                            val appContext = context.applicationContext as com.balajitechlabs.quickdash.QuickDashApplication
                            if (enabled) {
                                appContext.startShakeDetector()
                            } else {
                                appContext.stopShakeDetector()
                            }
                        }
                    )
                }
            )
        }

        // ── Group 2: Theming & Color System ────────────────────────────
        PreferenceGroup(
            title = "Customization",
            expanded = expandedGroup == "Customization",
            onHeaderClick = {
                triggerFeedback()
                expandedGroup = if (expandedGroup == "Customization") null else "Customization"
            }
        ) {
            PreferenceItem(
                title = "Theme Mode",
                subtitle = "Select light / dark preference: $themeMode",
                iconVector = Icons.Default.Palette
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    SettingsFilterChip(
                        selected = themeMode == "SYSTEM",
                        onClick = {
                            triggerFeedback()
                            onChangeThemeMode("SYSTEM")
                        },
                        label = "Sys"
                    )
                }
                item {
                    SettingsFilterChip(
                        selected = themeMode == "LIGHT",
                        onClick = {
                            triggerFeedback()
                            onChangeThemeMode("LIGHT")
                        },
                        label = "Light"
                    )
                }
                item {
                    SettingsFilterChip(
                        selected = themeMode == "DARK",
                        onClick = {
                            triggerFeedback()
                            onChangeThemeMode("DARK")
                        },
                        label = "Dark"
                    )
                }
                item {
                    SettingsFilterChip(
                        selected = themeMode == "AMOLED",
                        onClick = {
                            triggerFeedback()
                            onChangeThemeMode("AMOLED")
                        },
                        label = "AMOLED"
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Curated Theme Presets",
                subtitle = "Apply distinct seed style palettes instantly",
                iconVector = Icons.Default.Style
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    "Default" to "#1E88E5",
                    "Cyberpunk" to "#FF0055",
                    "Hacker" to "#39FF14",
                    "Midnight" to "#1976D2",
                    "Lavender" to "#9575CD",
                    "Sunset" to "#FF7043",
                    "Ocean" to "#00ACC1",
                    "Forest" to "#43A047"
                )
                items(presets) { (name, hex) ->
                    val isSelected = seedColorHex.equals(hex, ignoreCase = true)
                    SettingsFilterChip(
                        selected = isSelected,
                        onClick = {
                            triggerFeedback()
                            coroutineScope.launch { userStore.saveSeedColor(hex) }
                        },
                        label = name
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Aesthetic Seed Color",
                subtitle = "Tonal palettes generated from: $seedColorHex",
                iconVector = Icons.Default.ColorLens
            )

            // Horizontal color picker of presets
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val colors = listOf(
                    "#1E88E5", // Blue
                    "#9C27B0", // Purple
                    "#E91E63", // Pink
                    "#4CAF50", // Green
                    "#FF9800", // Orange
                    "#FFC107", // Amber
                    "#E53935", // Red
                    "#009688", // Teal
                    "#00F0FF", // Neon Cyber
                    "#212121"  // Dark Gray
                )
                items(colors) { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                coroutineScope.launch { userStore.saveSeedColor(hex) }
                            }
                            .border(
                                width = if (seedColorHex == hex) 3.dp else 1.dp,
                                color = if (seedColorHex == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        if (seedColorHex == hex) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (hex == "#212121") Color.White else Color.Black,
                                modifier = Modifier.align(Alignment.Center).size(20.dp)
                            )
                        }
                    }
                }
            }

            // Custom hex input
            var hexInput by remember(seedColorHex) { mutableStateOf(seedColorHex) }
            OutlinedTextField(
                value = hexInput,
                onValueChange = {
                    hexInput = it
                    if (it.matches(Regex("^#[0-9a-fA-F]{6}$"))) {
                        coroutineScope.launch { userStore.saveSeedColor(it) }
                    }
                },
                label = { Text("Manual Hex Code") },
                singleLine = true,
                placeholder = { Text("#1E88E5") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = customShape
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PreferenceItem(
                    title = "Monet Dynamic Colors",
                    subtitle = "Extract primary accents from phone wallpaper",
                    iconVector = Icons.Default.Wallpaper,
                    trailing = {
                        StyledSwitch(
                            style = activeSwitchStyle,
                            checked = dynamicColor,
                            onCheckedChange = {
                                triggerFeedback()
                                onToggleDynamicColor(it)
                            }
                        )
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            val qrUseEmojiOverlay by userStore.qrUseEmojiOverlay.collectAsState(initial = false)
            PreferenceItem(
                title = "QR Emoji Badges",
                subtitle = "Overlay signature emoji inside QR codes instead of app logo",
                iconVector = Icons.Default.QrCode,
                trailing = {
                    StyledSwitch(
                        style = activeSwitchStyle,
                        checked = qrUseEmojiOverlay,
                        onCheckedChange = {
                            triggerFeedback()
                            coroutineScope.launch { userStore.saveQrUseEmojiOverlay(it) }
                        }
                    )
                }
            )


        }

        // ── Group 3: Component Skins, Borders & Shadows ───────────────
        PreferenceGroup(
            title = "Borders & Shapes",
            expanded = expandedGroup == "Borders & Shapes",
            onHeaderClick = {
                triggerFeedback()
                expandedGroup = if (expandedGroup == "Borders & Shapes") null else "Borders & Shapes"
            }
        ) {
            PreferenceItem(
                title = "Switch Visual Skin",
                subtitle = "Active style: $switchStyleStr",
                iconVector = Icons.Default.ToggleOn
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("MaterialYou", "Cupertino", "Pixel", "Fluent", "LiquidGlass")) { style ->
                    SettingsFilterChip(
                        selected = switchStyleStr == style,
                        onClick = {
                            triggerFeedback()
                            coroutineScope.launch { userStore.saveSwitchStyle(style) }
                        },
                        label = style
                    )
                }
            }
            
            // Switch Preview Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Switch Preview", style = MaterialTheme.typography.bodyMedium)
                var previewChecked by remember { mutableStateOf(true) }
                StyledSwitch(
                    style = activeSwitchStyle,
                    checked = previewChecked,
                    onCheckedChange = {
                        triggerFeedback()
                        previewChecked = it
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Slider Visual Skin",
                subtitle = "Active style: $sliderStyleStr",
                iconVector = Icons.Default.Tune
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("MaterialYou", "Fancy", "HyperOS")) { style ->
                    SettingsFilterChip(
                        selected = sliderStyleStr == style,
                        onClick = {
                            triggerFeedback()
                            coroutineScope.launch { userStore.saveSliderStyle(style) }
                        },
                        label = style
                    )
                }
            }

            // Slider Preview Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("Slider Preview", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                var previewValue by remember { mutableStateOf(0.6f) }
                StyledSlider(
                    style = activeSliderStyle,
                    value = previewValue,
                    onValueChange = {
                        previewValue = it
                    },
                    valueRange = 0f..1f
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Corner Shape Style",
                subtitle = "Active style: $shapeStyleStr",
                iconVector = Icons.Default.RoundedCorner
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("Rounded", "Cut", "Squircle", "Smooth", "Octagon", "Pentagon", "Clover", "Star", "Heart")) { shape ->
                    SettingsFilterChip(
                        selected = shapeStyleStr == shape,
                        onClick = {
                            triggerFeedback()
                            coroutineScope.launch { userStore.saveShapeStyle(shape) }
                        },
                        label = shape
                    )
                }
            }

            // 3D Visual Style Preview Panel at the top of customization
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Visual Style Preview", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                val customShape = com.balajitechlabs.quickdash.core.ui.components.getCustomShape(shapeStyleStr, localRadius)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (showShadow) Modifier.shadow(8.dp, customShape) else Modifier)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), customShape)
                        .border(localBorderWidth.dp, MaterialTheme.colorScheme.primary, customShape),
                    shape = customShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("3D Card Preview", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Switch Skin", style = MaterialTheme.typography.bodySmall)
                            var previewChecked by remember { mutableStateOf(true) }
                            StyledSwitch(
                                style = activeSwitchStyle,
                                checked = previewChecked,
                                onCheckedChange = {
                                    triggerFeedback()
                                    previewChecked = it
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Slider Skin", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            var previewValue by remember { mutableStateOf(0.6f) }
                            StyledSlider(
                                style = activeSliderStyle,
                                value = previewValue,
                                onValueChange = { previewValue = it },
                                valueRange = 0f..1f
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Corner Radius Size",
                subtitle = "Radius: ${localRadius.toInt()}dp",
                iconVector = Icons.Default.Architecture
            )
            StyledSlider(
                style = activeSliderStyle,
                value = localRadius,
                onValueChange = {
                    localRadius = it
                },
                onValueChangeFinished = {
                    coroutineScope.launch { userStore.saveCornerRadius(localRadius) }
                },
                valueRange = 4f..32f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Border Line Thickness",
                subtitle = "Thickness: ${"%.1f".format(localBorderWidth)}dp",
                iconVector = Icons.Default.LineWeight
            )
            StyledSlider(
                style = activeSliderStyle,
                value = localBorderWidth,
                onValueChange = {
                    localBorderWidth = it
                },
                onValueChangeFinished = {
                    coroutineScope.launch { userStore.saveBorderWidth(localBorderWidth) }
                },
                valueRange = 0f..4f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Show Card Shadows",
                subtitle = "Master shadow overlay switch",
                iconVector = Icons.Default.FilterFrames,
                trailing = {
                    StyledSwitch(
                        style = activeSwitchStyle,
                        checked = showShadow,
                        onCheckedChange = {
                            triggerFeedback()
                            coroutineScope.launch { userStore.saveShowShadow(it) }
                        }
                    )
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Show Tool Descriptions",
                subtitle = "Render description text on dashboard cards",
                iconVector = Icons.Default.Description,
                trailing = {
                    StyledSwitch(
                        style = activeSwitchStyle,
                        checked = showToolDescriptions,
                        onCheckedChange = {
                            triggerFeedback()
                            coroutineScope.launch { userStore.saveShowToolDescriptions(it) }
                        }
                    )
                }
            )
        }
    }

    val column1Groups = @Composable {
        // ── Group 4: Typography & Fonts ───────────────────────────────
            PreferenceGroup(
            title = "Typography & Fonts",
            expanded = expandedGroup == "Typography & Fonts",
            onHeaderClick = {
                triggerFeedback()
                expandedGroup = if (expandedGroup == "Typography & Fonts") null else "Typography & Fonts"
            }
        ) {
            PreferenceItem(
                title = "Typography Font Size Scale",
                subtitle = "Scale factor: ${"%.2f".format(localFontScale)}x",
                iconVector = Icons.Default.FormatSize
            )
            StyledSlider(
                style = activeSliderStyle,
                value = localFontScale,
                onValueChange = {
                    localFontScale = it
                },
                onValueChangeFinished = {
                    coroutineScope.launch { userStore.saveFontScale(localFontScale) }
                },
                valueRange = 0.8f..1.4f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            PreferenceItem(
                title = "App Font Family",
                subtitle = "Active font: ${fontFamilyName.lowercase().replaceFirstChar { it.uppercase() }}",
                iconVector = Icons.Default.TextFields
            )
            val fontFamilies = listOf(
                "Default" to "SYSTEM",
                "Sans-Serif" to "SANSSERIF",
                "Serif" to "SERIF",
                "Monospace" to "MONOSPACE",
                "Cursive" to "CURSIVE",
                "Nunito" to "NUNITO",
                "Poppins" to "POPPINS",
                "Space Grotesk" to "SPACE_GROTESK"
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(fontFamilies) { (label, code) ->
                    SettingsFilterChip(
                        selected = fontFamilyName.uppercase() == code.uppercase(),
                        onClick = {
                            triggerFeedback()
                            coroutineScope.launch { userStore.saveFontFamilyKey(code) }
                        },
                        label = label
                    )
                }
            }
        }

        // ── Group 5: Behavior & Security Settings ─────────────────────
        PreferenceGroup(
            title = "Behavior & Security",
            expanded = expandedGroup == "Behavior & Security",
            onHeaderClick = {
                triggerFeedback()
                expandedGroup = if (expandedGroup == "Behavior & Security") null else "Behavior & Security"
            }
        ) {
            PreferenceItem(
                title = "Secure Mode",
                subtitle = "Blocks screenshots and app switching previews",
                iconVector = Icons.Default.Security,
                trailing = {
                    StyledSwitch(
                        style = activeSwitchStyle,
                        checked = secureMode,
                        onCheckedChange = {
                            coroutineScope.launch { userStore.saveSecureMode(it) }
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Max Brightness on QR",
                subtitle = "Force 100% brightness when showing QR Code",
                iconVector = Icons.Default.BrightnessHigh,
                trailing = {
                    StyledSwitch(
                        style = activeSwitchStyle,
                        checked = maxBrightness,
                        onCheckedChange = {
                            coroutineScope.launch { userStore.saveMaxBrightness(it) }
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Show Image Previews",
                subtitle = "Render images inline inside the notifications feed",
                iconVector = Icons.Default.Image,
                trailing = {
                    StyledSwitch(
                        style = activeSwitchStyle,
                        checked = showImagePreviews,
                        onCheckedChange = {
                            coroutineScope.launch { userStore.saveShowImagePreviews(it) }
                        }
                    )
                }
            )
            PreferenceItem(
                title = "Advanced Thumbnail",
                subtitle = "Enable advanced thumbnail options based on your preference",
                iconVector = Icons.Default.Image,
                trailing = {
                    StyledSwitch(
                        style = activeSwitchStyle,
                        checked = advancedThumbnail,
                        onCheckedChange = {
                            coroutineScope.launch { userStore.saveAdvancedThumbnail(it) }
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Custom Top Bar Emoji",
                subtitle = "Emoji: $emojiHeader",
                iconVector = Icons.Default.Mood
            )
            OutlinedTextField(
                value = emojiHeader,
                onValueChange = {
                    if (it.length <= 4) {
                        coroutineScope.launch { userStore.saveEmojiHeader(it) }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = customShape
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Confetti Effect Player",
                subtitle = "Toggle visual effects on successful actions",
                iconVector = Icons.Default.Celebration,
                trailing = {
                    StyledSwitch(
                        style = activeSwitchStyle,
                        checked = confettiEnabled,
                        onCheckedChange = {
                            triggerFeedback()
                            coroutineScope.launch { userStore.saveConfettiEnabled(it) }
                        }
                    )
                }
            )

            if (confettiEnabled) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PreferenceItem(
                    title = "Confetti Effect Style",
                    subtitle = "Select overlay style: $confettiType",
                    iconVector = Icons.Default.AutoAwesome
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("Default", "Right", "Corner", "Export")) { type ->
                        SettingsFilterChip(
                            selected = confettiType == type,
                            onClick = {
                                triggerFeedback()
                                coroutineScope.launch { userStore.saveConfettiType(type) }
                                onTriggerConfetti(type)
                            },
                            label = type
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Tactile Vibration Duration",
                subtitle = "Vibration length: ${hapticDuration.toInt()} ms ${if (hapticDuration <= 0f) "(Off)" else ""}",
                iconVector = Icons.Default.Vibration
            )
            Slider(
                value = hapticDuration,
                onValueChange = {
                    coroutineScope.launch { userStore.saveHapticDuration(it) }
                },
                valueRange = 0f..100f,
                steps = 20,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Clipboard Auto-Clean",
                subtitle = "Interval: $clipboardAutocleanInterval",
                iconVector = Icons.Default.CleaningServices
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("OFF", "1H", "12H", "1D")) { interval ->
                    SettingsFilterChip(
                        selected = clipboardAutocleanInterval == interval,
                        onClick = {
                            triggerFeedback()
                            coroutineScope.launch { userStore.setClipboardAutocleanInterval(interval) }
                        },
                        label = when (interval) {
                            "OFF" -> "Off"
                            "1H" -> "1 Hour"
                            "12H" -> "12 Hours"
                            "1D" -> "1 Day"
                            else -> interval
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Shake to Open Bubble",
                subtitle = "Shake device to quickly launch floating bubble",
                iconVector = Icons.Default.ScreenRotation,
                trailing = {
                    StyledSwitch(
                        style = activeSwitchStyle,
                        checked = shakeToTrigger,
                        onCheckedChange = {
                            triggerFeedback()
                            coroutineScope.launch { userStore.saveShakeToTrigger(it) }
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Custom Search Engines",
                subtitle = "Configure custom search engines in app",
                iconVector = Icons.Default.Search,
                onClick = {
                    triggerFeedback()
                    showCustomSearchDialog = true
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PreferenceItem(
                title = "Support Development (Donate)",
                subtitle = "Keep the project rockin'! 🎸",
                iconVector = Icons.Default.Favorite,
                onClick = {
                    triggerFeedback()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://razorpay.me/@balajitechlabs"))
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            val isAppLocked by userStore.isAppLocked.collectAsState(initial = false)
            PreferenceItem(
                title = "Biometric Lock",
                subtitle = "App lock protection switch",
                iconVector = Icons.Default.Lock,
                trailing = {
                    StyledSwitch(
                        style = activeSwitchStyle,
                        checked = isAppLocked,
                        onCheckedChange = { enabled ->
                            triggerFeedback()
                            coroutineScope.launch { userStore.setAppLocked(enabled) }
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            val isTabLocked by userStore.tabBiometricLock.collectAsState(initial = false)
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
                            coroutineScope.launch { userStore.saveTabBiometricLock(enabled) }
                        }
                    )
                }
            )
        }

        // ── Group 6: Data & Backup ─────────────────────────────────────
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
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Manage UPI & Payment IDs",
                subtitle = "Add, edit, or remove your payment UPI IDs and display name",
                iconVector = Icons.Default.Payment,
                onClick = {
                    triggerFeedback()
                    onManageUpiIds()
                }
            )
        }

        // ── Group: Recommendations & Tips ───────────────────────────────
        PreferenceGroup(
            title = "Usage Tips & Recommendations",
            expanded = expandedGroup == "Usage Tips & Recommendations",
            onHeaderClick = {
                triggerFeedback()
                expandedGroup = if (expandedGroup == "Usage Tips & Recommendations") null else "Usage Tips & Recommendations"
            }
        ) {
            PreferenceItem(
                title = "💡 View All Tips & Recommendations",
                subtitle = "Tap to see quick tips to get the most out of QuickDash",
                iconVector = Icons.Default.Lightbulb,
                onClick = {
                    triggerFeedback()
                    showTipsDialog = true
                }
            )
        }

        // ── Group 7: Stats & Developer Info ────────────────────────────
        PreferenceGroup(
            title = "About QuickDash",
            expanded = expandedGroup == "About QuickDash",
            onHeaderClick = {
                triggerFeedback()
                showAboutDialog = true
                expandedGroup = if (expandedGroup == "About QuickDash") null else "About QuickDash"
            }
        ) {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            PreferenceItem(
                title = "About QuickDash",
                subtitle = "QuickDash $versionName · Check for updates, version info",
                iconVector = Icons.Default.Info,
                onClick = {
                    triggerFeedback()
                    showAboutDialog = true
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "What's New",
                subtitle = "View the latest features and bug fixes",
                iconVector = Icons.Default.NewReleases,
                onClick = {
                    triggerFeedback()
                    showWhatsNewDialog = true
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Privacy Policy",
                subtitle = "View our data collection policy",
                iconVector = Icons.Default.PrivacyTip,
                onClick = {
                    triggerFeedback()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Balajitechlabs/quickdash/blob/main/PRIVACY_POLICY.md"))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Rate QuickDash",
                subtitle = "Tell us what you think!",
                iconVector = Icons.Default.Star,
                onClick = {
                    triggerFeedback()
                    showRatingDialog = true
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "App License",
                subtitle = "View open source license guidelines",
                iconVector = Icons.Default.Gavel,
                onClick = {
                    triggerFeedback()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Balajitechlabs/quickdash/blob/main/LICENSE"))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Project Changelog",
                subtitle = "View full release updates history",
                iconVector = Icons.Default.HistoryEdu,
                onClick = {
                    triggerFeedback()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Balajitechlabs/quickdash/blob/main/CHANGELOG.md"))
                    context.startActivity(intent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "App Statistics",
                subtitle = "View your usage statistics",
                iconVector = Icons.Default.BarChart,
                onClick = { showStatsDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Bug Reporter",
                subtitle = "Submit issue report directly to dev logs",
                iconVector = Icons.Default.BugReport,
                onClick = { showFeedbackDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            PreferenceItem(
                title = "Request a Feature",
                subtitle = "Request features or enhancements",
                iconVector = Icons.Default.Lightbulb,
                onClick = { showFeatureRequestDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            var isLoggingActive by remember { mutableStateOf(com.balajitechlabs.quickdash.core.utils.DiagnosticLogger.isActive()) }
            PreferenceItem(
                title = "Diagnostic Logs (Crash Reporter)",
                subtitle = if (isLoggingActive) "Recording active process logcat..." else "Record diagnostic logs for developer crash reports",
                iconVector = Icons.Default.BugReport,
                onClick = {
                    triggerFeedback()
                    if (isLoggingActive) {
                        val logFile = com.balajitechlabs.quickdash.core.utils.DiagnosticLogger.stopLogging(context)
                        if (logFile != null) {
                            try {
                                val authority = "${context.packageName}.provider"
                                val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, logFile)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Diagnostic Log"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                                android.widget.Toast.makeText(context, "Failed to share log", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        isLoggingActive = false
                    } else {
                        com.balajitechlabs.quickdash.core.utils.DiagnosticLogger.startLogging(context)
                        isLoggingActive = true
                    }
                }
            )

            // Developer Logs - Prominent Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        triggerFeedback()
                        try { onNavigateToSystemLogs() } catch (e: Exception) { e.printStackTrace() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Developer Logs", fontWeight = FontWeight.SemiBold)
                }
            }

            // 7 interactive star buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val starLabels = listOf("⭐", "⭐", "⭐", "🎸", "⭐", "⭐", "⭐")
                starLabels.forEachIndexed { index, emoji ->
                    IconButton(
                        onClick = {
                            triggerFeedback()
                            if (index == 3) {
                                // Guitar special star
                                android.widget.Toast.makeText(context, "🎸 Rock on!", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                showRatingDialog = true
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        }

        if (isTabletOrLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    column0Groups()
                }
                Column(modifier = Modifier.weight(1f)) {
                    column1Groups()
                }
            }
        } else {
            column0Groups()
            column1Groups()
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Made by ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "BalajiTechLabs",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/balajitechlabs"))
                    context.startActivity(intent)
                }
            )
        }


    }

    if (showWhatsNewDialog) {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName ?: "3.2.0"
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
                                if (attachScreenshot) "📸 Screenshot attached" else "Attach screenshot (optional)",
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
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = customShape
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (galleryBitmap != null) "✅ Image from Gallery attached" else "Upload from Gallery",
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
                                val message = "🐞 <b>Bug Report</b>\nModel: ${Build.MODEL}\nReport: $safeFeedback"
                                if (capturedBitmap != null) {
                                    com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker.sendPhoto(
                                        caption = message.replace(Regex("<[^>]*>"), ""),
                                        bitmap = capturedBitmap
                                    )
                                } else {
                                    com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker.sendMessage(message)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
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
                            com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker.sendMessage("💡 <b>Feature Request</b>\nIdea: $safeIdea")
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
                                            userStore.saveCustomSearchEngines(gson.toJson(updated))
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
                            userStore.saveCustomSearchEngines(gson.toJson(updated))
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
                                val message = "🌟 <b>New App Rating</b>\nStars: $selectedStars⭐\nReview: ${if (reviewText.isBlank()) "None" else reviewText}\nDevice: ${Build.MODEL}"
                                com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker.sendBroadcastBotMessage(message)
                                // Record rating stat
                                userStore.incrementAppOpens()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        showRatingDialog = false
                        android.widget.Toast.makeText(context, "Thank you for your rating! ⭐", android.widget.Toast.LENGTH_SHORT).show()
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
                            Text("Version $vNameAbout (Build $vCodeAbout)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            Text("Made with ❤️ by BalajiTechLabs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
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
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.Idle -> {
                                    Text("Status: Up to date ✅", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Button(
                                        onClick = {
                                            try { com.balajitechlabs.quickdash.core.utils.UpdateManager.checkForUpdates(context, manual = true) } catch (e: Exception) { e.printStackTrace() }
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
                                    Text("Error checking updates ❌", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    Text(updateState.message, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                                    Button(
                                        onClick = {
                                            try { com.balajitechlabs.quickdash.core.utils.UpdateManager.checkForUpdates(context, manual = true) } catch (e: Exception) { e.printStackTrace() }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Retry Check")
                                    }
                                }
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.UpdateAvailable -> {
                                    Text("New Update Available! 🎉\nVersion v${updateState.versionName}", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Button(
                                        onClick = {
                                            try { com.balajitechlabs.quickdash.core.utils.UpdateManager.startDownload(context, updateState.apkUrl, updateState.versionName) } catch (e: Exception) { e.printStackTrace() }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Download v${updateState.versionName}")
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
                                    Text("Update ready to install! 📦", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                    Button(
                                        onClick = {
                                            try { com.balajitechlabs.quickdash.core.utils.UpdateManager.installApk(context, updateState.fileName) } catch (e: Exception) { e.printStackTrace() }
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

    // ── Update Check Dialog ───────────────────────────────────────────────
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("🔄 Check for Updates", fontWeight = FontWeight.Bold) },
            text = { Text("Would you like to check for the latest version of QuickDash?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = {
                    showUpdateDialog = false
                    try { com.balajitechlabs.quickdash.core.utils.UpdateManager.checkForUpdates(context, manual = true) } catch (e: Exception) { e.printStackTrace() }
                }) { Text("Check Now") }
            },
            dismissButton = { TextButton(onClick = { showUpdateDialog = false }) { Text("Later") } }
        )
    }

    // ── Tips & Recommendations Dialog ────────────────────────────────────
    if (showTipsDialog) {
        val tips = listOf(
            "🔑 GitHub Rate Limit" to "Generate a Personal Access Token on GitHub (Settings → Developer Settings → Tokens) and paste it in Advanced & API Settings. Raises limit from 60 to 5,000 requests/hour.",
            "📱 Social Link Routing" to "Social media profile links open natively in their apps when installed. On emulators, they fallback to your browser automatically.",
            "🛡️ Play Protect" to "For sideloaded APKs, tap 'More details → Install anyway' on the Play Protect prompt. The Play Store version is auto-trusted.",
            "📸 QR Scanner First Load" to "First QR scan may show a brief overlay — Google Play Services sets up the barcode engine once. Subsequent scans are instant.",
            "💾 Backup Your Data" to "Use Data Management → Backup Data to export all settings, notes, and clipboard history as a JSON file before switching phones.",
            "🌙 Save Battery" to "Switch to AMOLED theme in Launch & Windows for true-black backgrounds that save battery on OLED displays.",
            "🔔 Manage Notifications" to "Swipe LEFT on any notification to dismiss it. Swipe RIGHT to pin it to the top of the feed for quick access.",
            "⚡ Quick Collect" to "Set your Default Target Payment App in Advanced & API Settings to pre-select GPay, PhonePe, or Paytm for faster QR generation.",
            "🎨 Custom Seed Color" to "Use the seed color picker in Appearance to create a unique color theme applied across the entire app.",
            "⏱️ Timer Persistence" to "Countdown timers use AlarmManager exact alarms — they continue even in deep Doze mode when the phone is idle.",
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
        val scope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { showBackupOptionsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Export / Share Backup", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose how you want to export your settings and data:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Option 1: Save Local File
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            showBackupOptionsDialog = false
                            val time = System.currentTimeMillis() / 1000
                            backupLauncher.launch("quickdash_backup_$time.json")
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Save Local File", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Save JSON file directly to device storage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option 2: Share via WhatsApp
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            showBackupOptionsDialog = false
                            scope.launch {
                                try {
                                    val jsonString = com.balajitechlabs.quickdash.core.utils.BackupRestoreManager.getBackupJsonString(context)
                                    shareBackupFile(context, jsonString, "com.whatsapp")
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Share via WhatsApp", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Send JSON backup directly to a chat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option 3: Share via Telegram
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            showBackupOptionsDialog = false
                            scope.launch {
                                try {
                                    val jsonString = com.balajitechlabs.quickdash.core.utils.BackupRestoreManager.getBackupJsonString(context)
                                    shareBackupFile(context, jsonString, "org.telegram.messenger")
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF2196F3))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Share via Telegram", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Send JSON backup to a channel/chat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Option 4: General Share
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            showBackupOptionsDialog = false
                            scope.launch {
                                try {
                                    val jsonString = com.balajitechlabs.quickdash.core.utils.BackupRestoreManager.getBackupJsonString(context)
                                    shareBackupFile(context, jsonString, null)
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Share File...", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Send using other messaging/cloud apps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupOptionsDialog = false }) { Text("Cancel") }
            }
        )
    }
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
        e.printStackTrace()
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
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Could not open target app. Falling back to share chooser.", android.widget.Toast.LENGTH_SHORT).show()
        if (targetPackage != null) {
            shareBackupFile(context, jsonString, null)
        }
    }
}
