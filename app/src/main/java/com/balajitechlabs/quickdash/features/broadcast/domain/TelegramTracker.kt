/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/broadcast/domain
 * File: TelegramTracker.kt
 * Description: Manages read receipts and polling offset counters for received broadcast messages.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.broadcast.domain

import android.graphics.Bitmap


object TelegramTracker {
    suspend fun sendMessage(message: String) {
        // No-Op in FOSS edition
    }

    suspend fun sendBroadcastBotMessage(message: String) {
        // No-Op in FOSS edition
    }

    suspend fun sendPhoto(caption: String, bitmap: Bitmap) {
        // No-Op in FOSS edition
    }
}
