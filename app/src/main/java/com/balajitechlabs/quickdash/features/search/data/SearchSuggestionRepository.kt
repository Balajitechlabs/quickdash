/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/search/data
 * File: SearchSuggestionRepository.kt
 * Description: Fetches search autocomplete suggestions off the UI thread so QuickSearchScreen stays declarative.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.search.data

import android.util.Log
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchSuggestionRepository @Inject constructor() {

    suspend fun fetchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        var connection: HttpURLConnection? = null
        try {
            val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
            val url = URL("https://suggestqueries.google.com/complete/search?client=firefox&q=$encodedQuery")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
            }
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JsonParser.parseString(response).asJsonArray
            if (jsonArray.size() > 1) {
                jsonArray.get(1).asJsonArray.map { it.asString }
            } else {
                emptyList()
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w("SearchSuggestions", "Suggestion fetch failed: ${e.message}")
            emptyList()
        } finally {
            connection?.disconnect()
        }
    }
}
