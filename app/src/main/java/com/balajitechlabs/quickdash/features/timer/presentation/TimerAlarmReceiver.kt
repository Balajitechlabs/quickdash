/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/timer/presentation
 * File: TimerAlarmReceiver.kt
 * Description: BroadcastReceiver handling timer expiration alarms and posting system notifications.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.timer.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.balajitechlabs.quickdash.MainActivity
import android.util.Log

class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channelId = "quickdash_timer_channel"
        val notificationId = 1001

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Timer Completion",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when your countdown timer completes"
                enableVibration(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Timer Finished!"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Your QuickDash countdown timer has completed."

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(alarmUri)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        notificationManager?.notify(notificationId, notification)

        try {
            val ringtone = RingtoneManager.getRingtone(context, alarmUri)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("QuickDash", "Error occurred: ${e.message}", e)
        }
    }
}
