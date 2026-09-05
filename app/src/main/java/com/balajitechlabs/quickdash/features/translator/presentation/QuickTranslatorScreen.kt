/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/translator/presentation
 * File: QuickTranslatorScreen.kt
 * Description: Text translation tool supporting language pairs with clipboard integration.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.translator.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private val LANGUAGES = listOf(
    "Auto Detect" to "auto",
    "English" to "en",
    "Hindi" to "hi",
    "Spanish" to "es",
    "French" to "fr",
    "German" to "de",
    "Japanese" to "ja",
    "Chinese (Simplified)" to "zh-CN",
    "Arabic" to "ar",
    "Portuguese" to "pt",
    "Russian" to "ru",
    "Italian" to "it",
    "Korean" to "ko",
    "Turkish" to "tr",
    "Dutch" to "nl",
    "Bengali" to "bn",
    "Tamil" to "ta",
    "Telugu" to "te",
    "Urdu" to "ur",
    "Marathi" to "mr"
)

private suspend fun performGoogleTranslate(text: String, from: String, to: String): String = withContext(Dispatchers.IO) {
    return@withContext try {
        val srcLang = if (from == "auto") "auto" else from
        val encoded = URLEncoder.encode(text, "UTF-8")
        val urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$srcLang&tl=$to&dt=t&dj=1&q=$encoded"
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        conn.connectTimeout = 7000
        conn.readTimeout = 7000

        if (conn.responseCode == 200) {
            val json = conn.inputStream.bufferedReader().readText()
            val obj = runCatching { JSONObject(json) }.getOrNull()
            if (obj != null && obj.has("sentences")) {
                val sentences = obj.getJSONArray("sentences")
                val sb = StringBuilder()
                for (i in 0 until sentences.length()) {
                    val s = sentences.getJSONObject(i)
                    if (s.has("trans")) sb.append(s.getString("trans"))
                }
                sb.toString()
            } else text
        } else {
            "Translation service error (HTTP ${conn.responseCode})"
        }
    } catch (e: Exception) {
        "Connection failed: ${e.localizedMessage ?: "Please check your network"}"
    }
}

private data class TranslationHistoryItem(
    val originalText: String,
    val translatedText: String,
    val fromLang: String,
    val toLang: String
)

@Composable
fun QuickTranslatorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // TTS engine setup
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine = tts
            }
        }
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Translation states
    var sourceLanguageIndex by remember { mutableIntStateOf(0) }
    var targetLanguageIndex by remember { mutableIntStateOf(3) }
    var inputText by remember { mutableStateOf("") }
    var translatedResult by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    val translationHistory = remember { mutableStateListOf<TranslationHistoryItem>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    LanguageDropdown(selectedIndex = sourceLanguageIndex, onSelect = { sourceLanguageIndex = it }, label = "From", modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            if (sourceLanguageIndex != 0) {
                                val tmp = sourceLanguageIndex; sourceLanguageIndex = targetLanguageIndex; targetLanguageIndex = tmp
                                val tmpText = translatedResult; translatedResult = inputText; inputText = tmpText
                            }
                        },
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(Icons.Filled.SwapHoriz, "Swap languages", tint = MaterialTheme.colorScheme.primary)
                    }
                    LanguageDropdown(selectedIndex = targetLanguageIndex, onSelect = { targetLanguageIndex = it }, label = "To", modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Enter text to translate...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 180.dp),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = { inputText = ""; translatedResult = "" }) {
                                Icon(Icons.Filled.Clear, "Clear")
                            }
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = cb.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                val txt = clip.getItemAt(0).text?.toString() ?: ""
                                if (txt.isNotBlank()) inputText = txt
                            }
                        }
                    ) {
                        Icon(Icons.Filled.ContentPaste, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Paste from Clipboard", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    onClick = {
                        if (inputText.isNotBlank() && !isTranslating) {
                            isTranslating = true
                            scope.launch {
                                val res = performGoogleTranslate(
                                    inputText,
                                    LANGUAGES[sourceLanguageIndex].second,
                                    LANGUAGES[targetLanguageIndex].second
                                )
                                val isError = res.startsWith("Translation service error") || res.startsWith("Connection failed")
                                translatedResult = res
                                isTranslating = false
                                if (!isError) {
                                    translationHistory.add(
                                        0,
                                        TranslationHistoryItem(
                                            inputText,
                                            res,
                                            LANGUAGES[sourceLanguageIndex].first,
                                            LANGUAGES[targetLanguageIndex].first
                                        )
                                    )
                                }
                            }
                        }
                    },
                    enabled = !isTranslating && inputText.isNotBlank()
                ) {
                    if (isTranslating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Translating…", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Filled.Translate, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Translate Now", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Google Translate External Launcher Button (Full-width bar)
                OutlinedButton(
                    onClick = {
                        val encoded = runCatching { URLEncoder.encode(inputText, "UTF-8") }.getOrDefault("")
                        val src = LANGUAGES[sourceLanguageIndex].second
                        val tgt = LANGUAGES[targetLanguageIndex].second
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://translate.google.com/?sl=$src&tl=$tgt&text=$encoded")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF44474F)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF2C2C2E),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_globe),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Open in Google Translate (Web / App)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                val hasError = translatedResult.startsWith("Translation service error") || translatedResult.startsWith("Connection failed")
                AnimatedVisibility(visible = translatedResult.isNotEmpty()) {
                    Column {
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            color = if (hasError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("→ ${LANGUAGES[targetLanguageIndex].first}", style = MaterialTheme.typography.labelMedium, color = if (hasError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer.copy(0.75f))
                                    if (!hasError) {
                                        Row {
                                            IconButton(onClick = { speakText(context, ttsEngine, translatedResult, LANGUAGES[targetLanguageIndex].second) }) {
                                                Icon(Icons.AutoMirrored.Filled.VolumeUp, "Listen", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                            }
                                            IconButton(onClick = { copyToClipboard(context, "Translation", translatedResult) }) {
                                                Icon(Icons.Filled.ContentCopy, "Copy", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                            }
                                        }
                                    }
                                }
                                Text(translatedResult, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                    }
                }
            }
        }

        // Recent Translation History Section
        if (translationHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(" Recent Translations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                TextButton(onClick = { translationHistory.clear() }) {
                    Text("Clear", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                translationHistory.forEach { item ->
                    OutlinedCard(
                        onClick = {
                            inputText = item.originalText
                            translatedResult = item.translatedText
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.fromLang} → ${item.toLang}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = "Remove",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { translationHistory.remove(item) },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = item.originalText, style = MaterialTheme.typography.bodySmall, maxLines = 1, fontWeight = FontWeight.SemiBold)
                            Text(text = item.translatedText, style = MaterialTheme.typography.bodySmall, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = LANGUAGES[selectedIndex].first,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            shape = RoundedCornerShape(10.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LANGUAGES.forEachIndexed { index, (name, _) ->
                DropdownMenuItem(
                    text = { Text(name, fontSize = 13.sp) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun speakText(context: Context, tts: TextToSpeech?, text: String, langCode: String) {
    if (tts == null) {
        Toast.makeText(context, "Text-to-Speech not ready", Toast.LENGTH_SHORT).show()
        return
    }
    val locale = Locale.forLanguageTag(langCode)
    tts.language = locale
    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "QuickTranslateTTS")
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
}
