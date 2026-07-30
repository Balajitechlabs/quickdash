package com.balajitechlabs.quickdash.database

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
}
