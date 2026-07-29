package com.balajitechlabs.quickdash.features.dashboard.presentation

import android.content.Context
import android.os.VibratorManager
import java.util.Calendar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.ui.components.responsiveDimensions
import com.balajitechlabs.quickdash.core.data.UserStore

enum class QuickTool {
    UPI, WHATSAPP, INSTAGRAM, NOTES, SEARCH, WIFI, CLIPBOARD, CALCULATOR, TIMER, CONVERTER, TRANSLATOR, CAPTURE, EYEDROPPER, POMODORO, PASSWORD, DISCOUNT, EXCHANGE, VOICEMEMOS, REMINDERS, QRSCANNER
}

data class ToolDef(
    val tool: QuickTool,
    val title: String,
    val description: String,
    val iconRes: Int,
    val containerColor: @Composable () -> Color,
    val iconColor: @Composable () -> Color,
)

fun toolDefinitions(usePaypal: Boolean = false, cs: ColorScheme): List<ToolDef> {
    return listOf(
        ToolDef(QuickTool.UPI,         if (usePaypal) "Quick PayPal" else "Quick Collect",      if (usePaypal) "Quick PayPal link generators" else "UPI & payment app QR codes",        if (usePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay,     { cs.secondaryContainer },       { cs.onSecondaryContainer }),
        ToolDef(QuickTool.WHATSAPP,    "Quick Chat",         "Prefilled direct chat & templates",       R.drawable.ic_shortcut_chat,     { cs.tertiaryContainer },        { cs.onTertiaryContainer }),
        ToolDef(QuickTool.INSTAGRAM,   "Quick Social Access",        "Social media & GitHub profiler", R.drawable.ic_instagram,   { cs.errorContainer },           { cs.onErrorContainer }),
        ToolDef(QuickTool.TRANSLATOR,  "Quick Translator",   "Language translate & AI assistant",     R.drawable.ic_globe,       { cs.tertiaryContainer },        { cs.onTertiaryContainer }),
        ToolDef(QuickTool.CLIPBOARD,   "Smart Clipboard",    "Copy history & sensitive data guard", R.drawable.ic_note,        { cs.primaryContainer },         { cs.onPrimaryContainer }),
        ToolDef(QuickTool.NOTES,       "Quick Notes",        "Offline notes with Markdown",          R.drawable.ic_note,        { cs.primaryContainer },         { cs.onPrimaryContainer }),
        ToolDef(QuickTool.CONVERTER,   "Quick Converter",    "Currency & unit conversions",          R.drawable.ic_currency_rupee,{ cs.secondaryContainer },       { cs.onSecondaryContainer }),
        ToolDef(QuickTool.CAPTURE,     "Quick Capture",      "Screen recorder & doodle annotator",   R.drawable.ic_tools,       { cs.primaryContainer },         { cs.onPrimaryContainer }),
        ToolDef(QuickTool.SEARCH,      "Quick Search",       "Multi-engine web search",             R.drawable.ic_search,      { cs.secondaryContainer },       { cs.onSecondaryContainer }),
        ToolDef(QuickTool.WIFI,        "Quick Wi-Fi",        "Share Wi-Fi via encrypted QR",         R.drawable.ic_qr_code,     { cs.tertiaryContainer },        { cs.onTertiaryContainer }),
        ToolDef(QuickTool.CALCULATOR,  "Quick Calculator",   "Expression calc with history",         R.drawable.ic_calculator,  { cs.primaryContainer },         { cs.onPrimaryContainer }),
        ToolDef(QuickTool.TIMER,       "Quick Timer",        "Stopwatch & countdown timer",          R.drawable.ic_timer,       { cs.secondaryContainer },       { cs.onSecondaryContainer }),
        ToolDef(QuickTool.EYEDROPPER,  "Quick Eyedropper",   "Pixel loupe & HEX/RGB color picker",   R.drawable.ic_tools,       { cs.tertiaryContainer },        { cs.onTertiaryContainer }),
        ToolDef(QuickTool.POMODORO,    "Quick Pomodoro",     "25m focus & study interval timer",     R.drawable.ic_timer,       { cs.primaryContainer },         { cs.onPrimaryContainer }),
        ToolDef(QuickTool.PASSWORD,    "Quick Password",     "Secure passwords & SHA-256 hash",      R.drawable.ic_note,        { cs.secondaryContainer },       { cs.onSecondaryContainer }),
        ToolDef(QuickTool.DISCOUNT,    "Quick Discount",     "Compare prices & discounts per unit",  R.drawable.ic_calculator,  { cs.tertiaryContainer },        { cs.onTertiaryContainer }),
        ToolDef(QuickTool.EXCHANGE,    "Quick Exchange",     "Offline exchange rates converter",     R.drawable.ic_currency_rupee,{ cs.primaryContainer },       { cs.onPrimaryContainer }),
        ToolDef(QuickTool.VOICEMEMOS,  "Quick Voice Memos",  "Floating audio voice recorder",        R.drawable.ic_tools,       { cs.secondaryContainer },       { cs.onSecondaryContainer }),
        ToolDef(QuickTool.REMINDERS,   "Quick Reminders",    "Floating alarm & break reminder",      R.drawable.ic_timer,       { cs.tertiaryContainer },        { cs.onTertiaryContainer }),
        ToolDef(QuickTool.QRSCANNER,   "Quick QR Scanner",   "Camera QR & barcode scanner with history", R.drawable.ic_qr_code, { cs.primaryContainer },       { cs.onPrimaryContainer }),
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    hapticEnabled: Boolean = true,
    isFloating: Boolean = false,
    usePaypal: Boolean = false,
    showToolDescriptions: Boolean = true,
    mainViewModel: com.balajitechlabs.quickdash.MainViewModel? = null,
    gridState: LazyGridState = rememberLazyGridState(),
    listState: LazyListState = rememberLazyListState(),
    onToolSelected: (QuickTool) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val allTools = remember(usePaypal, cs) { toolDefinitions(usePaypal, cs) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isRefreshing by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dims = responsiveDimensions()

    // Dynamic greeting based on time of day
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 6..11 -> "☀️ Good morning"
            in 12..17 -> "🌤️ Good afternoon"
            in 18..21 -> "🌅 Good evening"
            else -> "🌙 Good night"
        }
    }

    val favToolsFlow = mainViewModel?.settingsRepository?.favoriteTools?.collectAsState(initial = "")
    val userFavorites = remember(favToolsFlow?.value) {
        val str = favToolsFlow?.value ?: ""
        if (str.isEmpty()) listOf(QuickTool.UPI, QuickTool.CLIPBOARD, QuickTool.NOTES, QuickTool.QRSCANNER, QuickTool.EYEDROPPER, QuickTool.CAPTURE, QuickTool.PASSWORD, QuickTool.TIMER)
        else str.split(",").mapNotNull { runCatching { QuickTool.valueOf(it) }.getOrNull() }
    }

    val toolOrderFlow = mainViewModel?.settingsRepository?.toolOrder?.collectAsState(initial = "")
    var toolOrderOverride by remember { mutableStateOf<String?>(null) }
    val effectiveOrderString = toolOrderOverride ?: (toolOrderFlow?.value ?: "")

    val orderedAllTools = remember(allTools, effectiveOrderString) {
        if (effectiveOrderString.isEmpty()) {
            allTools
        } else {
            val orderList = effectiveOrderString.split(",").mapNotNull { runCatching { QuickTool.valueOf(it) }.getOrNull() }
            val orderMap = orderList.withIndex().associate { it.value to it.index }
            allTools.sortedBy { orderMap[it.tool] ?: Int.MAX_VALUE }
        }
    }

    val filteredTools = remember(orderedAllTools, searchQuery, selectedCategory, userFavorites) {
        orderedAllTools.filter { tool ->
            val matchesSearch = searchQuery.isBlank() ||
                    tool.title.contains(searchQuery, ignoreCase = true) ||
                    tool.description.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                "⭐ Favorites" -> tool.tool in userFavorites
                "Finance" -> tool.tool in listOf(QuickTool.UPI, QuickTool.EXCHANGE, QuickTool.DISCOUNT)
                "Tools"   -> tool.tool in listOf(QuickTool.CALCULATOR, QuickTool.CONVERTER, QuickTool.TIMER, QuickTool.POMODORO, QuickTool.EYEDROPPER, QuickTool.PASSWORD)
                "Media"   -> tool.tool in listOf(QuickTool.CAPTURE, QuickTool.VOICEMEMOS, QuickTool.QRSCANNER)
                else      -> true
            }

            matchesSearch && matchesCategory
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                kotlinx.coroutines.delay(800)
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (!isFloating) {
                // 🌟 Hero Banner Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "20 TOOLS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Instant floating utilities at your fingertips",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                // ✏️ Reorder Mode Active Banner
                AnimatedVisibility(
                    visible = isEditMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✨", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "Reorder Tools Mode",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Tap arrows on cards to rearrange order",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Button(
                                onClick = { isEditMode = false },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Text("✓ Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 🔍 Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 20+ tools...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search tools") },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .semantics { contentDescription = "Search tools field" }
                )

                // ⭐ Favorite Quick Bar
                val favTools = remember(allTools) {
                    allTools.filter { tool ->
                        tool.tool in listOf(QuickTool.UPI, QuickTool.CLIPBOARD, QuickTool.NOTES, QuickTool.QRSCANNER, QuickTool.EYEDROPPER, QuickTool.CAPTURE)
                    }
                }
                if (searchQuery.isBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "⭐ Quick Favorites",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Long-press cards to reorder",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        )
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favTools, key = { "fav_${it.tool.name}" }) { tool ->
                            AssistChip(
                                onClick = { onToolSelected(tool.tool) },
                                label = { Text(tool.title, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = tool.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                // 🏷️ Category Filter Row
                val categories = listOf("All", "⭐ Favorites", "Finance", "Tools", "Media")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it }) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            if (isFloating) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (filteredTools.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔍", style = MaterialTheme.typography.displaySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No tools match ‘$searchQuery’",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FilledTonalButton(onClick = { searchQuery = "" }) { Text("Clear search") }
                            }
                        }
                    }
                    itemsIndexed(filteredTools, key = { _, tool -> tool.tool.name }) { index, tool ->
                        CompactToolCard(
                            tool = tool,
                            hapticEnabled = hapticEnabled,
                            animDelay = index * 30,
                            showToolDescriptions = showToolDescriptions,
                            dims = dims,
                            isEditMode = isEditMode,
                            onToggleEditMode = { isEditMode = !isEditMode },
                            onMove = { dir ->
                                val list = orderedAllTools.map { it.tool }.toMutableList()
                                val i = list.indexOf(tool.tool)
                                if (i >= 0 && i + dir >= 0 && i + dir < list.size) {
                                    val temp = list[i]
                                    list[i] = list[i + dir]
                                    list[i + dir] = temp
                                    val newOrderStr = list.joinToString(",")
                                    toolOrderOverride = newOrderStr
                                    scope.launch { mainViewModel?.settingsRepository?.saveToolOrder(newOrderStr) }
                                }
                            },
                            onClick = { onToolSelected(tool.tool) }
                        )
                    }
                }
            } else {
                if (filteredTools.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No tools match ‘$searchQuery’",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FilledTonalButton(onClick = { searchQuery = ""; selectedCategory = "All" }) {
                                Text("Clear search")
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(dims.gridColumns),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(filteredTools, key = { _, tool -> tool.tool.name }) { index, tool ->
                            GridToolCard(
                                tool = tool,
                                hapticEnabled = hapticEnabled,
                                animDelay = index * 30,
                                showToolDescriptions = showToolDescriptions,
                                dims = dims,
                                isEditMode = isEditMode,
                                onToggleEditMode = { isEditMode = !isEditMode },
                                onMove = { dir ->
                                    val list = orderedAllTools.map { it.tool }.toMutableList()
                                    val i = list.indexOf(tool.tool)
                                    if (i >= 0 && i + dir >= 0 && i + dir < list.size) {
                                        val temp = list[i]
                                        list[i] = list[i + dir]
                                        list[i + dir] = temp
                                        val newOrderStr = list.joinToString(",")
                                        toolOrderOverride = newOrderStr
                                        scope.launch { mainViewModel?.settingsRepository?.saveToolOrder(newOrderStr) }
                                    }
                                },
                                onClick = { onToolSelected(tool.tool) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridToolCard(
    tool: ToolDef,
    hapticEnabled: Boolean,
    animDelay: Int,
    showToolDescriptions: Boolean,
    dims: com.balajitechlabs.quickdash.core.ui.components.ResponsiveDimensions,
    isEditMode: Boolean,
    onToggleEditMode: () -> Unit,
    onMove: (Int) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animDelay.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "cardAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "jiggleGrid")
    val jiggleAngle by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jiggleAngle"
    )
    val rotation = if (isEditMode) jiggleAngle else 0f

    val containerColor = tool.containerColor()
    val iconColor = tool.iconColor()

    Card(
        border = if (isEditMode) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(dims.gridCardAspectRatio)
            .scale(scale)
            .graphicsLayer { rotationZ = rotation }
            .combinedClickable(
                onClick = {
                    if (!isEditMode) {
                        if (hapticEnabled) triggerHaptic(context)
                        onClick()
                    }
                },
                onLongClick = {
                    if (hapticEnabled) triggerHaptic(context)
                    onToggleEditMode()
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon container
                Surface(
                    color = containerColor,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size(dims.gridIconSize)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            painter = painterResource(tool.iconRes),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(dims.gridIconSize * 0.54f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Column {
                    Text(
                        text = tool.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showToolDescriptions) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tool.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
            if (isEditMode) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            if (hapticEnabled) triggerHaptic(context)
                            onMove(-1)
                        },
                        modifier = Modifier.size(30.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("←", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    FilledTonalIconButton(
                        onClick = {
                            if (hapticEnabled) triggerHaptic(context)
                            onMove(1)
                        },
                        modifier = Modifier.size(30.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("→", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactToolCard(
    tool: ToolDef,
    hapticEnabled: Boolean,
    animDelay: Int,
    showToolDescriptions: Boolean,
    dims: com.balajitechlabs.quickdash.core.ui.components.ResponsiveDimensions,
    isEditMode: Boolean = false,
    onToggleEditMode: () -> Unit = {},
    onMove: (Int) -> Unit = {},
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "jiggleCompact")
    val jiggleAngle by infiniteTransition.animateFloat(
        initialValue = -1.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jiggleAngle"
    )
    val rotation = if (isEditMode) jiggleAngle else 0f

    val containerColor = tool.containerColor()
    val iconColor = tool.iconColor()

    Card(
        border = if (isEditMode) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { rotationZ = rotation }
            .combinedClickable(
                onClick = {
                    if (!isEditMode) {
                        if (hapticEnabled) triggerHaptic(context)
                        onClick()
                    }
                },
                onLongClick = {
                    if (hapticEnabled) triggerHaptic(context)
                    onToggleEditMode()
                }
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(tool.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            },
            supportingContent = if (showToolDescriptions) {
                { Text(tool.description, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            } else null,
            leadingContent = {
                Surface(color = containerColor, shape = MaterialTheme.shapes.small, modifier = Modifier.size(dims.compactIconSize)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(painter = painterResource(tool.iconRes), contentDescription = null, tint = iconColor, modifier = Modifier.size(dims.compactIconSize * 0.55f))
                    }
                }
            },
            trailingContent = {
                if (isEditMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalIconButton(
                            onClick = {
                                if (hapticEnabled) triggerHaptic(context)
                                onMove(-1)
                            },
                            modifier = Modifier.size(30.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text("↑", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        FilledTonalIconButton(
                            onClick = {
                                if (hapticEnabled) triggerHaptic(context)
                                onMove(1)
                            },
                            modifier = Modifier.size(30.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text("↓", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }
}

private fun triggerHaptic(context: Context) {
    // Use VibratorManager on Android 12+ (API 31+); fall back gracefully on older versions
    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    if (vibrator?.hasVibrator() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, 80))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(50)
        }
    }
}