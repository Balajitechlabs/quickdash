package com.balajitechlabs.quickdash.features.dashboard.presentation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.R

enum class QuickTool {
    UPI, WHATSAPP, INSTAGRAM, NOTES, SEARCH, WEB, WIFI, CLIPBOARD, CALCULATOR, TIMER
}

private data class ToolDef(
    val tool: QuickTool,
    val title: String,
    val description: String,
    val iconRes: Int,
    val containerColor: @Composable () -> Color,
    val iconColor: @Composable () -> Color,
)

@Composable
private fun toolDefinitions(usePaypal: Boolean = false): List<ToolDef> {
    val cs = MaterialTheme.colorScheme
    return listOf(
        ToolDef(QuickTool.CLIPBOARD,   "Smart Clipboard",    "Copy history & sensitive data guard", R.drawable.ic_note,        { cs.primaryContainer },         { cs.onPrimaryContainer }),
        ToolDef(QuickTool.UPI,         if (usePaypal) "Quick PayPal" else "Quick Collect",      if (usePaypal) "Quick PayPal link generators" else "UPI & payment app QR codes",        if (usePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay,     { cs.secondaryContainer },       { cs.onSecondaryContainer }),
        ToolDef(QuickTool.WHATSAPP,    "Quick Chat",         "Prefilled direct chat & templates",       R.drawable.ic_shortcut_chat,     { cs.tertiaryContainer },        { cs.onTertiaryContainer }),
        ToolDef(QuickTool.INSTAGRAM,   "Quick Social Access",        "Social media & GitHub profiler", R.drawable.ic_instagram,   { cs.errorContainer },           { cs.onErrorContainer }),
        ToolDef(QuickTool.NOTES,       "Quick Notes",        "Offline notes with Markdown",          R.drawable.ic_note,        { cs.primaryContainer },         { cs.onPrimaryContainer }),
        ToolDef(QuickTool.SEARCH,      "Quick Search",       "Multi-engine web search",              R.drawable.ic_search,      { cs.secondaryContainer },       { cs.onSecondaryContainer }),
        ToolDef(QuickTool.WEB,         "Quick Web",          "Floating mini browser",                R.drawable.ic_globe,       { cs.tertiaryContainer },        { cs.onTertiaryContainer }),
        ToolDef(QuickTool.WIFI,        "Quick Wi-Fi",        "Share Wi-Fi via encrypted QR",         R.drawable.ic_qr_code,     { cs.tertiaryContainer },        { cs.onTertiaryContainer }),
        ToolDef(QuickTool.CALCULATOR,  "Quick Calculator",   "Expression calc with history",         R.drawable.ic_calculator,  { cs.primaryContainer },         { cs.onPrimaryContainer }),
        ToolDef(QuickTool.TIMER,       "Quick Timer",        "Stopwatch & countdown timer",          R.drawable.ic_timer,       { cs.secondaryContainer },       { cs.onSecondaryContainer }),
    )
}

@Composable
fun DashboardScreen(
    hapticEnabled: Boolean = true,
    isFloating: Boolean = false,
    usePaypal: Boolean = false,
    onToolSelected: (QuickTool) -> Unit
) {
    val tools = toolDefinitions(usePaypal)

    if (isFloating) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(tools) { index, tool ->
                CompactToolCard(
                    tool = tool,
                    hapticEnabled = hapticEnabled,
                    animDelay = index * 40,
                    onClick = { onToolSelected(tool.tool) }
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(tools) { index, tool ->
                GridToolCard(
                    tool = tool,
                    hapticEnabled = hapticEnabled,
                    animDelay = index * 40,
                    onClick = { onToolSelected(tool.tool) }
                )
            }
        }
    }
}

@Composable
private fun GridToolCard(
    tool: ToolDef,
    hapticEnabled: Boolean,
    animDelay: Int,
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

    val containerColor = tool.containerColor()
    val iconColor = tool.iconColor()

    ElevatedCard(
        onClick = {
            if (hapticEnabled) triggerHaptic(context)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .scale(scale),
        interactionSource = interactionSource,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
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
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        painter = painterResource(tool.iconRes),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(26.dp)
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
}

@Composable
private fun CompactToolCard(
    tool: ToolDef,
    hapticEnabled: Boolean,
    animDelay: Int,
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

    val containerColor = tool.containerColor()
    val iconColor = tool.iconColor()

    Card(
        onClick = {
            if (hapticEnabled) triggerHaptic(context)
            onClick()
        },
        modifier = Modifier.fillMaxWidth().scale(scale),
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(tool.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            },
            supportingContent = {
                Text(tool.description, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            leadingContent = {
                Surface(color = containerColor, shape = MaterialTheme.shapes.small, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(painter = painterResource(tool.iconRes), contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                    }
                }
            },
            trailingContent = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }
}

private fun triggerHaptic(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, 80))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(50)
        }
    }
}