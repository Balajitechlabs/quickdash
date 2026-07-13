package com.balajitechlabs.quickdash.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

import androidx.room.Index

@Entity(tableName = "notes", indices = [Index(value = ["timestamp"])])
data class NoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)