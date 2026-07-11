package com.balajitechlabs.quickdash.features.notes.presentation

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SuggestionChip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.data.database.AppDatabase
import com.balajitechlabs.quickdash.core.data.database.NoteEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val annotatedString = remember(text, primaryColor) {
        buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                if (line.startsWith("# ")) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryColor)) {
                        append(line.removePrefix("# "))
                    }
                } else if (line.startsWith("- ")) {
                    append("•  ")
                    append(line.removePrefix("- "))
                } else {
                    var currentLine = line
                    while (currentLine.contains("**")) {
                        val start = currentLine.indexOf("**")
                        val end = currentLine.indexOf("**", start + 2)
                        if (end != -1) {
                            append(currentLine.substring(0, start))
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(currentLine.substring(start + 2, end))
                            }
                            currentLine = currentLine.substring(end + 2)
                        } else {
                            break
                        }
                    }
                    append(currentLine)
                }
                if (index < lines.size - 1) append("\n")
            }
        }
    }
    Text(
        text = annotatedString,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNotesScreen(userStore: UserStore, isFloating: Boolean = false, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val database = remember { AppDatabase.getDatabase(context) }
    val noteDao = database.noteDao()
    
    val notes by noteDao.getAllNotes().collectAsState(initial = emptyList())
    
    // Migration Logic: Transfer old DataStore JSON notes to Room DB
    val notesJson by userStore.notesHistory.collectAsState(initial = "")
    LaunchedEffect(notesJson) {
        if (notesJson.isNotBlank() && notesJson != "[]") {
            try {
                var migratedNotes = emptyList<NoteEntity>()
                try {
                    val listType = object : TypeToken<List<NoteEntity>>() {}.type
                    migratedNotes = Gson().fromJson<List<NoteEntity>>(notesJson, listType) ?: emptyList()
                } catch (e: Exception) {
                    try {
                        val stringListType = object : TypeToken<List<String>>() {}.type
                        val simpleStrings = Gson().fromJson<List<String>>(notesJson, stringListType) ?: emptyList()
                        migratedNotes = simpleStrings.map { textStr -> NoteEntity(text = textStr) }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
                
                if (migratedNotes.isNotEmpty()) {
                    noteDao.insertAll(migratedNotes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                userStore.saveNotesHistory("[]") // Guarantee clear to avoid infinite loops
            }
        }
    }

    var text by remember { mutableStateOf("") }
    var showArchived by remember { mutableStateOf(false) }

    val filteredNotes = remember(notes, showArchived) {
        notes.filter { it.isArchived == showArchived }
    }

    val isTabLocked by userStore.tabBiometricLock.collectAsState(initial = false)
    var isUnlocked by remember { mutableStateOf(false) }

    if (isTabLocked && !isUnlocked) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("This tab is locked", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                com.balajitechlabs.quickdash.core.utils.BiometricHelper.authenticate(
                    context = context,
                    onSuccess = { isUnlocked = true }
                )
            }) {
                Text("Tap to Unlock")
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // Quick-insert suggestion chips
        val suggestions = listOf(
            "📅" to java.time.LocalDate.now().toString(),
            "⏰" to java.time.LocalTime.now().let { String.format("%02d:%02d", it.hour, it.minute) },
            "✅" to "[ ] ",
            "🔢" to "1. ",
            "💡" to "Note: "
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(suggestions) { (emoji, insert) ->
                SuggestionChip(
                    onClick = { text = text + insert },
                    label = { Text(emoji) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
            placeholder = { Text("Write something… (# Header, **bold**, - bullet)") },
            shape = MaterialTheme.shapes.large,
            maxLines = 5,
            trailingIcon = if (text.isNotEmpty()) ({
                FilledTonalIconButton(
                    onClick = {
                        val noteText = text.trim()
                        if (noteText.isEmpty()) return@FilledTonalIconButton
                        coroutineScope.launch {
                            noteDao.insertNote(NoteEntity(text = noteText))
                            userStore.incrementNotesSaved()
                        }
                        text = ""
                    },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Save note", modifier = Modifier.size(20.dp))
                }
            }) else null
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val noteText = text.trim()
                if (noteText.isEmpty()) return@Button
                coroutineScope.launch {
                    noteDao.insertNote(NoteEntity(text = noteText))
                    userStore.incrementNotesSaved()
                }
                text = ""
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            enabled = text.isNotBlank()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Note")
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Active vs Archive filter — M3 SegmentedButton
        if (!isFloating) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !showArchived,
                    onClick = { showArchived = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    label = { Text("Active Notes") },
                    icon = { SegmentedButtonDefaults.Icon(active = !showArchived) }
                )
                SegmentedButton(
                    selected = showArchived,
                    onClick = { showArchived = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    label = { Text("Archived") },
                    icon = { SegmentedButtonDefaults.Icon(active = showArchived) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        val displayNotes = if (isFloating) filteredNotes.take(3) else filteredNotes
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(displayNotes, key = { it.id }) { note ->
                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp, pressedElevation = 0.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (note.isPinned)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth().animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                MarkdownText(text = note.text)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Pin Toggle Action
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    noteDao.updateNote(note.copy(isPinned = !note.isPinned))
                                }
                            }) {
                                Icon(
                                    Icons.Default.PushPin, 
                                    contentDescription = "Pin",
                                    tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Archive/Unarchive Action
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    noteDao.updateNote(note.copy(isArchived = !note.isArchived))
                                }
                            }) {
                                Icon(
                                    imageVector = if (note.isArchived) Icons.Default.Unarchive else Icons.Default.Archive, 
                                    contentDescription = "Archive",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Export to Txt File (Share sheet) Action
                            IconButton(onClick = {
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, note.text)
                                        putExtra(Intent.EXTRA_TITLE, "QuickDash Note")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Export Note to File"))
                                } catch (e: Exception) { e.printStackTrace() }
                            }) {
                                Icon(
                                    Icons.Default.Share, 
                                    contentDescription = "Export as Txt File",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Delete Action
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    noteDao.deleteNote(note)
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Reading metrics footer badges
                        val charCount = note.text.length
                        val wordList = note.text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                        val wordCount = wordList.size
                        val readTimeMin = (wordCount / 200).coerceAtLeast(1)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📏 $charCount chars",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "✍️ $wordCount words",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "⏱️ $readTimeMin min read",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
        
        
        if (!isFloating) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
