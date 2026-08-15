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

/**
 * ⚡ Android Quick Settings Tile ("QuickDash Bubble").
 * Enables instant 1-tap toggling of the floating overlay bubble directly
 * from the notification / Control Center swipe-down shade.
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
            if (!Settings.canDrawOverlays(context)) {
                Toast.makeText(context, "Please grant Overlay Permission first", Toast.LENGTH_SHORT).show()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startActivityAndCollapse(android.app.PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                    ))
                } else {
                    @Suppress("DEPRECATION")
                    startActivityAndCollapse(intent)
                }
                return@launch
            }

            val isEnabled = userStore.bubbleEnabled.first()
            val newStatus = !isEnabled
            userStore.setBubbleEnabled(newStatus)

            val serviceIntent = Intent(context, FloatingBubbleService::class.java).apply {
                setPackage(context.packageName)
            }

            if (newStatus) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    Toast.makeText(context, "Quick Bubble: Enabled ⚡", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.util.Log.w("QuickTileService", "Fallback startService", e)
                    try {
                        context.startService(serviceIntent)
                    } catch (e2: Exception) {
                        android.util.Log.e("QuickTileService", "Failed to start FloatingBubbleService", e2)
                    }
                }
                tile.state = Tile.STATE_ACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Active"
                }
            } else {
                try {
                    context.stopService(serviceIntent)
                    Toast.makeText(context, "Quick Bubble: Disabled", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.util.Log.e("QuickTileService", "Failed to stop FloatingBubbleService", e)
                }
                tile.state = Tile.STATE_INACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Off"
                }
            }
            tile.updateTile()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val isEnabled = userStore.bubbleEnabled.first()
            tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (isEnabled) "Active" else "Off"
            }
            tile.updateTile()
        }
    }
}
