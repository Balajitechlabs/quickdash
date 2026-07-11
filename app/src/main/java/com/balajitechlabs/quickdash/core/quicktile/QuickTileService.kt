package com.balajitechlabs.quickdash.core.quicktile

import com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.balajitechlabs.quickdash.MainActivity
import com.balajitechlabs.quickdash.R
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import com.balajitechlabs.quickdash.core.data.UserStore

class QuickTileService : TileService() {
    @Suppress("DEPRECATION")
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        val userStore = UserStore(applicationContext)
        val style = runBlocking { userStore.launchStyle.first() }
        
        val intent = if (style == "FLOATING_DIALOG") {
            Intent(applicationContext, FloatingDialogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            }
        } else {
            Intent(applicationContext, com.balajitechlabs.quickdash.MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            startActivityAndCollapse(intent)
        }
    }

    override fun onStartListening() {
        super.onStartListening()

        qsTile.apply {
            state = Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = getString(R.string.quick_tile_subtitle)
            }
            updateTile()
        }
    }
}
