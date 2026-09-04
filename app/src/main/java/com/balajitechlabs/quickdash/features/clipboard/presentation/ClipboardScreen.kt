/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/clipboard
 * File: ClipboardScreen.kt
 * Description: EssentialX-styled component for features/clipboard supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.clipboard.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val TAG = "ClipboardScreen"

data class ActionableItem(
    val label: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val intent: Intent
)

fun parseClipboardContent(text: String, context: Context): List<ActionableItem> {
    val items = mutableListOf<ActionableItem>()
    val trimmed = text.trim()

    // 0. OTP / Verification Code Detection (4-8 digits)
    val otpRegex = Regex("""\b(\d{4,8})\b""")
    val lowerText = trimmed.lowercase()
    val isOtpContext = lowerText.contains("otp") || lowerText.contains("code") || lowerText.contains("verification") || lowerText.contains("password") || lowerText.contains("login") || lowerText.contains("pin")
    if (isOtpContext || (trimmed.all { it.isDigit() } && trimmed.length in 4..8)) {
        val otpMatch = otpRegex.find(trimmed)
        if (otpMatch != null) {
            val code = otpMatch.groupValues[1]
            val copyIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, code)
            }
            items.add(
                ActionableItem(
                    label = "Copy OTP: $code",
                    value = code,
                    icon = Icons.Filled.ContentCopy,
                    intent = copyIntent
                )
            )
        }
    }

    // 1. Phone number detection
    val phoneRegex = Regex("\\+?[0-9][0-9\\s-]{7,14}[0-9]")
    phoneRegex.findAll(trimmed).forEach { match ->
        val matchedPhone = match.value
        val digits = matchedPhone.filter { it.isDigit() }
        if (digits.length >= 8) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$matchedPhone")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            items.add(
                ActionableItem(
                    label = "Call $matchedPhone",
                    value = matchedPhone,
                    icon = Icons.Filled.Call,
                    intent = intent
                )
            )

            // Direct WhatsApp Chat Pill
            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            items.add(
                ActionableItem(
                    label = "WhatsApp $matchedPhone",
                    value = matchedPhone,
                    icon = Icons.Filled.Share,
                    intent = waIntent
                )
            )
        }
    }

    // 2. Email detection
    val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}")
    emailRegex.findAll(trimmed).forEach { match ->
        val matchedEmail = match.value
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$matchedEmail")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        items.add(
            ActionableItem(
                label = "Email $matchedEmail",
                value = matchedEmail,
                icon = Icons.Filled.Email,
                intent = intent
            )
        )
    }

    // 3. Link detection
    val urlRegex = Regex("(https?://[^\\s]+|www\\.[^\\s]+)")
    urlRegex.findAll(trimmed).forEach { match ->
        val matchedUrl = match.value
        val finalUrl = if (matchedUrl.startsWith("www.")) "https://$matchedUrl" else matchedUrl
        val uri = Uri.parse(finalUrl)
        val host = uri.host?.lowercase() ?: ""

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val (label, icon) = when {
            host.contains("youtube.com") || host.contains("youtu.be") -> {
                intent.setPackage("com.google.android.youtube")
                "Open YouTube" to Icons.Filled.PlayArrow
            }
            host.contains("maps.google") || host.contains("google.com/maps") || host.contains("maps.app.goo.gl") -> {
                intent.setPackage("com.google.android.apps.maps")
                "Open Maps" to Icons.Filled.LocationOn
            }
            host.contains("play.google.com") -> {
                intent.setPackage("com.android.vending")
                "Open Play Store" to Icons.Filled.Info
            }
            host.contains("instagram.com") -> {
                intent.setPackage("com.instagram.android")
                "Open Instagram" to Icons.Filled.Share
            }
            host.contains("twitter.com") || host.contains("x.com") -> {
                intent.setPackage("com.twitter.android")
                "Open X / Twitter" to Icons.Filled.Share
            }
            finalUrl.startsWith("upi:") -> {
                "UPI Payment" to Icons.Filled.QrCode
            }
            else -> "Browse Link" to Icons.AutoMirrored.Filled.OpenInNew
        }

        // Check if package manager can resolve specific app package, if not, reset package to let system choose
        try {
            val pm = context.packageManager
            if (intent.`package` != null && intent.resolveActivity(pm) == null) {
                intent.setPackage(null)
            }
        } catch (_: Exception) {
            intent.setPackage(null)
        }

        items.add(
            ActionableItem(
                label = label,
                value = matchedUrl,
                icon = icon,
                intent = intent
            )
        )
    }

    return items
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardScreen(
    viewModel: ClipboardViewModel = hiltViewModel(),
    isFloating: Boolean = false,
    onTriggerConfetti: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    val clipboardJson by viewModel.clipboardHistory.collectAsStateWithLifecycle(initialValue = "[]")
    val pinnedJson by viewModel.clipboardPinned.collectAsStateWithLifecycle(initialValue = "[]")

    // Auto-capture latest clipboard item when opening the screen
    LaunchedEffect(Unit) {
        try {
            val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipManager != null && clipManager.hasPrimaryClip()) {
                val clipText = clipManager.primaryClip?.getItemAt(0)?.text?.toString()
                if (!clipText.isNullOrBlank()) {
                    viewModel.addClipboardItem(clipText)
                }
            }
        } catch (_: Exception) {}
    }

    val gson = Gson()
    val listType = object : TypeToken<List<String>>() {}.type
    val clipboardItems = remember(clipboardJson) {
        try { gson.fromJson<List<String>>(clipboardJson, listType) ?: emptyList() }
        catch (_: Exception) { emptyList() }
    }
    val pinnedItems = remember(pinnedJson) {
        try { gson.fromJson<List<String>>(pinnedJson, listType) ?: emptyList() }
        catch (_: Exception) { emptyList() }
    }

    var selectedFilter by remember { mutableStateOf("All") }
    val filteredItems = remember(clipboardItems, selectedFilter, pinnedItems) {
        when (selectedFilter) {
            "Pinned" -> pinnedItems
            "Links"  -> clipboardItems.filter { it.contains("http://") || it.contains("https://") || it.contains("www.") }
            "Phones" -> clipboardItems.filter { it.matches(Regex(".*[0-9]{7,15}.*")) }
            "Emails" -> clipboardItems.filter { it.contains("@") && it.contains(".") }
            else     -> clipboardItems
        }
    }

    var revealedItems by remember { mutableStateOf(setOf<String>()) }

    fun isLuhnValid(number: String): Boolean {
        val cleanNumber = number.filter { it.isDigit() }
        if (cleanNumber.length < 13 || cleanNumber.length > 19) return false
        var sum = 0
        var alternate = false
        for (i in cleanNumber.length - 1 downTo 0) {
            var n = cleanNumber[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) n = (n % 10) + 1
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun isSensitive(text: String): Boolean {
        val lower = text.lowercase()
        if (lower.contains("password") || lower.contains("key") || lower.contains("token") ||
            lower.contains("secret") || lower.contains("pwd") || lower.contains("pin") ||
            (text.length > 25 && !text.contains(" ") && !text.contains("/"))) {
            return true
        }
        val ccRegex = Regex("\\b\\d{13,19}\\b")
        val cleanDigits = text.filter { it.isDigit() || it.isWhitespace() }.replace("\\s+".toRegex(), "")
        val ccMatch = ccRegex.find(cleanDigits)
        if (ccMatch != null && isLuhnValid(ccMatch.value)) {
            return true
        }
        val hasOtpKeyword = lower.contains("otp") || lower.contains("code") || lower.contains("verification") || lower.contains("passcode") || lower.contains("one-time")
        val digitRegex = Regex("\\b\\d{4,8}\\b")
        if (hasOtpKeyword && digitRegex.containsMatchIn(text)) {
            return true
        }
        return false
    }

    val isTabLocked by viewModel.tabBiometricLock.collectAsStateWithLifecycle(initialValue = false)
    var isUnlocked by remember { mutableStateOf(false) }
    var showClearAllConfirmation by remember { mutableStateOf(false) }

    // ── Biometric lock screen ─────────────────────────────────────────────
    if (isTabLocked && !isUnlocked) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Text("Clipboard is Locked", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Your clipboard history is protected. Authenticate to view it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    com.balajitechlabs.quickdash.core.utils.BiometricHelper.authenticate(
                        context = context,
                        onSuccess = { isUnlocked = true }
                    )
                },
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Filled.Fingerprint, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Unlock with Biometrics")
            }
        }
        return
    }

    // ── Main content ──────────────────────────────────────────────────────
    Column(modifier = Modifier.fillMaxWidth().imePadding()) {

        // Header with count badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Clipboard History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (com.balajitechlabs.quickdash.core.security.IncognitoManager.isIncognitoActive) {
                    Text(
                        "Incognito Active — History Paused",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (clipboardItems.isNotEmpty()) {
                    Text(
                        "${clipboardItems.size} item${if (clipboardItems.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (clipboardItems.isNotEmpty()) {
                FilledTonalIconButton(
                    onClick = { showClearAllConfirmation = true },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear all", modifier = Modifier.size(20.dp))
                }
            }
        }

        // ── Filter chips ──────────────────────────────────────────────────
        if (!isFloating) {
            val filters = listOf("All", "Pinned", "Links", "Phones", "Emails")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(filters, key = { it }) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (selectedFilter == filter) ({
                            Icon(Icons.Filled.FilterList, null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                        }) else null
                    )
                }
            }
        }

        // ── Empty state ───────────────────────────────────────────────────
        if (filteredItems.isEmpty()) {
            com.balajitechlabs.quickdash.core.ui.components.EmptyStateCard(
                icon = Icons.Filled.ContentCopy,
                title = if (clipboardItems.isEmpty()) "Nothing copied yet" else "No $selectedFilter items found",
                subtitle = if (clipboardItems.isEmpty()) "Text copied while the app is running will appear here automatically." else "Try a different filter to see your clipboard history.",
                actionLabel = if (clipboardItems.isNotEmpty() && selectedFilter != "All") "Show All" else null,
                onActionClick = { selectedFilter = "All" },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // ── Clipboard entries ─────────────────────────────────────────────
        val displayItems = if (isFloating) filteredItems.take(5) else filteredItems
        if (filteredItems.isNotEmpty()) {
            LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            // Render Pinned section first if "All" is selected and there are pinned items
            if (selectedFilter == "All" && pinnedItems.isNotEmpty() && !isFloating) {
                item {
                    Text(
                        text = "Pinned Items",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp).animateItem()
                    )
                }
                itemsIndexed(pinnedItems, key = { index, item -> "pinned:$index:$item" }) { _, item ->
                    Box(modifier = Modifier.animateItem()) {
                        ClipboardItemCard(
                            item = item,
                            pinnedItems = pinnedItems,
                            clipboardItems = clipboardItems,
                            viewModel = viewModel,
                            onTriggerConfetti = onTriggerConfetti,
                            coroutineScope = coroutineScope,
                            gson = gson,
                            context = context,
                            sensitive = isSensitive(item),
                            revealed = revealedItems.contains(item),
                            onToggleReveal = {
                                revealedItems = if (revealedItems.contains(item)) revealedItems - item else revealedItems + item
                            }
                        )
                    }
                }
                item {
                    Text(
                        text = "Recent History",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp).animateItem()
                    )
                }
            }

            // Render main filtered list with swipe-to-delete
            items(displayItems, key = { it }) { item ->
                @Suppress("DEPRECATION")
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                            viewModel.removeClipboardItem(item)
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.animateItem(),
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                ) {
                    ClipboardItemCard(
                        item = item,
                        pinnedItems = pinnedItems,
                        clipboardItems = clipboardItems,
                        viewModel = viewModel,
                        onTriggerConfetti = onTriggerConfetti,
                        coroutineScope = coroutineScope,
                        gson = gson,
                        context = context,
                        sensitive = isSensitive(item),
                        revealed = revealedItems.contains(item),
                        onToggleReveal = {
                            revealedItems = if (revealedItems.contains(item)) revealedItems - item else revealedItems + item
                        }
                    )
                }
            }
        }
        }
    }

    if (showClearAllConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmation = false },
            title = { Text("Clear Clipboard History") },
            text = { Text("Are you sure you want to clear all items in your clipboard history?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        viewModel.saveClipboardHistory("[]")
                    }
                    showClearAllConfirmation = false
                }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ClipboardItemCard(
    item: String,
    pinnedItems: List<String>,
    clipboardItems: List<String>,
    viewModel: ClipboardViewModel,
    onTriggerConfetti: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    gson: com.google.gson.Gson,
    context: android.content.Context,
    sensitive: Boolean,
    revealed: Boolean,
    onToggleReveal: () -> Unit
) {
    val isPinned = pinnedItems.contains(item)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (sensitive && !revealed)
                Color(0xFF3B2424)
            else Color(0xFF38393F)
        ),
        border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Clipboard Text (Full Width)
            Text(
                text = if (sensitive && !revealed) "Sensitive content hidden" else item,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                maxLines = if (revealed || !sensitive) 8 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = sensitive) { onToggleReveal() }
            )
            
            // Sensitive subtitle
            if (sensitive) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (revealed) "Tap to hide" else "Tap to reveal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable { onToggleReveal() }
                )
            }

            // Action Chips Row (e.g. Call number)
            if (!sensitive || revealed) {
                val actions = remember(item) { parseClipboardContent(item, context) }
                if (actions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(actions, key = { it.label }) { action ->
                            AssistChip(
                                onClick = {
                                    try {
                                        context.startActivity(action.intent)
                                    } catch (e: Exception) {
                                        try {
                                            action.intent.setPackage(null)
                                            context.startActivity(action.intent)
                                        } catch (ex: Exception) {
                                            android.util.Log.e("QuickDash", "Error occurred: ${ex.message}", ex)
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = action.label,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFB0C6FF)
                                    )
                                },
                                label = {
                                    Text(
                                        text = action.label,
                                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFF1E2024),
                                    labelColor = Color.White
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = Color(0xFF44474F)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF44474F).copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(2.dp))

            // Action Buttons Row (At the bottom)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Sensitive Reveal / Hide Toggle
                if (sensitive) {
                    IconButton(
                        onClick = onToggleReveal,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (revealed) "Hide sensitive text" else "Reveal sensitive text",
                            modifier = Modifier.size(18.dp),
                            tint = if (revealed) MaterialTheme.colorScheme.primary else Color(0xFFC5C6D0)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Pin/Unpin
                IconButton(
                    onClick = {
                        val newList = if (isPinned) {
                            pinnedItems.filter { it != item }
                        } else {
                            pinnedItems + item
                        }
                        coroutineScope.launch {
                            viewModel.saveClipboardPinned(gson.toJson(newList))
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = if (isPinned) "Unpin" else "Pin",
                        modifier = Modifier.size(18.dp),
                        tint = if (isPinned) Color(0xFFB0C6FF) else Color(0xFFC5C6D0)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Share
                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, item)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Clipboard Item"))
                            onTriggerConfetti()
                        } catch (e: Exception) { Log.e(TAG, "Failed to share clipboard item", e) }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.Share, "Share", modifier = Modifier.size(18.dp), tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Copy
                IconButton(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("QuickDash", item))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, "Copy", modifier = Modifier.size(18.dp), tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete
                IconButton(
                    onClick = {
                        val newList = clipboardItems.toMutableList().apply { remove(item) }
                        viewModel.saveClipboardHistory(gson.toJson(newList))
                        if (isPinned) {
                            val newPinned = pinnedItems.filter { it != item }
                            viewModel.saveClipboardPinned(gson.toJson(newPinned))
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.Delete, "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
