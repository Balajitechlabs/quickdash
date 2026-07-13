package com.balajitechlabs.quickdash.features.onboarding.presentation.steps

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.PermissionExplanationCard
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.OnboardingScaffold

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
        stepTitle = "Enable Smart Features",
        stepSubtitle = "These permissions unlock specific features. You can change them anytime in Settings.",
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
            description = "Payment confirmations, update alerts, and sync progress.",
            whyTitle = "Why we need this",
            whyExplanation = "QuickDash sends overlay notifications when payments succeed, when new versions are available, and to show real-time sync status. Notifications are never used for ads or marketing.",
            isGranted = notifGranted,
            onGrantClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onNotifGranted()
                }
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        PermissionExplanationCard(
            icon = Icons.Default.LocationOn,
            title = "Location",
            description = "Read your Wi-Fi network name to share it via QR code.",
            whyTitle = "Why we need this",
            whyExplanation = "Android requires Location permission to access Wi-Fi SSID (network name). QuickDash does NOT track, store, or transmit your GPS location. This is purely for Wi-Fi sharing.",
            isGranted = locationGranted,
            onGrantClick = {
                locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        val bothGranted = notifGranted && locationGranted

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = if (bothGranted) "Continue" else "Continue Without",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
