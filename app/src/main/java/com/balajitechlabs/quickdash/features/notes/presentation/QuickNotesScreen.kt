/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/notes/presentation
 * File: QuickNotesScreen.kt
 * Description: Lightweight notes tool supporting fast scratchpad capture, markdown formatting, and sharing.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.notes.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.MainViewModel
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.data.database.AppDatabase
import com.balajitechlabs.quickdash.core.data.database.NoteEntity
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FormattedMarkdown(text: String, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val annotatedString = remember(text, primaryColor) {
        buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                when {
                    line.startsWith("# ") -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = primaryColor)) {
                            append(line.removePrefix("# "))
                        }
                    }
                    line.startsWith("## ") -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)) {
                            append(line.removePrefix("## "))
                        }
                    }
                    line.startsWith("- ") || line.startsWith("* ") -> {
                        withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                            append("•  ")
                        }
                        append(line.substring(2))
                    }
                    line.startsWith("[ ] ") -> {
                        withStyle(SpanStyle(color = Color.Gray)) {
                            append("  ")
                        }
                        append(line.removePrefix("[ ] "))
                    }
                    line.startsWith("[x] ") -> {
                        withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                            append("  ")
                        }
                        append(line.removePrefix("[x] "))
                    }
                    line.startsWith("> ") -> {
                        withStyle(SpanStyle(color = Color(0xFF81C784), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                            append(" ${line.removePrefix("> ")}")
                        }
                    }
                    line.startsWith("`") && line.endsWith("`") -> {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0xFF2A2B30), color = Color(0xFFFFD54F))) {
                            append(" ${line.removeSurrounding("`")} ")
                        }
                    }
                    else -> {
                        var current = line
                        while (current.contains("**")) {
                            val start = current.indexOf("**")
                            val end = current.indexOf("**", start + 2)
                            if (end != -1) {
                                append(current.substring(0, start))
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                                    append(current.substring(start + 2, end))
                                }
                                current = current.substring(end + 2)
                            } else break
                        }
                        append(current)
                    }
                }
                if (index < lines.size - 1) append("\n")
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
        color = Color(0xFFE0E0E0),
        textAlign = TextAlign.Start
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNotesScreen(
    mainViewModel: MainViewModel,
    isFloating: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val noteDao = database.noteDao()
    val notes by noteDao.getAllNotes().collectAsStateWithLifecycle(initialValue = emptyList())

    var noteInput by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("Write") } // "Write" or "Preview"
    var searchQuery by remember { mutableStateOf("") }

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) notes
        else notes.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Mode Switcher: [ Write / Edit ] vs [ Live Preview ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Write" to Icons.Default.Edit, "Preview" to Icons.Default.Visibility).forEach { (mode, icon) ->
                val isSelected = selectedMode == mode
                Surface(
                    onClick = {
                        com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                        selectedMode = mode
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF2A2B30),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = mode,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mode,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White
                        )
                    }
                }
            }
        }

        if (selectedMode == "Write") {
            // Markdown Quick Tags
            val markdownShortcuts = listOf(
                "# Header" to "# ",
                "**Bold**" to "**bold**",
                "- Bullet" to "- ",
                " Todo" to "[ ] ",
                "> Quote" to "> ",
                "`Code`" to "`code`"
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(markdownShortcuts) { (label, snippet) ->
                    Surface(
                        onClick = {
                            com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true, 16L)
                            noteInput += if (noteInput.isEmpty() || noteInput.endsWith("\n")) snippet else "\n$snippet"
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2A2B30),
                        border = BorderStroke(1.dp, Color(0xFF44474F))
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Editor
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                placeholder = { Text("Write notes in markdown…", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 160.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E2024),
                    unfocusedContainerColor = Color(0xFF1E2024),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFF44474F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playHeavyVibration(context, true, 28L)
                    val trimmed = noteInput.trim()
                    if (trimmed.isNotBlank()) {
                        coroutineScope.launch {
                            noteDao.insertNote(NoteEntity(text = trimmed))
                            mainViewModel.userStore.incrementNotesSaved()
                            noteInput = ""
                            Toast.makeText(context, "Note Saved ", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = noteInput.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Note", fontWeight = FontWeight.Bold)
            }
        } else {
            // Live Preview Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E2024),
                border = BorderStroke(1.dp, Color(0xFF44474F))
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    if (noteInput.isBlank()) {
                        Text(
                            text = "Type text in Write mode to see live markdown preview here.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        FormattedMarkdown(text = noteInput)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Saved Notes Header + Search
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Saved Notes (${filteredNotes.size})",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "Swipe right to delete",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Notes List with Swipe-to-Delete
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(filteredNotes, key = { it.id }) { note ->
                @Suppress("DEPRECATION")
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            coroutineScope.launch {
                                noteDao.deleteNote(note)
                                Toast.makeText(context, "Note Deleted", Toast.LENGTH_SHORT).show()
                            }
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFD32F2F).copy(alpha = 0.8f))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    }
                ) {
                    RoundedCardContainer(
                        containerColor = Color(0xFF2A2B30),
                        cornerRadius = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            FormattedMarkdown(text = note.text)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true, 18L)
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("QuickNote", note.text))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true, 18L)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, note.text)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Note"))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
