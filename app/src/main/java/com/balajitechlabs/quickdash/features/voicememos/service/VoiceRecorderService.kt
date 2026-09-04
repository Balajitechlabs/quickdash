/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/voicememos
 * File: VoiceRecorderService.kt
 * Description: EssentialX-styled component for features/voicememos supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.voicememos.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.balajitechlabs.quickdash.MainActivity
import com.balajitechlabs.quickdash.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VoiceRecorderService : Service() {

    companion object {
        const val CHANNEL_ID = "voice_recorder_channel"
        const val NOTIFICATION_ID = 2026
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        private val _isRecording = MutableStateFlow(false)
        val isRecording: StateFlow<Boolean> = _isRecording

        private val _recordingDurationSeconds = MutableStateFlow(0)
        val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds

        private val _lastSavedFile = MutableStateFlow<String?>(null)
        val lastSavedFile: StateFlow<String?> = _lastSavedFile
    }

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        if (_isRecording.value) return

        try {
            val dir = File(getExternalFilesDir(null) ?: filesDir, "VoiceMemos")
            if (!dir.exists()) dir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(dir, "VoiceMemo_$timestamp.m4a")
            currentOutputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            _isRecording.value = true
            _recordingDurationSeconds.value = 0

            startForeground(NOTIFICATION_ID, buildNotification(0))

            timerJob?.cancel()
            timerJob = serviceScope.launch {
                while (_isRecording.value) {
                    delay(1000)
                    _recordingDurationSeconds.value += 1
                    updateNotification(_recordingDurationSeconds.value)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("QuickDash", "Error occurred: ${e.message}", e)
            stopRecording()
        }
    }

    private fun stopRecording() {
        timerJob?.cancel()
        timerJob = null

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            android.util.Log.e("QuickDash", "Error occurred: ${e.message}", e)
        } finally {
            mediaRecorder = null
            _isRecording.value = false
            currentOutputFile?.let {
                if (it.exists() && it.length() > 0) {
                    _lastSavedFile.value = it.absolutePath
                }
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Recorder",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing voice memo recording"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(seconds: Int): Notification {
        val stopIntent = Intent(this, VoiceRecorderService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appIntent = Intent(this, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mins = seconds / 60
        val secs = seconds % 60
        val timeString = String.format("%02d:%02d", mins, secs)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("Recording Voice Memo ($timeString)")
            .setContentText("Tap to return to QuickDash or stop below")
            .setContentIntent(appPendingIntent)
            .addAction(R.drawable.ic_delete, "Stop & Save", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(seconds: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(seconds))
    }

    override fun onDestroy() {
        stopRecording()
        serviceScope.cancel()
        super.onDestroy()
    }
}
