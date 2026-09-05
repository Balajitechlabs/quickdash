/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/wifi/presentation
 * File: QuickWifiScreen.kt
 * Description: Wi-Fi credential generator and scanner screen producing connectable network QR codes.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.wifi.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.wifi.WifiManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.core.utils.AppLogger
import com.balajitechlabs.quickdash.core.utils.QRCodeGenerator
import com.balajitechlabs.quickdash.features.wifi.presentation.components.WifiEncryptionSelector
import com.balajitechlabs.quickdash.features.wifi.presentation.components.WifiHotspotCard
import com.balajitechlabs.quickdash.features.wifi.presentation.components.WifiTrafficMonitorCard
import com.balajitechlabs.quickdash.features.wifi.presentation.dialogs.WifiShareQrDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun QuickWifiScreen(
    viewModel: WifiViewModel = hiltViewModel(),
    isFloating: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val savedSsid by viewModel.wifiSsid.collectAsStateWithLifecycle()
    val savedPassword by viewModel.wifiPassword.collectAsStateWithLifecycle()
    val wifiHistoryJson by viewModel.wifiHistoryJson.collectAsStateWithLifecycle()
    val serverJson by viewModel.serverCredentials.collectAsStateWithLifecycle()
    val savedHotspotMode by viewModel.wifiHotspotMode.collectAsStateWithLifecycle()

    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var encryptionType by remember { mutableStateOf("WPA") }
    var isHidden by remember { mutableStateOf(false) }
    var hotspotMode by remember(savedHotspotMode) { mutableStateOf(savedHotspotMode) }
    var showHistory by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
    }

    LaunchedEffect(savedSsid, savedPassword) {
        if (ssid.isEmpty() && savedSsid.isNotEmpty()) {
            ssid = savedSsid
            password = savedPassword
        }
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted && !hotspotMode) {
            try {
                val wifiManager = context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
                @Suppress("DEPRECATION")
                val info = wifiManager.connectionInfo
                val current = info.ssid?.removeSurrounding("\"")
                if (!current.isNullOrBlank() && current != "<unknown ssid>") {
                    ssid = current
                    password = if (current == savedSsid) savedPassword else ""
                }
            } catch (e: Exception) {
                AppLogger.e("QuickWifiScreen", "Failed to retrieve Wi-Fi SSID", e)
            }
        } else if (!locationPermissionGranted) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

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

    val wifiString = if (isHidden) {
        "WIFI:S:$escapedSsid;T:$encryptionType;P:$escapedPassword;H:true;;"
    } else {
        "WIFI:S:$escapedSsid;T:$encryptionType;P:$escapedPassword;;"
    }

    LaunchedEffect(showQrDialog, ssid, password, encryptionType, isHidden) {
        if (showQrDialog && ssid.isNotEmpty()) {
            qrBitmap = withContext(Dispatchers.Default) {
                try {
                    QRCodeGenerator.generateQRCode(
                        context = context,
                        text = wifiString,
                        width = 800,
                        height = 800,
                        qrColor = AndroidColor.BLACK,
                        centerEmoji = null,
                        qrGradientColors = null,
                        useCircularDots = false
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Wi-Fi Share",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )
            IconButton(
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                    showHistory = true
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = "Show history",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        WifiHotspotCard(
            hotspotMode = hotspotMode,
            onHotspotModeChange = {
                hotspotMode = it
                viewModel.saveWifiHotspotMode(it)
            },
            onTurnOnHotspot = {
                try {
                    val intent = Intent().apply {
                        setClassName("com.android.settings", "com.android.settings.TetherSettings")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            }
        )

        WifiEncryptionSelector(
            encryptionType = encryptionType,
            onEncryptionTypeChange = { encryptionType = it },
            isHidden = isHidden,
            onHiddenChange = { isHidden = it }
        )

        OutlinedTextField(
            value = ssid,
            onValueChange = { ssid = it },
            label = { Text(if (hotspotMode) "Hotspot Name (SSID)" else "Network Name (SSID)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Filled.Wifi, contentDescription = null) },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = {
                    com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                    passwordVisible = !passwordVisible
                }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            singleLine = true
        )

        WifiTrafficMonitorCard(
            serverJson = serverJson,
            onSaveServerCredentials = { viewModel.saveServerCredentials(it) }
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                com.balajitechlabs.quickdash.core.ui.playHeavyVibration(context, true, 26L)
                if (ssid.isNotBlank()) {
                    showQrDialog = true
                } else {
                    Toast.makeText(context, "Please enter a Wi-Fi network name", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2C2C2E),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Filled.QrCode,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Generate & View Wi-Fi QR Code",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = {
                com.balajitechlabs.quickdash.core.ui.playSuccessVibration(context, true)
                coroutineScope.launch {
                    viewModel.saveWifiCredentials(ssid, password)
                    if (ssid.isNotBlank()) {
                        viewModel.addWifiHistory(ssid, password, encryptionType)
                    }
                }
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Save Credentials")
        }

        if (!isFloating) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done")
            }
        }

        Spacer(Modifier.height(120.dp))
    }

    if (showHistory) {
        WifiHistoryDialog(
            historyJson = wifiHistoryJson,
            onClearHistory = { viewModel.clearWifiHistory() },
            onRemoveEntry = { targetSsid -> viewModel.removeWifiHistoryEntry(targetSsid) },
            onSelectNetwork = { selectedSsid, selectedPassword ->
                ssid = selectedSsid
                password = selectedPassword
            },
            onDismiss = { showHistory = false }
        )
    }

    if (showQrDialog) {
        WifiShareQrDialog(
            ssid = ssid,
            qrBitmap = qrBitmap,
            onDismiss = { showQrDialog = false }
        )
    }
}
