/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/converter/presentation
 * File: QuickConverterScreen.kt
 * Description: Multi-category unit and currency conversion tool with live input calculation.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.converter.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.features.converter.presentation.components.CurrencyConverterCard
import com.balajitechlabs.quickdash.features.converter.presentation.components.OFFLINE_RATES
import com.balajitechlabs.quickdash.features.converter.presentation.components.UnitCategory
import com.balajitechlabs.quickdash.features.converter.presentation.components.UnitConverterCard
import com.balajitechlabs.quickdash.features.converter.presentation.components.fetchLiveRates
import kotlinx.coroutines.launch

enum class ConverterTab { CURRENCY, UNITS }

@Composable
fun QuickConverterScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(ConverterTab.CURRENCY) }

    var liveRates by remember { mutableStateOf<Map<String, Double>>(OFFLINE_RATES) }
    var isLive by remember { mutableStateOf(false) }
    var isLoadingRates by remember { mutableStateOf(false) }

    var fromCurrencyIndex by remember { mutableIntStateOf(3) }
    var toCurrencyIndex by remember { mutableIntStateOf(0) }
    var currencyInput by remember { mutableStateOf("0") }

    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    var fromUnitIndex by remember { mutableIntStateOf(0) }
    var toUnitIndex by remember { mutableIntStateOf(2) }
    var unitInput by remember { mutableStateOf("1") }

    LaunchedEffect(Unit) {
        isLoadingRates = true
        val fetched = fetchLiveRates()
        if (fetched != null) {
            liveRates = fetched
            isLive = true
        }
        isLoadingRates = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                    selectedTab = ConverterTab.CURRENCY
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == ConverterTab.CURRENCY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedTab == ConverterTab.CURRENCY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Currency", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                    selectedTab = ConverterTab.UNITS
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == ConverterTab.UNITS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedTab == ConverterTab.UNITS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Units", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == ConverterTab.CURRENCY) {
            CurrencyConverterCard(
                liveRates = liveRates,
                isLive = isLive,
                isLoadingRates = isLoadingRates,
                onRefreshRates = {
                    scope.launch {
                        isLoadingRates = true
                        val fetched = fetchLiveRates()
                        if (fetched != null) {
                            liveRates = fetched
                            isLive = true
                        } else {
                            Toast.makeText(context, "Could not fetch live rates. Using offline data.", Toast.LENGTH_SHORT).show()
                        }
                        isLoadingRates = false
                    }
                },
                fromCurrencyIndex = fromCurrencyIndex,
                onFromCurrencyIndexChange = { fromCurrencyIndex = it },
                toCurrencyIndex = toCurrencyIndex,
                onToCurrencyIndexChange = { toCurrencyIndex = it },
                currencyInput = currencyInput,
                onCurrencyInputChange = { currencyInput = it }
            )
        } else {
            UnitConverterCard(
                selectedCategory = selectedCategory,
                onSelectedCategoryChange = {
                    selectedCategory = it
                    fromUnitIndex = 0
                    toUnitIndex = minOf(1, it.units.lastIndex)
                },
                fromUnitIndex = fromUnitIndex,
                onFromUnitIndexChange = { fromUnitIndex = it },
                toUnitIndex = toUnitIndex,
                onToUnitIndexChange = { toUnitIndex = it },
                unitInput = unitInput,
                onUnitInputChange = { unitInput = it }
            )
        }
    }
}
