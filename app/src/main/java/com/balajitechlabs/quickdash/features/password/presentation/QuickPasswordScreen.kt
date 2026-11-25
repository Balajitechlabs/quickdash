/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/password/presentation
 * File: QuickPasswordScreen.kt
 * Description: Material 3 Expressive password and SHA-256 hash generator screen with zero hardcoded colors.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.password.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashCard
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashCardGroup
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashTonalCard
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashHaptics
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashShapeTokens
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashTokens

@Composable
fun QuickPasswordScreen(
    isFloating: Boolean = false,
    viewModel: QuickPasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    fun copyToClipboard(text: String, label: String) {
        QuickDashHaptics.perform(context, QuickDashHaptics.Style.CLICK)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .then(if (isFloating) Modifier.fillMaxWidth().wrapContentHeight() else Modifier.fillMaxSize())
            .verticalScroll(rememberScrollState())
            .padding(QuickDashTokens.Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(QuickDashTokens.Spacing.sm))
            Text(
                text = "Quick Password & Hash",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.lg))

        QuickDashCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GENERATED PASSWORD",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.xs))
                    SelectionContainer {
                        Text(
                            text = state.generatedPassword,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(QuickDashTokens.Spacing.xs)) {
                    FilledTonalIconButton(
                        onClick = {
                            QuickDashHaptics.perform(context, QuickDashHaptics.Style.CLICK)
                            viewModel.regenerate()
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Regenerate")
                    }
                    FilledTonalIconButton(
                        onClick = { copyToClipboard(state.generatedPassword, "Password") }
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.md))

        QuickDashCardGroup {
            QuickDashCard(shape = QuickDashShapeTokens.CardCompact) {
                Text(
                    text = "Length: ${state.length} characters",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = state.length.toFloat(),
                    onValueChange = { viewModel.setLength(it.toInt()) },
                    valueRange = 8f..32f,
                    steps = 23
                )
            }

            QuickDashCard(shape = QuickDashShapeTokens.CardCompact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Include Numbers (0-9)",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.includeNumbers,
                        onCheckedChange = {
                            QuickDashHaptics.perform(context, QuickDashHaptics.Style.TICK)
                            viewModel.setIncludeNumbers(it)
                        }
                    )
                }
            }

            QuickDashCard(shape = QuickDashShapeTokens.CardCompact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Include Symbols (!@#$)",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.includeSymbols,
                        onCheckedChange = {
                            QuickDashHaptics.perform(context, QuickDashHaptics.Style.TICK)
                            viewModel.setIncludeSymbols(it)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.md))

        OutlinedTextField(
            value = state.hashInput,
            onValueChange = viewModel::onHashInputChange,
            label = { Text("Compute SHA-256 Hash") },
            modifier = Modifier.fillMaxWidth(),
            shape = QuickDashShapeTokens.Input
        )

        if (state.sha256Output.isNotEmpty()) {
            Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.sm))
            QuickDashTonalCard(shape = QuickDashShapeTokens.CardCompact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionContainer(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.sha256Output,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                    IconButton(onClick = { copyToClipboard(state.sha256Output, "SHA-256 Hash") }) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy Hash")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}
