/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data/database
 * File: NoteDao.kt
 * Description: Data access object providing Room database operations for creating, updating, and querying user notes.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesSync(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>): List<Long>

    @Update
    suspend fun updateNote(note: NoteEntity): Int

    @Delete
    suspend fun deleteNote(note: NoteEntity): Int
    
    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNotesCount(): Long
}