/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/voicememos/presentation
 * File: QuickVoiceMemosScreen.kt
 * Description: Voice memo recorder tool with playback controls, waveforms, and file export.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.voicememos.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import com.balajitechlabs.quickdash.features.voicememos.service.VoiceRecorderService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class VoiceMemoItem(val file: File, val name: String, val formattedDate: String, val sizeKb: Long)

@Composable
fun QuickVoiceMemosScreen(isFloating: Boolean = false) {
    val context = LocalContext.current

    val isRecording by VoiceRecorderService.isRecording.collectAsStateWithLifecycle()
    val elapsedSeconds by VoiceRecorderService.recordingDurationSeconds.collectAsStateWithLifecycle()
    val lastSavedFile by VoiceRecorderService.lastSavedFile.collectAsStateWithLifecycle()

    // Playback state
    var playingFile by remember { mutableStateOf<File?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // List of saved memos
    var savedMemos by remember { mutableStateOf<List<VoiceMemoItem>>(emptyList()) }

    fun loadSavedMemos() {
        val dirs = listOfNotNull(
            File(context.filesDir, "VoiceMemos"),
            context.getExternalFilesDir(null)?.let { File(it, "VoiceMemos") }
        )
        val allFiles = dirs.flatMap { d ->
            if (d.exists()) d.listFiles { _, name -> name.endsWith(".m4a") }?.toList() ?: emptyList()
            else emptyList()
        }.sortedByDescending { it.lastModified() }

        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        savedMemos = allFiles.map { f ->
            VoiceMemoItem(
                file = f,
                name = f.nameWithoutExtension,
                formattedDate = dateFormat.format(Date(f.lastModified())),
                sizeKb = f.length() / 1024
            )
        }
    }

    LaunchedEffect(Unit) {
        loadSavedMemos()
    }

    LaunchedEffect(lastSavedFile) {
        if (lastSavedFile != null) {
            loadSavedMemos()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun startService() {
        val intent = Intent(context, VoiceRecorderService::class.java).apply {
            action = VoiceRecorderService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopService() {
        val intent = Intent(context, VoiceRecorderService::class.java).apply {
            action = VoiceRecorderService.ACTION_STOP
        }
        context.startService(intent)
        Toast.makeText(context, "Voice memo saved! ", Toast.LENGTH_SHORT).show()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startService()
        else Toast.makeText(context, "Microphone permission required to record audio", Toast.LENGTH_SHORT).show()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Voice Memos",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recording Studio Pod
        RoundedCardContainer(
            containerColor = Color(0xFF1E2024),
            cornerRadius = 24.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val mins = elapsedSeconds / 60
                val secs = elapsedSeconds % 60
                val timerText = String.format("%02d:%02d", mins, secs)

                Text(
                    text = if (isRecording) "RECORDING IN BACKGROUND" else "TAP TO RECORD",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isRecording) MaterialTheme.colorScheme.error else Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = timerText,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = if (isRecording) MaterialTheme.colorScheme.primary else Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Big Floating Action Button with Pulse
                Surface(
                    shape = CircleShape,
                    color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(76.dp)
                        .scale(if (isRecording) pulseScale else 1f)
                        .clickable {
                            if (isRecording) {
                                stopService()
                            } else {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    startService()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = if (isRecording) "Stop" else "Record",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Saved Memos Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Saved Recordings (${savedMemos.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (savedMemos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No voice memos yet.\nYour recorded memos will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(savedMemos, key = { it.file.absolutePath }) { memo ->
                    val isThisPlaying = playingFile?.absolutePath == memo.file.absolutePath && isPlaying

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isThisPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFF1E2024),
                        border = if (isThisPlaying) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Play/Pause button
                            Surface(
                                shape = CircleShape,
                                color = if (isThisPlaying) MaterialTheme.colorScheme.primary else Color(0xFF2A2B30),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable {
                                        if (isThisPlaying) {
                                            mediaPlayer?.pause()
                                            isPlaying = false
                                        } else {
                                            mediaPlayer?.release()
                                            mediaPlayer = MediaPlayer().apply {
                                                setDataSource(memo.file.absolutePath)
                                                prepare()
                                                start()
                                                setOnCompletionListener {
                                                    isPlaying = false
                                                    playingFile = null
                                                }
                                            }
                                            playingFile = memo.file
                                            isPlaying = true
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isThisPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = "Play",
                                        tint = if (isThisPlaying) Color.Black else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = memo.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${memo.formattedDate} • ${memo.sizeKb} KB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }

                            // Share Action
                            IconButton(
                                onClick = {
                                    try {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            memo.file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "audio/m4a"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Voice Memo"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot share file", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Share",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Delete Action
                            IconButton(
                                onClick = {
                                    if (playingFile?.absolutePath == memo.file.absolutePath) {
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                        playingFile = null
                                        isPlaying = false
                                    }
                                    memo.file.delete()
                                    loadSavedMemos()
                                    Toast.makeText(context, "Memo deleted", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
