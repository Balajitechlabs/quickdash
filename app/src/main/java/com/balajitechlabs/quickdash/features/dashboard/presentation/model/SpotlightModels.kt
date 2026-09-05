/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/dashboard/presentation/model
 * File: SpotlightModels.kt
 * Description: Data models and categories representing dashboard spotlight tools and search providers.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.dashboard.presentation.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.ui.graphics.vector.ImageVector
import com.balajitechlabs.quickdash.core.ui.QuickTool
import java.util.Locale

data class SpotlightToolItem(
    val tool: QuickTool,
    val title: String,
    val description: String,
    val iconRes: Int = 0,
    val category: String,
    val imageVector: ImageVector? = null
)

data class SpotlightShortcutResult(
    val category: String,
    val expression: String,
    val result: String,
    val icon: ImageVector
)

/**
 * Lightweight mathematical evaluator supporting +, -, *, /, %, ^ and parentheses.
 */
fun evaluateMathExpression(expr: String): String {
    val clean = expr.replace(" ", "").replace("x", "*").replace("X", "*")
    val tokens = mutableListOf<String>()
    var i = 0
    while (i < clean.length) {
        val c = clean[i]
        if (c.isDigit() || c == '.') {
            var num = ""
            while (i < clean.length && (clean[i].isDigit() || clean[i] == '.')) {
                num += clean[i]
                i++
            }
            tokens.add(num)
        } else {
            tokens.add(c.toString())
        }
        i++
    }

    if (tokens.isEmpty()) return ""

    var result = tokens[0].toDoubleOrNull() ?: return ""
    var opIndex = 1
    while (opIndex < tokens.size - 1) {
        val op = tokens[opIndex]
        val nextVal = tokens[opIndex + 1].toDoubleOrNull() ?: return ""
        when (op) {
            "+" -> result += nextVal
            "-" -> result -= nextVal
            "*" -> result *= nextVal
            "/" -> if (nextVal != 0.0) result /= nextVal else return "Cannot divide by 0"
            "%" -> result %= nextVal
        }
        opIndex += 2
    }

    return if (result % 1.0 == 0.0) {
        result.toLong().toString()
    } else {
        String.format(Locale.US, "%.4f", result).trimEnd('0').trimEnd('.')
    }
}

fun evaluateSpotlightQuery(query: String): SpotlightShortcutResult? {
    val clean = query.trim()
    if (clean.length < 2) return null

    // 1. Currency Conversion (e.g. 100 USD to INR, 50 EUR in USD, 1000 inr usd)
    val currRegex = """^(\d+(?:\.\d+)?)\s*([a-zA-Z]{3})\s*(?:to|in|=)?\s*([a-zA-Z]{3})$""".toRegex()
    val currMatch = currRegex.find(clean)
    if (currMatch != null) {
        val amount = currMatch.groupValues[1].toDoubleOrNull()
        val fromCurr = currMatch.groupValues[2].uppercase()
        val toCurr = currMatch.groupValues[3].uppercase()

        val rates = mapOf(
            "USD" to 1.0,
            "INR" to 86.50,
            "EUR" to 0.92,
            "GBP" to 0.79,
            "AED" to 3.67,
            "CAD" to 1.38,
            "AUD" to 1.55,
            "JPY" to 152.0,
            "SGD" to 1.34,
            "CNY" to 7.24
        )

        val fromRate = rates[fromCurr]
        val toRate = rates[toCurr]
        if (amount != null && fromRate != null && toRate != null) {
            val converted = (amount / fromRate) * toRate
            return SpotlightShortcutResult(
                category = "Currency Conversion",
                expression = "$amount $fromCurr → $toCurr",
                result = String.format(Locale.US, "%.2f %s", converted, toCurr),
                icon = Icons.Rounded.CurrencyExchange
            )
        }
    }

    // 2. Common Unit Conversion (e.g. 10 km to mi, 5 kg in lbs, 100 f to c, 6 ft in cm)
    val unitRegex = """^(\d+(?:\.\d+)?)\s*([a-zA-Z]+)\s*(?:to|in|=)?\s*([a-zA-Z]+)$""".toRegex()
    val unitMatch = unitRegex.find(clean)
    if (unitMatch != null) {
        val value = unitMatch.groupValues[1].toDoubleOrNull()
        val fromUnit = unitMatch.groupValues[2].lowercase()
        val toUnit = unitMatch.groupValues[3].lowercase()

        if (value != null) {
            val converted: Double? = when {
                fromUnit in listOf("km", "kilometer", "kilometers") && toUnit in listOf("mi", "mile", "miles") -> value * 0.621371
                fromUnit in listOf("mi", "mile", "miles") && toUnit in listOf("km", "kilometer", "kilometers") -> value * 1.60934
                fromUnit in listOf("m", "meter", "meters") && toUnit in listOf("ft", "feet") -> value * 3.28084
                fromUnit in listOf("ft", "feet") && toUnit in listOf("m", "meter", "meters") -> value * 0.3048
                fromUnit in listOf("cm", "centimeter") && toUnit in listOf("in", "inch", "inches") -> value * 0.393701
                fromUnit in listOf("in", "inch", "inches") && toUnit in listOf("cm", "centimeter") -> value * 2.54
                fromUnit in listOf("kg", "kilogram") && toUnit in listOf("lbs", "lb", "pound", "pounds") -> value * 2.20462
                fromUnit in listOf("lbs", "lb", "pound", "pounds") && toUnit in listOf("kg", "kilogram") -> value * 0.453592
                fromUnit in listOf("g", "gram") && toUnit in listOf("oz", "ounce") -> value * 0.035274
                fromUnit in listOf("oz", "ounce") && toUnit in listOf("g", "gram") -> value * 28.3495
                fromUnit in listOf("c", "celsius") && toUnit in listOf("f", "fahrenheit") -> (value * 9.0 / 5.0) + 32.0
                fromUnit in listOf("f", "fahrenheit") && toUnit in listOf("c", "celsius") -> (value - 32.0) * 5.0 / 9.0
                else -> null
            }
            if (converted != null) {
                val formatted = if (converted % 1.0 == 0.0) converted.toLong().toString() else String.format(Locale.US, "%.2f", converted)
                return SpotlightShortcutResult(
                    category = "Unit Conversion",
                    expression = "$value $fromUnit → $toUnit",
                    result = "$formatted $toUnit",
                    icon = Icons.Rounded.Straighten
                )
            }
        }
    }

    // 3. Mathematical Evaluation Heuristic
    if (clean.length >= 2 && clean.all { it.isDigit() || it in "+-*/().%^ xX" } && clean.any { it in "+-*/%xX" }) {
        try {
            val mathRes = evaluateMathExpression(clean)
            if (mathRes.isNotBlank()) {
                return SpotlightShortcutResult(
                    category = "Calculation Result",
                    expression = clean,
                    result = "= $mathRes",
                    icon = Icons.Rounded.Calculate
                )
            }
        } catch (_: Exception) {}
    }

    return null
}
