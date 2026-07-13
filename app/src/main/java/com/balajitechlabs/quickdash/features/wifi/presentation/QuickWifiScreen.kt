package com.balajitechlabs.quickdash.features.wifi.presentation

import android.Manifest
import com.balajitechlabs.quickdash.core.utils.AppLogger
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.wifi.WifiManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.utils.QRCodeGenerator
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WifiEntry(val ssid: String, val password: String, val savedAt: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickWifiScreen(userStore: UserStore, isFloating: Boolean = false, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val savedSsid by userStore.wifiSsid.collectAsState(initial = "")
    val savedPassword by userStore.wifiPassword.collectAsState(initial = "")
    val wifiHistoryJson by userStore.wifiHistory.collectAsState(initial = "[]")
    val emojiHeader by userStore.emojiHeader.collectAsState(initial = "🚀")
    val qrUseEmojiOverlay by userStore.qrUseEmojiOverlay.collectAsState(initial = false)

    val wifiHistory = remember(wifiHistoryJson) {
        try {
            val arr = JsonParser.parseString(wifiHistoryJson).asJsonArray
            arr.map { el ->
                val obj = el.asJsonObject
                WifiEntry(
                    ssid = obj.get("ssid")?.asString ?: "",
                    password = obj.get("password")?.asString ?: "",
                    savedAt = obj.get("savedAt")?.asLong ?: 0L
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var encryptionType by remember { mutableStateOf("WPA") }
    var isHidden by remember { mutableStateOf(false) }
    val savedHotspotMode by userStore.wifiHotspotMode.collectAsState(initial = false)
    var hotspotMode by remember(savedHotspotMode) { mutableStateOf(savedHotspotMode) }
    var showHistory by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var useCircularDots by remember { mutableStateOf(false) }
    var useGradient by remember { mutableStateOf(false) }

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> locationPermissionGranted = isGranted }

    LaunchedEffect(savedSsid, savedPassword) {
        if (ssid.isEmpty() && savedSsid.isNotEmpty()) {
            ssid = savedSsid; password = savedPassword
        }
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted && !hotspotMode) {
            try {
                val wifiManager = context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
                val info = wifiManager.connectionInfo
                val current = info.ssid?.removeSurrounding("\"")
                if (!current.isNullOrBlank() && current != "<unknown ssid>") {
                    ssid = current
                    password = if (current == savedSsid) savedPassword else ""
                }
            } catch (e: Exception) { AppLogger.e("QuickWifiScreen", "Failed to retrieve Wi-Fi SSID", e) }
        } else if (!locationPermissionGranted) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Helper function to escape special characters (\, ;, :, ") in Wi-Fi strings
    fun escapeWifiString(input: String): String {
        val sb = StringBuilder()
        for (c in input) {
            if (c == '\\' || c == ';' || c == ':' || c == '"') {
                sb.append('\\')
            }
            sb.append(c)
        }
        return sb.toString()
    }

    val escapedSsid = escapeWifiString(ssid)
    val escapedPassword = escapeWifiString(password)

    // Regenerate QR whenever inputs change
    val wifiString = if (isHidden) {
        "WIFI:S:$escapedSsid;T:$encryptionType;P:$escapedPassword;H:true;;"
    } else {
        "WIFI:S:$escapedSsid;T:$encryptionType;P:$escapedPassword;;"
    }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    LaunchedEffect(ssid, password, encryptionType, isHidden, emojiHeader, qrUseEmojiOverlay, primaryColor, useCircularDots, useGradient) {
        qrBitmap = if (ssid.isNotEmpty()) {
            withContext(Dispatchers.Default) {
                try {
                    QRCodeGenerator.generateQRCode(
                        context = context,
                        text = wifiString,
                        width = 800,
                        height = 800,
                        qrColor = primaryColor,
                        centerEmoji = if (qrUseEmojiOverlay) emojiHeader else null,
                        qrGradientColors = if (useGradient) Pair(primaryColor, secondaryColor) else null,
                        useCircularDots = useCircularDots
                    )
                } catch (e: Exception) { null }
            }
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Header ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Wi-Fi Share", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            // History toggle
            IconButton(onClick = { showHistory = true }) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Show history",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // ── Hotspot Mode Toggle ────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (hotspotMode) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Wifi,
                        contentDescription = null,
                        tint = if (hotspotMode) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Mobile Hotspot QR",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Mode for sharing your hotspot",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hotspotMode) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hotspotMode,
                        onCheckedChange = {
                            hotspotMode = it
                            coroutineScope.launch { userStore.saveWifiHotspotMode(it) }
                        }
                    )
                }

                AnimatedVisibility(visible = hotspotMode) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = android.content.Intent().apply {
                                    setClassName("com.android.settings", "com.android.settings.TetherSettings")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e2: Exception) { }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
                    ) {
                        Text("Turn On System Hotspot")
                    }
                }
            }
        }

        // ── Encryption Type Chips ──────────────────────────────────────
        val encryptionScrollState = rememberScrollState()
        val wifiScope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(encryptionScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("WPA", "WEP", "nopass").forEach { type ->
                    FilterChip(
                        selected = encryptionType == type,
                        onClick = { encryptionType = type },
                        label = { Text(type) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = isHidden,
                    onClick = { isHidden = !isHidden },
                    label = { Text("Hidden") },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Left scroll button
            if (encryptionScrollState.value > 0) {
                IconButton(
                    onClick = {
                        wifiScope.launch {
                            encryptionScrollState.animateScrollTo((encryptionScrollState.value - 120).coerceAtLeast(0))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Scroll Left",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Right scroll button
            if (encryptionScrollState.value < encryptionScrollState.maxValue) {
                IconButton(
                    onClick = {
                        wifiScope.launch {
                            encryptionScrollState.animateScrollTo((encryptionScrollState.value + 120).coerceAtMost(encryptionScrollState.maxValue))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Scroll Right",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ── SSID + Password Fields ─────────────────────────────────────
        OutlinedTextField(
            value = ssid,
            onValueChange = { ssid = it },
            label = { Text(if (hotspotMode) "Hotspot Name (SSID)" else "Network Name (SSID)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (passwordVisible) {
                androidx.compose.ui.text.input.VisualTransformation.None
            } else {
                androidx.compose.ui.text.input.PasswordVisualTransformation()
            },
            singleLine = true
        )

        // ── Group: Network Live Traffic ──────────────────────────────────
        var rxSpeed by remember { mutableLongStateOf(0L) }
        var txSpeed by remember { mutableLongStateOf(0L) }
        var totalSessionBytes by remember { mutableLongStateOf(0L) }
        
        LaunchedEffect(Unit) {
            var lastRx = android.net.TrafficStats.getTotalRxBytes()
            var lastTx = android.net.TrafficStats.getTotalTxBytes()
            val startTotal = lastRx + lastTx
            while (true) {
                kotlinx.coroutines.delay(1000)
                val currentRx = android.net.TrafficStats.getTotalRxBytes()
                val currentTx = android.net.TrafficStats.getTotalTxBytes()
                if (lastRx > 0 && currentRx >= lastRx) rxSpeed = currentRx - lastRx
                if (lastTx > 0 && currentTx >= lastTx) txSpeed = currentTx - lastTx
                totalSessionBytes = (currentRx + currentTx) - startTotal
                lastRx = currentRx
                lastTx = currentTx
            }
        }

        fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1024 * 1024 * 1024 -> String.format(java.util.Locale.getDefault(), "%.2f GB", bytes.toDouble() / (1024 * 1024 * 1024))
                bytes >= 1024 * 1024 -> String.format(java.util.Locale.getDefault(), "%.2f MB", bytes.toDouble() / (1024 * 1024))
                bytes >= 1024 -> String.format(java.util.Locale.getDefault(), "%.2f KB", bytes.toDouble() / 1024)
                else -> "$bytes B"
            }
        }

        val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 2 })
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) { page ->
            if (page == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Live Network Traffic Monitor",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Download Speed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${formatBytes(rxSpeed)}/s", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Upload Speed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${formatBytes(txSpeed)}/s", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Session Total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatBytes(totalSessionBytes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            } else {
                var host by remember { mutableStateOf("") }
                var port by remember { mutableStateOf("") }
                var user by remember { mutableStateOf("") }
                var pass by remember { mutableStateOf("") }
                
                val serverJson by userStore.serverCredentials.collectAsState(initial = "{}")
                LaunchedEffect(serverJson) {
                    try {
                        val obj = com.google.gson.JsonParser.parseString(serverJson).asJsonObject
                        if (host.isEmpty()) {
                            host = obj.get("host")?.asString ?: ""
                            port = obj.get("port")?.asString ?: ""
                            user = obj.get("username")?.asString ?: ""
                            pass = obj.get("password")?.asString ?: ""
                        }
                    } catch (_: Exception) {}
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Server Credentials Config",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = host,
                                onValueChange = { host = it },
                                label = { Text("Host/IP", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(2f),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                            )
                            OutlinedTextField(
                                value = port,
                                onValueChange = { port = it },
                                label = { Text("Port", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = user,
                                onValueChange = { user = it },
                                label = { Text("User", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                            )
                            OutlinedTextField(
                                value = pass,
                                onValueChange = { pass = it },
                                label = { Text("Pass", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val obj = com.google.gson.JsonObject().apply {
                                    addProperty("host", host)
                                    addProperty("port", port)
                                    addProperty("username", user)
                                    addProperty("password", pass)
                                }
                                coroutineScope.launch {
                                    userStore.saveServerCredentials(obj.toString())
                                }
                                android.widget.Toast.makeText(context, "Server saved!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Credentials", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(2) { index ->
                val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))

        // ── QR Code Styling Selectors ──────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            FilterChip(
                selected = useCircularDots,
                onClick = { useCircularDots = !useCircularDots },
                label = { Text("Circular Dots") }
            )
            FilterChip(
                selected = useGradient,
                onClick = { useGradient = !useGradient },
                label = { Text("Gradient Colors") }
            )
        }

        // ── QR Code Display ────────────────────────────────────────────
        qrBitmap?.let { bitmap ->
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(260.dp)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Wi-Fi QR Code",
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        filterQuality = FilterQuality.None
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Save Button ────────────────────────────────────────────────
        Button(
            onClick = {
                coroutineScope.launch {
                    userStore.saveWifiCredentials(ssid, password)
                    if (ssid.isNotBlank()) {
                        userStore.addWifiHistory(ssid, password, encryptionType)
                    }
                }
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Save Credentials")
        }
        if (!isFloating) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done")
            }
        }
    }

    if (showHistory) {
        WifiHistoryDialog(
            historyJson = wifiHistoryJson,
            onClearHistory = {
                coroutineScope.launch { userStore.clearWifiHistory() }
            },
            onRemoveEntry = { targetSsid ->
                coroutineScope.launch { userStore.removeWifiHistoryEntry(targetSsid) }
            },
            onSelectNetwork = { selectedSsid, selectedPassword ->
                ssid = selectedSsid
                password = selectedPassword
            },
            onDismiss = { showHistory = false }
        )
    }
}
