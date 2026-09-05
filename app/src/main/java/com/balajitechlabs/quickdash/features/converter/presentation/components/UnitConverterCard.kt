/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/converter/presentation/components
 * File: UnitConverterCard.kt
 * Description: Unit converter card with category chips, unit dropdown selectors, and instant result computation.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.converter.presentation.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun UnitConverterCard(
    selectedCategory: UnitCategory,
    onSelectedCategoryChange: (UnitCategory) -> Unit,
    fromUnitIndex: Int,
    onFromUnitIndexChange: (Int) -> Unit,
    toUnitIndex: Int,
    onToUnitIndexChange: (Int) -> Unit,
    unitInput: String,
    onUnitInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UnitCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                            onSelectedCategoryChange(category)
                        },
                        label = { Text(category.displayName, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Input Value",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = unitInput,
                onValueChange = { if (it.length <= 15) onUnitInputChange(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitDropdown(
                    units = selectedCategory.units,
                    selectedIndex = fromUnitIndex.coerceAtMost(selectedCategory.units.lastIndex),
                    onSelect = onFromUnitIndexChange,
                    label = "From",
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true, 20L)
                        val tmp = fromUnitIndex
                        onFromUnitIndexChange(toUnitIndex)
                        onToUnitIndexChange(tmp)
                    },
                    modifier = Modifier.align(Alignment.Bottom)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = "Swap units",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                UnitDropdown(
                    units = selectedCategory.units,
                    selectedIndex = toUnitIndex.coerceAtMost(selectedCategory.units.lastIndex),
                    onSelect = onToUnitIndexChange,
                    label = "To",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val valDouble = unitInput.toDoubleOrNull() ?: 0.0
            val safeFromIdx = fromUnitIndex.coerceAtMost(selectedCategory.units.lastIndex)
            val safeToIdx = toUnitIndex.coerceAtMost(selectedCategory.units.lastIndex)
            val convertedUnitVal = convertUnits(selectedCategory, safeFromIdx, safeToIdx, valDouble)

            val formattedResult = if (convertedUnitVal != 0.0 && (convertedUnitVal < 0.0001 || convertedUnitVal > 1_000_000.0)) {
                String.format(Locale.US, "%.4e", convertedUnitVal)
            } else {
                String.format(Locale.US, "%.6f", convertedUnitVal).trimEnd('0').trimEnd('.')
            }
            val unitResultText = "$formattedResult ${selectedCategory.units.getOrElse(safeToIdx) { "" }}"

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
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
                            text = "Result",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = unitResultText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    IconButton(onClick = {
                        com.balajitechlabs.quickdash.core.ui.playSuccessVibration(context, true)
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Unit Conversion", unitResultText))
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitDropdown(
    units: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(2.dp))
            OutlinedCard(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedIndex in units.indices) units[selectedIndex] else "",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.ArrowDropDown, null)
                }
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEachIndexed { index, name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}
