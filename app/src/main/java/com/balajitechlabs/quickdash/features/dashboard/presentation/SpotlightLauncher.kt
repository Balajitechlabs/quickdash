/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/dashboard/presentation
 * File: SpotlightLauncher.kt
 * Description: Quick search bar and app launcher indexing installed apps, tools, and actions.
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
import android.widget.Toast
import com.balajitechlabs.quickdash.features.dashboard.presentation.components.SearchEngineChip
import com.balajitechlabs.quickdash.features.dashboard.presentation.components.SpotlightShortcutCard
import com.balajitechlabs.quickdash.features.dashboard.presentation.components.SpotlightWebSearchCard
import com.balajitechlabs.quickdash.features.dashboard.presentation.components.ToolListItemRow
import com.balajitechlabs.quickdash.features.dashboard.presentation.dialogs.CustomizeFavoritesDialog
import com.balajitechlabs.quickdash.features.dashboard.presentation.dialogs.TelegramPinDialog
import com.balajitechlabs.quickdash.features.dashboard.presentation.model.SpotlightShortcutResult
import com.balajitechlabs.quickdash.features.dashboard.presentation.model.SpotlightToolItem
import com.balajitechlabs.quickdash.features.dashboard.presentation.model.evaluateSpotlightQuery

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
    onToolSelected: (QuickTool) -> Unit,
    onScanQr: () -> Unit = {}
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
                                onScanQr()
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
                SpotlightShortcutCard(
                    result = spotlightShortcutResult,
                    onCopyResult = {
                        triggerClickHaptic()
                        clipboardManager.setText(AnnotatedString(spotlightShortcutResult.result))
                        Toast.makeText(context, "Copied ${spotlightShortcutResult.result}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // ── 3. Web Search Quick Action ─────────────────────────
        if (searchQuery.isNotBlank() && spotlightShortcutResult == null) {
            item(key = "web_search_action") {
                SpotlightWebSearchCard(
                    searchQuery = searchQuery,
                    onTriggerHaptic = ::triggerClickHaptic
                )
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
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
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
