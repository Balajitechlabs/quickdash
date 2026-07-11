package com.balajitechlabs.quickdash.features.broadcast.data

import com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker
import com.balajitechlabs.quickdash.core.utils.AppLogger

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.balajitechlabs.quickdash.MainActivity
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.BuildConfig
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class TelegramPollerWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userStore = UserStore(context)
        if (!userStore.isAnalyticsEnabled()) {
            return Result.success()
        }
        val lastUpdateId = userStore.lastTelegramUpdateId.first()
        val token = BuildConfig.TG_BROADCAST_BOT_TOKEN
        val chatId = BuildConfig.TG_CHAT_ID

        try {
            val offsetParam = if (lastUpdateId > 0) "?offset=${lastUpdateId + 1}" else ""
            val url = URL("https://api.telegram.org/bot$token/getUpdates$offsetParam")

            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    if (jsonResponse.getBoolean("ok")) {
                        val result = jsonResponse.getJSONArray("result")
                        var newLastUpdateId = lastUpdateId

                        for (i in 0 until result.length()) {
                            val update = result.getJSONObject(i)
                            val updateId = update.getLong("update_id")
                            newLastUpdateId = maxOf(newLastUpdateId, updateId)

                            if (!update.has("message")) continue

                            val message = update.getJSONObject("message")
                            val fromChatId = message.getJSONObject("chat").getString("id")

                            val hasText = message.has("text")
                            val hasPhoto = message.has("photo")
                            val hasCaption = message.has("caption")
                            val hasVideo = message.has("video")
                            val hasPoll = message.has("poll")
                            val hasDocument = message.has("document")

                            if (!(hasText || hasPhoto || hasCaption || hasVideo || hasPoll || hasDocument)) continue

                            // ── Native Telegram poll ──────────────────────────────────────
                            if (hasPoll) {
                                try {
                                    val pollObj = message.getJSONObject("poll")
                                    val question = pollObj.getString("question")
                                    val optionsArray = pollObj.getJSONArray("options")
                                    val optionsList = mutableListOf<String>()
                                    for (j in 0 until optionsArray.length()) {
                                        optionsList.add(optionsArray.getJSONObject(j).getString("text"))
                                    }

                                    val rawJson = userStore.firebaseBlogPosts.first()
                                    val listType = object : com.google.gson.reflect.TypeToken<MutableList<Map<String, Any>>>() {}.type
                                    val gson = com.google.gson.Gson()
                                    val list: MutableList<Map<String, Any>> = gson.fromJson(rawJson, listType) ?: mutableListOf()

                                    val isDuplicate = list.any {
                                        (it["body"] as? String)?.trim() == question.trim() &&
                                        Math.abs(System.currentTimeMillis() - ((it["timestamp"] as? Number)?.toLong() ?: 0L)) < 600000L
                                    }
                                    if (!isDuplicate) {
                                        val newPost = mapOf(
                                            "type" to "poll",
                                            "title" to "📊 Live Poll",
                                            "body" to question,
                                            "options" to optionsList,
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                        list.add(0, newPost)
                                        userStore.saveFirebaseBlogPosts(gson.toJson(list.take(30)))
                                        showNotification("📊 Live Poll", question)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                continue
                            }

                            // ── Text / photo / video messages ────────────────────────────
                            val text = when {
                                hasText -> message.getString("text")
                                hasCaption -> message.getString("caption")
                                else -> ""
                            }

                            val isAdminChat = fromChatId == chatId

                            if (isAdminChat) {
                                // ── Admin /poll command ──────────────────────────────────
                                if (text.startsWith("/poll ")) {
                                    val content = text.removePrefix("/poll ").trim()
                                    val parts = content.split("|").map { it.trim() }
                                    if (parts.size >= 2) {
                                        val question = parts[0]
                                        val options = parts.drop(1)
                                        try {
                                            val rawJson = userStore.firebaseBlogPosts.first()
                                            val listType = object : com.google.gson.reflect.TypeToken<MutableList<Map<String, Any>>>() {}.type
                                            val gson = com.google.gson.Gson()
                                            val list: MutableList<Map<String, Any>> = gson.fromJson(rawJson, listType) ?: mutableListOf()
                                            val newPost = mapOf(
                                                "type" to "poll",
                                                "title" to "📊 Live Poll",
                                                "body" to question,
                                                "options" to options,
                                                "timestamp" to System.currentTimeMillis()
                                            )
                                            list.add(0, newPost)
                                            userStore.saveFirebaseBlogPosts(gson.toJson(list.take(30)))
                                            showNotification("📊 Poll", question)
                                            TelegramTracker.sendMessage("✅ Poll broadcast sent")
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    continue
                                }

                                // ── Admin /ask command ───────────────────────────────────
                                if (text.startsWith("/ask ")) {
                                    val question = text.removePrefix("/ask ").trim()
                                    try {
                                        val rawJson = userStore.firebaseBlogPosts.first()
                                        val listType = object : com.google.gson.reflect.TypeToken<MutableList<Map<String, Any>>>() {}.type
                                        val gson = com.google.gson.Gson()
                                        val list: MutableList<Map<String, Any>> = gson.fromJson(rawJson, listType) ?: mutableListOf()
                                        val newPost = mapOf(
                                            "type" to "ask",
                                            "title" to "❓ Quick Question",
                                            "body" to question,
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                        list.add(0, newPost)
                                        userStore.saveFirebaseBlogPosts(gson.toJson(list.take(30)))
                                        showNotification("❓ Quick Question", question)
                                        TelegramTracker.sendMessage("✅ Question broadcasted: $question")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    continue
                                }

                                // ── Other admin commands ─────────────────────────────────
                                when (text.trim()) {
                                    "/ping" -> TelegramTracker.sendMessage("🏓 Pong! QuickDash is online on ${Build.MODEL}")
                                    "/stats" -> {
                                        val opens = userStore.totalAppOpens.first()
                                        val qrs = userStore.totalQrGenerated.first()
                                        val notes = userStore.totalNotesSaved.first()
                                        TelegramTracker.sendMessage("📊 <b>App Stats</b>\nOpens: $opens\nQRs: $qrs\nNotes: $notes")
                                    }
                                    "/lock" -> {
                                        userStore.setAppLocked(true)
                                        TelegramTracker.sendMessage("🔒 App locked remotely.")
                                    }
                                    "/unlock" -> {
                                        userStore.setAppLocked(false)
                                        TelegramTracker.sendMessage("🔓 App unlocked remotely.")
                                    }
                                    "/wipe_clipboard" -> {
                                        userStore.clearClipboardHistory()
                                        TelegramTracker.sendMessage("🧹 Clipboard history wiped remotely.")
                                    }
                                    "/clear_broadcast" -> {
                                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                        nm.cancel(1001)
                                        TelegramTracker.sendMessage("🗑 Active broadcast notification cleared remotely.")
                                    }
                                    "/active_broadcast" -> {
                                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                        val broadcastNotif = nm.activeNotifications.find { it.id == 1001 }
                                        if (broadcastNotif != null) {
                                            val t = broadcastNotif.notification.extras.getString(NotificationCompat.EXTRA_TITLE)
                                            val b = broadcastNotif.notification.extras.getString(NotificationCompat.EXTRA_TEXT)
                                            TelegramTracker.sendMessage("📢 <b>Active Broadcast on ${Build.MODEL}</b>\n$t\n$b")
                                        } else {
                                            TelegramTracker.sendMessage("ℹ️ No active broadcast currently on ${Build.MODEL}.")
                                        }
                                    }
                                    "/device_info" -> {
                                        val appVer = try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "N/A" } catch (e: Exception) { "N/A" }
                                        val freeMem = try {
                                            val mi = android.app.ActivityManager.MemoryInfo()
                                            (context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager).getMemoryInfo(mi)
                                            mi.availMem / (1024 * 1024)
                                        } catch (e: Exception) { -1L }
                                        TelegramTracker.sendMessage("""
                                            📱 <b>Device Info</b>
                                            <b>Brand:</b> ${Build.BRAND}
                                            <b>Model:</b> ${Build.MANUFACTURER} ${Build.MODEL}
                                            <b>Android:</b> ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
                                            <b>App Version:</b> $appVer
                                            <b>Free RAM:</b> ${freeMem}MB
                                        """.trimIndent())
                                    }
                                    "/app_version" -> {
                                        val vn = try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "N/A" } catch (e: Exception) { "N/A" }
                                        TelegramTracker.sendMessage("📦 <b>QuickDash v$vn</b> running on ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
                                    }
                                    "/force_poll" -> {
                                        userStore.setLastTelegramUpdateId(0L)
                                        TelegramTracker.sendMessage("🔄 Update cursor reset. Next poll will re-fetch recent messages.")
                                    }
                                    "/help" -> {
                                        TelegramTracker.sendMessage("""
                                            🤖 <b>QuickDash Admin Bot Commands</b>
                                            /ping - Check if device is online
                                            /stats - Get app usage stats
                                            /lock - Lock the app remotely
                                            /unlock - Unlock the app remotely
                                            /wipe_clipboard - Clear clipboard history
                                            /clear_broadcast - Clear active broadcast notification
                                            /active_broadcast - Show current broadcast notification
                                            /device_info - Get device info
                                            /app_version - Get app version
                                            /force_poll - Reset message cursor
                                            /poll [Question] | [Option A] | [Option B] - Broadcast a poll
                                            /ask [Question] - Broadcast a question
                                            (Just send any message) - Broadcast as notification
                                        """.trimIndent())
                                    }
                                    "/format" -> {
                                        TelegramTracker.sendMessage("""
                                            📝 <b>Notification Format</b>:
                                            Title: [Notification Title]
                                            Body: [Notification Body text]
                                            Image: [Image URL (Optional)]
                                        """.trimIndent())
                                    }
                                    else -> {
                                        // Not a slash command — treat as broadcast notification
                                        if (text.isNotBlank() || hasPhoto || hasVideo || hasDocument) {
                                            broadcastMessage(userStore, token, message, text, hasPhoto, hasVideo, hasDocument)
                                        }
                                    }
                                }
                            } else {
                                // Not from admin chat — treat any non-command message as a broadcast
                                if (!text.startsWith("/") && (text.isNotBlank() || hasPhoto || hasVideo || hasDocument)) {
                                    broadcastMessage(userStore, token, message, text, hasPhoto, hasVideo, hasDocument)
                                }
                            }
                        }

                        // Save the new lastUpdateId so we don't reprocess these messages
                        if (newLastUpdateId > lastUpdateId) {
                            userStore.setLastTelegramUpdateId(newLastUpdateId)
                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private suspend fun broadcastMessage(
        userStore: UserStore,
        token: String,
        message: JSONObject,
        text: String,
        hasPhoto: Boolean,
        hasVideo: Boolean,
        hasDocument: Boolean
    ) {
        var title = "📢 Notification"
        var displayBody = text.ifBlank { "Sent an attachment" }
        var imageUrl: String? = null
        var videoUrl: String? = null

        // Support structured format: Title: / Body: / Image:
        if (text.contains("Title:", ignoreCase = true) || text.contains("Body:", ignoreCase = true)) {
            val lines = text.split("\n")
            var tempTitle: String? = null
            var tempBody: String? = null
            var tempImage: String? = null
            for (line in lines) {
                when {
                    line.startsWith("Title:", ignoreCase = true) -> tempTitle = line.substring(6).trim()
                    line.startsWith("Body:", ignoreCase = true) -> tempBody = line.substring(5).trim()
                    line.startsWith("Image:", ignoreCase = true) -> tempImage = line.substring(6).trim()
                }
            }
            if (!tempTitle.isNullOrBlank()) title = "📢 $tempTitle"
            if (!tempBody.isNullOrBlank()) displayBody = tempBody
            if (!tempImage.isNullOrBlank()) imageUrl = tempImage
        }

        // Extract photo attachment
        if (hasPhoto && imageUrl == null) {
            try {
                val photoArray = message.getJSONArray("photo")
                if (photoArray.length() > 0) {
                    val largest = photoArray.getJSONObject(photoArray.length() - 1)
                    val fileId = largest.getString("file_id")
                    imageUrl = getTelegramFileUrl(token, fileId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Extract video attachment
        if (hasVideo) {
            try {
                val videoObj = message.getJSONObject("video")
                val fileId = videoObj.getString("file_id")
                videoUrl = getTelegramFileUrl(token, fileId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var documentUrl: String? = null
        var documentMimeType: String? = null
        var documentName: String? = null
        if (hasDocument) {
            try {
                val docObj = message.getJSONObject("document")
                val fileId = docObj.getString("file_id")
                documentUrl = getTelegramFileUrl(token, fileId)
                if (docObj.has("mime_type")) documentMimeType = docObj.getString("mime_type")
                if (docObj.has("file_name")) documentName = docObj.getString("file_name")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Show system notification
        showNotification(title, displayBody, imageUrl = imageUrl)

        // Save to notification feed
        try {
            val rawJson = userStore.firebaseBlogPosts.first()
            val listType = object : com.google.gson.reflect.TypeToken<MutableList<Map<String, Any>>>() {}.type
            val gson = com.google.gson.Gson()
            val list: MutableList<Map<String, Any>> = gson.fromJson(rawJson, listType) ?: mutableListOf()

            val isDuplicate = list.any {
                (it["title"] as? String)?.trim() == title.trim() &&
                (it["body"] as? String)?.trim() == displayBody.trim() &&
                Math.abs(System.currentTimeMillis() - ((it["timestamp"] as? Number)?.toLong() ?: 0L)) < 600000L
            }
            if (!isDuplicate) {
                val newPost = mutableMapOf<String, Any>(
                    "title" to title,
                    "body" to displayBody,
                    "timestamp" to System.currentTimeMillis()
                )
                if (imageUrl != null) newPost["imageUrl"] = imageUrl
                if (videoUrl != null) newPost["videoUrl"] = videoUrl
                if (documentUrl != null) {
                    newPost["documentUrl"] = documentUrl
                    if (documentMimeType != null) newPost["documentMimeType"] = documentMimeType
                    if (documentName != null) newPost["documentName"] = documentName
                }
                list.add(0, newPost)
                userStore.saveFirebaseBlogPosts(gson.toJson(list.take(30)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        AppLogger.i("Telegram", "Broadcast received: $displayBody")
    }

    private fun getTelegramFileUrl(token: String, fileId: String): String? {
        return try {
            val url = URL("https://api.telegram.org/bot$token/getFile?file_id=$fileId")
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    if (json.getBoolean("ok")) {
                        val filePath = json.getJSONObject("result").getString("file_path")
                        "https://api.telegram.org/file/bot$token/$filePath"
                    } else null
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showNotification(title: String, message: String, imageUrl: String? = null, isPoll: Boolean = false) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "quickdash_announcements"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Developer Announcements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Updates and broadcasts from the developer"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = "com.balajitechlabs.quickdash.ACTION_VIEW_NOTIFICATION"
            putExtra("title", title)
            putExtra("message", message)
            if (imageUrl != null) putExtra("imageUrl", imageUrl)
            putExtra("isPoll", isPoll)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_quickdash_tile)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(1001, builder.build())
    }
}
