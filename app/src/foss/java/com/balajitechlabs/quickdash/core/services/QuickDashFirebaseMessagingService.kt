package com.balajitechlabs.quickdash.core.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * 🌿 FOSS Edition No-Op Service.
 * Eliminates proprietary Firebase Messaging dependencies while keeping the Android manifest valid.
 */
class QuickDashFirebaseMessagingService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
