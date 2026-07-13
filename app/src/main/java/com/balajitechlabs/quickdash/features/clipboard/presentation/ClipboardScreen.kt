package com.balajitechlabs.quickdash.features.clipboard.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.core.data.UserStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

data class ActionableItem(
    val label: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val intent: Intent
)

fun parseClipboardContent(text: String, context: Context): List<ActionableItem> {
    val items = mutableListOf<ActionableItem>()
    val trimmed = text.trim()

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
                    icon = Icons.Default.Call,
                    intent = intent
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
                icon = Icons.Default.Email,
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
                "Open YouTube" to Icons.Default.PlayArrow
            }
            host.contains("maps.google") || host.contains("google.com/maps") || host.contains("maps.app.goo.gl") -> {
                intent.setPackage("com.google.android.apps.maps")
                "Open Maps" to Icons.Default.LocationOn
            }
            host.contains("play.google.com") -> {
                intent.setPackage("com.android.vending")
                "Open Play Store" to Icons.Default.Info
            }
            host.contains("instagram.com") -> {
                intent.setPackage("com.instagram.android")
                "Open Instagram" to Icons.Default.Share
            }
            host.contains("twitter.com") || host.contains("x.com") -> {
                intent.setPackage("com.twitter.android")
                "Open X / Twitter" to Icons.Default.Share
            }
            finalUrl.startsWith("upi:") -> {
                "UPI Payment" to Icons.Default.QrCode
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
    userStore: UserStore,
    isFloating: Boolean = false,
    onTriggerConfetti: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    val clipboardJson by userStore.clipboardHistory.collectAsState(initial = "[]")
    val pinnedJson by userStore.clipboardPinned.collectAsState(initial = "[]")

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

    val isTabLocked by userStore.tabBiometricLock.collectAsState(initial = false)
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
                        Icons.Default.Lock,
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
                Icon(Icons.Default.Fingerprint, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Unlock with Biometrics")
            }
        }
        return
    }

    // ── Main content ──────────────────────────────────────────────────────
    Column(modifier = Modifier.fillMaxWidth()) {

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
                    Icon(Icons.Default.Delete, contentDescription = "Clear all", modifier = Modifier.size(20.dp))
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
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (selectedFilter == filter) ({
                            Icon(Icons.Default.FilterList, null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                        }) else null
                    )
                }
            }
        }

        // ── Empty state ───────────────────────────────────────────────────
        if (filteredItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    if (clipboardItems.isEmpty()) "Nothing copied yet"
                    else "No $selectedFilter items found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    if (clipboardItems.isEmpty()) "Text copied while the app is running will appear here automatically."
                    else "Try a different filter to see your clipboard history.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Clipboard entries ─────────────────────────────────────────────
        val displayItems = if (isFloating) filteredItems.take(5) else filteredItems
        if (filteredItems.isNotEmpty()) {
            LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            // Render Pinned section first if "All" is selected and there are pinned items
            if (selectedFilter == "All" && pinnedItems.isNotEmpty() && !isFloating) {
                item {
                    Text(
                        text = "📌 Pinned Items",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                itemsIndexed(pinnedItems, key = { index, item -> "pinned:$index:$item" }) { _, item ->
                    ClipboardItemCard(
                        item = item,
                        pinnedItems = pinnedItems,
                        clipboardItems = clipboardItems,
                        userStore = userStore,
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
                item {
                    Text(
                        text = "Recent History",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
            }

            // Render main filtered list
            itemsIndexed(displayItems, key = { index, item -> "filtered:$index:$item" }) { _, item ->
                ClipboardItemCard(
                    item = item,
                    pinnedItems = pinnedItems,
                    clipboardItems = clipboardItems,
                    userStore = userStore,
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

    if (showClearAllConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmation = false },
            title = { Text("Clear Clipboard History") },
            text = { Text("Are you sure you want to clear all items in your clipboard history?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        userStore.saveClipboardHistory("[]")
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
    userStore: UserStore,
    onTriggerConfetti: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    gson: com.google.gson.Gson,
    context: android.content.Context,
    sensitive: Boolean,
    revealed: Boolean,
    onToggleReveal: () -> Unit
) {
    val isPinned = pinnedItems.contains(item)
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (sensitive && !revealed)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Clipboard Text (Full Width)
            Text(
                text = if (sensitive && !revealed) "🔒 Sensitive content hidden" else item,
                style = MaterialTheme.typography.bodyMedium,
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
                        items(actions) { action ->
                            AssistChip(
                                onClick = {
                                    try {
                                        context.startActivity(action.intent)
                                    } catch (e: Exception) {
                                        try {
                                            action.intent.setPackage(null)
                                            context.startActivity(action.intent)
                                        } catch (ex: Exception) {
                                            ex.printStackTrace()
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                label = {
                                    Text(
                                        text = action.label,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(2.dp))

            // Action Buttons Row (At the bottom)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Pin/Unpin
                IconButton(
                    onClick = {
                        val newList = if (isPinned) {
                            pinnedItems.filter { it != item }
                        } else {
                            pinnedItems + item
                        }
                        coroutineScope.launch {
                            userStore.saveClipboardPinned(gson.toJson(newList))
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = if (isPinned) "Unpin" else "Pin",
                        modifier = Modifier.size(18.dp),
                        tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
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
                        } catch (e: Exception) { e.printStackTrace() }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Share, "Share", modifier = Modifier.size(18.dp))
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
                    Icon(Icons.Default.ContentCopy, "Copy", modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete
                IconButton(
                    onClick = {
                        val newList = clipboardItems.toMutableList().apply { remove(item) }
                        coroutineScope.launch { userStore.saveClipboardHistory(gson.toJson(newList)) }
                        if (isPinned) {
                            val newPinned = pinnedItems.filter { it != item }
                            coroutineScope.launch { userStore.saveClipboardPinned(gson.toJson(newPinned)) }
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
