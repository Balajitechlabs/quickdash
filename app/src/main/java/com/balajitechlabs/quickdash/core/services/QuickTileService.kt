package com.balajitechlabs.quickdash.core.services

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.balajitechlabs.quickdash.core.data.UserStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Android Quick Settings Tile ("QuickDash Hub").
 * Allows users to toggle the floating bubble directly from the notification swipe-down shade.
 */
@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class QuickTileService : TileService() {

    @Inject
    lateinit var userStore: UserStore

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        val context = applicationContext

        CoroutineScope(Dispatchers.Main).launch {
            val isEnabled = userStore.bubbleEnabled.first()
            val newStatus = !isEnabled
            userStore.setBubbleEnabled(newStatus)

            if (newStatus) {
                val intent = Intent(context, FloatingBubbleService::class.java)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch (e: Exception) {
                    android.util.Log.w("QuickTileService", "Could not start foreground service from tile, trying startService fallback", e)
                    try {
                        context.startService(intent)
                    } catch (e2: Exception) {
                        android.util.Log.e("QuickTileService", "Failed to start FloatingBubbleService from tile", e2)
                    }
                }
                tile.state = Tile.STATE_ACTIVE
            } else {
                val intent = Intent(context, FloatingBubbleService::class.java)
                try {
                    context.stopService(intent)
                } catch (e: Exception) {
                    android.util.Log.e("QuickTileService", "Failed to stop FloatingBubbleService", e)
                }
                tile.state = Tile.STATE_INACTIVE
            }
            tile.updateTile()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val isEnabled = userStore.bubbleEnabled.first()
            tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.updateTile()
        }
    }
}
