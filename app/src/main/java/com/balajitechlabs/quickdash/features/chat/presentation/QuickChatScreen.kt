/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/chat/presentation
 * File: QuickChatScreen.kt
 * Description: Direct chat screen initiating WhatsApp or SMS conversations without saving phone contacts.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.chat.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.balajitechlabs.quickdash.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.balajitechlabs.quickdash.features.chat.domain.ALL_COUNTRIES
import com.balajitechlabs.quickdash.features.chat.domain.Country
import com.balajitechlabs.quickdash.features.chat.domain.detectCountryIso
import com.balajitechlabs.quickdash.features.chat.domain.getFlagEmoji
import com.balajitechlabs.quickdash.features.chat.presentation.components.ChatSettingsView
import com.balajitechlabs.quickdash.features.chat.presentation.components.CountryPickerView

@Composable
fun QuickChatScreen(
    viewModel: QuickChatViewModel = hiltViewModel(),
    showSettings: Boolean,
    onToggleSettings: (Boolean) -> Unit,
    selectingCountry: Boolean,
    onToggleSelectingCountry: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Collect settings from ViewModel
    val defaultCode by viewModel.chatDefaultCode.collectAsStateWithLifecycle(initialValue = "91")
    val defaultIso by viewModel.chatDefaultIso.collectAsStateWithLifecycle(initialValue = "IN")
    val historyList by viewModel.chatHistory.collectAsStateWithLifecycle(initialValue = emptyList())
    val pauseHistory by viewModel.chatPauseHistory.collectAsStateWithLifecycle(initialValue = false)

    var phoneNumber by remember { mutableStateOf("") }

    if (showSettings) {
        if (selectingCountry) {
            CountryPickerView(
                countries = ALL_COUNTRIES,
                onSelectCountry = { country ->
                    scope.launch {
                        viewModel.saveChatDefaultCountry(country.code, country.iso)
                        onToggleSelectingCountry(false)
                    }
                }
            )
        } else {
            ChatSettingsView(
                defaultCode = defaultCode,
                defaultIso = defaultIso,
                countries = ALL_COUNTRIES,
                historyList = historyList,
                pauseHistory = pauseHistory,
                onToggleSelectingCountry = onToggleSelectingCountry,
                onSavePauseHistory = { viewModel.saveChatPauseHistory(it) },
                onClearHistory = { viewModel.clearChatHistory() },
                onSelectHistoryNumber = { number ->
                    phoneNumber = number
                    onToggleSettings(false)
                }
            )
        }
    } else {
        // --- Redesigned Smart Chat Input Screen ---
        var selectedTab by remember { mutableStateOf("WhatsApp") } // "WhatsApp", "Telegram", "Signal", "SMS"
        var telegramMode by remember { mutableStateOf("Username") } // "Username" or "Phone"

        val detectedIso = detectCountryIso(phoneNumber, defaultIso)
        val activeFlag = getFlagEmoji(detectedIso)

        val digitsOnly = phoneNumber.replace(Regex("[^0-9a-zA-Z]"), "")
        val isLink = phoneNumber.trim().startsWith("http") || phoneNumber.trim().startsWith("t.me") || phoneNumber.trim().contains("/")
        val isUsername = selectedTab == "Telegram" && (telegramMode == "Username" || isLink)
        val isValid = if (isLink) {
            phoneNumber.trim().length >= 5
        } else if (isUsername) {
            phoneNumber.trim().length >= 3
        } else {
            digitsOnly.length >= 7
        }

        val finalNumber = when {
            phoneNumber.trim().startsWith("+") -> {
                "+$digitsOnly"
            }
            digitsOnly.startsWith(defaultCode) && digitsOnly.length > 10 -> {
                "+$digitsOnly"
            }
            else -> {
                "+$defaultCode$digitsOnly"
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Select Target App",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Horizontal Tab Chips with custom icons in frames
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tabs = listOf("WhatsApp", "Telegram", "Signal", "SMS")
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val iconRes = when (tab) {
                        "WhatsApp" -> R.drawable.ic_whatsapp
                        "Telegram" -> R.drawable.ic_telegram
                        "Signal" -> R.drawable.ic_signal
                        else -> R.drawable.ic_sms
                    }
                    Card(
                        onClick = { selectedTab = tab },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF38393F) else Color(0xFF1E2024)
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color.White else Color(0xFF44474F).copy(alpha = 0.4f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = tab,
                                modifier = Modifier.size(24.dp),
                                tint = if (isSelected) Color.White else Color(0xFFC5C6D0)
                            )
                        }
                    }
                }
            }

            // Telegram mode selection
            if (selectedTab == "Telegram") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Username" to "Open with Wizard ID", "Phone" to "Open with Phone Number").forEach { (mode, label) ->
                        val isSelected = telegramMode == mode
                        Card(
                            onClick = { 
                                telegramMode = mode 
                                phoneNumber = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF0088CC).copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF0088CC) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF0088CC) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone / Username Input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(if (isUsername) "Telegram Username / Link" else "Phone Number") },
                placeholder = {
                    if (isUsername) {
                        Text("@username or t.me/joinlink")
                    } else {
                        Text("+$defaultCode 98765-43210")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isUsername) KeyboardType.Text else KeyboardType.Phone
                ),
                leadingIcon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 6.dp)
                            .then(
                                if (!isUsername) {
                                    Modifier.clickable { onToggleSelectingCountry(true) }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isUsername) {
                                Text(text = "", fontSize = 18.sp)
                            } else {
                                Text(text = activeFlag, fontSize = 18.sp)
                                Text(
                                    text = "+$defaultCode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Select country code",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFC5C6D0)
                                )
                            }
                        }
                    }
                },
                trailingIcon = {
                    Row {
                        if (phoneNumber.isNotEmpty()) {
                            IconButton(onClick = { phoneNumber = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFFC5C6D0)
                                )
                            }
                        }
                        IconButton(onClick = {
                            com.balajitechlabs.quickdash.features.qr.utils.QrScannerHelper.startScan(
                                context = context,
                                onResult = { raw ->
                                    val clean = raw.trim()
                                    val lower = clean.lowercase()
                                    val isWhatsAppPayload = lower.contains("wa.me/") ||
                                            lower.contains("api.whatsapp.com/send") ||
                                            lower.contains("whatsapp://send") ||
                                            lower.contains("web.whatsapp.com/send") ||
                                            lower.contains("chat.whatsapp.com/") ||
                                            lower.contains("whatsapp.com/channel/")
                                    
                                    var parsed: String? = null
                                    if (isWhatsAppPayload) {
                                        parsed = Regex("[?&]phone=([+0-9]+)", RegexOption.IGNORE_CASE)
                                            .find(clean)?.groupValues?.get(1)
                                        if (parsed == null) {
                                            parsed = Regex("wa\\.me/([+0-9]+)", RegexOption.IGNORE_CASE)
                                                .find(clean)?.groupValues?.get(1)
                                        }
                                    } else {
                                        if (clean.matches(Regex("^[+0-9]+$"))) {
                                            parsed = clean
                                        }
                                    }

                                    if (parsed != null) {
                                        phoneNumber = parsed.removePrefix("+$defaultCode").removePrefix("+")
                                    } else {
                                        Toast.makeText(context, "Invalid WhatsApp QR code", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = "Scan QR",
                                tint = Color(0xFFC5C6D0)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF44474F),
                    unfocusedBorderColor = Color(0xFF44474F).copy(alpha = 0.6f),
                    focusedContainerColor = Color(0xFF38393F),
                    unfocusedContainerColor = Color(0xFF38393F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button
            val cleanNumericDigits = when {
                phoneNumber.trim().startsWith("+") -> phoneNumber.trim().removePrefix("+").replace(Regex("[^0-9]"), "")
                phoneNumber.replace(Regex("[^0-9]"), "").startsWith(defaultCode) && phoneNumber.replace(Regex("[^0-9]"), "").length > 10 -> phoneNumber.replace(Regex("[^0-9]"), "")
                else -> "$defaultCode${phoneNumber.replace(Regex("[^0-9]"), "")}"
            }

            Button(
                onClick = {
                    if (isValid) {
                        scope.launch {
                            val flagToSave = if (isUsername) "" else activeFlag
                            viewModel.saveChatNumberToHistory(
                                if (isUsername) phoneNumber.trim() else "+$cleanNumericDigits",
                                flagToSave
                            )
                        }

                        try {
                            when (selectedTab) {
                                "WhatsApp" -> {
                                    val waUri = Uri.parse("https://wa.me/$cleanNumericDigits")
                                    val waIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(waIntent)
                                }
                                "Telegram" -> {
                                    val tgUri = if (isLink) {
                                        var url = phoneNumber.trim()
                                        if (!url.startsWith("http")) url = "https://$url"
                                        Uri.parse(url)
                                    } else if (isUsername) {
                                        val user = phoneNumber.trim().removePrefix("@")
                                        Uri.parse("https://t.me/$user")
                                    } else {
                                        Uri.parse("https://t.me/+$cleanNumericDigits")
                                    }
                                    val tgIntent = Intent(Intent.ACTION_VIEW, tgUri).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(tgIntent)
                                }
                                "Signal" -> {
                                    val signalIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://signal.me/#p/+$cleanNumericDigits")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(signalIntent)
                                }
                                "SMS" -> {
                                    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:+$cleanNumericDigits")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(smsIntent)
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open $selectedTab client", Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF2A2B30),
                    disabledContentColor = Color(0xFF8E9099)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val iconRes = when (selectedTab) {
                        "WhatsApp" -> R.drawable.ic_whatsapp
                        "Telegram" -> R.drawable.ic_telegram
                        "Signal" -> R.drawable.ic_signal
                        else -> R.drawable.ic_sms
                    }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Open Chat in $selectedTab",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}
