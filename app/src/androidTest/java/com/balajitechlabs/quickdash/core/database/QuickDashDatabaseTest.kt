/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/database
 * File: QuickDashDatabaseTest.kt
 * Description: Instrumentation tests verifying Room database operations and schema integrity on device.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.balajitechlabs.quickdash.core.data.database.AppDatabase
import com.balajitechlabs.quickdash.core.data.database.NoteDao
import com.balajitechlabs.quickdash.core.data.database.NoteEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class QuickDashDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        noteDao = db.noteDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadNote() = runTest {
        val note = NoteEntity(id = "note_1", text = "Test Note", isPinned = true)
        noteDao.insertNote(note)
        val allNotes = noteDao.getAllNotesSync()
        assertThat(allNotes).hasSize(1)
        assertThat(allNotes[0].text).isEqualTo("Test Note")
        assertThat(allNotes[0].isPinned).isTrue()
    }

    @Test
    fun deleteNoteRemovesEntry() = runTest {
        val note = NoteEntity(id = "note_2", text = "Delete Me")
        noteDao.insertNote(note)
        assertThat(noteDao.getNotesCount()).isEqualTo(1L)

        noteDao.deleteNote(note)
        assertThat(noteDao.getNotesCount()).isEqualTo(0L)
    }

    @Test
    fun insertMultipleAndReadAll() = runTest {
        val note1 = NoteEntity(id = "a", text = "Alpha")
        val note2 = NoteEntity(id = "b", text = "Beta", isPinned = true)
        noteDao.insertAll(listOf(note1, note2))

        val all = noteDao.getAllNotesSync()
        assertThat(all).hasSize(2)
    }

    @Test
    fun updateNoteModifiesExisting() = runTest {
        val note = NoteEntity(id = "note_3", text = "Original")
        noteDao.insertNote(note)

        val updated = note.copy(text = "Updated")
        noteDao.updateNote(updated)

        val all = noteDao.getAllNotesSync()
        assertThat(all[0].text).isEqualTo("Updated")
    }

    @Test
    fun notesOrderedByIsPinnedThenTimestamp() = runTest {
        val oldPinned = NoteEntity(id = "old_pinned", text = "Old Pinned", isPinned = true, timestamp = 100)
        val newUnpinned = NoteEntity(id = "new_unpinned", text = "New Unpinned", isPinned = false, timestamp = 300)
        val newPinned = NoteEntity(id = "new_pinned", text = "New Pinned", isPinned = true, timestamp = 200)

        noteDao.insertAll(listOf(oldPinned, newUnpinned, newPinned))

        val all = noteDao.getAllNotesSync()
        assertThat(all).hasSize(3)
        assertThat(all[0].text).isEqualTo("New Pinned")
        assertThat(all[1].text).isEqualTo("Old Pinned")
        assertThat(all[2].text).isEqualTo("New Unpinned")
    }
}
