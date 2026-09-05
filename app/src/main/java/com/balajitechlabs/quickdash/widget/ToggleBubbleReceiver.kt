/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: widget
 * File: ToggleBubbleReceiver.kt
 * Description: BroadcastReceiver handling widget taps to toggle the floating bubble service.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.services.FloatingBubbleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log

/**
 * BroadcastReceiver (`ToggleBubbleReceiver.kt`)
 * Enables instant 1-tap toggling of the QuickDash Floating Bubble directly
 * from Home Screen Glance Widgets and Quick Actions.
 */
class ToggleBubbleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                if (!Settings.canDrawOverlays(context)) {
                    Toast.makeText(context, "Enable Overlay Permission in QuickDash first", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val userStore = UserStore(context)
                val currentEnabled = userStore.bubbleEnabled.first()
                val nextState = !currentEnabled

                userStore.saveBubbleEnabled(nextState)

                val serviceIntent = Intent(context, FloatingBubbleService::class.java).apply {
                    setPackage(context.packageName)
                }

                if (nextState) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Toast.makeText(context, "Quick Bubble enabled", Toast.LENGTH_SHORT).show()
                } else {
                    context.stopService(serviceIntent)
                    Toast.makeText(context, "Quick Bubble: Disabled", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("QuickDash", "Error occurred: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
