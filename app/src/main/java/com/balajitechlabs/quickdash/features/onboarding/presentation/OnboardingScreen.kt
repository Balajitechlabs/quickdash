package com.balajitechlabs.quickdash.features.onboarding.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.MainActivity
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.AnimatedStepTransition
import com.balajitechlabs.quickdash.features.onboarding.presentation.steps.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit
import androidx.compose.ui.zIndex

/**
 * Onboarding flow orchestrator.
 *
 * Step mapping (7 content steps + splash):
 *   0 = Splash (auto-advances)
 *   1 = Welcome
 *   2 = Permissions (notifications + location in one screen)
 *   3 = Payment Setup (UPI / PayPal)
 *   4 = Theme Selection
 *   5 = Shape & Border
 *   6 = Font & Typography
 *   7 = Completion
 */
@Composable
fun OnboardingScreen(
    userStore: UserStore,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(0) }
    var showConfetti by remember { mutableStateOf(false) }
    val confettiEnabled by userStore.confettiEnabled.collectAsState(initial = true)

    // --- Persisted state ---
    val savedUpiIds by userStore.upiIds.collectAsState(initial = emptyList())
    val savedDefaultUpiId by userStore.defaultUpiId.collectAsState(initial = null)
    val savedPayeeName by userStore.payeeName.collectAsState(initial = null)
    val themeMode by userStore.themeMode.collectAsState(initial = "SYSTEM")
    val shapeStyle by userStore.shapeStyle.collectAsState(initial = "Rounded")
    val cornerRadius by userStore.cornerRadius.collectAsState(initial = 16f)
    val borderWidth by userStore.borderWidth.collectAsState(initial = 1f)
    val fontFamilyName by userStore.fontFamilyKey.collectAsState(initial = "SYSTEM")
    val fontScale by userStore.fontScale.collectAsState(initial = 1f)

    // --- Ephemeral permission state ---
    var notifGranted by remember { mutableStateOf(false) }
    var locationGranted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedStepTransition(currentStep = step) {
            when (step) {
                0 -> SplashStep(onFinished = { step = 1 })

                1 -> WelcomeStep(onNext = { step = 2 })

                2 -> PermissionsStep(
                    onBack = { step = 1 },
                    onNext = { step = 3 },
                    onSkip = { step = 3 },
                    notifGranted = notifGranted,
                    locationGranted = locationGranted,
                    onNotifGranted = { notifGranted = true },
                    onLocationGranted = { locationGranted = true }
                )

                3 -> PaymentSetupStep(
                    upiIds = savedUpiIds,
                    defaultUpiId = savedDefaultUpiId,
                    payeeName = savedPayeeName,
                    onSave = { ids, name, defaultId ->
                        scope.launch {
                            userStore.saveUpiIds(ids)
                            userStore.saveDefaultUpiId(defaultId)
                            userStore.savePayeeName(name)
                            step = 4
                        }
                    },
                    onBack = { step = 2 },
                    onSkip = { step = 4 }
                )

                4 -> ThemeStep(
                    currentTheme = themeMode,
                    onThemeSelected = { code ->
                        scope.launch { userStore.saveThemeMode(code) }
                    },
                    onBack = { step = 3 },
                    onNext = { step = 5 }
                )

                5 -> ShapeStep(
                    currentShape = shapeStyle,
                    currentRadius = cornerRadius,
                    currentBorder = borderWidth,
                    onShapeSelected = { style ->
                        scope.launch { userStore.saveShapeStyle(style) }
                    },
                    onRadiusChanged = { radius ->
                        scope.launch { userStore.saveCornerRadius(radius) }
                    },
                    onBorderChanged = { border ->
                        scope.launch { userStore.saveBorderWidth(border) }
                    },
                    onBack = { step = 4 },
                    onNext = { step = 6 }
                )

                6 -> FontStep(
                    currentFontFamily = fontFamilyName,
                    currentFontScale = fontScale,
                    shapeStyle = shapeStyle,
                    cornerRadius = cornerRadius,
                    borderWidth = borderWidth,
                    onFontFamilySelected = { code ->
                        scope.launch { userStore.saveFontFamilyKey(code) }
                    },
                    onFontScaleChanged = { scale ->
                        scope.launch { userStore.saveFontScale(scale) }
                    },
                    onBack = { step = 5 },
                    onNext = { step = 7 }
                )

                7 -> {
                    LaunchedEffect(Unit) {
                        if (confettiEnabled) {
                            delay(600)
                            showConfetti = true
                            delay(3000)
                            showConfetti = false
                        }
                    }

                    CompletionStep(
                        notifGranted = notifGranted,
                        locationGranted = locationGranted,
                        upiConfigured = savedUpiIds.isNotEmpty(),
                        themeMode = when (themeMode) {
                            "LIGHT" -> "Light"
                            "DARK" -> "Dark"
                            "AMOLED" -> "AMOLED Black"
                            else -> "System"
                        },
                        shapeStyle = shapeStyle,
                        fontName = fontFamilyName.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        onLaunch = {
                            sendWelcomeNotification(context)
                            onComplete()
                        }
                    )
                }
            }
        }

        // Confetti overlay
        if (showConfetti) {
            val party = Party(
                speed = 0f,
                maxSpeed = 60f,
                damping = 0.9f,
                angle = 0,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                emitter = Emitter(duration = 300, TimeUnit.MILLISECONDS).max(300),
                position = Position.Relative(0.5, 0.4),
                size = listOf(Size(24, 5f), Size(32, 5f))
            )

            KonfettiView(
                modifier = Modifier.fillMaxSize().zIndex(10f),
                parties = listOf(party),
                updateListener = object : OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(
                        system: nl.dionsegijn.konfetti.core.PartySystem,
                        activeSystems: Int
                    ) {
                        if (activeSystems == 0) {
                            showConfetti = false
                        }
                    }
                }
            )
        }
    }
}

private fun sendWelcomeNotification(context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "quickdash_welcome"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Welcome Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Welcome message after setup"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = android.app.PendingIntent.getActivity(
        context, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
    )

    val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_quickdash_tile)
        .setContentTitle("Welcome to QuickDash!")
        .setContentText("Your floating hub is ready. Tap here to start exploring!")
        .setStyle(
            androidx.core.app.NotificationCompat.BigTextStyle()
                .bigText("Your floating hub is ready. Tap here to start exploring your new superpowers!")
        )
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)

    try {
        notificationManager.notify(1001, builder.build())
    } catch (_: SecurityException) {
        // Notification permission not granted
    }
}
