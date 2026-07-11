package com.balajitechlabs.quickdash.features.notes.domain.repository

import com.balajitechlabs.quickdash.features.notes.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getAllNotesSync(): List<Note>
    suspend fun insertNote(note: Note): Long
    suspend fun insertAll(notes: List<Note>): List<Long>
    suspend fun updateNote(note: Note): Int
    suspend fun deleteNote(note: Note): Int
    suspend fun getNotesCount(): Long
}
