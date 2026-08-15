package com.balajitechlabs.quickdash.features.broadcast.domain

import android.graphics.Bitmap

/**
 * 100% Zero-Tracker / Zero-Telemetry FOSS No-Op Implementation.
 * Ensures no network connections to api.telegram.org in F-Droid builds.
 */
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
