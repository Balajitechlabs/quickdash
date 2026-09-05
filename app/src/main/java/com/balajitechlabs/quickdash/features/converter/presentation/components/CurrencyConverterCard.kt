/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/converter/presentation/components
 * File: CurrencyConverterCard.kt
 * Description: Currency converter card featuring live rate badges, input controls, and currency swapping.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.converter.presentation.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun CurrencyConverterCard(
    liveRates: Map<String, Double>,
    isLive: Boolean,
    isLoadingRates: Boolean,
    onRefreshRates: () -> Unit,
    fromCurrencyIndex: Int,
    onFromCurrencyIndexChange: (Int) -> Unit,
    toCurrencyIndex: Int,
    onToCurrencyIndexChange: (Int) -> Unit,
    currencyInput: String,
    onCurrencyInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isLive) Color(0xFF1B5E20) else Color(0xFF37474F))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isLoadingRates) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        strokeWidth = 1.5.dp,
                        color = Color.White
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isLive) Color(0xFF69F0AE) else Color(0xFF90A4AE))
                    )
                }
                Text(
                    text = if (isLoadingRates) "Fetching rates…" else if (isLive) "Live Rates" else "Offline Rates",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                    onRefreshRates()
                },
                enabled = !isLoadingRates
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh rates",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val fromCode = CURRENCY_DISPLAY[fromCurrencyIndex].first
        val toCode = CURRENCY_DISPLAY[toCurrencyIndex].first
        val fromRate = liveRates[fromCode] ?: OFFLINE_RATES[fromCode] ?: 1.0
        val toRate = liveRates[toCode] ?: OFFLINE_RATES[toCode] ?: 1.0
        val inputVal = currencyInput.toDoubleOrNull() ?: 0.0
        val convertedVal = (inputVal / fromRate) * toRate
        val resultText = String.format(Locale.US, "%.4f %s", convertedVal, toCode)
        val rateHint = String.format(Locale.US, "1 %s = %.4f %s", fromCode, (1.0 / fromRate) * toRate, toCode)

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "From Currency",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurrencyDropdown(
                        selectedIndex = fromCurrencyIndex,
                        onSelect = onFromCurrencyIndexChange,
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = currencyInput,
                        onValueChange = { if (it.length <= 15) onCurrencyInputChange(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rateHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    IconButton(
                        onClick = {
                            com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true, 20L)
                            val temp = fromCurrencyIndex
                            onFromCurrencyIndexChange(toCurrencyIndex)
                            onToCurrencyIndexChange(temp)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapVert,
                            contentDescription = "Swap currencies",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "To Currency",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                CurrencyDropdown(
                    selectedIndex = toCurrencyIndex,
                    onSelect = onToCurrencyIndexChange,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Converted Amount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = resultText,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = {
                            com.balajitechlabs.quickdash.core.ui.playSuccessVibration(context, true)
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Currency Conversion", resultText))
                            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyDropdown(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CURRENCY_DISPLAY[selectedIndex].second,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ArrowDropDown, null)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CURRENCY_DISPLAY.forEachIndexed { index, (code, label) ->
                DropdownMenuItem(
                    text = { Text("$label ($code)") },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}
