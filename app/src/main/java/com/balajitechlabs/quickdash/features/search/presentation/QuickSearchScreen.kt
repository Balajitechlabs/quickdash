/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/search/presentation
 * File: QuickSearchScreen.kt
 * Description: Universal web and device search screen querying multiple search engines with history.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.search.presentation


import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.JsonParser
import android.content.Intent
import android.net.Uri
import com.balajitechlabs.quickdash.core.utils.safeStartActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.balajitechlabs.quickdash.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.core.data.UserStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.zIndex

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.util.Log
import java.net.URLEncoder

@Composable
fun QuickSearchScreen(mainViewModel: com.balajitechlabs.quickdash.MainViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gson = Gson()

    var query by remember { mutableStateOf("") }
    var showAddEngineDialog by remember { mutableStateOf(false) }

    val searchHistoryJson by mainViewModel.userStore.searchHistory.collectAsStateWithLifecycle(initialValue = "[]")
    val searchHistory = remember(searchHistoryJson) {
        try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(searchHistoryJson, type) ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    val customEnginesJson by mainViewModel.userStore.customSearchEngines.collectAsStateWithLifecycle(initialValue = "[]")
    
    val engines = remember(customEnginesJson) {
        val defaultEngines = listOf(
            "Google" to "https://www.google.com/search?q=",
            "DuckDuckGo" to "https://duckduckgo.com/?q=",
            "YouTube" to "https://www.youtube.com/results?search_query=",
            "GitHub" to "https://github.com/search?q=",
            "Wikipedia" to "https://en.wikipedia.org/wiki/Special:Search?search="
        )
        val customList = try {
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            gson.fromJson<List<Map<String, String>>>(customEnginesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val customParsed = customList.mapNotNull {
            val name = it["name"] ?: return@mapNotNull null
            val url = it["url"] ?: return@mapNotNull null
            name to url
        }
        defaultEngines + customParsed
    }
    
    var selectedEngine by remember(engines) { mutableStateOf(engines[0]) }
    var querySuggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.length > 1) {
            kotlinx.coroutines.delay(200)
            withContext(Dispatchers.IO) {
                try {
                    val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
                    val urlString = "https://suggestqueries.google.com/complete/search?client=firefox&q=$encodedQuery"
                    val connection = java.net.URL(urlString).openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 3000
                    connection.readTimeout = 3000
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    
                    val jsonArray = JsonParser.parseString(response).asJsonArray
                    if (jsonArray.size() > 1) {
                        val suggestionList = jsonArray.get(1).asJsonArray
                        val list = mutableListOf<String>()
                        suggestionList.forEach { list.add(it.asString) }
                        querySuggestions = list
                    }
                } catch (e: java.lang.Exception) {
                    Log.e("QuickDash", "Error occurred: ${e.message}", e)
                }
            }
        } else {
            querySuggestions = emptyList()
        }
    }

    fun doSearch(q: String) {
        if (q.isBlank()) return
        coroutineScope.launch {
            mainViewModel.userStore.addSearchHistory(q)
        }
        try {
            val encoded = URLEncoder.encode(q, "UTF-8")
            val uri = Uri.parse(selectedEngine.second + encoded)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.safeStartActivity(intent, "No browser found to open search link")
        } catch (e: Exception) {
            com.balajitechlabs.quickdash.core.utils.AppLogger.e("QuickSearchScreen", "Failed to search", e)
        }
        onDismiss()
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).imePadding()) {
        // Engine selector with overlaid scroll buttons
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                engines.forEach { engine ->
                    val isSelected = selectedEngine == engine
                    val iconRes = when (engine.first) {
                        "Google" -> R.drawable.ic_search
                        "DuckDuckGo" -> R.drawable.ic_globe
                        "YouTube" -> R.drawable.ic_share
                        "GitHub" -> R.drawable.ic_github
                        else -> R.drawable.ic_note
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedEngine = engine },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = engine.first,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { Text(engine.first, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                IconButton(onClick = { showAddEngineDialog = true }) {
                    Icon(androidx.compose.material.icons.Icons.Filled.Add, contentDescription = "Add Engine")
                }
            }

            // Left scroll button
            if (scrollState.value > 0) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo((scrollState.value - 150).coerceAtLeast(0))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f), CircleShape)
                ) {
                    @Suppress("DEPRECATION")
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Scroll Left",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Right scroll button
            if (scrollState.value < scrollState.maxValue) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo((scrollState.value + 150).coerceAtMost(scrollState.maxValue))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f), CircleShape)
                ) {
                    @Suppress("DEPRECATION")
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Scroll Right",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().zIndex(10f)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search ${selectedEngine.first}...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { doSearch(query) })
            )
            
            val filteredHistory = remember(searchHistory, query) {
                if (query.isBlank()) emptyList()
                else searchHistory.filter { it.contains(query, ignoreCase = true) && it.lowercase() != query.lowercase() }.take(3)
            }
            
            val combinedSuggestions = remember(filteredHistory, querySuggestions) {
                val list = mutableListOf<Pair<String, Boolean>>() // Pair(suggestion, isHistory)
                filteredHistory.forEach { list.add(Pair(it, true)) }
                querySuggestions.forEach { sug ->
                    if (list.none { it.first.lowercase() == sug.lowercase() }) {
                        list.add(Pair(sug, false))
                    }
                }
                list.take(6)
            }

            if (combinedSuggestions.isNotEmpty() && query.isNotBlank()) {
                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp)
                        .zIndex(20f)
                ) {
                    combinedSuggestions.forEach { pair ->
                        val item = pair.first
                        val isHistory = pair.second
                        ListItem(
                            headlineContent = {
                                Text(item, style = MaterialTheme.typography.bodyMedium)
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = if (isHistory) Icons.Filled.History else Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.clickable { query = item; doSearch(item) },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { doSearch(query) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Search", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Popular Suggestions",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        val suggestionsScrollState = rememberScrollState()
        val suggestionsList = listOf(
            "Kotlin Flow guide",
            "Jetpack Compose",
            "Material 3 colors",
            "GitHub trends",
            "Gemini API docs",
            "StackOverflow",
            "Gradle Kotlin DSL",
            "Android Studio release",
            "Jetpack Navigation",
            "QuickDash repo"
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(suggestionsScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestionsList.forEach { term ->
                    SuggestionChip(
                        onClick = {
                            query = term
                            doSearch(term)
                        },
                        label = { Text(term, style = MaterialTheme.typography.bodySmall) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Left scroll button
            if (suggestionsScrollState.value > 0) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            suggestionsScrollState.animateScrollTo((suggestionsScrollState.value - 150).coerceAtLeast(0))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f), CircleShape)
                ) {
                    @Suppress("DEPRECATION")
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Scroll Left",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Right scroll button
            if (suggestionsScrollState.value < suggestionsScrollState.maxValue) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            suggestionsScrollState.animateScrollTo((suggestionsScrollState.value + 150).coerceAtMost(suggestionsScrollState.maxValue))
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f), CircleShape)
                ) {
                    @Suppress("DEPRECATION")
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Scroll Right",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }


        if (searchHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Searches",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = {
                    coroutineScope.launch { mainViewModel.userStore.clearSearchHistory() }
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(searchHistory.take(10)) { item ->
                    ListItem(
                        headlineContent = { Text(item, style = MaterialTheme.typography.bodyMedium) },
                        leadingContent = {
                            Icon(
                                Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { query = item },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    }
    if (showAddEngineDialog) {
        var engineName by remember { mutableStateOf("") }
        var engineUrl by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddEngineDialog = false },
            title = { Text("Add Custom Engine") },
            text = {
                Column {
                    OutlinedTextField(
                        value = engineName,
                        onValueChange = { engineName = it },
                        label = { Text("Engine Name (e.g. MyWiki)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = engineUrl,
                        onValueChange = { engineUrl = it },
                        label = { Text("Search URL (e.g. https://mywiki.com/q=)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (engineName.isNotBlank() && engineUrl.isNotBlank()) {
                            val currentList = try {
                                val type = object : TypeToken<List<Map<String, String>>>() {}.type
                                gson.fromJson<List<Map<String, String>>>(customEnginesJson, type) ?: emptyList()
                            } catch (e: Exception) { emptyList() }
                            
                            val newList = currentList.toMutableList()
                            newList.add(mapOf("name" to engineName.trim(), "url" to engineUrl.trim()))
                            
                            coroutineScope.launch {
                                mainViewModel.userStore.saveCustomSearchEngines(gson.toJson(newList))
                            }
                            showAddEngineDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEngineDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
