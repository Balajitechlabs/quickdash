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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.ui.text.style.TextAlign
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
                userStore.saveNotesHistory("[]") // Only clear after successful migration
            } catch (e: Exception) {
                com.balajitechlabs.quickdash.core.utils.AppLogger.e("QuickNotesScreen", "Failed to migrate notes to Room database", e)
            }
        }
    }

    var text by remember { mutableStateOf("") }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var showArchived by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("Newest") }

    val filteredNotes = remember(notes, showArchived, searchQuery, sortOption) {
        val base = notes.filter { 
            it.isArchived == showArchived &&
            (searchQuery.isEmpty() || it.text.contains(searchQuery, ignoreCase = true))
        }
        when (sortOption) {
            "Oldest" -> base.sortedBy { it.timestamp }
            "A-Z" -> base.sortedBy { it.text.lowercase() }
            else -> base.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.timestamp })
        }
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
            "📅" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            "⏰" to java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
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

        if (!isFloating) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                placeholder = { Text("Search notes…") },
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                trailingIcon = if (searchQuery.isNotEmpty()) ({
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }) else null
            )

            // Sort Options Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sort:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listOf("Newest", "Oldest", "A-Z").forEach { option ->
                    FilterChip(
                        selected = sortOption == option,
                        onClick = { sortOption = option },
                        label = { Text(option, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

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
        if (displayNotes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No notes found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try searching for something else" else "Tap '+' above to add your first note",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
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
                                noteToDelete = note
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

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(onClick = {
                    val note = noteToDelete
                    if (note != null) {
                        coroutineScope.launch {
                            noteDao.deleteNote(note)
                        }
                    }
                    noteToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
