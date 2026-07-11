package com.balajitechlabs.quickdash.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.utils.LogManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemLogsScreen(onDismiss: () -> Unit) {
    var logsText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    fun refreshLogs() {
        logsText = LogManager.readLogs()
    }

    LaunchedEffect(Unit) {
        refreshLogs()
        while (true) {
            delay(2000) // Auto-refresh every 2 seconds
            refreshLogs()
        }
    }

    // Scroll to bottom on load
    LaunchedEffect(logsText) {
        if (scrollState.maxValue > 0) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Logs") },
                actions = {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("System Logs", logsText)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        androidx.compose.material3.Text("Copy", color = androidx.compose.ui.graphics.Color.White, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = {
                        val sendIntent: android.content.Intent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, logsText)
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Share System Logs")
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { refreshLogs() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = {
                        LogManager.clearLogs()
                        refreshLogs()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black) // Terminal-like background
                .padding(8.dp)
        ) {
            Text(
                text = logsText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color.Green, // Terminal green
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            )
        }
    }
}
