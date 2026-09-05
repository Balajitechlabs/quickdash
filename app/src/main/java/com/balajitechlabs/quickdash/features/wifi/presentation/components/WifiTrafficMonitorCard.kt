/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/wifi/presentation/components
 * File: WifiTrafficMonitorCard.kt
 * Description: Pager component displaying live network traffic stats and server credentials management.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.wifi.presentation.components

import android.net.TrafficStats
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.utils.AppLogger
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun WifiTrafficMonitorCard(
    serverJson: String,
    onSaveServerCredentials: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var rxSpeed by remember { mutableLongStateOf(0L) }
    var txSpeed by remember { mutableLongStateOf(0L) }
    var totalSessionBytes by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        var lastRx = TrafficStats.getTotalRxBytes()
        var lastTx = TrafficStats.getTotalTxBytes()
        val startTotal = lastRx + lastTx
        while (true) {
            delay(1000)
            val currentRx = TrafficStats.getTotalRxBytes()
            val currentTx = TrafficStats.getTotalTxBytes()
            if (lastRx > 0 && currentRx >= lastRx) rxSpeed = currentRx - lastRx
            if (lastTx > 0 && currentTx >= lastTx) txSpeed = currentTx - lastTx
            totalSessionBytes = (currentRx + currentTx) - startTotal
            lastRx = currentRx
            lastTx = currentTx
        }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format(Locale.getDefault(), "%.2f GB", bytes.toDouble() / (1024 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.2f MB", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> String.format(Locale.getDefault(), "%.2f KB", bytes.toDouble() / 1024)
            else -> "$bytes B"
        }
    }

    val pagerState = rememberPagerState(pageCount = { 2 })

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
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
                                Text(
                                    text = "Download Speed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${formatBytes(rxSpeed)}/s",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = "Upload Speed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${formatBytes(txSpeed)}/s",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = "Session Total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatBytes(totalSessionBytes),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                var host by remember { mutableStateOf("") }
                var port by remember { mutableStateOf("") }
                var user by remember { mutableStateOf("") }
                var pass by remember { mutableStateOf("") }

                LaunchedEffect(serverJson) {
                    try {
                        val obj = JsonParser.parseString(serverJson).asJsonObject
                        if (host.isEmpty()) {
                            host = obj.get("host")?.asString ?: ""
                            port = obj.get("port")?.asString ?: ""
                            user = obj.get("username")?.asString ?: ""
                            pass = obj.get("password")?.asString ?: ""
                        }
                    } catch (e: Exception) {
                        AppLogger.e("WifiTrafficMonitorCard", "Failed to parse server credentials JSON", e)
                    }
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
                                visualTransformation = PasswordVisualTransformation()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val obj = JsonObject().apply {
                                    addProperty("host", host)
                                    addProperty("port", port)
                                    addProperty("username", user)
                                    addProperty("password", pass)
                                }
                                onSaveServerCredentials(obj.toString())
                                Toast.makeText(context, "Server saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(2) { index ->
                val indicatorColor = if (pagerState.currentPage == index) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                }
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
        }
    }
}
