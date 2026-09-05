/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/services
 * File: ShakeDetectorService.kt
 * Description: Background sensor service monitoring accelerometer events to trigger the floating overlay upon device shake.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import android.util.Log

class ShakeDetectorService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private lateinit var userStore: UserStore

    private var shakeAcceleration = 10f
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var lastAcceleration = SensorManager.GRAVITY_EARTH

    private var lastShakeTime = 0L
    private var shakeCount = 0
    private var lastActionTime = 0L

    private var shakeMode = "DOUBLE"
    private var threshold = 14f
    private var vibrationMs = 25L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        userStore = UserStore(this)

        startForegroundNotification()

        serviceScope.launch {
            userStore.shakeMode.collect { mode ->
                shakeMode = mode
            }
        }

        serviceScope.launch {
            userStore.shakeSensitivity.collect { sens ->
                threshold = when (sens) {
                    "HIGH" -> 11f
                    "LOW" -> 18f
                    else -> 14f
                }
            }
        }

        serviceScope.launch {
            userStore.hapticDuration.collect { dur ->
                vibrationMs = dur.toLong().coerceIn(10L, 100L)
            }
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = currentAcceleration - lastAcceleration
        shakeAcceleration = shakeAcceleration * 0.9f + delta

        val now = System.currentTimeMillis()

        if (shakeAcceleration > threshold) {
            if (now - lastActionTime < 1500) return // Cooldown between activations

            if (shakeMode == "SINGLE") {
                lastActionTime = now
                triggerOpen()
            } else {
                // DOUBLE SHAKE
                if (now - lastShakeTime < 700) {
                    shakeCount++
                    if (shakeCount >= 2) {
                        lastActionTime = now
                        shakeCount = 0
                        lastShakeTime = 0
                        triggerOpen()
                    }
                } else {
                    shakeCount = 1
                    lastShakeTime = now
                }
            }
        }
    }

    private fun triggerOpen() {
        // Haptic feedback with user-configured intensity
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(vibrationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(vibrationMs)
                }
            }
        } catch (_: Exception) {}

        // Launch floating dialog activity over whatever app the user is currently using (e.g. WhatsApp)
        val intent = Intent(this, FloatingDialogActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {}
    }

    private fun startForegroundNotification() {
        val channelId = "quickdash_shake_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Shake to Open", NotificationManager.IMPORTANCE_MIN).apply {
                description = "Monitors device gestures to launch QuickDash on shake"
                setShowBadge(false)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Shake to Open Active")
            .setContentText("Shake phone to open QuickDash")
            .setSmallIcon(R.drawable.ic_quickdash_tile)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    startForeground(102, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                } catch (se: Exception) {
                    Log.e("ShakeDetectorService", "specialUse startForeground failed, falling back: ${se.message}")
                    startForeground(102, notification)
                }
            } else {
                startForeground(102, notification)
            }
        } catch (e: Exception) {
            Log.e("ShakeDetectorService", "startForeground failed: ${e.message}", e)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        sensorManager?.unregisterListener(this)
    }
}
