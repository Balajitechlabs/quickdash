package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DiagnosticLogger {
    private var isLoggingActive = false
    private var startTimestamp: Long = 0
    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun startLogging(context: Context) {
        if (isLoggingActive) return
        isLoggingActive = true
        startTimestamp = System.currentTimeMillis()

        // Set uncaught exception handler
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            saveCrashLog(context, throwable)
            defaultExceptionHandler?.uncaughtException(thread, throwable)
        }
    }

    fun stopLogging(context: Context): File? {
        if (!isLoggingActive) return null
        isLoggingActive = false
        // Restore default handler
        if (defaultExceptionHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(defaultExceptionHandler)
            defaultExceptionHandler = null
        }
        return generateLogFile(context, null)
    }

    fun isActive(): Boolean = isLoggingActive

    private fun captureLogcat(): List<String> {
        val logLines = mutableListOf<String>()
        try {
            val process = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "time"))
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    logLines.add(line)
                }
            }
        } catch (e: Exception) {
            logLines.add("Error capturing logcat: \${e.message}")
        }
        return logLines
    }

    private fun generateLogFile(context: Context, throwable: Throwable?): File? {
        try {
            val logLines = captureLogcat()
            val jsonLog = JSONObject()
            jsonLog.put("device_model", android.os.Build.MODEL)
            jsonLog.put("android_version", android.os.Build.VERSION.RELEASE)
            jsonLog.put("app_version", "2.2.9")
            jsonLog.put("timestamp", System.currentTimeMillis())
            
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            jsonLog.put("date", formattedDate)

            val linesArray = JSONArray()
            logLines.forEach { linesArray.put(it) }
            jsonLog.put("logs", linesArray)

            if (throwable != null) {
                val crashObj = JSONObject()
                crashObj.put("message", throwable.message)
                crashObj.put("stacktrace", Log.getStackTraceString(throwable))
                jsonLog.put("crash", crashObj)
            }

            val format = SimpleDateFormat("ddMMyyyy_HHmm", Locale.getDefault()).format(Date())
            val fileName = "QuickDash_log_${format}.json"
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(jsonLog.toString(2))
            return cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun saveCrashLog(context: Context, throwable: Throwable) {
        try {
            val file = generateLogFile(context, throwable)
            if (file != null) {
                val destFile = File(context.filesDir, "pending_crash_log.json")
                file.copyTo(destFile, overwrite = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPendingCrashLogFile(context: Context): File? {
        val file = File(context.filesDir, "pending_crash_log.json")
        if (file.exists()) {
            try {
                val format = SimpleDateFormat("ddMMyyyy_HHmm", Locale.getDefault()).format(Date())
                val timestampName = "QuickDash_log_${format}.json"
                val cacheFile = File(context.cacheDir, timestampName)
                file.copyTo(cacheFile, overwrite = true)
                file.delete()
                return cacheFile
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }
}
