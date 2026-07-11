package com.balajitechlabs.quickdash.features.broadcast.domain

import com.balajitechlabs.quickdash.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import android.graphics.Bitmap

object TelegramTracker {
    // Secrets are injected at compile time from local.properties via BuildConfig.
    // The token is NEVER stored in source code or committed to Git.
    private val BOT_TOKEN get() = BuildConfig.TG_BOT_TOKEN
    private val CHAT_ID   get() = BuildConfig.TG_CHAT_ID

    suspend fun sendMessage(message: String) = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.telegram.org/bot$BOT_TOKEN/sendMessage")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val jsonParam = JSONObject()
            jsonParam.put("chat_id", CHAT_ID)
            jsonParam.put("text", message)
            jsonParam.put("parse_mode", "HTML")

            val os = conn.outputStream
            val writer = OutputStreamWriter(os, "UTF-8")
            writer.write(jsonParam.toString())
            writer.flush()
            writer.close()
            os.close()

            val responseCode = conn.responseCode
            // We can read response here if needed, but since it's analytics, fire and forget is fine.
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun sendBroadcastBotMessage(message: String) = withContext(Dispatchers.IO) {
        try {
            val token = BuildConfig.TG_BROADCAST_BOT_TOKEN
            val url = URL("https://api.telegram.org/bot$token/sendMessage")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val jsonParam = JSONObject()
            jsonParam.put("chat_id", CHAT_ID)
            jsonParam.put("text", message)
            jsonParam.put("parse_mode", "HTML")

            val os = conn.outputStream
            val writer = OutputStreamWriter(os, "UTF-8")
            writer.write(jsonParam.toString())
            writer.flush()
            writer.close()
            os.close()

            conn.responseCode
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Sends a screenshot bitmap + caption to the Telegram bot via multipart/form-data.
     */
    suspend fun sendPhoto(caption: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        try {
            val boundary = "----QuickDashBoundary"
            val url = URL("https://api.telegram.org/bot$BOT_TOKEN/sendPhoto")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            conn.doOutput = true

            // Compress bitmap to JPEG bytes
            val bitmapBytes = ByteArrayOutputStream().also { bos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos)
            }.toByteArray()

            val dos = DataOutputStream(conn.outputStream)

            // -- chat_id field
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n")
            dos.writeBytes("$CHAT_ID\r\n")

            // -- caption field
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"caption\"\r\n\r\n")
            dos.writeBytes("$caption\r\n")

            // -- photo file
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"photo\"; filename=\"screenshot.jpg\"\r\n")
            dos.writeBytes("Content-Type: image/jpeg\r\n\r\n")
            dos.write(bitmapBytes)
            dos.writeBytes("\r\n")

            dos.writeBytes("--$boundary--\r\n")
            dos.flush()
            dos.close()

            conn.responseCode // consume response
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}