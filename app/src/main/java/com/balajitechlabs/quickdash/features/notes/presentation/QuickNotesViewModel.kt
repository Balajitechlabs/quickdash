/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/notes/presentation
 * File: QuickNotesViewModel.kt
 * Description: ViewModel managing Quick Notes CRUD state and separating Room/DataStore I/O from the Compose UI.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.notes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.notes.domain.model.Note
import com.balajitechlabs.quickdash.features.notes.domain.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Coordinates note persistence via [NotesRepository] and tracks usage stats in [UserStore]
 * so [QuickNotesScreen] stays purely declarative without direct database access.
 */
@HiltViewModel
class QuickNotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val userStore: UserStore
) : ViewModel() {

    val notes: StateFlow<List<Note>> = notesRepository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun saveNote(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            notesRepository.insertNote(Note(text = trimmed))
            userStore.incrementNotesSaved()
        }
    }

    fun updateNote(note: Note, updatedText: String) {
        val trimmed = updatedText.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            notesRepository.updateNote(
                note.copy(
                    text = trimmed,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            notesRepository.deleteNote(note)
        }
    }
}
