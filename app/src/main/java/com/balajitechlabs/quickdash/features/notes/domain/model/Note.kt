/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/notes/domain/model
 * File: Note.kt
 * Description: Domain model representing a user note with timestamp, pin state, and content.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.notes.domain.model

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)