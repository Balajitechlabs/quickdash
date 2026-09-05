/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/services
 * File: QuickTileService.kt
 * Description: Primary Quick Settings tile service toggling the floating bubble overlay across any active app.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.services

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.balajitechlabs.quickdash.core.data.UserStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

import android.graphics.drawable.Icon
import com.balajitechlabs.quickdash.R

/**
 * Android Quick Settings Tile ("QuickDash Bubble").
 * Enables instant 1-tap toggling of the floating overlay bubble directly
 * from the notification / Control Center swipe-down shade.
 */
@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class QuickTileService : TileService() {

    @Inject
    lateinit var userStore: UserStore

    private var isBubbleActive = false

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return

        // 1. Check Overlay Permission synchronously
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please grant Overlay Permission first", Toast.LENGTH_SHORT).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = android.app.PendingIntent.getActivity(
                    this, 0, intent,
                    android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        // 2. Toggle state immediately and dispatch service synchronously within onClick()
        val newStatus = !isBubbleActive
        isBubbleActive = newStatus

        val serviceIntent = Intent(this, FloatingBubbleService::class.java).apply {
            setPackage(packageName)
        }

        if (newStatus) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                Toast.makeText(this, "Quick Bubble enabled", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("QuickTileService", "Failed to start FloatingBubbleService", e)
            }
            tile.state = Tile.STATE_ACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Active"
            }
        } else {
            try {
                stopService(serviceIntent)
                Toast.makeText(this, "Quick Bubble: Disabled", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("QuickTileService", "Failed to stop FloatingBubbleService", e)
            }
            tile.state = Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Off"
            }
        }
        tile.icon = Icon.createWithResource(this, R.drawable.ic_quickdash_tile)
        tile.updateTile()

        // 3. Persist state to UserStore DataStore asynchronously on IO
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userStore.setBubbleEnabled(newStatus)
            } catch (e: Exception) {
                Log.e("QuickTileService", "Failed to save bubble state", e)
            }
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        tile.icon = Icon.createWithResource(this, R.drawable.ic_quickdash_tile)
        tile.label = "Quick Bubble"

        CoroutineScope(Dispatchers.Main).launch {
            isBubbleActive = try {
                userStore.bubbleEnabled.first()
            } catch (_: Exception) {
                false
            }
            tile.state = if (isBubbleActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (isBubbleActive) "Active" else "Off"
            }
            tile.updateTile()
        }
    }
}
