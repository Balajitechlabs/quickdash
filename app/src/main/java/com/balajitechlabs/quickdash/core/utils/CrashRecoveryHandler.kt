package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import java.io.File

object CrashRecoveryHandler {

    private const val CRASH_FLAG_FILE = "quickdash_last_crash.txt"

    fun init(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = File(context.filesDir, CRASH_FLAG_FILE)
                file.writeText("CRASH: ${throwable.localizedMessage}\n${throwable.stackTraceToString()}")
            } catch (e: Exception) {
                e.printStackTrace()
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
            e.printStackTrace()
        }
    }
}
