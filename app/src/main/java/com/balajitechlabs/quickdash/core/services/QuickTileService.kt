package com.balajitechlabs.quickdash.core.services

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.balajitechlabs.quickdash.core.data.SettingsRepository
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
    lateinit var settingsRepository: SettingsRepository

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        val context = applicationContext

        CoroutineScope(Dispatchers.Main).launch {
            val isEnabled = settingsRepository.bubbleEnabled.first()
            val newStatus = !isEnabled
            settingsRepository.setBubbleEnabled(newStatus)

            if (newStatus) {
                val intent = Intent(context, FloatingBubbleService::class.java)
                if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                tile.state = Tile.STATE_ACTIVE
            } else {
                val intent = Intent(context, FloatingBubbleService::class.java)
                context.stopService(intent)
                tile.state = Tile.STATE_INACTIVE
            }
            tile.updateTile()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val isEnabled = settingsRepository.bubbleEnabled.first()
            tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.updateTile()
        }
    }
}
