package com.balajitechlabs.quickdash.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.balajitechlabs.quickdash.MainActivity
import com.balajitechlabs.quickdash.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QuickDashFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")


        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "New Announcement"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val timestamp = System.currentTimeMillis()

        if (body.isNotBlank()) {
            sendNotification(title, body)
            
            // Persist the message locally in UserStore
            val userStore = com.balajitechlabs.quickdash.core.data.UserStore(applicationContext)
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val rawJson = userStore.firebaseBlogPosts.first()
                    val listType = object : com.google.gson.reflect.TypeToken<MutableList<Map<String, Any>>>() {}.type
                    val gson = com.google.gson.Gson()
                    val list: MutableList<Map<String, Any>> = gson.fromJson(rawJson, listType) ?: mutableListOf()
                    
                    val isDuplicate = list.any { 
                        (it["title"] as? String)?.trim() == title.trim() && 
                        (it["body"] as? String)?.trim() == body.trim() &&
                        Math.abs(System.currentTimeMillis() - ((it["timestamp"] as? Number)?.toLong() ?: 0L)) < 600000L
                    }
                    if (!isDuplicate) {
                        val newPost = mapOf(
                            "title" to title,
                            "body" to body,
                            "timestamp" to timestamp
                        )
                        list.add(0, newPost)
                        userStore.saveFirebaseBlogPosts(gson.toJson(list.take(30)))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        val userStore = com.balajitechlabs.quickdash.core.data.UserStore(applicationContext)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                userStore.saveFcmToken(token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // Implement this method to send token to your app server.
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "quickdash_fcm_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_quickdash_tile)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Push Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "QuickDashFCMService"
    }
}