package com.balajitechlabs.quickdash.features.voicememos.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VoiceMemoItem(val file: File, val name: String, val formattedDate: String, val sizeKb: Long)

/**
 * 🎙️ Tool #18 — Quick Voice Memos Recorder (`QuickVoiceMemosScreen.kt`).
 * Production-ready floating audio voice recorder with real MediaRecorder & MediaPlayer.
 */
@Composable
fun QuickVoiceMemosScreen(isFloating: Boolean = false) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var activeRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentOutputFile by remember { mutableStateOf<File?>(null) }
    
    // Playback state
    var playingFile by remember { mutableStateOf<File?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    
    // List of saved memos
    var savedMemos by remember { mutableStateOf<List<VoiceMemoItem>>(emptyList()) }

    fun getVoiceDir(): File {
        val dir = File(context.filesDir, "VoiceMemos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun loadSavedMemos() {
        val dir = getVoiceDir()
        val files = dir.listFiles { _, name -> name.endsWith(".m4a") }?.sortedByDescending { it.lastModified() } ?: emptyList()
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        savedMemos = files.map { f ->
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

    // Live recording timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            elapsedSeconds = 0
            while (isRecording) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    fun stopAudioRecording() {
        try {
            activeRecorder?.apply {
                stop()
                release()
            }
            activeRecorder = null
            isRecording = false
            Toast.makeText(context, "Voice memo saved! 💾", Toast.LENGTH_SHORT).show()
            loadSavedMemos()
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
        }
    }

    fun startAudioRecording() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getVoiceDir(), "Memo_$timeStamp.m4a")
            currentOutputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            activeRecorder = recorder
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to start recording: ${e.message}", Toast.LENGTH_LONG).show()
            isRecording = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startAudioRecording()
        } else {
            Toast.makeText(context, "Microphone permission required to record audio", Toast.LENGTH_SHORT).show()
        }
    }

    fun togglePlayMemo(item: VoiceMemoItem) {
        if (playingFile == item.file) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            playingFile = null
        } else {
            mediaPlayer?.release()
            try {
                val mp = MediaPlayer().apply {
                    setDataSource(item.file.absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        playingFile = null
                        mediaPlayer = null
                    }
                }
                mediaPlayer = mp
                playingFile = item.file
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to play audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteMemo(item: VoiceMemoItem) {
        if (playingFile == item.file) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            playingFile = null
        }
        item.file.delete()
        loadSavedMemos()
        Toast.makeText(context, "Voice memo deleted", Toast.LENGTH_SHORT).show()
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                activeRecorder?.stop()
                activeRecorder?.release()
            } catch (_: Exception) {}
            mediaPlayer?.release()
        }
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val formattedTimer = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .then(if (isFloating) Modifier.fillMaxWidth().wrapContentHeight() else Modifier.fillMaxSize())
            .animateContentSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "🎙️ Quick Voice Memos",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (isRecording) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = null,
                tint = if (isRecording) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = if (isRecording) "Recording... $formattedTimer" else "Tap below to start recording",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = {
                if (isRecording) {
                    stopAudioRecording()
                } else {
                    val hasMicPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    if (hasMicPerm) {
                        startAudioRecording()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (isRecording) "STOP RECORDING ($formattedTimer)" else "START RECORDING 🎙️",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
        }

        RoundedCardContainer {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Voice Notes (${savedMemos.size})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (savedMemos.isEmpty()) {
                    Text(
                        text = "No saved voice notes yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    savedMemos.forEach { item ->
                        val isPlaying = playingFile == item.file
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${item.formattedDate} · ${item.sizeKb} KB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { togglePlayMemo(item) }, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                            contentDescription = "Play/Stop",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { deleteMemo(item) }, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
