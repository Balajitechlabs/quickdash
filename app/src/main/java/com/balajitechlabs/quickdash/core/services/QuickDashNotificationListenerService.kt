/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: core/services
 * File: QuickDashNotificationListenerService.kt
 * Description: Notification listener service monitoring incoming alerts to provide quick reply and notification capture.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.balajitechlabs.quickdash.core.data.HistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@AndroidEntryPoint
class QuickDashNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var historyRepository: HistoryRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val historyMutex = Mutex()
    private val gson = Gson()
    private val listType = object : TypeToken<MutableList<Map<String, Any>>>() {}.type

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        // Exclude system UI or our own app
        if (packageName == "android" || packageName == applicationContext.packageName) {
            return
        }

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        val appName = try {
            val pm = packageManager
            val ai = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (_: Exception) {
            packageName
        }

        val timestamp = System.currentTimeMillis()

        serviceScope.launch {
            try {
                historyMutex.withLock {
                    val currentJson = historyRepository.notificationHistory.first()
                    val list: MutableList<Map<String, Any>> =
                        gson.fromJson(currentJson, listType) ?: mutableListOf()

                    val item = mapOf(
                        "appName" to appName,
                        "packageName" to packageName,
                        "title" to title,
                        "text" to text,
                        "timestamp" to timestamp
                    )
                    list.add(0, item)

                    // Keep last 50 notification logs
                    historyRepository.saveNotificationHistory(gson.toJson(list.take(50)))
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Log.e("QuickDash", "Failed to save notification history: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
