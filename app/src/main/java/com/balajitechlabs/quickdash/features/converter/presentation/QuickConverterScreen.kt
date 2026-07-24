package com.balajitechlabs.quickdash.features.converter.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

enum class ConverterTab { CURRENCY, UNITS }

enum class UnitCategory(val displayName: String, val units: List<String>) {
    LENGTH("Length", listOf("Meters (m)", "Kilometers (km)", "Feet (ft)", "Inches (in)", "Miles (mi)", "Centimeters (cm)", "Millimeters (mm)")),
    WEIGHT("Weight", listOf("Kilograms (kg)", "Grams (g)", "Pounds (lbs)", "Ounces (oz)", "Milligrams (mg)", "Tonnes (t)")),
    TEMPERATURE("Temperature", listOf("Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)")),
    AREA("Area", listOf("Sq Meters (m²)", "Sq Feet (ft²)", "Acres", "Hectares", "Sq Kilometers (km²)")),
    VOLUME("Volume", listOf("Liters (L)", "Milliliters (mL)", "Gallons (US)", "Cups", "Pints", "Fluid Oz")),
    SPEED("Speed", listOf("m/s", "km/h", "mph", "knots", "ft/s")),
    DATA("Data", listOf("Bytes (B)", "Kilobytes (KB)", "Megabytes (MB)", "Gigabytes (GB)", "Terabytes (TB)"))
}

// Fallback offline rates (USD base, updated 2025)
private val OFFLINE_RATES = mapOf(
    "USD" to 1.0,
    "EUR" to 0.92,
    "GBP" to 0.78,
    "INR" to 83.50,
    "JPY" to 157.20,
    "CAD" to 1.36,
    "AUD" to 1.50,
    "AED" to 3.67,
    "SGD" to 1.35,
    "CNY" to 7.26,
    "CHF" to 0.91,
    "HKD" to 7.84,
    "KRW" to 1340.0,
    "MXN" to 17.20,
    "BRL" to 5.10,
    "SEK" to 10.75,
    "NOK" to 10.60,
    "DKK" to 6.85,
    "NZD" to 1.64,
    "ZAR" to 18.80
)

private val CURRENCY_DISPLAY = listOf(
    "USD" to "🇺🇸 US Dollar",
    "EUR" to "🇪🇺 Euro",
    "GBP" to "🇬🇧 British Pound",
    "INR" to "🇮🇳 Indian Rupee",
    "JPY" to "🇯🇵 Japanese Yen",
    "CAD" to "🇨🇦 Canadian Dollar",
    "AUD" to "🇦🇺 Australian Dollar",
    "AED" to "🇦🇪 UAE Dirham",
    "SGD" to "🇸🇬 Singapore Dollar",
    "CNY" to "🇨🇳 Chinese Yuan",
    "CHF" to "🇨🇭 Swiss Franc",
    "HKD" to "🇭🇰 Hong Kong Dollar",
    "KRW" to "🇰🇷 South Korean Won",
    "MXN" to "🇲🇽 Mexican Peso",
    "BRL" to "🇧🇷 Brazilian Real",
    "SEK" to "🇸🇪 Swedish Krona",
    "NOK" to "🇳🇴 Norwegian Krone",
    "DKK" to "🇩🇰 Danish Krone",
    "NZD" to "🇳🇿 New Zealand Dollar",
    "ZAR" to "🇿🇦 South African Rand"
)

/**
 * Fetches live exchange rates from open.er-api.com (free, no API key).
 * Returns a map of currency code → rate (USD base) or null on failure.
 */
private suspend fun fetchLiveRates(): Map<String, Double>? = withContext(Dispatchers.IO) {
    return@withContext try {
        val url = URL("https://open.er-api.com/v6/latest/USD")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        if (conn.responseCode == 200) {
            val json = conn.inputStream.bufferedReader().readText()
            val obj = JSONObject(json)
            val rates = obj.getJSONObject("rates")
            val rateMap = mutableMapOf<String, Double>()
            rates.keys().forEach { key ->
                rateMap[key] = rates.getDouble(key)
            }
            rateMap
        } else null
    } catch (_: Exception) { null }
}

@Composable
fun QuickConverterScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(ConverterTab.CURRENCY) }

    // Live rates state
    var liveRates by remember { mutableStateOf<Map<String, Double>>(OFFLINE_RATES) }
    var isLive by remember { mutableStateOf(false) }
    var isLoadingRates by remember { mutableStateOf(false) }

    // Currency states
    var fromCurrencyIndex by remember { mutableIntStateOf(3) } // INR
    var toCurrencyIndex by remember { mutableIntStateOf(0) }   // USD
    var currencyInput by remember { mutableStateOf("0") }

    // Unit states
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    var fromUnitIndex by remember { mutableIntStateOf(0) }
    var toUnitIndex by remember { mutableIntStateOf(2) }
    var unitInput by remember { mutableStateOf("1") }

    // Fetch live rates on first launch
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
        // Tab Selector Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { selectedTab = ConverterTab.CURRENCY },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == ConverterTab.CURRENCY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedTab == ConverterTab.CURRENCY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) { Text("💱 Currency", fontWeight = FontWeight.Bold) }

            Button(
                onClick = { selectedTab = ConverterTab.UNITS },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == ConverterTab.UNITS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedTab == ConverterTab.UNITS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) { Text("📏 Units", fontWeight = FontWeight.Bold) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == ConverterTab.CURRENCY) {
            // Live / Offline status badge
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
                        CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp, color = Color.White)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isLive) Color(0xFF69F0AE) else Color(0xFF90A4AE))
                        )
                    }
                    Text(
                        if (isLoadingRates) "Fetching rates…" else if (isLive) "Live Rates" else "Offline Rates",
                        color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            isLoadingRates = true
                            val fetched = fetchLiveRates()
                            if (fetched != null) { liveRates = fetched; isLive = true }
                            else Toast.makeText(context, "Could not fetch live rates. Using offline data.", Toast.LENGTH_SHORT).show()
                            isLoadingRates = false
                        }
                    },
                    enabled = !isLoadingRates
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh rates", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val fromCode = CURRENCY_DISPLAY[fromCurrencyIndex].first
            val toCode = CURRENCY_DISPLAY[toCurrencyIndex].first
            val fromRate = liveRates[fromCode] ?: OFFLINE_RATES[fromCode] ?: 1.0
            val toRate = liveRates[toCode] ?: OFFLINE_RATES[toCode] ?: 1.0
            val inputVal = currencyInput.toDoubleOrNull() ?: 0.0
            // Convert: amount in FROM → USD → TO
            val convertedVal = (inputVal / fromRate) * toRate
            val resultText = String.format(Locale.US, "%.4f %s", convertedVal, toCode)
            val rateHint = String.format(Locale.US, "1 %s = %.4f %s", fromCode, (1.0 / fromRate) * toRate, toCode)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("From Currency", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CurrencyDropdown(selectedIndex = fromCurrencyIndex, onSelect = { fromCurrencyIndex = it }, modifier = Modifier.weight(1.4f))
                        OutlinedTextField(
                            value = currencyInput,
                            onValueChange = { if (it.length <= 15) currencyInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true, modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(rateHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f))
                        IconButton(
                            onClick = {
                                val temp = fromCurrencyIndex; fromCurrencyIndex = toCurrencyIndex; toCurrencyIndex = temp
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Default.SwapVert, "Swap currencies", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("To Currency", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    CurrencyDropdown(selectedIndex = toCurrencyIndex, onSelect = { toCurrencyIndex = it }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Converted Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f))
                                Text(resultText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            IconButton(onClick = { copyToClipboard(context, "Currency Conversion", resultText) }) {
                                Icon(Icons.Default.ContentCopy, "Copy", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }
        } else {
            // Unit Converter
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Scrollable horizontal category chips
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
                                    selectedCategory = category
                                    fromUnitIndex = 0
                                    toUnitIndex = minOf(1, category.units.lastIndex)
                                },
                                label = { Text(category.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Input Value", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = unitInput,
                        onValueChange = { if (it.length <= 15) unitInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UnitDropdown(
                            units = selectedCategory.units,
                            selectedIndex = fromUnitIndex.coerceAtMost(selectedCategory.units.lastIndex),
                            onSelect = { fromUnitIndex = it }, label = "From", modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val tmp = fromUnitIndex; fromUnitIndex = toUnitIndex; toUnitIndex = tmp
                            },
                            modifier = Modifier.align(Alignment.Bottom)
                        ) {
                            Icon(Icons.Default.SwapVert, "Swap units", tint = MaterialTheme.colorScheme.primary)
                        }
                        UnitDropdown(
                            units = selectedCategory.units,
                            selectedIndex = toUnitIndex.coerceAtMost(selectedCategory.units.lastIndex),
                            onSelect = { toUnitIndex = it }, label = "To", modifier = Modifier.weight(1f)
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

                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Result", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(0.7f))
                                Text(unitResultText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            IconButton(onClick = { copyToClipboard(context, "Unit Conversion", unitResultText) }) {
                                Icon(Icons.Default.ContentCopy, "Copy", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyDropdown(selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedCard(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(CURRENCY_DISPLAY[selectedIndex].second, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, null)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CURRENCY_DISPLAY.forEachIndexed { index, (code, label) ->
                DropdownMenuItem(
                    text = { Text("$label ($code)") },
                    onClick = { onSelect(index); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun UnitDropdown(units: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, label: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(2.dp))
            OutlinedCard(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selectedIndex in units.indices) units[selectedIndex] else "", fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEachIndexed { index, name ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onSelect(index); expanded = false })
            }
        }
    }
}

private fun convertUnits(category: UnitCategory, fromIdx: Int, toIdx: Int, value: Double): Double {
    if (fromIdx == toIdx) return value
    return when (category) {
        UnitCategory.LENGTH -> {
            // Convert to meters first
            val meters = when (fromIdx) {
                0 -> value              // m
                1 -> value * 1000.0     // km
                2 -> value * 0.3048    // ft
                3 -> value * 0.0254    // in
                4 -> value * 1609.344  // mi
                5 -> value * 0.01      // cm
                6 -> value * 0.001     // mm
                else -> value
            }
            when (toIdx) {
                0 -> meters
                1 -> meters / 1000.0
                2 -> meters / 0.3048
                3 -> meters / 0.0254
                4 -> meters / 1609.344
                5 -> meters / 0.01
                6 -> meters / 0.001
                else -> meters
            }
        }
        UnitCategory.WEIGHT -> {
            val kg = when (fromIdx) {
                0 -> value              // kg
                1 -> value / 1000.0     // g
                2 -> value * 0.453592  // lbs
                3 -> value * 0.0283495 // oz
                4 -> value / 1_000_000.0 // mg
                5 -> value * 1000.0    // tonnes
                else -> value
            }
            when (toIdx) {
                0 -> kg
                1 -> kg * 1000.0
                2 -> kg / 0.453592
                3 -> kg / 0.0283495
                4 -> kg * 1_000_000.0
                5 -> kg / 1000.0
                else -> kg
            }
        }
        UnitCategory.TEMPERATURE -> {
            // Two-step: from → Celsius → to
            val celsius = when (fromIdx) {
                0 -> value                           // °C
                1 -> (value - 32.0) * (5.0 / 9.0)  // °F
                2 -> value - 273.15                 // K
                else -> value
            }
            when (toIdx) {
                0 -> celsius
                1 -> celsius * 9.0 / 5.0 + 32.0
                2 -> celsius + 273.15
                else -> celsius
            }
        }
        UnitCategory.AREA -> {
            val sqM = when (fromIdx) {
                0 -> value               // m²
                1 -> value * 0.092903   // ft²
                2 -> value * 4046.856   // acres
                3 -> value * 10000.0    // hectares
                4 -> value * 1_000_000.0 // km²
                else -> value
            }
            when (toIdx) {
                0 -> sqM
                1 -> sqM / 0.092903
                2 -> sqM / 4046.856
                3 -> sqM / 10000.0
                4 -> sqM / 1_000_000.0
                else -> sqM
            }
        }
        UnitCategory.VOLUME -> {
            val liters = when (fromIdx) {
                0 -> value              // L
                1 -> value / 1000.0    // mL
                2 -> value * 3.78541   // US gallon
                3 -> value * 0.236588  // cup
                4 -> value * 0.473176  // pint
                5 -> value * 0.0295735 // fl oz
                else -> value
            }
            when (toIdx) {
                0 -> liters
                1 -> liters * 1000.0
                2 -> liters / 3.78541
                3 -> liters / 0.236588
                4 -> liters / 0.473176
                5 -> liters / 0.0295735
                else -> liters
            }
        }
        UnitCategory.SPEED -> {
            val ms = when (fromIdx) {
                0 -> value              // m/s
                1 -> value / 3.6       // km/h
                2 -> value * 0.44704   // mph
                3 -> value * 0.514444  // knots
                4 -> value * 0.3048    // ft/s
                else -> value
            }
            when (toIdx) {
                0 -> ms
                1 -> ms * 3.6
                2 -> ms / 0.44704
                3 -> ms / 0.514444
                4 -> ms / 0.3048
                else -> ms
            }
        }
        UnitCategory.DATA -> {
            val bytes = when (fromIdx) {
                0 -> value                      // B
                1 -> value * 1024.0            // KB
                2 -> value * 1024.0 * 1024.0   // MB
                3 -> value * 1024.0.pow(3)     // GB
                4 -> value * 1024.0.pow(4)     // TB
                else -> value
            }
            when (toIdx) {
                0 -> bytes
                1 -> bytes / 1024.0
                2 -> bytes / (1024.0 * 1024.0)
                3 -> bytes / 1024.0.pow(3)
                4 -> bytes / 1024.0.pow(4)
                else -> bytes
            }
        }
    }
}

private fun Double.pow(n: Int): Double {
    var result = 1.0
    repeat(n) { result *= this }
    return result
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
}
