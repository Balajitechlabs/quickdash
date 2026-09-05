/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/network
 * File: FeedbackSender.kt
 * Description: Dispatches user feedback, diagnostics, and issue reports to the developer support channel.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.network

import android.content.Context
import android.os.Build

object FeedbackSender {

    suspend fun sendFeedback(
        context: Context,
        message: String,
        rating: Int
    ): Boolean {
        val request = FeedbackRequest(
            app_version = com.balajitechlabs.quickdash.BuildConfig.VERSION_NAME,
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            android_version = "${Build.VERSION.SDK_INT}",
            message = message,
            rating = rating
        )
        return QuickDashApiClient.submitFeedback(request)
    }
}
