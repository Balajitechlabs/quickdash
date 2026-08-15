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
            val clazz = Class.forName("com.google.firebase.crashlytics.FirebaseCrashlytics")
            val getInstance = clazz.getMethod("getInstance")
            val instance = getInstance.invoke(null)
            if (throwable != null) {
                val recordException = clazz.getMethod("recordException", Throwable::class.java)
                recordException.invoke(instance, throwable)
            } else {
                val logMethod = clazz.getMethod("log", String::class.java)
                logMethod.invoke(instance, "[$safeTag] $safeMsg")
            }
        } catch (_: Throwable) { /* Crashlytics not present in FOSS build or disabled */ }
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
        val logEntry = "[$time] [$level] [$tag]: $message"
        val currentList = _logs.value.toMutableList()
        if (currentList.size >= MAX_LOGS) {
            currentList.removeAt(0)
        }
        currentList.add(logEntry)
        _logs.value = currentList
    }

    fun getLogsAsText(): String {
        return _logs.value.joinToString("\n")
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}