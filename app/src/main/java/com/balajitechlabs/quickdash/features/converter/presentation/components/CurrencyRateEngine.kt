/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/converter/presentation/components
 * File: CurrencyRateEngine.kt
 * Description: Currency definitions, offline base rates, and live open-access exchange rate network loader.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.converter.presentation.components

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

val OFFLINE_RATES = mapOf(
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

val CURRENCY_DISPLAY = listOf(
    "USD" to " US Dollar",
    "EUR" to " Euro",
    "GBP" to " British Pound",
    "INR" to " Indian Rupee",
    "JPY" to " Japanese Yen",
    "CAD" to " Canadian Dollar",
    "AUD" to " Australian Dollar",
    "AED" to " UAE Dirham",
    "SGD" to " Singapore Dollar",
    "CNY" to " Chinese Yuan",
    "CHF" to " Swiss Franc",
    "HKD" to " Hong Kong Dollar",
    "KRW" to " South Korean Won",
    "MXN" to " Mexican Peso",
    "BRL" to " Brazilian Real",
    "SEK" to " Swedish Krona",
    "NOK" to " Norwegian Krone",
    "DKK" to " Danish Krone",
    "NZD" to " New Zealand Dollar",
    "ZAR" to " South African Rand"
)

suspend fun fetchLiveRates(): Map<String, Double>? = withContext(Dispatchers.IO) {
    try {
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
    } catch (_: Exception) {
        null
    }
}
