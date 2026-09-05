/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/quicktile
 * File: QrScannerTileService.kt
 * Description: Quick Settings tile service launching the QuickDash QR scanner instantly from the notification shade.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.quicktile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.balajitechlabs.quickdash.MainActivity

import android.graphics.drawable.Icon
import com.balajitechlabs.quickdash.R

class QrScannerTileService : TileService() {
    @Suppress("DEPRECATION")
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            action = "com.balajitechlabs.quickdash.ACTION_SCAN_QR"
            putExtra("launch_section", "scan_qr")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                101,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            startActivityAndCollapse(intent)
        }
    }

    override fun onStartListening() {
        super.onStartListening()

        val tile = qsTile ?: return
        tile.icon = Icon.createWithResource(this, R.drawable.ic_qr_code_2)
        tile.state = Tile.STATE_INACTIVE
        tile.label = "Scan QR Code"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = "Inbuilt QR Scanner"
        }
        tile.updateTile()
    }
}
