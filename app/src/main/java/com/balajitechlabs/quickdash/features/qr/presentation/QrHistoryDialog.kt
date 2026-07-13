package com.balajitechlabs.quickdash.features.qr.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QrHistoryItem(
    val amount: String,
    val note: String,
    val upiId: String,
    val targetApp: String,
    val category: String = "Other",
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrHistoryDialog(
    historyJson: String,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val items = remember(historyJson) {
        try {
            val arr = JsonParser.parseString(historyJson).asJsonArray
            val list = mutableListOf<QrHistoryItem>()
            arr.forEach { el ->
                val obj = el.asJsonObject
                list.add(
                    QrHistoryItem(
                        amount = obj.get("amount")?.asString ?: "",
                        note = obj.get("note")?.asString ?: "",
                        upiId = obj.get("upiId")?.asString ?: "",
                        targetApp = obj.get("targetApp")?.asString ?: "ANY",
                        category = obj.get("category")?.asString ?: "Other",
                        timestamp = obj.get("timestamp")?.asLong ?: 0L
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    var showClearConfirmation by remember { mutableStateOf(false) }

    val periods = listOf("All", "Today", "This Week", "This Month")
    var selectedPeriod by remember { mutableStateOf("All") }

    val filteredItems = remember(items, selectedPeriod) {
        val now = java.lang.System.currentTimeMillis()
        when (selectedPeriod) {
            "Today" -> {
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                items.filter { it.timestamp >= cal.timeInMillis }
            }
            "This Week" -> {
                val sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000L
                items.filter { it.timestamp >= sevenDaysAgo }
            }
            "This Month" -> {
                val thirtyDaysAgo = now - 30 * 24 * 60 * 60 * 1000L
                items.filter { it.timestamp >= thirtyDaysAgo }
            }
            else -> items
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("QR Payment History", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                // Tab layout
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Log", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Analytics", fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Period filters row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        periods.forEach { period ->
                            val isSelected = selectedPeriod == period
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPeriod = period },
                                label = { Text(period, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    if (filteredItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No matching history found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredItems) { item ->
                                val dateStr = remember(item.timestamp) {
                                    if (item.timestamp == 0L) ""
                                    else SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
                                }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "₹${item.amount}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                // Category badge
                                                Surface(
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = item.category,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                // App badge
                                                val appColor = when (item.targetApp) {
                                                "GPAY" -> Color(0xFF1A73E8)
                                                "PHONEPE" -> Color(0xFF5F259F)
                                                "PAYTM" -> Color(0xFF00B9F5)
                                                "BHIM" -> Color(0xFFE27F22)
                                                else -> MaterialTheme.colorScheme.secondary
                                            }
                                            Surface(
                                                color = appColor.copy(alpha = 0.12f),
                                                contentColor = appColor,
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(1.dp, appColor.copy(alpha = 0.3f))
                                            ) {
                                                Text(
                                                    text = when (item.targetApp) {
                                                        "GPAY" -> "GPay"
                                                        "PHONEPE" -> "PhonePe"
                                                        "PAYTM" -> "Paytm"
                                                        "BHIM" -> "BHIM"
                                                        else -> "Any App"
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "UPI: ${item.upiId}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (item.note.isNotBlank()) {
                                            Text(
                                                text = "Note: ${item.note}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Analytics view
                    val totalAmount = remember(items) {
                        items.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                    }
                    val appStats = remember(items) {
                        val stats = mutableMapOf<String, Double>()
                        items.forEach { item ->
                            val amt = item.amount.toDoubleOrNull() ?: 0.0
                            stats[item.targetApp] = (stats[item.targetApp] ?: 0.0) + amt
                        }
                        stats
                    }
                    val categoryStats = remember(items) {
                        val stats = mutableMapOf<String, Double>()
                        items.forEach { item ->
                            val amt = item.amount.toDoubleOrNull() ?: 0.0
                            stats[item.category] = (stats[item.category] ?: 0.0) + amt
                        }
                        stats
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text("Total Volume Generated", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "₹${String.format(Locale.getDefault(), "%.2f", totalAmount)}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Across ${items.size} payments",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "App Breakdown",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        val appList = listOf("ANY" to "Any Payment App", "GPAY" to "Google Pay", "PHONEPE" to "PhonePe", "PAYTM" to "Paytm", "BHIM" to "BHIM")
                        items(appList) { (code, label) ->
                            val volume = appStats[code] ?: 0.0
                            val count = items.count { it.targetApp == code }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        Text("$count QRs generated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = "₹${String.format(Locale.getDefault(), "%.2f", volume)}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Category Breakdown",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }



                        val categoryList = listOf("Personal", "Business", "Dining", "Groceries", "Services", "Other")
                        items(categoryList) { category ->
                            val volume = categoryStats[category] ?: 0.0
                            val count = items.count { it.category == category }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(category, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                        Text("$count QRs generated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = "₹${String.format(Locale.getDefault(), "%.2f", volume)}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (items.isNotEmpty() && selectedTab == 0) {
                TextButton(
                    onClick = {
                        showClearConfirmation = true
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All")
                }
            }
        }
    )

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear QR History") },
            text = { Text("Are you sure you want to clear all QR payment generator history?") },
            confirmButton = {
                TextButton(onClick = {
                    onClearHistory()
                    showClearConfirmation = false
                }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
