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
            "⚠️ Translation service error (HTTP ${conn.responseCode})"
        }
    } catch (e: Exception) {
        "⚠️ Connection failed: ${e.localizedMessage}"
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
            tts?.stop()
            tts?.shutdown()
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = cb.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                val txt = clip.getItemAt(0).text?.toString() ?: ""
                                if (txt.isNotBlank()) inputText = txt
                            }
                        }
                    ) {
                        Icon(Icons.Filled.ContentPaste, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Paste")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (inputText.isBlank()) return@Button
                                isTranslating = true
                                scope.launch {
                                    val res = performGoogleTranslate(
                                        inputText,
                                        LANGUAGES[sourceLanguageIndex].second,
                                        LANGUAGES[targetLanguageIndex].second
                                    )
                                    translatedResult = res
                                    isTranslating = false
                                    if (!res.startsWith("⚠️")) {
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
                            },
                            enabled = !isTranslating && inputText.isNotBlank()
                        ) {
                            if (isTranslating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Translating…")
                            } else {
                                Icon(Icons.Filled.Translate, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Translate")
                            }
                        }

                        // Google Translate External Launcher Button (with official Globe/Translate App Icon)
                        OutlinedIconButton(
                            onClick = {
                                val encoded = runCatching { URLEncoder.encode(inputText, "UTF-8") }.getOrDefault("")
                                val src = LANGUAGES[sourceLanguageIndex].second
                                val tgt = LANGUAGES[targetLanguageIndex].second
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://translate.google.com/?sl=$src&tl=$tgt&text=$encoded")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                )
                            }
                        ) {
                            Icon(painterResource(R.drawable.ic_globe), contentDescription = "Open Google Translate", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                AnimatedVisibility(visible = translatedResult.isNotEmpty()) {
                    Column {
                        Spacer(Modifier.height(16.dp))
                        Surface(
                            color = if (translatedResult.startsWith("⚠️")) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("→ ${LANGUAGES[targetLanguageIndex].first}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.75f))
                                    if (!translatedResult.startsWith("⚠️")) {
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
                Text("📜 Recent Translations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
