package com.balajitechlabs.quickdash.features.onboarding.presentation.steps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.OnboardingScaffold
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.PermissionExplanationCard

@Composable
fun PermissionsStep(
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    notifGranted: Boolean,
    locationGranted: Boolean,
    onNotifGranted: () -> Unit,
    onLocationGranted: () -> Unit
) {
    val context = LocalContext.current
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onNotifGranted()
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onLocationGranted()
    }

    OnboardingScaffold(
        stepTitle = "Permissions & System Setup",
        stepSubtitle = "Configure system permissions for floating widgets and Quick Settings tiles.",
        currentStep = 1,
        totalSteps = 7,
        showBack = true,
        showSkip = true,
        onBack = onBack,
        onSkip = onSkip
    ) {
        PermissionExplanationCard(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            description = "Instant alerts for payment QR confirmations & feature updates.",
            whyTitle = "Why we need this",
            whyExplanation = "QuickDash sends overlay notifications when payments succeed and when sync events complete. We never send ad or marketing notifications.",
            isGranted = notifGranted,
            onGrantClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onNotifGranted()
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionExplanationCard(
            icon = Icons.Default.Layers,
            title = "Floating Display Window",
            description = "Draw overlay widgets over other apps for 1-tap quick access.",
            whyTitle = "Why we need this",
            whyExplanation = "Required for QuickDash floating bubbles and mini-widget launcher. Granting this allows QuickDash to appear over any app.",
            isGranted = overlayGranted,
            onGrantClick = {
                if (!overlayGranted) {
                    try {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        context.startActivity(intent)
                        overlayGranted = Settings.canDrawOverlays(context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionExplanationCard(
            icon = Icons.Default.LocationOn,
            title = "Wi-Fi Location Access",
            description = "Read your current Wi-Fi SSID network name to create shareable QR codes.",
            whyTitle = "Why we need this",
            whyExplanation = "Android system rules require Location permission to read Wi-Fi network names. QuickDash never tracks, stores, or transmits your GPS location.",
            isGranted = locationGranted,
            onGrantClick = {
                locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Continue Setup",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
