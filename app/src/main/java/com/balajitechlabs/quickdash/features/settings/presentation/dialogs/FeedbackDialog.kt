/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/dialogs
 * File: FeedbackDialog.kt
 * Description: In-app feedback form allowing users to submit bug reports and feature ideas.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.dialogs

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker
import kotlinx.coroutines.launch

private const val TAG = "FeedbackDialog"

fun captureScreenshot(activity: Activity, callback: (Bitmap?) -> Unit) {
    try {
        val window = activity.window
        val view = window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val locationOfViewInWindow = IntArray(2)
        view.getLocationInWindow(locationOfViewInWindow)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val handler = Handler(Looper.getMainLooper())
            PixelCopy.request(
                window,
                Rect(
                    locationOfViewInWindow[0],
                    locationOfViewInWindow[1],
                    locationOfViewInWindow[0] + view.width,
                    locationOfViewInWindow[1] + view.height
                ),
                bitmap,
                { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        callback(bitmap)
                    } else {
                        try {
                            val b = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                            val c = Canvas(b)
                            view.draw(c)
                            callback(b)
                        } catch (e: Exception) {
                            Log.e(TAG, "Canvas capture fallback failed", e)
                            callback(null)
                        }
                    }
                },
                handler
            )
        } else {
            val c = Canvas(bitmap)
            view.draw(c)
            callback(bitmap)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Screenshot capture failed", e)
        callback(null)
    }
}

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onLaunchGallery: () -> Unit,
    galleryBitmap: Bitmap?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var feedbackText by remember { mutableStateOf("") }
    var attachScreenshot by remember { mutableStateOf(false) }
    var screenshotBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val customShape = MaterialTheme.shapes.medium

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report a Bug", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Describe the issue...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (attachScreenshot) 90.dp else 130.dp),
                    shape = customShape,
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = if (attachScreenshot) 3 else 5
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = customShape,
                    color = if (attachScreenshot) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            attachScreenshot = !attachScreenshot
                            if (attachScreenshot) {
                                val activity = context as? Activity
                                if (activity != null) {
                                    captureScreenshot(activity) { bmp ->
                                        screenshotBitmap = bmp
                                    }
                                }
                            } else {
                                screenshotBitmap = null
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Attach screenshot",
                            tint = if (attachScreenshot) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            if (attachScreenshot) "Screenshot attached" else "Attach screenshot (optional)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (attachScreenshot) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                screenshotBitmap?.let { bmp ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Screenshot preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(customShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onLaunchGallery,
                    modifier = Modifier.fillMaxWidth(),
                    shape = customShape
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Gallery image", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (galleryBitmap != null) "Image attached from gallery" else "Upload from gallery",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (feedbackText.isNotBlank()) {
                    val capturedBitmap = screenshotBitmap ?: galleryBitmap
                    val capturedText = feedbackText
                    scope.launch {
                        try {
                            val safeFeedback = capturedText
                                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                            val message = "<b>Bug Report</b>\nModel: ${Build.MODEL}\nReport: $safeFeedback"
                            if (capturedBitmap != null) {
                                TelegramTracker.sendPhoto(
                                    caption = message.replace(Regex("<[^>]*>"), ""),
                                    bitmap = capturedBitmap
                                )
                            } else {
                                TelegramTracker.sendMessage(message)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to send bug report", e)
                        }
                    }
                }
                onDismiss()
            }) {
                Text(if (attachScreenshot) "Send with Screenshot" else "Send via Telegram")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
