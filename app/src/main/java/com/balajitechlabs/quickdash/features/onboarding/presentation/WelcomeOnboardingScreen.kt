package com.balajitechlabs.quickdash.features.onboarding.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import com.google.gson.Gson
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * 👋 Non-Tech Interactive Onboarding & Master Setup Wizard (`WelcomeOnboardingScreen.kt`).
 * 7-Step guided setup with Unified Permission Hub & direct DataStore state persistence.
 */
@Composable
fun WelcomeOnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userStore = remember { UserStore(context) }
    val pagerState = rememberPagerState(pageCount = { 7 })

    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        )
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showConfetti by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val updatedOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
                val updatedNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else true
                val updatedCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                val updatedAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

                if (updatedOverlay && !hasOverlayPermission) {
                    hasOverlayPermission = true
                    showConfetti = true
                }
                hasNotificationPermission = updatedNotif
                hasCameraPermission = updatedCam
                hasAudioPermission = updatedAudio
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) Toast.makeText(context, "Notification permission granted! 🔔", Toast.LENGTH_SHORT).show()
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) Toast.makeText(context, "Camera permission granted! 📸", Toast.LENGTH_SHORT).show()
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) Toast.makeText(context, "Audio capture permission granted! 🎙️", Toast.LENGTH_SHORT).show()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Title & Step Counter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QuickDash",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Step ${pagerState.currentPage + 1} of 7",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Pager Content — userScrollEnabled=false ensures users go through each step
                // via the Next button and cannot swipe past mandatory permission steps
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) { page ->
                    when (page) {
                        0 -> OnboardingStep1Welcome()
                        1 -> OnboardingStep2PermissionsHub(
                            hasOverlay = hasOverlayPermission,
                            hasNotification = hasNotificationPermission,
                            hasCamera = hasCameraPermission,
                            hasAudio = hasAudioPermission,
                            onRequestOverlay = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                }
                            },
                            onRequestNotification = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    Toast.makeText(context, "Notifications active!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onRequestCamera = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            onRequestAudio = {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )
                        2 -> OnboardingStep3PaymentSetup(userStore = userStore)
                        3 -> OnboardingStep4PinTools(userStore = userStore)
                        4 -> OnboardingStep5ThemePicker(userStore = userStore)
                        5 -> OnboardingStep6GestureSandbox()
                        6 -> OnboardingStep7GetStarted(onFinish = onFinishOnboarding)
                    }
                }

                // Bottom Navigation & Pager Indicator Dots
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(7) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateFloatAsState(
                                targetValue = if (isSelected) 24f else 8f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "dotWidth"
                            )
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(width.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (pagerState.currentPage < 6) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { onFinishOnboarding() },
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text("Skip")
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                },
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text("Next")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null)
                            }
                        }
                    } else {
                        Button(
                            onClick = onFinishOnboarding,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "LAUNCH QUICKDASH 🚀",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                }
            }
        }

        // Confetti Burst Overlay
        if (showConfetti) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(
                    Party(
                        speed = 0f,
                        maxSpeed = 30f,
                        damping = 0.9f,
                        spread = 360,
                        colors = listOf(0xf1c40f, 0xe74c3c, 0x3498db, 0x2ecc71),
                        position = Position.Relative(0.5, 0.3),
                        emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(80)
                    )
                )
            )
        }
    }
}

@Composable
private fun OnboardingStep1Welcome() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Layers,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to QuickDash!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your 15 floating micro-tools available anywhere, over any application, with a single tap.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        RoundedCardContainer {
            Column(modifier = Modifier.padding(16.dp)) {
                FeatureHighlightItem(
                    icon = Icons.Filled.TouchApp,
                    title = "Customizable Radial Bubble Wheel",
                    desc = "Long-press the bubble (350ms) to launch your favorite 4 shortcuts. Choose your favorite tools in Settings."
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureHighlightItem(
                    icon = Icons.Filled.Layers,
                    title = "1-Tap Quick Tile & Glance Widgets",
                    desc = "Turn the floating bubble on or off in 1 tap from Control Center or your Material You home screen widget."
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureHighlightItem(
                    icon = Icons.Filled.Palette,
                    title = "12 On-Demand Micro-Tools",
                    desc = "UPI QR, Translator, Quick Notes, Calculator, Timer, Wi-Fi Share & AES-256 Encrypted Backups."
                )
            }
        }
    }
}

/**
 * 🛡️ Step 2: Unified Permission Hub ("All Permissions in One Place")
 */
@Composable
private fun OnboardingStep2PermissionsHub(
    hasOverlay: Boolean,
    hasNotification: Boolean,
    hasCamera: Boolean,
    hasAudio: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestCamera: () -> Unit,
    onRequestAudio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.LockOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Unified Permissions Hub",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Text(
            text = "All required & optional permissions in one place. Manage them effortlessly below.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoundedCardContainer {
            Column(modifier = Modifier.padding(12.dp)) {
                // 1. Overlay Permission [Required]
                PermissionRowItem(
                    title = "Draw Over Other Apps",
                    subtitle = "Required to render the floating bubble overlay",
                    icon = Icons.Filled.Layers,
                    isGranted = hasOverlay,
                    isMandatory = true,
                    onGrantClick = onRequestOverlay
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Notification Permission [Recommended]
                PermissionRowItem(
                    title = "Push Notifications",
                    subtitle = "For Quick Settings tile & instant update alerts",
                    icon = Icons.Filled.Notifications,
                    isGranted = hasNotification,
                    isMandatory = false,
                    onGrantClick = onRequestNotification
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Camera Scanner [Optional]
                PermissionRowItem(
                    title = "Camera Access",
                    subtitle = "Required for QR code & Barcode scanner tool",
                    icon = Icons.Filled.CameraAlt,
                    isGranted = hasCamera,
                    isMandatory = false,
                    onGrantClick = onRequestCamera
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Audio & Screen Capture [Optional]
                PermissionRowItem(
                    title = "Audio & Screen Capture",
                    subtitle = "For Quick Capture screen recorder & doodle tool",
                    icon = Icons.Filled.Mic,
                    isGranted = hasAudio,
                    isMandatory = false,
                    onGrantClick = onRequestAudio
                )
            }
        }
    }
}

@Composable
private fun PermissionRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isGranted: Boolean,
    isMandatory: Boolean,
    onGrantClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGranted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (isMandatory) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "*Required",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Grant",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

/**
 * 💳 Step 3: Quick Collect Setup (UPI / PayPal ID)
 */
@Composable
private fun OnboardingStep3PaymentSetup(userStore: com.balajitechlabs.quickdash.core.data.UserStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var upiInput by remember { mutableStateOf("") }
    var payeeInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "💳 Quick Collect Setup",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Enter your primary UPI ID or PayPal username to generate instant payment QR codes.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        RoundedCardContainer {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = payeeInput,
                    onValueChange = {
                        payeeInput = it
                        scope.launch { userStore.savePayeeName(it) }
                    },
                    label = { Text("Your Name / Business Name") },
                    placeholder = { Text("e.g. Balaji Tech Labs") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = upiInput,
                    onValueChange = {
                        upiInput = it
                        if (it.isNotBlank()) {
                            scope.launch { userStore.saveUpiIds(listOf(it)) }
                        }
                    },
                    label = { Text("UPI ID or PayPal Handle") },
                    placeholder = { Text("e.g. name@upi or paypal.me/handle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (payeeInput.isNotBlank()) userStore.savePayeeName(payeeInput)
                            if (upiInput.isNotBlank()) userStore.saveUpiIds(listOf(upiInput))
                            Toast.makeText(context, "Payment details saved! 💳", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Payment Details")
                }
            }
        }
    }
}

/**
 * ⭐ Step 4: Favorite Tools Pinning
 */
@Composable
private fun OnboardingStep4PinTools(userStore: com.balajitechlabs.quickdash.core.data.UserStore) {
    val scope = rememberCoroutineScope()
    val launchStyle by userStore.launchStyle.collectAsState(initial = "FULL_SCREEN")
    val initialPinnedJson by userStore.pinnedToolsJson.collectAsState(initial = "[]")

    val allTools = remember {
        listOf(
            "Quick Collect", "Quick Chat", "Smart Clipboard", "Quick Notes",
            "Quick Eyedropper", "Quick Pomodoro", "Quick Password", "Quick Translate",
            "Quick Calculator", "Quick Wi-Fi", "Quick Converter", "Quick Capture"
        )
    }
    val selectedTools = remember { mutableStateListOf<String>() }

    LaunchedEffect(initialPinnedJson) {
        val gson = Gson()
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        val list: List<String> = try {
            gson.fromJson(initialPinnedJson, type) ?: listOf("Quick Collect", "Smart Clipboard", "Quick Notes")
        } catch (_: Exception) {
            listOf("Quick Collect", "Smart Clipboard", "Quick Notes")
        }
        selectedTools.clear()
        selectedTools.addAll(list.ifEmpty { listOf("Quick Collect", "Smart Clipboard", "Quick Notes") })
    }

    fun syncPinnedTools() {
        scope.launch {
            val gson = Gson()
            userStore.savePinnedToolsJson(gson.toJson(selectedTools.toList()))
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⭐ Pin Favorites & Display Format",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Choose your preferred layout mode and pin your top tools for priority access.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 📱 Launch Display Format Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Launch Display Mode",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { scope.launch { userStore.saveLaunchStyle("FULL_SCREEN") } },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (launchStyle == "FULL_SCREEN") ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("📱 Full App", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { scope.launch { userStore.saveLaunchStyle("FLOATING_DIALOG") } },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (launchStyle == "FLOATING_DIALOG") ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                        else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("🪟 Floating Card", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            items(allTools) { tool ->
                val isSelected = selectedTools.contains(tool)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) {
                                selectedTools.remove(tool)
                            } else if (selectedTools.size < 6) {
                                selectedTools.add(tool)
                            }
                            syncPinnedTools()
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = tool,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🎛️ Step 5: Bubble Theme & Size Quick-Pick
 */
@Composable
private fun OnboardingStep5ThemePicker(userStore: com.balajitechlabs.quickdash.core.data.UserStore) {
    val scope = rememberCoroutineScope()
    var bubbleSize by remember { mutableFloatStateOf(64f) }
    var opacity by remember { mutableFloatStateOf(0.9f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎛️ Customize Floating Bubble",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Set your preferred bubble size & transparency.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live Preview Bubble
        Box(
            modifier = Modifier
                .size(bubbleSize.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = opacity)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Layers,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size((bubbleSize * 0.45f).dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        RoundedCardContainer {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Bubble Size: ${bubbleSize.toInt()} dp",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = bubbleSize,
                    onValueChange = {
                        bubbleSize = it
                        scope.launch { userStore.saveBubbleSizeDp(it) }
                    },
                    valueRange = 48f..84f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Glass Opacity: ${(opacity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = opacity,
                    onValueChange = {
                        opacity = it
                        scope.launch { userStore.saveBubbleOpacityAlpha(it) }
                    },
                    valueRange = 0.3f..1.0f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 🖐️ Step 6: Interactive Gesture Practice Sandbox
 */
@Composable
private fun OnboardingStep6GestureSandbox() {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Gesture Practice Sandbox 🖐️",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Try dragging the bubble below! In actual use, it snaps to screen edges automatically.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.TouchApp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * 🎉 Step 7: Launch & Finish
 */
@Composable
private fun OnboardingStep7GetStarted(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉 You're All Set!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "QuickDash is fully configured and ready. Tap below to launch your floating productivity dashboard!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeatureHighlightItem(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
