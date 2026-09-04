/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/services
 * File: QuickTileCategoryService.kt
 * Description: EssentialX-styled component for core/services supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.services

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

/**
 * 🧱 Dedicated Quick Settings Tile Categories (`QuickTileCategoryService.kt`).
 * Enables 1-tap launching of Quick Collect, Quick Password, or Quick Eyedropper directly from notification shade.
 */
@RequiresApi(Build.VERSION_CODES.N)
class QuickTileCategoryService : TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, com.balajitechlabs.quickdash.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        startActivityAndCollapse(pendingIntent)
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.let {
            it.state = Tile.STATE_ACTIVE
            it.updateTile()
        }
    }
}
