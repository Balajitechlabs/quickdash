package com.balajitechlabs.quickdash.features.discount.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer

/**
 * 🏷️ Tool #16 — Quick Discount & Unit Price Comparison Tool (`QuickDiscountScreen.kt`).
 * Compares two product prices per kg/liter/unit side by side in shopping apps.
 */
@Composable
fun QuickDiscountScreen(isFloating: Boolean = false) {
    var priceA by remember { mutableStateOf("") }
    var qtyA by remember { mutableStateOf("") }
    var priceB by remember { mutableStateOf("") }
    var qtyB by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("") }

    val valPriceA = priceA.toDoubleOrNull() ?: 0.0
    val valQtyA = qtyA.toDoubleOrNull() ?: 1.0
    val valPriceB = priceB.toDoubleOrNull() ?: 0.0
    val valQtyB = qtyB.toDoubleOrNull() ?: 1.0
    val disc = discountPercent.toDoubleOrNull() ?: 0.0

    val unitPriceA = if (valQtyA > 0) (valPriceA * (1 - disc / 100)) / valQtyA else 0.0
    val unitPriceB = if (valQtyB > 0) valPriceB / valQtyB else 0.0

    val winner = when {
        unitPriceA > 0 && unitPriceB > 0 && unitPriceA < unitPriceB -> "Option A is cheaper!"
        unitPriceA > 0 && unitPriceB > 0 && unitPriceB < unitPriceA -> "Option B is cheaper!"
        unitPriceA > 0 && unitPriceB > 0 && unitPriceA == unitPriceB -> "Both options are equal price!"
        else -> "Enter price and quantity to compare."
    }

    Column(
        modifier = Modifier
            .then(if (isFloating) Modifier.fillMaxWidth().wrapContentHeight() else Modifier.fillMaxSize())
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🏷️ Unit Price & Discount Compare",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        RoundedCardContainer {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Option A (with Discount)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceA,
                        onValueChange = { priceA = it },
                        label = { Text("Price (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = qtyA,
                        onValueChange = { qtyA = it },
                        label = { Text("Qty (g/ml/pcs)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = discountPercent,
                    onValueChange = { discountPercent = it },
                    label = { Text("Discount (%)") },
                    placeholder = { Text("e.g. 15") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        RoundedCardContainer {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Option B (Standard)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceB,
                        onValueChange = { priceB = it },
                        label = { Text("Price (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = qtyB,
                        onValueChange = { qtyB = it },
                        label = { Text("Qty (g/ml/pcs)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = winner,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer),
                    textAlign = TextAlign.Center
                )
                if (unitPriceA > 0 && unitPriceB > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Unit Cost A: ₹${String.format("%.2f", unitPriceA)} / unit\nUnit Cost B: ₹${String.format("%.2f", unitPriceB)} / unit",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
