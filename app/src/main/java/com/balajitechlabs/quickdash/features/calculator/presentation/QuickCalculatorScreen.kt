/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/calculator/presentation
 * File: QuickCalculatorScreen.kt
 * Description: Material 3 Expressive calculator UI delegating all expression parsing and state to QuickCalculatorViewModel.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.calculator.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashSegmentedSelector
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashHaptics
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashShapeTokens
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashTokens
import com.balajitechlabs.quickdash.features.calculator.domain.CalcKey

private enum class CalculatorMode(val label: String) {
    BASIC("Basic"),
    SCIENTIFIC("Scientific")
}

private val StandardRows: List<List<CalcKey>> = listOf(
    listOf(CalcKey.Clear, CalcKey.ToggleSign, CalcKey.Percent, CalcKey.Op("÷")),
    listOf(CalcKey.Digit("7"), CalcKey.Digit("8"), CalcKey.Digit("9"), CalcKey.Op("×")),
    listOf(CalcKey.Digit("4"), CalcKey.Digit("5"), CalcKey.Digit("6"), CalcKey.Op("-")),
    listOf(CalcKey.Digit("1"), CalcKey.Digit("2"), CalcKey.Digit("3"), CalcKey.Op("+")),
    listOf(CalcKey.Sqrt, CalcKey.Digit("0"), CalcKey.Dot, CalcKey.Equals)
)

private val ScientificRows: List<List<CalcKey>> = listOf(
    listOf(CalcKey.ScientificOp("sin"), CalcKey.ScientificOp("cos"), CalcKey.ScientificOp("tan"), CalcKey.ScientificOp("^")),
    listOf(CalcKey.ScientificOp("ln"), CalcKey.ScientificOp("log"), CalcKey.ScientificOp("π"), CalcKey.ScientificOp("e")),
    listOf(CalcKey.ScientificOp("("), CalcKey.ScientificOp(")"), CalcKey.Sqrt, CalcKey.Percent)
)

@Composable
fun QuickCalculatorScreen(
    isFloating: Boolean = false,
    viewModel: QuickCalculatorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isScientific by viewModel.isScientific.collectAsStateWithLifecycle()
    val showHistory by viewModel.showHistory.collectAsStateWithLifecycle()

    val btnHeight = if (isScientific) (if (isFloating) 44.dp else 56.dp) else (if (isFloating) 58.dp else 68.dp)
    val fontSize = if (isScientific) (if (isFloating) 14.sp else 18.sp) else (if (isFloating) 18.sp else 22.sp)
    val hPad = if (isFloating) QuickDashTokens.Spacing.sm else QuickDashTokens.Spacing.md

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = hPad, vertical = QuickDashTokens.Spacing.xs)
    ) {
        Surface(
            tonalElevation = QuickDashTokens.Elevation.card,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = QuickDashTokens.Spacing.sm)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = QuickDashTokens.Spacing.md),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.history.isNotEmpty()) {
                        FilterChip(
                            selected = showHistory,
                            onClick = { viewModel.setShowHistory(!showHistory) },
                            label = { Text("History", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (showHistory) Icons.Filled.HistoryToggleOff else Icons.Filled.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    FilledIconButton(
                        onClick = {
                            QuickDashHaptics.perform(context, QuickDashHaptics.Style.CLICK)
                            viewModel.onKey(CalcKey.Backspace)
                        },
                        shape = MaterialTheme.shapes.medium,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.xs))

                AnimatedContent(
                    targetState = state.expression,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "expr"
                ) { expr ->
                    Text(
                        text = expr.ifEmpty { " " },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.xs))

                AnimatedContent(
                    targetState = state.display,
                    transitionSpec = {
                        slideInVertically { it / 4 } + fadeIn() togetherWith
                            slideOutVertically { -it / 4 } + fadeOut()
                    },
                    label = "display"
                ) { d ->
                    Text(
                        text = d,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = when {
                                d.length > 14 -> 24.sp
                                d.length > 10 -> 32.sp
                                else -> if (isFloating) 36.sp else 48.sp
                            }
                        ),
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        QuickDashSegmentedSelector(
            options = CalculatorMode.entries,
            selected = if (isScientific) CalculatorMode.SCIENTIFIC else CalculatorMode.BASIC,
            onSelect = { mode ->
                QuickDashHaptics.perform(context, QuickDashHaptics.Style.TICK)
                viewModel.setScientific(mode == CalculatorMode.SCIENTIFIC)
            },
            label = { it.label },
            modifier = Modifier.padding(bottom = QuickDashTokens.Spacing.sm)
        )

        if (isScientific) {
            KeypadGrid(
                rows = ScientificRows,
                buttonHeight = btnHeight,
                fontSize = fontSize,
                onKey = { key ->
                    QuickDashHaptics.perform(context, QuickDashHaptics.Style.TICK)
                    viewModel.onKey(key)
                }
            )
            Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.xs))
        }

        KeypadGrid(
            rows = StandardRows,
            buttonHeight = btnHeight,
            fontSize = fontSize,
            onKey = { key ->
                QuickDashHaptics.perform(context, QuickDashHaptics.Style.CLICK)
                viewModel.onKey(key)
            }
        )
    }

    if (showHistory) {
        CalculatorHistoryDialog(
            history = state.history,
            onSelect = viewModel::selectHistoryEntry,
            onClear = viewModel::clearHistory,
            onDismiss = { viewModel.setShowHistory(false) }
        )
    }
}

@Composable
private fun KeypadGrid(
    rows: List<List<CalcKey>>,
    buttonHeight: Dp,
    fontSize: TextUnit,
    onKey: (CalcKey) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(QuickDashTokens.Spacing.sm)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QuickDashTokens.Spacing.sm)
            ) {
                row.forEach { key ->
                    CalcButton(
                        key = key,
                        buttonHeight = buttonHeight,
                        fontSize = fontSize,
                        onClick = { onKey(key) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcButton(
    key: CalcKey,
    buttonHeight: Dp,
    fontSize: TextUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (key) {
        CalcKey.Clear -> colors.errorContainer to colors.onErrorContainer
        CalcKey.Equals -> colors.primary to colors.onPrimary
        is CalcKey.Op -> colors.primaryContainer to colors.onPrimaryContainer
        CalcKey.ToggleSign, CalcKey.Percent, CalcKey.Sqrt, is CalcKey.ScientificOp ->
            colors.secondaryContainer to colors.onSecondaryContainer
        else -> colors.surfaceContainerHigh to colors.onSurface
    }

    val label = when (key) {
        is CalcKey.Digit -> key.ch
        is CalcKey.Op -> key.ch
        is CalcKey.ScientificOp -> key.name
        CalcKey.Clear -> "AC"
        CalcKey.Backspace -> "⌫"
        CalcKey.Equals -> "="
        CalcKey.Dot -> "."
        CalcKey.ToggleSign -> "±"
        CalcKey.Percent -> "%"
        CalcKey.Sqrt -> "√"
    }

    FilledTonalButton(
        onClick = onClick,
        shape = if (key == CalcKey.Equals) QuickDashShapeTokens.Pill else QuickDashShapeTokens.CardCompact,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = container,
            contentColor = content
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.height(buttonHeight)
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = if (key is CalcKey.Op || key == CalcKey.Equals) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun CalculatorHistoryDialog(
    history: List<String>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calculation History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (history.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(QuickDashTokens.Spacing.sm)
            ) {
                items(history) { entry ->
                    Surface(
                        shape = QuickDashShapeTokens.CardCompact,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        onClick = { onSelect(entry) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = entry,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(QuickDashTokens.Spacing.md)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
