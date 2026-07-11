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

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        appendLog("DEBUG", tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        val errorMsg = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
        appendLog("ERROR", tag, errorMsg)
    }
    
    fun i(tag: String, message: String) {
        Log.i(tag, message)
        appendLog("INFO", tag, message)
    }
    
    fun w(tag: String, message: String) {
        Log.w(tag, message)
        appendLog("WARN", tag, message)
    }

    private fun appendLog(level: String, tag: String, message: String) {
        val time = dateFormat.format(Date())
        val logLine = "$time [$level] $tag: $message"
        val currentList = _logs.value.toMutableList()
        currentList.add(0, logLine) // add to top
        if (currentList.size > MAX_LOGS) {
            currentList.removeLast()
        }
        _logs.value = currentList
    }

    fun clear() {
        _logs.value = emptyList()
    }
}