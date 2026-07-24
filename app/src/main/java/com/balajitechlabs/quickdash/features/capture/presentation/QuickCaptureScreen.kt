package com.balajitechlabs.quickdash.features.capture.presentation

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.balajitechlabs.quickdash.features.capture.service.ScreenRecorderService
import kotlinx.coroutines.delay
import java.io.OutputStream
import java.util.Locale

enum class CaptureTab { RECORDER, ANNOTATOR }

data class LinePath(val path: List<Offset>, val color: Color, val strokeWidth: Float)

@Composable
fun QuickCaptureScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(CaptureTab.RECORDER) }

    // --- Screen Recorder State ---
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordAudio by remember { mutableStateOf(true) }
    var qualityRes by remember { mutableStateOf("1080p FHD") }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingStartAfterPermission by remember { mutableStateOf(false) }

    // --- Mic Permission Launcher ---
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

    // --- MediaProjection Launcher (screen capture consent) ---
    val projectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            // User granted screen capture — start the real recording service
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

    // Listen for stop broadcast from notification button
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

    // Launch MediaProjection consent after permissions are granted
    LaunchedEffect(pendingStartAfterPermission) {
        if (pendingStartAfterPermission) {
            pendingStartAfterPermission = false
            val pm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(pm.createScreenCaptureIntent())
        }
    }

    // Live recording timer
    LaunchedEffect(isRecording, isPaused) {
        if (isRecording && !isPaused) {
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    // --- Annotator State ---
    val paths = remember { mutableStateListOf<LinePath>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Color(0xFFFF3B30)) }
    var strokeWidth by remember { mutableFloatStateOf(8f) }
    var isEraser by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Tab Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Recorder tab with BETA badge
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { selectedTab = CaptureTab.RECORDER },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == CaptureTab.RECORDER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedTab == CaptureTab.RECORDER) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("🎥 Recorder", fontWeight = FontWeight.Bold) }
                // BETA badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-6).dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF9500))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("BETA", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 0.5.sp)
                }
            }

            Button(
                onClick = { selectedTab = CaptureTab.ANNOTATOR },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == CaptureTab.ANNOTATOR) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedTab == CaptureTab.ANNOTATOR) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) { Text("✏️ Annotator", fontWeight = FontWeight.Bold) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Crossfade(targetState = selectedTab, label = "CaptureTabTransition") { currentTab ->
            if (currentTab == CaptureTab.RECORDER) {
            // ── BETA Notice Card ──────────────────────────────────────────────────────
            Surface(
                color = Color(0xFF332200),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("🧪", fontSize = 20.sp)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFF9500))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) { Text("BETA", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) }
                            Text("Screen Recorder", fontWeight = FontWeight.Bold, color = Color(0xFFFFCC00), fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "This feature is in beta. Screen recording works on most devices but some may experience issues with audio sync or resolution. A fully refined version is coming in the next update.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFCC00).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ──────────────── Android 17-style floating capsule recorder ────────────────
            Surface(
                color = Color(0xFF1C1C1E),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing REC indicator pill (dynamic island style)
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.8f, targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f, targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlpha"
                    )

                    // Status Pill (Android 17 Dynamic Island style)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                when {
                                    isRecording && !isPaused -> Color(0xFFFF3B30).copy(alpha = 0.25f)
                                    isPaused -> Color(0xFFFF9500).copy(alpha = 0.25f)
                                    else -> Color(0xFF2C2C2E)
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    isRecording && !isPaused -> Color(0xFFFF3B30).copy(alpha = 0.6f)
                                    isPaused -> Color(0xFFFF9500).copy(alpha = 0.6f)
                                    else -> Color.White.copy(alpha = 0.1f)
                                },
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pulsing dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .scale(if (isRecording && !isPaused) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isRecording && !isPaused -> Color(0xFFFF3B30).copy(alpha = pulseAlpha)
                                        isPaused -> Color(0xFFFF9500)
                                        else -> Color(0xFF48484A)
                                    }
                                )
                        )

                        // Timer / Status text
                        val minutes = elapsedSeconds / 60
                        val seconds = elapsedSeconds % 60
                        Text(
                            text = when {
                                isRecording && !isPaused -> "REC  ${String.format(Locale.US, "%02d:%02d", minutes, seconds)}"
                                isPaused -> "PAUSED  ${String.format(Locale.US, "%02d:%02d", minutes, seconds)}"
                                else -> "READY"
                            },
                            color = when {
                                isRecording && !isPaused -> Color.White
                                isPaused -> Color(0xFFFF9500)
                                else -> Color(0xFF8E8E93)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Controls Pill (Floating capsule)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF2C2C2E))
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic Toggle
                        IconButton(
                            onClick = { recordAudio = !recordAudio },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (recordAudio) MaterialTheme.colorScheme.primaryContainer.copy(0.9f) else Color(0xFF3A3A3C))
                        ) {
                            Icon(
                                if (recordAudio) Icons.Default.Mic else Icons.Default.MicOff,
                                "Toggle Mic",
                                tint = if (recordAudio) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF636366)
                            )
                        }

                        // Main Record / Stop Button
                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    // Stop recording
                                    val stopIntent = Intent(context, ScreenRecorderService::class.java).apply {
                                        action = ScreenRecorderService.ACTION_STOP
                                    }
                                    context.startService(stopIntent)
                                    isRecording = false
                                    isPaused = false
                                    elapsedSeconds = 0
                                } else {
                                    // Check permissions then launch MediaProjection
                                    showPermissionDialog = true
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = if (isRecording)
                                            listOf(Color(0xFFFF453A), Color(0xFFBF0000))
                                        else
                                            listOf(Color(0xFF34C759), Color(0xFF248A3D))
                                    )
                                )
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                if (isRecording) "Stop Recording" else "Start Recording",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        // Pause / Resume Button
                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    isPaused = !isPaused
                                    // Note: MediaRecorder pause/resume requires API 24+
                                    Toast.makeText(
                                        context,
                                        if (isPaused) "Paused (timer frozen)" else "Resumed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = isRecording,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isPaused) Color(0xFFFF9500).copy(0.3f) else if (isRecording) Color(0xFF3A3A3C) else Color.Transparent)
                        ) {
                            Icon(
                                if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                "Pause/Resume",
                                tint = when {
                                    !isRecording -> Color(0xFF636366)
                                    isPaused -> Color(0xFFFF9500)
                                    else -> Color.White
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quality chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Resolution", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E8E93))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("720p HD", "1080p FHD", "4K Ultra").forEach { res ->
                                FilterChip(
                                    selected = qualityRes == res,
                                    onClick = { if (!isRecording) qualityRes = res },
                                    enabled = !isRecording,
                                    label = {
                                        Text(
                                            res, fontSize = 10.sp,
                                            color = if (qualityRes == res) Color.White else Color(0xFF8E8E93)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        containerColor = Color(0xFF2C2C2E)
                                    )
                                )
                            }
                        }
                    }

                    if (isRecording) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "📹 Screen is being recorded · $qualityRes",
                            color = Color(0xFF8E8E93), fontSize = 11.sp, letterSpacing = 0.5.sp
                        )
                        Text(
                            "Tap ⏹ to stop. Saved to Movies/QuickDash",
                            color = Color(0xFF636366), fontSize = 10.sp
                        )
                    }
                }
            }
        } else {
            // ──────────────── Annotator / Doodle Canvas ────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        // Color palette
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00),
                                Color(0xFF34C759), Color(0xFF007AFF), Color(0xFFAF52DE),
                                Color.White, Color.Black
                            ).forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(if (selectedColor == color && !isEraser) 28.dp else 24.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (selectedColor == color && !isEraser) 2.5.dp else 1.dp,
                                            color = if (selectedColor == color && !isEraser) MaterialTheme.colorScheme.primary else Color.Gray.copy(0.5f),
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = color; isEraser = false }
                                )
                            }
                        }

                        Row {
                            // Eraser
                            IconButton(
                                onClick = { isEraser = !isEraser },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isEraser) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent)
                            ) {
                                Icon(Icons.Default.Edit, "Eraser", tint = if (isEraser) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            // Undo
                            IconButton(onClick = { if (paths.isNotEmpty()) paths.removeAt(paths.lastIndex) }) {
                                Icon(Icons.AutoMirrored.Filled.Undo, "Undo")
                            }
                            // Clear
                            IconButton(onClick = { paths.clear() }) {
                                Icon(Icons.Default.Delete, "Clear all")
                            }
                        }
                    }

                    // Stroke width slider
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Stroke", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(42.dp))
                        Slider(
                            value = strokeWidth,
                            onValueChange = { strokeWidth = it },
                            valueRange = 3f..30f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Canvas Background Color Picker
                    var canvasBgColor by remember { mutableStateOf(Color.White) }
                    val bgColors = listOf(Color.White, Color(0xFFF5F5F7), Color(0xFFFFFDE7), Color(0xFF121212), Color(0xFF1E293B))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("Canvas Bg:", style = MaterialTheme.typography.labelSmall)
                        bgColors.forEach { color ->
                            val isSelected = canvasBgColor == color
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable { canvasBgColor = color }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Drawing Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(canvasBgColor)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), RoundedCornerShape(12.dp))
                            .pointerInput(canvasBgColor) {
                                detectDragGestures(
                                    onDragStart = { offset -> currentPath = listOf(offset) },
                                    onDrag = { change, _ -> currentPath = currentPath + change.position },
                                    onDragEnd = {
                                        if (currentPath.size >= 2) {
                                            val drawColor = if (isEraser) canvasBgColor else selectedColor
                                            val drawWidth = if (isEraser) strokeWidth * 3f else strokeWidth
                                            paths.add(LinePath(currentPath, drawColor, drawWidth))
                                        }
                                        currentPath = emptyList()
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            paths.forEach { lp -> drawLinePath(lp.path, lp.color, lp.strokeWidth) }
                            if (currentPath.size >= 2) {
                                val drawColor = if (isEraser) canvasBgColor else selectedColor
                                val drawWidth = if (isEraser) strokeWidth * 3f else strokeWidth
                                drawLinePath(currentPath, drawColor, drawWidth)
                            }
                        }

                        if (paths.isEmpty() && currentPath.isEmpty()) {
                            Text(
                                "Draw here…",
                                color = if (canvasBgColor == Color.White || canvasBgColor == Color(0xFFF5F5F7) || canvasBgColor == Color(0xFFFFFDE7)) Color(0xFF888888) else Color(0xFFAAAAAA),
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (paths.isEmpty()) Toast.makeText(context, "Canvas is empty! Draw something first.", Toast.LENGTH_SHORT).show()
                                else saveCanvasToGallery(context, paths, canvasBgColor)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Save Image", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                if (paths.isEmpty()) Toast.makeText(context, "Canvas is empty! Draw something first.", Toast.LENGTH_SHORT).show()
                                else saveCanvasToPdf(context, paths, canvasBgColor)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Save as PDF", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
    }

    // ──── Permission + Projection Dialog ────
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Start Screen Recording?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("QuickDash will:")
                    Text("• Capture everything on your screen")
                    if (recordAudio) Text("• Record audio from your microphone")
                    Text("• Save the recording to Movies/QuickDash in your gallery")
                    Spacer(Modifier.height(4.dp))
                    Text("Android will ask for screen capture permission on the next step.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    // Check and request mic permission first if needed
                    if (recordAudio) {
                        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        if (hasMic) {
                            // Directly launch MediaProjection consent
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
                        // No mic needed — go directly to screen capture
                        val pm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                        projectionLauncher.launch(pm.createScreenCaptureIntent())
                    }
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLinePath(
    points: List<Offset>, color: Color, strokeWidth: Float
) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            // Use quadratic bezier for smoother strokes
            val midX = (prev.x + curr.x) / 2f
            val midY = (prev.y + curr.y) / 2f
            quadraticTo(prev.x, prev.y, midX, midY)
        }
        lineTo(points.last().x, points.last().y)
    }
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun saveCanvasToGallery(context: Context, paths: List<LinePath>, bgColor: Color) {
    try {
        val width = 1200; val height = 900
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(bgColor.toArgb())

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (lp in paths) {
            paint.color = lp.color.toArgb()
            paint.strokeWidth = lp.strokeWidth * 2.5f
            val pts = lp.path
            if (pts.size >= 2) {
                val androidPath = android.graphics.Path()
                androidPath.moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) {
                    val prev = pts[i - 1]
                    val curr = pts[i]
                    val midX = (prev.x + curr.x) / 2f
                    val midY = (prev.y + curr.y) / 2f
                    androidPath.quadTo(prev.x, prev.y, midX, midY)
                }
                androidPath.lineTo(pts.last().x, pts.last().y)
                canvas.drawPath(androidPath, paint)
            }
        }

        val filename = "QuickDash_Annotate_${System.currentTimeMillis()}.png"
        val cv = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QuickDash")
            }
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { s: OutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, s)
            }
            Toast.makeText(context, "✅ Saved to Pictures/QuickDash!", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun saveCanvasToPdf(context: Context, paths: List<LinePath>, bgColor: Color) {
    try {
        val width = 1080
        val height = 1440
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(bgColor.toArgb())

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (lp in paths) {
            paint.color = lp.color.toArgb()
            paint.strokeWidth = lp.strokeWidth * 3f
            val pts = lp.path
            if (pts.size >= 2) {
                val androidPath = android.graphics.Path()
                androidPath.moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) {
                    val prev = pts[i - 1]
                    val curr = pts[i]
                    val midX = (prev.x + curr.x) / 2f
                    val midY = (prev.y + curr.y) / 2f
                    androidPath.quadTo(prev.x, prev.y, midX, midY)
                }
                androidPath.lineTo(pts.last().x, pts.last().y)
                canvas.drawPath(androidPath, paint)
            }
        }

        pdfDocument.finishPage(page)

        val filename = "QuickDash_Annotate_${System.currentTimeMillis()}.pdf"
        val cv = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/QuickDash")
            }
        }
        val targetUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
        val uri = context.contentResolver.insert(targetUri, cv)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { s: OutputStream ->
                pdfDocument.writeTo(s)
            }
            Toast.makeText(context, "✅ Saved PDF to Download/QuickDash!", Toast.LENGTH_SHORT).show()
        }
        pdfDocument.close()
    } catch (e: Exception) {
        Toast.makeText(context, "PDF export failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
