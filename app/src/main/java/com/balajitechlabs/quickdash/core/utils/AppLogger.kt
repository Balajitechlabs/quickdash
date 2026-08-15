package com.balajitechlabs.quickdash.core.utils

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)
    private const val MAX_LOGS = 200

    private fun sanitize(input: String): String {
        return input.replace('\r', '_').replace('\n', '_')
    }

    fun d(tag: String, message: String) {
        val safeTag = sanitize(tag)
        val safeMsg = sanitize(message)
        Log.d(safeTag, safeMsg)
        appendLog("DEBUG", safeTag, safeMsg)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val safeTag = sanitize(tag)
        val safeMsg = sanitize(message)
        Log.e(safeTag, safeMsg, throwable)
        val errorMsg = if (throwable != null) "$safeMsg\n${throwable.stackTraceToString()}" else safeMsg
        appendLog("ERROR", safeTag, errorMsg)
        
        try {
            if (throwable != null) {
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(throwable)
            } else {
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().log("[$safeTag] $safeMsg")
            }
        } catch (_: Exception) { /* Crashlytics disabled or not initialized */ }
    }
    
    fun i(tag: String, message: String) {
        val safeTag = sanitize(tag)
        val safeMsg = sanitize(message)
        Log.i(safeTag, safeMsg)
        appendLog("INFO", safeTag, safeMsg)
    }
    
    fun w(tag: String, message: String) {
        val safeTag = sanitize(tag)
        val safeMsg = sanitize(message)
        Log.w(safeTag, safeMsg)
        appendLog("WARN", safeTag, safeMsg)
    }

    private fun appendLog(level: String, tag: String, message: String) {
        val time = dateFormat.format(Date())
        val logLine = "$time [$level] $tag: $message"
        val currentList = _logs.value.toMutableList()
        currentList.add(0, logLine) // add to top
        if (currentList.size > MAX_LOGS) {
            currentList.removeAt(currentList.lastIndex)
        }
        _logs.value = currentList
    }

    fun clear() {
        _logs.value = emptyList()
    }
}