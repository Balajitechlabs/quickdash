package com.balajitechlabs.quickdash.core.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * ⚡ Side Bar Mini-Dock Service (`SideBarDockService.kt`).
 * Renders an edge-swiping side panel for 1-swipe tool launching over any screen.
 */
class SideBarDockService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
