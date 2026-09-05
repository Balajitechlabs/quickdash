/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/utils
 * File: CrashRecoveryHandler.kt
 * Description: UncaughtExceptionHandler providing graceful crash recovery, diagnostic logging, and restart options.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import java.io.File
import android.util.Log

object CrashRecoveryHandler {

    private const val CRASH_FLAG_FILE = "quickdash_last_crash.txt"

    fun init(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = File(context.filesDir, CRASH_FLAG_FILE)
                file.writeText("CRASH: ${throwable.localizedMessage}\n${throwable.stackTraceToString()}")
            } catch (e: Exception) {
                Log.e("QuickDash", "Error occurred: ${e.message}", e)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun hasPreviousCrash(context: Context): Boolean {
        val file = File(context.filesDir, CRASH_FLAG_FILE)
        return file.exists()
    }

    fun clearPreviousCrash(context: Context) {
        try {
            val file = File(context.filesDir, CRASH_FLAG_FILE)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            Log.e("QuickDash", "Error occurred: ${e.message}", e)
        }
    }
}
