/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/utils
 * File: AppLogger.kt
 * Description: Unified logging utility wrapping Android Logcat with debug-only emission and redaction.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object AppLogger {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val logLock = Any()
    private const val MAX_LOGS = 200
    private const val MAX_LOG_SIZE_BYTES = 1024 * 512 // 512 KB
    private var logFile: File? = null

    fun init(context: Context) {
        logFile = File(context.filesDir, "quickdash_system.log")
    }

    private fun sanitize(input: String): String {
        return input.replace('\r', '_').replace('\n', '_')
    }

    fun d(tag: String, message: String) {
        val safeTag = sanitize(tag)
        val safeMsg = sanitize(message)
        Log.d(safeTag, safeMsg)
        appendLog("DEBUG", safeTag, safeMsg)
        writeToFile("DEBUG", safeTag, safeMsg)
    }

    fun i(tag: String, message: String) {
        val safeTag = sanitize(tag)
        val safeMsg = sanitize(message)
        Log.i(safeTag, safeMsg)
        appendLog("INFO", safeTag, safeMsg)
        writeToFile("INFO", safeTag, safeMsg)
    }

    fun w(tag: String, message: String) {
        val safeTag = sanitize(tag)
        val safeMsg = sanitize(message)
        Log.w(safeTag, safeMsg)
        appendLog("WARN", safeTag, safeMsg)
        writeToFile("WARN", safeTag, safeMsg)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val safeTag = sanitize(tag)
        val safeMsg = sanitize(message)
        Log.e(safeTag, safeMsg, throwable)
        val errorMsg = if (throwable != null) "$safeMsg\n${throwable.stackTraceToString()}" else safeMsg
        appendLog("ERROR", safeTag, errorMsg)
        writeToFile("ERROR", safeTag, errorMsg)
    }

    private fun appendLog(level: String, tag: String, message: String) {
        synchronized(logLock) {
            val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)
            val time = dateFormat.format(Date())
            val logEntry = "[$time] [$level] [$tag]: $message"
            val currentList = _logs.value.toMutableList()
            if (currentList.size >= MAX_LOGS) {
                currentList.removeAt(0)
            }
            currentList.add(logEntry)
            _logs.value = currentList
        }
    }

    private fun writeToFile(level: String, tag: String, message: String) {
        val file = logFile ?: return
        try {
            if (file.exists() && file.length() > MAX_LOG_SIZE_BYTES) {
                file.delete()
            }
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val logLine = "[$timestamp] $level/$tag: $message\n"
            file.appendText(logLine)
        } catch (e: Exception) {
            Log.e("QuickDash", "Failed to write log to file: ${e.message}", e)
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

    fun getLogsAsText(): String {
        synchronized(logLock) {
            return _logs.value.joinToString("\n")
        }
    }

    fun clearLogs() {
        synchronized(logLock) {
            _logs.value = emptyList()
        }
        try {
            logFile?.delete()
        } catch (e: Exception) {
            Log.e("QuickDash", "Failed to clear log file: ${e.message}", e)
        }
    }

    fun getPendingCrashLogFile(context: Context): File? {
        val file = File(context.filesDir, "pending_crash_log.json")
        if (file.exists()) {
            try {
                val format = SimpleDateFormat("ddMMyyyy_HHmm", Locale.US).format(Date())
                val timestampName = "QuickDash_log_${format}.json"
                val cacheFile = File(context.cacheDir, timestampName)
                file.copyTo(cacheFile, overwrite = true)
                file.delete()
                return cacheFile
            } catch (e: Exception) {
                Log.e("QuickDash", "Failed to export pending crash log: ${e.message}", e)
            }
        }
        return null
    }

    fun saveCrashLog(context: Context, throwable: Throwable) {
        try {
            val file = File(context.filesDir, "pending_crash_log.json")
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val crashReport = buildString {
                appendLine("QuickDash Crash Report")
                appendLine("Timestamp: $format")
                appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})")
                appendLine("Exception: ${throwable.javaClass.name}: ${throwable.message}")
                appendLine("Stack Trace:")
                appendLine(throwable.stackTraceToString())
            }
            file.writeText(crashReport)
        } catch (e: Exception) {
            Log.e("QuickDash", "Failed to save crash log: ${e.message}", e)
        }
    }
}