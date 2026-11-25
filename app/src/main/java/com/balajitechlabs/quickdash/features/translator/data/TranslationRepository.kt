/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/translator/data
 * File: TranslationRepository.kt
 * Description: Network repository executing Google Translate requests off the main thread with proper connection cleanup.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.translator.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationRepository @Inject constructor() {

    suspend fun translate(text: String, fromCode: String, toCode: String): String = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = URL("https://translate.googleapis.com/translate_a/single?client=gtx&sl=$fromCode&tl=$toCode&dt=t&dj=1&q=$encoded")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0")
                connectTimeout = 7000
                readTimeout = 7000
            }

            if (connection.responseCode == 200) {
                val json = connection.inputStream.bufferedReader().use { it.readText() }
                val obj = runCatching { JSONObject(json) }.getOrNull()
                if (obj != null && obj.has("sentences")) {
                    val sentences = obj.getJSONArray("sentences")
                    buildString {
                        for (i in 0 until sentences.length()) {
                            val s = sentences.getJSONObject(i)
                            if (s.has("trans")) append(s.getString("trans"))
                        }
                    }
                } else {
                    text
                }
            } else {
                "Translation service error (HTTP ${connection.responseCode})"
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            "Connection failed: ${e.localizedMessage ?: "Please check your network"}"
        } finally {
            connection?.disconnect()
        }
    }
}
