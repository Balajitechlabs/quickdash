package com.balajitechlabs.quickdash.core.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import com.balajitechlabs.quickdash.core.data.UserStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class QuickDashNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var historyRepository: com.balajitechlabs.quickdash.core.data.HistoryRepository

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
        } catch (e: Exception) {
            packageName
        }

        val timestamp = System.currentTimeMillis()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentJson = historyRepository.notificationHistory.first()

                val gson = Gson()
                val listType = object : TypeToken<MutableList<Map<String, Any>>>() {}.type
                val list: MutableList<Map<String, Any>> = gson.fromJson(currentJson, listType) ?: mutableListOf()

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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
