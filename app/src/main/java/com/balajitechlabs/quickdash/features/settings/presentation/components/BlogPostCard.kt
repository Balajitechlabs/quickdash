/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/components
 * File: BlogPostCard.kt
 * Description: Card rendering developer updates, release notes, and community announcements.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BlogPostCard(
    post: Map<String, Any>,
    isPinned: Boolean,
    pollVotes: Map<String, String>,
    showImagePreviews: Boolean,
    tick: Int,
    context: Context,
    onDismiss: (String) -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onVote: (String, String, String) -> Unit,
    onSubmitAsk: (String, String, String) -> Unit,
    onLongPressPreview: (Map<String, Any>) -> Unit,
    onReleasePreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = post["title"] as? String ?: "Announcement"
    val body = post["body"] as? String ?: ""
    val ts = (post["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
    val postKey = "${ts}_${title}"

    val timeString = remember(ts, tick) {
        val diff = System.currentTimeMillis() - ts
        when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> "${diff / 86400000}d ago"
        }
    }

    @Suppress("DEPRECATION")
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDismiss(postKey)
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onTogglePin(postKey, isPinned)
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.secondaryContainer
                else -> Color.Transparent
            }
            val alignment = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.Center
            }
            val icon = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
                SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.PushPin
                else -> Icons.Filled.Delete
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                            MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(post) {
                        detectTapGestures(
                            onLongPress = {
                                @Suppress("DEPRECATION")
                                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator?.vibrate(50)
                                }
                                onLongPressPreview(post)
                            },
                            onPress = {
                                try {
                                    awaitRelease()
                                } finally {
                                    onReleasePreview()
                                }
                            }
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPinned) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .animateContentSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isPinned) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = "Pinned",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(end = 4.dp)
                                )
                            }
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    if (body.isNotEmpty()) {
                        val urlPattern = android.util.Patterns.WEB_URL
                        val matcher = remember(body) { urlPattern.matcher(body) }
                        val primaryColor = MaterialTheme.colorScheme.primary

                        val annotatedString = remember(body, primaryColor) {
                            buildAnnotatedString {
                                var lastIndex = 0
                                matcher.reset()
                                while (matcher.find()) {
                                    val start = matcher.start()
                                    val end = matcher.end()
                                    append(body.substring(lastIndex, start))
                                    pushStringAnnotation(tag = "URL", annotation = body.substring(start, end))
                                    withStyle(style = SpanStyle(color = primaryColor, textDecoration = TextDecoration.Underline)) {
                                        append(body.substring(start, end))
                                    }
                                    pop()
                                    lastIndex = end
                                }
                                append(body.substring(lastIndex))
                            }
                        }

                        @Suppress("DEPRECATION")
                        androidx.compose.foundation.text.ClickableText(
                            text = annotatedString,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            onClick = { offset ->
                                annotatedString.getStringAnnotations("URL", offset, offset)
                                    .firstOrNull()?.let { annotation ->
                                        var url = annotation.item
                                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                            url = "http://$url"
                                        }
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(intent)
                                    }
                            }
                        )
                    }

                    val type = post["type"] as? String
                    if (type == "poll") {
                        @Suppress("UNCHECKED_CAST")
                        val options = post["options"] as? List<String> ?: emptyList()
                        val myVote = pollVotes[postKey]

                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            options.forEach { option ->
                                val isSelected = myVote == option
                                val mockBaseVotes = (option.hashCode() % 50).let { if (it < 0) -it else it }
                                val displayVotes = if (isSelected) mockBaseVotes + 1 else mockBaseVotes
                                val totalVotes = options.sumOf { (it.hashCode() % 50).let { v -> if (v < 0) -v else v } } + (if (myVote != null) 1 else 0)
                                val percentage = if (totalVotes == 0) 0 else (displayVotes * 100) / totalVotes

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                        .clickable(enabled = myVote == null) {
                                            onVote(postKey, option, body)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    if (myVote != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(percentage / 100f)
                                                .fillMaxHeight()
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (myVote != null) {
                                            Text(
                                                text = "$percentage%",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (type == "ask") {
                        var responseText by remember(post) { mutableStateOf("") }
                        var isSubmitted by remember(post) { mutableStateOf(false) }

                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = responseText,
                                onValueChange = { if (!isSubmitted) responseText = it },
                                placeholder = { Text("Type your response here...", style = MaterialTheme.typography.bodyMedium) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isSubmitted,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (responseText.isNotBlank()) {
                                        isSubmitted = true
                                        onSubmitAsk(postKey, responseText, body)
                                    }
                                },
                                enabled = !isSubmitted && responseText.isNotBlank(),
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isSubmitted) "Submitted" else "Submit Response", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    val imageUrl = post["imageUrl"] as? String
                    if (!imageUrl.isNullOrBlank()) {
                        if (showImagePreviews) {
                            Spacer(modifier = Modifier.height(10.dp))
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Image attachment",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(Uri.parse(imageUrl), "image/*")
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Open Image"))
                                    },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(Uri.parse(imageUrl), "image/*")
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Open Image"))
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Image Attachment (Hold to preview)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    val videoUrl = post["videoUrl"] as? String
                    if (!videoUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(videoUrl), "video/*")
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Open Video"))
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Video",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Video Attachment",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    val documentUrl = post["documentUrl"] as? String
                    if (!documentUrl.isNullOrBlank()) {
                        val docMime = post["documentMimeType"] as? String ?: "*/*"
                        val docName = post["documentName"] as? String ?: "File Attachment"
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(documentUrl), docMime)
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Open File"))
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AttachFile,
                                contentDescription = "Document",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = docName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    )
}
