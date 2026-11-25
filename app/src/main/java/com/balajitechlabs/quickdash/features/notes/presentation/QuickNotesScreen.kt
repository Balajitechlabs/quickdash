/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/notes/presentation
 * File: QuickNotesScreen.kt
 * Description: Material 3 Expressive Quick Notes screen for markdown scratchpad capture, editing, selection, and sharing.
 *              Uses QuickNotesViewModel for all persistence and core/designsystem tokens with zero hardcoded colors.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.notes.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.MainViewModel
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashCard
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashCopyShareRow
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashEmptyState
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashPrimaryButton
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashSectionHeader
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashSegmentedSelector
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashHaptics
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashShapeTokens
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashTokens
import com.balajitechlabs.quickdash.features.notes.domain.model.Note

private enum class NoteEditorMode(val label: String) {
    WRITE("Write"),
    PREVIEW("Preview")
}

private val MarkdownShortcuts = listOf(
    "# Header" to "# ",
    "**Bold**" to "**bold**",
    "- Bullet" to "- ",
    "Todo" to "[ ] ",
    "> Quote" to "> ",
    "`Code`" to "`code`"
)

/**
 * Renders lightweight markdown syntax using [MaterialTheme.colorScheme] tokens and
 * wraps the output in [SelectionContainer] so users can press-and-hold to select any text.
 */
@Composable
fun FormattedMarkdown(text: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val annotatedString = remember(text, colors) {
        buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                when {
                    line.startsWith("# ") -> {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colors.primary
                            )
                        ) {
                            append(line.removePrefix("# "))
                        }
                    }
                    line.startsWith("## ") -> {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colors.onSurface
                            )
                        ) {
                            append(line.removePrefix("## "))
                        }
                    }
                    line.startsWith("- ") || line.startsWith("* ") -> {
                        withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)) {
                            append("•  ")
                        }
                        append(line.substring(2))
                    }
                    line.startsWith("[ ] ") -> {
                        withStyle(SpanStyle(color = colors.onSurfaceVariant)) {
                            append("☐  ")
                        }
                        append(line.removePrefix("[ ] "))
                    }
                    line.startsWith("[x] ") -> {
                        withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)) {
                            append("☑  ")
                        }
                        append(line.removePrefix("[x] "))
                    }
                    line.startsWith("> ") -> {
                        withStyle(
                            SpanStyle(
                                color = colors.tertiary,
                                fontStyle = FontStyle.Italic
                            )
                        ) {
                            append("│ ${line.removePrefix("> ")}")
                        }
                    }
                    line.startsWith("`") && line.endsWith("`") && line.length >= 2 -> {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = colors.surfaceContainerHighest,
                                color = colors.secondary
                            )
                        ) {
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
                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = colors.onSurface
                                    )
                                ) {
                                    append(current.substring(start + 2, end))
                                }
                                current = current.substring(end + 2)
                            } else {
                                break
                            }
                        }
                        append(current)
                    }
                }
                if (index < lines.size - 1) append("\n")
            }
        }
    }

    SelectionContainer(modifier = modifier.fillMaxWidth()) {
        Text(
            text = annotatedString,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = colors.onSurface,
            textAlign = TextAlign.Start
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun QuickNotesScreen(
    mainViewModel: MainViewModel,
    isFloating: Boolean = false,
    onDismiss: () -> Unit = {},
    notesViewModel: QuickNotesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val notes by notesViewModel.notes.collectAsStateWithLifecycle()

    var noteInput by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var selectedMode by remember { mutableStateOf(NoteEditorMode.WRITE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = QuickDashTokens.Spacing.lg,
                vertical = QuickDashTokens.Spacing.sm
            )
    ) {
        QuickDashSegmentedSelector(
            options = NoteEditorMode.entries,
            selected = selectedMode,
            onSelect = { mode ->
                QuickDashHaptics.perform(context, QuickDashHaptics.Style.TICK)
                selectedMode = mode
            },
            label = { it.label },
            icon = { mode ->
                when (mode) {
                    NoteEditorMode.WRITE -> Icons.Default.Edit
                    NoteEditorMode.PREVIEW -> Icons.Default.Visibility
                }
            },
            modifier = Modifier.padding(bottom = QuickDashTokens.Spacing.md)
        )

        if (selectedMode == NoteEditorMode.WRITE) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = QuickDashTokens.Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(QuickDashTokens.Spacing.sm)
            ) {
                items(MarkdownShortcuts, key = { it.first }) { (label, snippet) ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            QuickDashHaptics.perform(context, QuickDashHaptics.Style.TICK)
                            noteInput += if (noteInput.isEmpty() || noteInput.endsWith("\n")) {
                                snippet
                            } else {
                                "\n$snippet"
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                placeholder = {
                    Text(
                        text = "Write notes in markdown…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 160.dp),
                shape = QuickDashShapeTokens.Input,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QuickDashTokens.Spacing.sm)
            ) {
                if (editingNote != null) {
                    OutlinedButton(
                        onClick = {
                            QuickDashHaptics.perform(context, QuickDashHaptics.Style.CLICK)
                            editingNote = null
                            noteInput = ""
                        },
                        shape = QuickDashShapeTokens.Input,
                        modifier = Modifier.height(QuickDashTokens.Size.buttonHeight)
                    ) {
                        Text("Cancel")
                    }
                }
                QuickDashPrimaryButton(
                    text = if (editingNote != null) "Update Note" else "Save Note",
                    icon = if (editingNote != null) Icons.Default.Check else Icons.Default.Add,
                    enabled = noteInput.isNotBlank(),
                    onClick = {
                        QuickDashHaptics.perform(context, QuickDashHaptics.Style.HEAVY)
                        val currentEditing = editingNote
                        if (currentEditing != null) {
                            notesViewModel.updateNote(currentEditing, noteInput)
                            editingNote = null
                            noteInput = ""
                            Toast.makeText(context, "Note Updated", Toast.LENGTH_SHORT).show()
                        } else {
                            notesViewModel.saveNote(noteInput)
                            noteInput = ""
                            Toast.makeText(context, "Note Saved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            QuickDashCard(
                modifier = Modifier.heightIn(min = 120.dp, max = 200.dp)
            ) {
                if (noteInput.isBlank()) {
                    Text(
                        text = "Type text in Write mode to see live markdown preview here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    FormattedMarkdown(text = noteInput)
                }
            }
        }

        Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.lg))

        QuickDashSectionHeader(
            title = "Saved Notes (${notes.size})",
            trailing = {
                Text(
                    text = "Swipe right to delete",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.sm))

        if (notes.isEmpty()) {
            QuickDashEmptyState(
                icon = Icons.Default.Notes,
                title = "No notes yet",
                description = "Capture a quick markdown note above to keep it handy."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(QuickDashTokens.Spacing.sm),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                notesViewModel.deleteNote(note)
                                if (editingNote?.id == note.id) {
                                    editingNote = null
                                    noteInput = ""
                                }
                                Toast.makeText(context, "Note Deleted", Toast.LENGTH_SHORT).show()
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromEndToStart = false,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(QuickDashShapeTokens.Card)
                                    .padding(horizontal = QuickDashTokens.Spacing.xl),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    ) {
                        QuickDashCard {
                            FormattedMarkdown(text = note.text)
                            Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.sm))
                            QuickDashCopyShareRow(
                                onEdit = {
                                    QuickDashHaptics.perform(context, QuickDashHaptics.Style.CLICK)
                                    editingNote = note
                                    noteInput = note.text
                                    selectedMode = NoteEditorMode.WRITE
                                },
                                onCopy = {
                                    QuickDashHaptics.perform(context, QuickDashHaptics.Style.CLICK)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("QuickNote", note.text))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                onShare = {
                                    QuickDashHaptics.perform(context, QuickDashHaptics.Style.CLICK)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, note.text)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Note"))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
