/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/capture/presentation
 * File: QuickCaptureScreen.kt
 * Description: Screen recording and screenshot capture controls with resolution and audio settings.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.capture.presentation

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.balajitechlabs.quickdash.features.capture.presentation.components.AnnotatorCanvasSection
import com.balajitechlabs.quickdash.features.capture.presentation.components.ScreenRecorderSection
import com.balajitechlabs.quickdash.features.capture.presentation.dialogs.ScreenRecorderPermissionDialog
import com.balajitechlabs.quickdash.features.capture.service.ScreenRecorderService
import kotlinx.coroutines.delay

enum class CaptureTab { RECORDER, ANNOTATOR }

@Composable
fun QuickCaptureScreen(isFloating: Boolean = false) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(CaptureTab.RECORDER) }

    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordAudio by remember { mutableStateOf(true) }
    var qualityRes by remember { mutableStateOf("1080p FHD") }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingStartAfterPermission by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val micGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (micGranted || !recordAudio) {
            pendingStartAfterPermission = true
        } else {
            Toast.makeText(context, "Microphone permission denied. Disable audio toggle to record without mic.", Toast.LENGTH_LONG).show()
        }
    }

    val projectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val startIntent = Intent(context, ScreenRecorderService::class.java).apply {
                action = ScreenRecorderService.ACTION_START
                putExtra(ScreenRecorderService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenRecorderService.EXTRA_RESULT_DATA, result.data)
                putExtra(ScreenRecorderService.EXTRA_RECORD_AUDIO, recordAudio)
                putExtra(ScreenRecorderService.EXTRA_QUALITY, qualityRes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent)
            } else {
                context.startService(startIntent)
            }
            isRecording = true
            isPaused = false
            elapsedSeconds = 0
        } else {
            Toast.makeText(context, "Screen capture permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    ScreenRecorderService.BROADCAST_RECORDING_STOPPED -> {
                        isRecording = false
                        isPaused = false
                        elapsedSeconds = 0
                    }
                    ScreenRecorderService.BROADCAST_RECORDING_STARTED -> {
                        isRecording = true
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(ScreenRecorderService.BROADCAST_RECORDING_STOPPED)
            addAction(ScreenRecorderService.BROADCAST_RECORDING_STARTED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(pendingStartAfterPermission) {
        if (pendingStartAfterPermission) {
            pendingStartAfterPermission = false
            val pm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(pm.createScreenCaptureIntent())
        }
    }

    LaunchedEffect(isRecording, isPaused) {
        if (isRecording && !isPaused) {
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .then(if (isFloating) Modifier.fillMaxWidth().wrapContentHeight() else Modifier.fillMaxSize())
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { selectedTab = CaptureTab.RECORDER },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == CaptureTab.RECORDER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedTab == CaptureTab.RECORDER) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Screen Recorder", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { selectedTab = CaptureTab.ANNOTATOR },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == CaptureTab.ANNOTATOR) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedTab == CaptureTab.ANNOTATOR) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Annotator", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Crossfade(targetState = selectedTab, label = "CaptureTabTransition") { currentTab ->
            if (currentTab == CaptureTab.RECORDER) {
                Spacer(modifier = Modifier.height(12.dp))
                ScreenRecorderSection(
                    isRecording = isRecording,
                    isPaused = isPaused,
                    recordAudio = recordAudio,
                    qualityRes = qualityRes,
                    elapsedSeconds = elapsedSeconds,
                    onToggleAudio = { recordAudio = !recordAudio },
                    onToggleRecord = {
                        if (isRecording) {
                            val stopIntent = Intent(context, ScreenRecorderService::class.java).apply {
                                action = ScreenRecorderService.ACTION_STOP
                            }
                            context.startService(stopIntent)
                            isRecording = false
                            isPaused = false
                            elapsedSeconds = 0
                        } else {
                            showPermissionDialog = true
                        }
                    },
                    onTogglePause = {
                        if (isRecording) {
                            isPaused = !isPaused
                            Toast.makeText(
                                context,
                                if (isPaused) "Paused (timer frozen)" else "Resumed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onSelectQuality = { qualityRes = it }
                )
            } else {
                AnnotatorCanvasSection(context = context)
            }
        }
        Spacer(modifier = Modifier.height(120.dp))
    }

    if (showPermissionDialog) {
        ScreenRecorderPermissionDialog(
            recordAudio = recordAudio,
            onConfirm = {
                showPermissionDialog = false
                if (recordAudio) {
                    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    if (hasMic) {
                        val pm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                        projectionLauncher.launch(pm.createScreenCaptureIntent())
                    } else {
                        val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            perms.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        micPermissionLauncher.launch(perms.toTypedArray())
                    }
                } else {
                    val pm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    projectionLauncher.launch(pm.createScreenCaptureIntent())
                }
            },
            onDismissRequest = { showPermissionDialog = false }
        )
    }
}
