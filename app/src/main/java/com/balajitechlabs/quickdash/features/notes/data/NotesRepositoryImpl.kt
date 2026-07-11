package com.balajitechlabs.quickdash.features.notes.data

import com.balajitechlabs.quickdash.core.data.database.NoteDao
import com.balajitechlabs.quickdash.core.data.database.NoteEntity
import com.balajitechlabs.quickdash.features.notes.domain.model.Note
import com.balajitechlabs.quickdash.features.notes.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotesRepositoryImpl(private val noteDao: NoteDao) : NotesRepository {
    
    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllNotesSync(): List<Note> {
        return noteDao.getAllNotesSync().map { it.toDomain() }
    }

    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note.toEntity())
    }

    override suspend fun insertAll(notes: List<Note>): List<Long> {
        return noteDao.insertAll(notes.map { it.toEntity() })
    }

    override suspend fun updateNote(note: Note): Int {
        return noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(note: Note): Int {
        return noteDao.deleteNote(note.toEntity())
    }

    override suspend fun getNotesCount(): Long {
        return noteDao.getNotesCount()
    }
}

// Mapper extensions
private fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        text = text,
        timestamp = timestamp,
        isPinned = isPinned
    )
}

private fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        text = text,
        timestamp = timestamp,
        isPinned = isPinned,
        isArchived = false
    )
}
