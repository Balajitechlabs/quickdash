package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogManager {
    private const val MAX_LOG_SIZE_BYTES = 1024 * 512 // 512 KB
    private var logFile: File? = null

    fun init(context: Context) {
        logFile = File(context.filesDir, "quickdash_system.log")
    }

    fun d(tag: String, message: String) {
        android.util.Log.d(tag, message)
        writeToFile("DEBUG", tag, message)
    }

    fun e(tag: String, message: String, exception: Throwable? = null) {
        android.util.Log.e(tag, message, exception)
        writeToFile("ERROR", tag, "$message\n${android.util.Log.getStackTraceString(exception)}")
    }

    private fun writeToFile(level: String, tag: String, message: String) {
        val file = logFile ?: return
        try {
            if (file.exists() && file.length() > MAX_LOG_SIZE_BYTES) {
                // Keep the last half of the log file by cutting it, or just delete and start fresh
                // For simplicity, we just delete and start over if it gets too large
                file.delete()
            }
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val logLine = "[$timestamp] $level/$tag: $message\n"
            file.appendText(logLine)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readLogs(): String {
        return try {
            if (logFile?.exists() == true) {
                logFile?.readText() ?: "No logs found."
            } else {
                "No logs found."
            }
        } catch (e: Exception) {
            "Error reading logs: ${e.message}"
        }
    }
    
    fun clearLogs() {
        try {
            if (logFile?.exists() == true) {
                logFile?.writeText("")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}