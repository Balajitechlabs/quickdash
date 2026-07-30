package com.balajitechlabs.quickdash.features.broadcast.domain

import android.graphics.Bitmap
import android.util.Log
import com.balajitechlabs.quickdash.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object TelegramTracker {
    private const val TAG = "TelegramTracker"
    private const val TELEGRAM_API = "https://api.telegram.org/bot"

    private val BOT_TOKEN get() = BuildConfig.TG_BOT_TOKEN
    private val BROADCAST_TOKEN get() = BuildConfig.TG_BROADCAST_BOT_TOKEN
    private val CHAT_ID   get() = BuildConfig.TG_CHAT_ID

    private suspend fun sendToTelegram(token: String, message: String): Int = withContext(Dispatchers.IO) {
        try {
            val url = URL("$TELEGRAM_API$token/sendMessage")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val jsonParam = JSONObject()
            jsonParam.put("chat_id", CHAT_ID)
            jsonParam.put("text", message)
            jsonParam.put("parse_mode", "HTML")
            jsonParam.put("disable_web_page_preview", true)

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(jsonParam.toString())
                writer.flush()
            }

            val code = conn.responseCode
            if (code !in 200..299) {
                val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: "no body"
                Log.w(TAG, "Telegram API error $code: $errorBody")
            } else {
                Log.d(TAG, "Message sent (HTTP $code)")
            }
            conn.disconnect()
            code
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
            -1
        }
    }

    suspend fun sendMessage(message: String) {
        sendToTelegram(BOT_TOKEN, message)
    }

    suspend fun sendBroadcastBotMessage(message: String) {
        sendToTelegram(BROADCAST_TOKEN, message)
    }

    suspend fun sendPhoto(caption: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        try {
            val boundary = "----QuickDashBoundary"
            val url = URL("$TELEGRAM_API$BOT_TOKEN/sendPhoto")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            conn.doOutput = true
            conn.connectTimeout = 12000
            conn.readTimeout = 12000

            val bitmapBytes = ByteArrayOutputStream().also { bos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos)
            }.toByteArray()

            DataOutputStream(conn.outputStream).use { dos ->
                dos.writeBytes("--$boundary\r\n")
                dos.writeBytes("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n")
                dos.writeBytes("$CHAT_ID\r\n")

                dos.writeBytes("--$boundary\r\n")
                dos.writeBytes("Content-Disposition: form-data; name=\"caption\"\r\n\r\n")
                dos.writeBytes("$caption\r\n")

                dos.writeBytes("--$boundary\r\n")
                dos.writeBytes("Content-Disposition: form-data; name=\"photo\"; filename=\"screenshot.jpg\"\r\n")
                dos.writeBytes("Content-Type: image/jpeg\r\n\r\n")
                dos.write(bitmapBytes)
                dos.writeBytes("\r\n")

                dos.writeBytes("--$boundary--\r\n")
                dos.flush()
            }

            val code = conn.responseCode
            if (code !in 200..299) {
                val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: "no body"
                Log.w(TAG, "Telegram sendPhoto error $code: $errorBody")
            }
            conn.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send photo", e)
        }
    }
}