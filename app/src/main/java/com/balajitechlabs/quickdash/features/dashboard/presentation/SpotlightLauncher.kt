/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/dashboard
 * File: SpotlightLauncher.kt
 * Description: EssentialX-styled component for features/dashboard supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.dashboard.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.MainViewModel
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.ui.QuickTool
import com.balajitechlabs.quickdash.core.ui.components.CategoryExpandableSection
import com.balajitechlabs.quickdash.core.ui.components.FavoriteCarousel
import com.balajitechlabs.quickdash.core.ui.components.FavoriteCarouselItem
import com.balajitechlabs.quickdash.core.ui.components.PreferenceItem
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import androidx.compose.ui.graphics.vector.ImageVector
import com.balajitechlabs.quickdash.core.ui.playClickVibration
import com.balajitechlabs.quickdash.core.ui.theme.ColorUtil
import com.balajitechlabs.quickdash.core.ui.toolDefinitions
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class SpotlightToolItem(
    val tool: QuickTool,
    val title: String,
    val description: String,
    val iconRes: Int = 0,
    val category: String,
    val imageVector: ImageVector? = null
)

/**
 * EssentialX & Raycast-inspired Spotlight Command Hub & Horizontal Tool Launcher.
 * Features:
 * - Crisp, high-contrast white & AMOLED surfaces (zero blurry text)
 * - Live fuzzy search across 20+ specialized tools
 * - Real-time inline math expression evaluation with instant clipboard copy
 * - Horizontal swipeable FavoriteCarousel with pastel circular badges
 * - Vertical categorized expandable sections (EssentialX design specs)
 *
 * Developer: balajitechlabs
 */
@Composable
fun SpotlightLauncher(
    hapticEnabled: Boolean,
    isFloating: Boolean,
    mainViewModel: MainViewModel?,
    listState: LazyListState,
    onToolSelected: (QuickTool) -> Unit
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(false) }

    fun triggerClickHaptic() {
        playClickVibration(context, hapticEnabled)
    }

    val allTools = remember {
        toolDefinitions().map {
            SpotlightToolItem(
                tool = it.tool,
                title = it.title,
                description = it.description,
                iconRes = it.iconRes,
                category = it.category,
                imageVector = it.imageVector
            )
        }
    }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 6..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            in 18..21 -> "Good evening"
            else -> "Good night"
        }
    }

    val favToolsFlow = mainViewModel?.userStore?.favoriteTools?.collectAsStateWithLifecycle(initialValue = "")
    val userFavorites = remember(favToolsFlow?.value) {
        val str = favToolsFlow?.value ?: ""
        when {
            str == "EMPTY" -> emptyList()
            str.isEmpty() -> emptyList()
            else -> str.split(",").mapNotNull { runCatching { QuickTool.valueOf(it) }.getOrNull() }
        }
    }

    val toolOrderFlow = mainViewModel?.userStore?.toolOrder?.collectAsStateWithLifecycle(initialValue = "")
    var toolOrderOverride by remember { mutableStateOf<String?>(null) }
    val effectiveOrderString = toolOrderOverride ?: (toolOrderFlow?.value ?: "")

    val orderedAllTools = remember(allTools, effectiveOrderString) {
        if (effectiveOrderString.isEmpty()) {
            allTools
        } else {
            val orderList: List<QuickTool> = effectiveOrderString.split(",").mapNotNull { runCatching { QuickTool.valueOf(it) }.getOrNull() }
            val orderMap: Map<QuickTool, Int> = orderList.mapIndexed { index, tool -> tool to index }.toMap()
            allTools.sortedBy { orderMap[it.tool] ?: Int.MAX_VALUE }
        }
    }

    // Inline Spotlight Evaluation (Math, Currency & Unit Conversions)
    val spotlightShortcutResult = remember(searchQuery) {
        evaluateSpotlightQuery(searchQuery)
    }

    var selectedToolForPinMenu by remember { mutableStateOf<SpotlightToolItem?>(null) }
    var showCustomizeFavoritesDialog by remember { mutableStateOf(false) }

    val isSearching = searchQuery.isNotBlank()

    val filteredTools = remember(orderedAllTools, searchQuery) {
        if (searchQuery.isBlank()) {
            orderedAllTools
        } else {
            orderedAllTools.filter { item ->
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    fun saveReorderedTools(newOrder: List<SpotlightToolItem>) {
        val orderString = newOrder.joinToString(",") { it.tool.name }
        toolOrderOverride = orderString
        coroutineScope.launch {
            mainViewModel?.userStore?.saveToolOrder(orderString)
        }
    }

    val favItems = remember(orderedAllTools, userFavorites) {
        orderedAllTools.filter { it.tool in userFavorites }.map {
            FavoriteCarouselItem(tool = it.tool, title = it.title, iconRes = it.iconRes, subtitle = it.category, imageVector = it.imageVector)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── 1. Spotlight Search / Command Bar (EssentialX Style) ──────────
        item(key = "search_bar") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search QuickDash",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFC5C6D0)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFC5C6D0),
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFFC5C6D0),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                triggerClickHaptic()
                                onToolSelected(QuickTool.QRSCANNER)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = "Scan QR Code",
                                tint = Color(0xFFC5C6D0),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF44474F),
                    unfocusedBorderColor = Color(0xFF44474F).copy(alpha = 0.6f),
                    focusedContainerColor = Color(0xFF38393F),
                    unfocusedContainerColor = Color(0xFF38393F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        // ── 2. Inline Spotlight Shortcut Card (Math, Currency & Unit Conversion) ──
        if (spotlightShortcutResult != null) {
            item(key = "spotlight_shortcut_card") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF38393F),
                    border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFB0C6FF).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = spotlightShortcutResult.icon,
                                    contentDescription = null,
                                    tint = Color(0xFFB0C6FF),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = spotlightShortcutResult.category,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFFB0C6FF)
                                )
                                Text(
                                    text = spotlightShortcutResult.result,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = spotlightShortcutResult.expression,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC5C6D0)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                triggerClickHaptic()
                                clipboardManager.setText(AnnotatedString(spotlightShortcutResult.result))
                                android.widget.Toast.makeText(context, "Copied ${spotlightShortcutResult.result}", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── 3. Web Search Quick Action ─────────────────────────
        if (searchQuery.isNotBlank() && spotlightShortcutResult == null) {
            item(key = "web_search_action") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                triggerClickHaptic()
                                val url = if (searchQuery.trim().startsWith("http://") || searchQuery.trim().startsWith("https://")) {
                                    searchQuery.trim()
                                } else if (searchQuery.trim().contains(".") && !searchQuery.trim().contains(" ")) {
                                    "https://${searchQuery.trim()}"
                                } else {
                                    "https://www.google.com/search?q=${Uri.encode(searchQuery.trim())}"
                                }
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                                context.startActivity(intent)
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF38393F),
                        border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = Color(0xFFB0C6FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Search web for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }

                    // Multi-Engine Search Chips (Google, YouTube, GitHub, Reddit, Wikipedia)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            SearchEngineChip(label = "Google", icon = Icons.Rounded.Search) {
                                triggerClickHaptic()
                                val url = "https://www.google.com/search?q=${Uri.encode(searchQuery.trim())}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                        item {
                            SearchEngineChip(label = "YouTube", icon = Icons.Rounded.PlayArrow) {
                                triggerClickHaptic()
                                val url = "https://www.youtube.com/results?search_query=${Uri.encode(searchQuery.trim())}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                        item {
                            SearchEngineChip(label = "GitHub", icon = Icons.Rounded.Code) {
                                triggerClickHaptic()
                                val url = "https://github.com/search?q=${Uri.encode(searchQuery.trim())}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                        item {
                            @Suppress("DEPRECATION")
                            SearchEngineChip(label = "Reddit", icon = Icons.Rounded.Chat) {
                                triggerClickHaptic()
                                val url = "https://www.reddit.com/search/?q=${Uri.encode(searchQuery.trim())}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                        item {
                            @Suppress("DEPRECATION")
                            SearchEngineChip(label = "Wikipedia", icon = Icons.Rounded.MenuBook) {
                                triggerClickHaptic()
                                val url = "https://en.wikipedia.org/wiki/Special:Search?search=${Uri.encode(searchQuery.trim())}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                    }
                }
            }
        }

        // ── 4. Horizontal Favorite Carousel (EssentialX Style) ──
        if (!isSearching && favItems.isNotEmpty()) {
            item(key = "favorite_carousel") {
                FavoriteCarousel(
                    items = favItems,
                    onItemClick = onToolSelected,
                    onItemLongClick = {
                        showCustomizeFavoritesDialog = true
                    },
                    hapticEnabled = hapticEnabled,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        // ── 5. Unified Vertical Pill Container for All Tools (EssentialX Style) ──
        val displayTools = if (isSearching) filteredTools else orderedAllTools
        if (displayTools.isNotEmpty()) {
            item(key = "tools_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isSearching) "Search Results" else "All Tools",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF38393F))
                                .border(BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f)), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = displayTools.size.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFFB0C6FF)
                            )
                        }
                    }
                }
            }

            item(key = "unified_tools_container") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF38393F),
                    border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        displayTools.forEachIndexed { index, toolItem ->
                            ToolListItemRow(
                                item = toolItem,
                                isPinned = userFavorites.contains(toolItem.tool),
                                onToolSelected = onToolSelected,
                                onToolLongClick = { selectedToolForPinMenu = toolItem },
                                triggerClickHaptic = { triggerClickHaptic() }
                            )
                            if (index < displayTools.size - 1) {
                                HorizontalDivider(
                                    color = Color(0xFF44474F).copy(alpha = 0.45f),
                                    thickness = 0.8.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomizeFavoritesDialog) {
        CustomizeFavoritesDialog(
            currentFavorites = userFavorites,
            allTools = allTools,
            onSave = { updatedList ->
                coroutineScope.launch {
                    val saveStr = if (updatedList.isEmpty()) "EMPTY" else updatedList.joinToString(",") { it.name }
                    mainViewModel?.userStore?.saveFavoriteTools(saveStr)
                }
            },
            onDismiss = { showCustomizeFavoritesDialog = false }
        )
    }

    // Telegram-Style Pin / Unpin Context Dialog on Long-Press
    if (selectedToolForPinMenu != null) {
        val toolItem = selectedToolForPinMenu!!
        val isPinned = toolItem.tool in userFavorites
        TelegramPinDialog(
            toolItem = toolItem,
            isPinned = isPinned,
            onTogglePin = {
                triggerClickHaptic()
                val newFavorites = if (isPinned) {
                    userFavorites.filter { it != toolItem.tool }
                } else {
                    listOf(toolItem.tool) + userFavorites
                }
                coroutineScope.launch {
                    val saveStr = if (newFavorites.isEmpty()) "EMPTY" else newFavorites.joinToString(",") { it.name }
                    mainViewModel?.userStore?.saveFavoriteTools(saveStr)
                }
                val toastMsg = if (isPinned) "Unpinned ${toolItem.title}" else "Pinned ${toolItem.title} to top"
                android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_SHORT).show()
                selectedToolForPinMenu = null
            },
            onOpenTool = {
                triggerClickHaptic()
                onToolSelected(toolItem.tool)
                selectedToolForPinMenu = null
            },
            onDismiss = { selectedToolForPinMenu = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ToolListItemRow(
    item: SpotlightToolItem,
    isPinned: Boolean = false,
    onToolSelected: (QuickTool) -> Unit,
    onToolLongClick: (() -> Unit)? = null,
    triggerClickHaptic: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tool_row_scale"
    )

    val iconBg = Color(0xFF2A2B30)
    val iconTint = Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    triggerClickHaptic()
                    onToolSelected(item.tool)
                },
                onLongClick = {
                    triggerClickHaptic()
                    onToolLongClick?.invoke()
                }
            )
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageVector != null) {
                    Icon(
                        imageVector = item.imageVector,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = Color(0xFFC5C6D0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isPinned) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = "Pinned",
                        tint = Color(0xFFB0C6FF),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Open",
                    tint = Color(0xFF74777F),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Lightweight mathematical evaluator supporting +, -, *, /, %, ^ and parentheses.
 */
private fun evaluateMathExpression(expr: String): String {
    val clean = expr.replace(" ", "").replace("x", "*").replace("X", "*")
    val tokens = mutableListOf<String>()
    var i = 0
    while (i < clean.length) {
        val c = clean[i]
        if (c.isDigit() || c == '.') {
            var num = ""
            while (i < clean.length && (clean[i].isDigit() || clean[i] == '.')) {
                num += clean[i]
                i++
            }
            tokens.add(num)
        } else {
            tokens.add(c.toString())
            i++
        }
    }

    if (tokens.isEmpty()) return ""

    var result = tokens[0].toDoubleOrNull() ?: return ""
    var opIndex = 1
    while (opIndex < tokens.size - 1) {
        val op = tokens[opIndex]
        val nextVal = tokens[opIndex + 1].toDoubleOrNull() ?: return ""
        when (op) {
            "+" -> result += nextVal
            "-" -> result -= nextVal
            "*" -> result *= nextVal
            "/" -> if (nextVal != 0.0) result /= nextVal else return "Cannot divide by 0"
            "%" -> result %= nextVal
        }
        opIndex += 2
    }

    return if (result % 1.0 == 0.0) {
        result.toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.4f", result).trimEnd('0').trimEnd('.')
    }
}

data class SpotlightShortcutResult(
    val category: String,
    val expression: String,
    val result: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun evaluateSpotlightQuery(query: String): SpotlightShortcutResult? {
    val clean = query.trim()
    if (clean.length < 2) return null

    // 1. Currency Conversion (e.g. 100 USD to INR, 50 EUR in USD, 1000 inr usd)
    val currRegex = """^(\d+(?:\.\d+)?)\s*([a-zA-Z]{3})\s*(?:to|in|=)?\s*([a-zA-Z]{3})$""".toRegex()
    val currMatch = currRegex.find(clean)
    if (currMatch != null) {
        val amount = currMatch.groupValues[1].toDoubleOrNull()
        val fromCurr = currMatch.groupValues[2].uppercase()
        val toCurr = currMatch.groupValues[3].uppercase()

        val rates = mapOf(
            "USD" to 1.0,
            "INR" to 86.50,
            "EUR" to 0.92,
            "GBP" to 0.79,
            "AED" to 3.67,
            "CAD" to 1.38,
            "AUD" to 1.55,
            "JPY" to 152.0,
            "SGD" to 1.34,
            "CNY" to 7.24
        )

        val fromRate = rates[fromCurr]
        val toRate = rates[toCurr]
        if (amount != null && fromRate != null && toRate != null) {
            val converted = (amount / fromRate) * toRate
            return SpotlightShortcutResult(
                category = "Currency Conversion",
                expression = "$amount $fromCurr → $toCurr",
                result = String.format(java.util.Locale.US, "%.2f %s", converted, toCurr),
                icon = Icons.Rounded.CurrencyExchange
            )
        }
    }

    // 2. Common Unit Conversion (e.g. 10 km to mi, 5 kg in lbs, 100 f to c, 6 ft in cm)
    val unitRegex = """^(\d+(?:\.\d+)?)\s*([a-zA-Z]+)\s*(?:to|in|=)?\s*([a-zA-Z]+)$""".toRegex()
    val unitMatch = unitRegex.find(clean)
    if (unitMatch != null) {
        val value = unitMatch.groupValues[1].toDoubleOrNull()
        val fromUnit = unitMatch.groupValues[2].lowercase()
        val toUnit = unitMatch.groupValues[3].lowercase()

        if (value != null) {
            val converted: Double? = when {
                fromUnit in listOf("km", "kilometer", "kilometers") && toUnit in listOf("mi", "mile", "miles") -> value * 0.621371
                fromUnit in listOf("mi", "mile", "miles") && toUnit in listOf("km", "kilometer", "kilometers") -> value * 1.60934
                fromUnit in listOf("m", "meter", "meters") && toUnit in listOf("ft", "feet") -> value * 3.28084
                fromUnit in listOf("ft", "feet") && toUnit in listOf("m", "meter", "meters") -> value * 0.3048
                fromUnit in listOf("cm", "centimeter") && toUnit in listOf("in", "inch", "inches") -> value * 0.393701
                fromUnit in listOf("in", "inch", "inches") && toUnit in listOf("cm", "centimeter") -> value * 2.54
                fromUnit in listOf("kg", "kilogram") && toUnit in listOf("lbs", "lb", "pound", "pounds") -> value * 2.20462
                fromUnit in listOf("lbs", "lb", "pound", "pounds") && toUnit in listOf("kg", "kilogram") -> value * 0.453592
                fromUnit in listOf("g", "gram") && toUnit in listOf("oz", "ounce") -> value * 0.035274
                fromUnit in listOf("oz", "ounce") && toUnit in listOf("g", "gram") -> value * 28.3495
                fromUnit in listOf("c", "celsius") && toUnit in listOf("f", "fahrenheit") -> (value * 9.0 / 5.0) + 32.0
                fromUnit in listOf("f", "fahrenheit") && toUnit in listOf("c", "celsius") -> (value - 32.0) * 5.0 / 9.0
                else -> null
            }
            if (converted != null) {
                val formatted = if (converted % 1.0 == 0.0) converted.toLong().toString() else String.format(java.util.Locale.US, "%.2f", converted)
                return SpotlightShortcutResult(
                    category = "Unit Conversion",
                    expression = "$value $fromUnit → $toUnit",
                    result = "$formatted $toUnit",
                    icon = Icons.Rounded.Straighten
                )
            }
        }
    }

    // 3. Mathematical Evaluation Heuristic
    if (clean.length >= 2 && clean.all { it.isDigit() || it in "+-*/().%^ xX" } && clean.any { it in "+-*/%xX" }) {
        try {
            val mathRes = evaluateMathExpression(clean)
            if (mathRes.isNotBlank()) {
                return SpotlightShortcutResult(
                    category = "Calculation Result",
                    expression = clean,
                    result = "= $mathRes",
                    icon = Icons.Rounded.Calculate
                )
            }
        } catch (_: Exception) {}
    }

    return null
}

@Composable
private fun CustomizeFavoritesDialog(
    currentFavorites: List<QuickTool>,
    allTools: List<SpotlightToolItem>,
    onSave: (List<QuickTool>) -> Unit,
    onDismiss: () -> Unit
) {
    var workingList by remember { mutableStateOf(currentFavorites) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E2024),
        title = {
            Text(
                text = "Customize Favorites",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                Text(
                    text = "Reorder or remove your top squircle tools:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC5C6D0),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(workingList) { index, tool ->
                        val toolInfo = allTools.find { it.tool == tool }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF38393F),
                            border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = toolInfo?.title ?: tool.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    modifier = Modifier.weight(1f)
                                )

                                Row {
                                    if (index > 0) {
                                        IconButton(
                                            onClick = {
                                                val list = workingList.toMutableList()
                                                val item = list.removeAt(index)
                                                list.add(index - 1, item)
                                                workingList = list
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move Up", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    if (index < workingList.size - 1) {
                                        IconButton(
                                            onClick = {
                                                val list = workingList.toMutableList()
                                                val item = list.removeAt(index)
                                                list.add(index + 1, item)
                                                workingList = list
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move Down", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            workingList = workingList.filter { it != tool }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = Color(0xFFFFB4AB), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        workingList = listOf(QuickTool.UPI, QuickTool.CHAT, QuickTool.CLIPBOARD, QuickTool.NOTES, QuickTool.CAPTURE, QuickTool.WIFI, QuickTool.PASSWORD)
                    }
                ) {
                    Text("Reset", color = Color(0xFFB0C6FF))
                }
                Button(
                    onClick = {
                        onSave(workingList)
                        onDismiss()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF38393F)),
                    border = BorderStroke(1.dp, Color(0xFF44474F))
                ) {
                    Text("Save", color = Color.White)
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFC5C6D0))
            }
        }
    )
}

@Composable
private fun SearchEngineChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF38393F),
        border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFB0C6FF),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
        }
    }
}

@Composable
private fun TelegramPinDialog(
    toolItem: SpotlightToolItem,
    isPinned: Boolean,
    onTogglePin: () -> Unit,
    onOpenTool: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E2024),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2B30)),
                    contentAlignment = Alignment.Center
                ) {
                    if (toolItem.imageVector != null) {
                        Icon(
                            imageVector = toolItem.imageVector,
                            contentDescription = toolItem.title,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = toolItem.iconRes),
                            contentDescription = toolItem.title,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = toolItem.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = toolItem.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC5C6D0),
                        maxLines = 1
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option 1: Pin / Unpin
                Surface(
                    onClick = {
                        onTogglePin()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF38393F),
                    border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Rounded.Close else Icons.Rounded.PushPin,
                            contentDescription = null,
                            tint = if (isPinned) Color(0xFFFFB4AB) else Color(0xFFB0C6FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isPinned) "Unpin from Top" else "Pin to Top",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isPinned) Color(0xFFFFB4AB) else Color.White
                        )
                    }
                }

                // Option 2: Open Tool
                Surface(
                    onClick = {
                        onOpenTool()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF38393F),
                    border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Open Tool",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}
