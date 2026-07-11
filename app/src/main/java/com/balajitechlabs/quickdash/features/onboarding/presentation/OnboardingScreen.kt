package com.balajitechlabs.quickdash.features.onboarding.presentation

import com.balajitechlabs.quickdash.features.qr.presentation.SetupScreen
import com.balajitechlabs.quickdash.core.utils.GoogleDriveSyncManager

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.MainActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.theme.getGoogleFontFamily

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit
import com.balajitechlabs.quickdash.core.data.UserStore
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send

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

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        step = 3
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        step = 4
    }


    val savedUpiIds by userStore.upiIds.collectAsState(initial = emptyList())
    val activeIds = savedUpiIds
    
    val savedDefaultUpiId by userStore.defaultUpiId.collectAsState(initial = null)
    val activeDefaultId = savedDefaultUpiId ?: savedUpiIds.firstOrNull() ?: ""
    val savedPayeeName by userStore.payeeName.collectAsState(initial = null)

    val themeMode by userStore.themeMode.collectAsState(initial = "AMOLED")
    val shapeStyle by userStore.shapeStyle.collectAsState(initial = "Rounded")
    val cornerRadius by userStore.cornerRadius.collectAsState(initial = 16f)
    val borderWidth by userStore.borderWidth.collectAsState(initial = 1f)
    val fontFamilyName by userStore.fontFamilyKey.collectAsState(initial = "system")
    val fontScale by userStore.fontScale.collectAsState(initial = 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .then(
                    if (step != 4 && step != 0) Modifier.verticalScroll(rememberScrollState())
                    else Modifier
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val stepTitle = when (step) {
                1 -> "Welcome to QuickDash"
                2 -> "Notification Alerts"
                3 -> "Location Services"
                4 -> "Quick Collect Setup"
                5 -> "Theme Mode"
                6 -> "Corners & Shapes"
                7 -> "App Typography"
                8 -> "Setup Complete!"
                else -> ""
            }

            if (step in 2..7) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { step-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back Step",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { (step - 2) / 5f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Step ${step - 1} of 6: $stepTitle",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            when (step) {
                0 -> {
                    // Step 0: Splash Screen
                    val scaleVal = remember { Animatable(0.5f) }
                    val alphaVal = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        launch {
                            scaleVal.animateTo(1f, animationSpec = tween(1200, easing = EaseOutBack))
                        }
                        launch {
                            alphaVal.animateTo(1f, animationSpec = tween(1000))
                        }
                        delay(2500)
                        step = 1
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scaleVal.value,
                                scaleY = scaleVal.value,
                                alpha = alphaVal.value
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier
                                .size(140.dp)
                                .shadow(16.dp, RoundedCornerShape(32.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "QuickDash Logo",
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "QuickDash",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your Ultimate Productivity Hub",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(60.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Made with ❤️ by balajitechlabs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
                1 -> {
                    // Step 1: Welcome to QuickDash
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.size(72.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(id = R.drawable.app_logo),
                                        contentDescription = "Logo",
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Welcome to QuickDash",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Your ultimate productivity dashboard for floating widgets, fast actions, QR payments, and customization.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Feature list
                            val features = listOf(
                                Icons.Default.QrCode to "Quick Collect QR Payments" to "Predefine UPI targets & pay instantly.",
                                Icons.Default.ContentPaste to "Smart Clipboard Rules" to "Auto-process & pin links, emails, and phone numbers.",
                                Icons.Default.Search to "Quick Search Hub" to "Search across multiple web engines with auto suggestions.",
                                Icons.Default.Send to "Direct Chat Redirects" to "Direct message contacts via WhatsApp or Telegram."
                            )
                            
                            features.forEach { (pair, desc) ->
                                val (icon, title) = pair
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Get Started",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                2 -> {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        "Stay Updated",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "QuickDash uses notifications to send updates, alert overlays, and sync progress cleanly. Please grant notifications access.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                step = 3
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Allow Notifications")
                    }
                    TextButton(
                        onClick = { step = 3 },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Skip for now")
                    }
                }
                3 -> {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        "Location Services",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Android requires Location permission to read connected Wi-Fi credentials (SSID) for sharing network details via QR Code.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = {
                            locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Allow Location")
                    }
                    TextButton(
                        onClick = { step = 4 },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Skip for now")
                    }
                }
                4 -> {
                    // Step 4: Quick Collect setup
                    SetupScreen(
                        upiIds = activeIds,
                        defaultUpiId = activeDefaultId,
                        payeeName = savedPayeeName,
                        onSaveUpiIds = { ids, name, defaultId ->
                            scope.launch {
                                userStore.saveUpiIds(ids)
                                userStore.saveDefaultUpiId(defaultId)
                                userStore.savePayeeName(name)
                                step = 5
                            }
                        }
                    )
                }

                5 -> {
                    // Step 5: Select Theme Mode
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Choose Theme Mode",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Set your default look. You can toggle this any time in Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val themes = listOf(
                        "☀ Light Mode" to "LIGHT",
                        "🌙 Dark Mode" to "DARK",
                        "⚫ Deep Black (AMOLED)" to "AMOLED"
                    )

                    themes.forEach { (label, code) ->
                        Card(
                            onClick = {
                                scope.launch { userStore.saveThemeMode(code) }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (themeMode == code) MaterialTheme.colorScheme.primaryContainer 
                                                 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (themeMode == code) 2.dp else 1.dp,
                                color = if (themeMode == code) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = themeMode == code,
                                    onClick = {
                                        scope.launch { userStore.saveThemeMode(code) }
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { step = 6 },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Continue to Shapes")
                    }
                }
                6 -> {
                    // Step 6: Custom Shape & Border Configuration
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Customize Corners",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Adjust corner shape, radius, and border thickness for cards and buttons.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Shape selector row
                    val shapes = listOf("Rounded", "Cut", "Hexagon", "Star")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        shapes.forEach { style ->
                            val isSelected = shapeStyle == style
                            Card(
                                onClick = { scope.launch { userStore.saveShapeStyle(style) } },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                                     else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = style,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Corner Radius slider
                    Text(
                        text = "Corner Radius: ${cornerRadius.toInt()}dp",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = cornerRadius,
                        onValueChange = {
                            scope.launch { userStore.saveCornerRadius(it) }
                        },
                        valueRange = 8f..28f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Border Width slider
                    Text(
                        text = "Border Width: ${"%.1f".format(borderWidth)}dp",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = borderWidth,
                        onValueChange = {
                            scope.launch { userStore.saveBorderWidth(it) }
                        },
                        valueRange = 0f..4f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val previewShape = remember(shapeStyle, cornerRadius) {
                        com.balajitechlabs.quickdash.core.ui.components.getCustomShape(shapeStyle, cornerRadius)
                    }
                    Card(
                        shape = previewShape,
                        border = androidx.compose.foundation.BorderStroke(borderWidth.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Live Corner & Border Preview",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { step = 7 },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Continue to Fonts")
                    }
                }
                7 -> {
                    // Step 7: Typography & Font Family
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Configure Fonts",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Choose the application typography and scale factor.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

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

                    Text(
                        text = "Font Family",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        fontFamilies.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowItems.forEach { (label, code) ->
                                    val isSelected = fontFamilyName.uppercase() == code.uppercase()
                                    Card(
                                        onClick = { scope.launch { userStore.saveFontFamilyKey(code) } },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary 
                                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                if (rowItems.size < 3) {
                                    Spacer(modifier = Modifier.weight((3 - rowItems.size).toFloat()))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Font Size Scale slider
                    Text(
                        text = "Text Scale Factor: ${"%.2f".format(fontScale)}x",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Slider(
                        value = fontScale,
                        onValueChange = {
                            scope.launch { userStore.saveFontScale(it) }
                        },
                        valueRange = 0.8f..1.4f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val previewFontFamily = remember(fontFamilyName) {
                        when (fontFamilyName.uppercase()) {
                            "SANSSERIF" -> androidx.compose.ui.text.font.FontFamily.SansSerif
                            "SERIF" -> androidx.compose.ui.text.font.FontFamily.Serif
                            "MONOSPACE" -> androidx.compose.ui.text.font.FontFamily.Monospace
                            "CURSIVE" -> androidx.compose.ui.text.font.FontFamily.Cursive
                            "NUNITO" -> getGoogleFontFamily("Nunito")
                            "POPPINS" -> getGoogleFontFamily("Poppins")
                            "SPACE_GROTESK" -> getGoogleFontFamily("Space Grotesk")
                            else -> androidx.compose.ui.text.font.FontFamily.Default
                        }
                    }
                    val previewShape = remember(shapeStyle, cornerRadius) {
                        com.balajitechlabs.quickdash.core.ui.components.getCustomShape(shapeStyle, cornerRadius)
                    }

                    Card(
                        shape = previewShape,
                        border = androidx.compose.foundation.BorderStroke(borderWidth.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Typography Preview",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = previewFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "QuickDash makes daily utilities simple and customizable.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = previewFontFamily),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            step = 8
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Finalize Setup")
                    }
                }
                8 -> {
                    // Step 8: Setup Complete
                    LaunchedEffect(Unit) {
                        if (confettiEnabled) {
                            showConfetti = true
                            kotlinx.coroutines.delay(2000)
                            showConfetti = false
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        "You're All Set!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "QuickDash is ready. Let's get things done faster.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = {
                            sendWelcomeNotification(context)
                            onComplete()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Launch QuickDash")
                    }
                }
            }
        }

        if (showConfetti) {
            val party = Party(
                speed = 0f,
                maxSpeed = 60f,
                damping = 0.9f,
                angle = 0,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(300),
                position = Position.Relative(0.5, 0.5),
                size = listOf(Size(24, 5f), Size(32, 5f))
            )

            KonfettiView(
                modifier = Modifier.fillMaxSize().zIndex(10f),
                parties = listOf(party),
                updateListener = object : OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(system: nl.dionsegijn.konfetti.core.PartySystem, activeSystems: Int) {
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
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
        context, 0, intent,
        android.app.PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_quickdash_tile)
        .setContentTitle("Welcome to QuickDash! 🎉")
        .setContentText("Your floating hub is ready. Made with ❤️ by balajitechlabs.")
        .setStyle(NotificationCompat.BigTextStyle().bigText("Your floating hub is ready. Tap here to start exploring your new superpowers! Made with ❤️ by balajitechlabs."))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)

    try {
        notificationManager.notify(1001, builder.build())
    } catch (e: SecurityException) {
        // Notification permission not granted, ignore
        e.printStackTrace()
    }
}
