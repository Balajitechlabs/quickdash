/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/capture/service
 * File: ScreenRecorderService.kt
 * Description: Foreground service coordinating Android MediaProjection screen capture and video encoding.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.capture.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.balajitechlabs.quickdash.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Foreground service that performs real MediaProjection-based screen recording.
 * Start with ACTION_START + resultCode + resultData from MediaProjectionManager consent.
 * Stop with ACTION_STOP.
 */
class ScreenRecorderService : Service() {

    companion object {
        const val ACTION_START = "com.balajitechlabs.quickdash.SCREEN_RECORD_START"
        const val ACTION_STOP  = "com.balajitechlabs.quickdash.SCREEN_RECORD_STOP"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
        const val EXTRA_RECORD_AUDIO = "record_audio"
        const val EXTRA_QUALITY = "quality"

        const val CHANNEL_ID = "screen_recorder_channel"
        const val NOTIF_ID = 2002

        // Broadcast sent to UI to report state changes
        const val BROADCAST_RECORDING_STARTED = "com.balajitechlabs.quickdash.RECORDING_STARTED"
        const val BROADCAST_RECORDING_STOPPED = "com.balajitechlabs.quickdash.RECORDING_STOPPED"
        const val TAG = "ScreenRecorderService"
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var outputFilePath: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording(intent)
            ACTION_STOP  -> stopRecordingAndSave()
        }
        return START_NOT_STICKY
    }

    private fun startRecording(intent: Intent) {
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, -1)
        val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_RESULT_DATA)
        } ?: return

        val recordAudio = intent.getBooleanExtra(EXTRA_RECORD_AUDIO, true)
        val quality = intent.getStringExtra(EXTRA_QUALITY) ?: "1080p FHD"

        // Start as foreground immediately (required before projection starts)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIF_ID,
                buildRecordingNotification(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIF_ID, buildRecordingNotification())
        }

        // Get screen metrics
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        val (width, height, density) = resolveQuality(quality, metrics)

        // Set up output file in Movies/QuickDash
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "QuickDash_REC_$timestamp.mp4"

        // Configure MediaRecorder with robust audio fallback
        var actualRecordAudio = recordAudio && androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val cacheFile = File(cacheDir, filename)
        outputFilePath = cacheFile.absolutePath

        fun configureRecorder(withAudio: Boolean): Boolean {
            return try {
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(this@ScreenRecorderService)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                recorder.apply {
                    if (withAudio) setAudioSource(MediaRecorder.AudioSource.MIC)
                    setVideoSource(MediaRecorder.VideoSource.SURFACE)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    if (withAudio) {
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setAudioSamplingRate(44100)
                        setAudioEncodingBitRate(128000)
                    }
                    setVideoSize(width, height)
                    setVideoFrameRate(30)
                    setVideoEncodingBitRate(calculateBitrate(width, height))
                    setOutputFile(outputFilePath)
                    prepare()
                }
                mediaRecorder = recorder
                true
            } catch (e: Exception) {
                Log.e("QuickDash", "Error occurred: ${e.message}", e)
                false
            }
        }

        if (!configureRecorder(actualRecordAudio) && actualRecordAudio) {
            // Audio recording failed — retry without audio
            configureRecorder(false)
        }

        if (mediaRecorder == null) {
            Toast.makeText(this, "Failed to start screen recorder engine", Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }

        // Acquire MediaProjection
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { stopRecordingAndSave() }
        }, null)

        // Create VirtualDisplay fed into MediaRecorder
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "QuickDashRecorder",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder!!.surface,
            null, null
        )

        mediaRecorder!!.start()
        sendBroadcast(Intent(BROADCAST_RECORDING_STARTED).apply { `package` = packageName })
    }

    private fun stopRecordingAndSave() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop MediaRecorder", e)
        }

        mediaRecorder?.release()
        mediaRecorder = null
        virtualDisplay?.release()
        virtualDisplay = null
        mediaProjection?.stop()
        mediaProjection = null

        // Move file to MediaStore (visible in Gallery)
        outputFilePath?.let { path ->
            val file = File(path)
            if (file.exists() && file.length() > 0) {
                saveToMediaStore(file)
            }
        }

        sendBroadcast(Intent(BROADCAST_RECORDING_STOPPED).apply { `package` = packageName })
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun saveToMediaStore(file: File) {
        try {
            val cv = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/QuickDash")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cv)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { out ->
                    file.inputStream().copyTo(out)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cv.clear()
                    cv.put(MediaStore.Video.Media.IS_PENDING, 0)
                    contentResolver.update(it, cv, null, null)
                }
                file.delete() // Remove cache copy
                Toast.makeText(this, " Recording saved to Movies/QuickDash!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Recording saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    private fun resolveQuality(quality: String, metrics: DisplayMetrics): Triple<Int, Int, Int> {
        val density = metrics.densityDpi
        return when {
            quality.contains("4K") -> Triple(3840, 2160, density)
            quality.contains("1080") -> Triple(1920, 1080, density)
            quality.contains("720") -> Triple(1280, 720, density)
            else -> Triple(1920, 1080, density)
        }
    }

    private fun calculateBitrate(width: Int, height: Int): Int {
        return when {
            width >= 3840 -> 20_000_000 // 4K: 20 Mbps
            width >= 1920 -> 8_000_000  // 1080p: 8 Mbps
            else -> 4_000_000           // 720p: 4 Mbps
        }
    }

    private fun buildRecordingNotification(): Notification {
        val stopIntent = Intent(this, ScreenRecorderService::class.java).apply { action = ACTION_STOP }
        val stopPi = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(" Recording Screen")
            .setContentText("Tap to stop recording")
            .setSmallIcon(R.drawable.ic_quickdash_tile)
            .addAction(0, " Stop", stopPi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Screen Recorder",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active screen recording notification"
                setShowBadge(false)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
                ?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecordingAndSave()
    }
}
