package com.balajitechlabs.quickdash.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.net.Uri
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.OpenInNew

import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.Intent
import com.balajitechlabs.quickdash.core.data.UserStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import com.balajitechlabs.quickdash.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.BorderStroke
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import java.io.File
import java.io.FileOutputStream
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentValues
import android.widget.Toast

fun saveImageToGallery(context: Context, imageUrl: String, coroutineScope: kotlinx.coroutines.CoroutineScope) {
    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val url = java.net.URL(imageUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            val bitmap = android.graphics.BitmapFactory.decodeStream(input)
            val filename = "QuickDash_${System.currentTimeMillis()}.jpg"
            var fos: java.io.OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                }
            } else {
                @Suppress("DEPRECATION")
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }
            if (fos != null) {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "Image saved to Gallery ✓", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BlogPostsScreen(userStore: UserStore) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gson = remember { Gson() }

    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(15000)
            tick++
        }
    }

    val rawPostsJson by userStore.firebaseBlogPosts.collectAsState(initial = "[]")
    val pollVotesJson by userStore.pollVotes.collectAsState(initial = "{}")
    val hiddenJson by userStore.hiddenNotifications.collectAsState(initial = "[]")
    val pinnedJson by userStore.pinnedNotifications.collectAsState(initial = "[]")

    val posts = remember(rawPostsJson) {
        try {
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            gson.fromJson<List<Map<String, Any>>>(rawPostsJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    var showClearFeedConfirmation by remember { mutableStateOf(false) }

    val pollVotes = remember(pollVotesJson) {
        try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson<Map<String, String>>(pollVotesJson, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    val hiddenSet = remember(hiddenJson) {
        try {
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson<Set<String>>(hiddenJson, type) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    val pinnedSet = remember(pinnedJson) {
        try {
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson<Set<String>>(pinnedJson, type) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    // Optimistic local state for instant visual removal on swipe
    var localHiddenSet by remember { mutableStateOf(setOf<String>()) }

    // Filter and sort: pinned first, then normal. Hide dismissed ones.
    val visiblePosts = remember(posts, hiddenSet, localHiddenSet, pinnedSet) {
        val combinedHidden = hiddenSet + localHiddenSet
        val filtered = posts.filter { post ->
            val ts = (post["timestamp"] as? Number)?.toLong() ?: 0L
            val key = "${ts}_${post["title"]}"
            !combinedHidden.contains(key)
        }
        filtered.sortedByDescending { post ->
            val ts = (post["timestamp"] as? Number)?.toLong() ?: 0L
            val key = "${ts}_${post["title"]}"
            if (pinnedSet.contains(key)) Long.MAX_VALUE else ts
        }
    }

    val showImagePreviews by userStore.showImagePreviews.collectAsState(initial = true)
    var previewPost by remember { mutableStateOf<Map<String, Any>?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Feed Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val oneTimeRequest = androidx.work.OneTimeWorkRequestBuilder<com.balajitechlabs.quickdash.features.broadcast.data.TelegramPollerWorker>().build()
                    androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
                        "telegram_poller_immediate",
                        androidx.work.ExistingWorkPolicy.REPLACE,
                        oneTimeRequest
                    )
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh notifications",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = {
                    showClearFeedConfirmation = true
                }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Feed",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (visiblePosts.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notifications found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = visiblePosts,
                    key = { post -> "${(post["timestamp"] as? Number)?.toLong() ?: 0L}_${post["title"]}" }
                ) { post ->
                    val title = post["title"] as? String ?: "Announcement"
                    val body = post["body"] as? String ?: ""
                    val ts = (post["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    val postKey = "${ts}_${title}"
                    val isPinned = pinnedSet.contains(postKey)
                    
                    val timeString = remember(ts, tick) {
                        val diff = System.currentTimeMillis() - ts
                        when {
                            diff < 60000 -> "Just now"
                            diff < 3600000 -> "${diff / 60000}m ago"
                            diff < 86400000 -> "${diff / 3600000}h ago"
                            else -> "${diff / 86400000}d ago"
                        }
                    }

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            when (dismissValue) {
                                SwipeToDismissBoxValue.EndToStart -> {
                                    // Immediately hide optimistically so item disappears without bounce
                                    localHiddenSet = localHiddenSet + postKey
                                    coroutineScope.launch {
                                        try {
                                            val updated = hiddenSet + postKey
                                            userStore.saveHiddenNotifications(gson.toJson(updated))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    true
                                }
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    coroutineScope.launch {
                                        try {
                                            val updated = if (isPinned) pinnedSet - postKey else pinnedSet + postKey
                                            userStore.savePinnedNotifications(gson.toJson(updated))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    false
                                }
                                else -> false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier,
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
                                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.PushPin
                                else -> Icons.Default.Delete
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
                                                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    vibrator?.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    vibrator?.vibrate(50)
                                                }
                                                previewPost = post
                                            },
                                            onPress = {
                                                try {
                                                    awaitRelease()
                                                } finally {
                                                    previewPost = null
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
                                Column(modifier = Modifier
                                    .padding(16.dp)
                                    .animateContentSize()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isPinned) {
                                                Icon(
                                                    imageVector = Icons.Default.PushPin,
                                                    contentDescription = "Pinned",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(16.dp).padding(end = 4.dp)
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
                                        val context = LocalContext.current
                                        val urlPattern = android.util.Patterns.WEB_URL
                                        val matcher = remember(body) { urlPattern.matcher(body) }
                                        
                                        val primaryColor = MaterialTheme.colorScheme.primary
                                        val annotatedString = remember(body, primaryColor) {
                                            androidx.compose.ui.text.buildAnnotatedString {
                                                var lastIndex = 0
                                                matcher.reset()
                                                while (matcher.find()) {
                                                    val start = matcher.start()
                                                    val end = matcher.end()
                                                    append(body.substring(lastIndex, start))
                                                    pushStringAnnotation(tag = "URL", annotation = body.substring(start, end))
                                                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = primaryColor, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) {
                                                        append(body.substring(start, end))
                                                    }
                                                    pop()
                                                    lastIndex = end
                                                }
                                                append(body.substring(lastIndex))
                                            }
                                        }
                                        
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
                                                // Local mock vote count logic for demonstration
                                                // Normally, you'd parse real vote counts from the bot payload here.
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
                                                            coroutineScope.launch {
                                                                val updatedVotes = pollVotes.toMutableMap()
                                                                updatedVotes[postKey] = option
                                                                userStore.savePollVote(gson.toJson(updatedVotes))
                                                                com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker.sendBroadcastBotMessage(
                                                                    "🗳 <b>New Poll Vote</b>\nQuestion: $body\nVote: $option"
                                                                )
                                                            }
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
                                                        coroutineScope.launch {
                                                            com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker.sendBroadcastBotMessage(
                                                                "💬 <b>User Data Response</b>\nQuestion: $body\nResponse: $responseText"
                                                            )
                                                        }
                                                    }
                                                },
                                                enabled = !isSubmitted && responseText.isNotBlank(),
                                                modifier = Modifier.align(Alignment.End),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(if (isSubmitted) "Submitted ✓" else "Submit Response", style = MaterialTheme.typography.labelMedium)
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
                                                    imageVector = Icons.Default.Image,
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
                                                imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
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
                                                imageVector = Icons.Default.AttachFile,
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
            }
        }

        if (hiddenSet.isNotEmpty() || localHiddenSet.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    localHiddenSet = emptySet()
                    coroutineScope.launch {
                        userStore.saveHiddenNotifications("[]")
                    }
                    Toast.makeText(context, "All notifications restored!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Restore Dismissed Notifications 🔄", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Real-time Notification Sync",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
        )
    }

    // Preview overlay
    previewPost?.let { post ->
        val title = post["title"] as? String ?: "Notification"
        val body = post["body"] as? String ?: ""
        val imageUrl = post["imageUrl"] as? String
        val videoUrl = post["videoUrl"] as? String

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .zIndex(9999f),
            contentAlignment = Alignment.Center
        ) {
            // Backdrop Blur Effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!videoUrl.isNullOrBlank()) {
                        AndroidView(
                            factory = { ctx ->
                                android.widget.VideoView(ctx).apply {
                                    setVideoPath(videoUrl)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        mp.setVolume(0f, 0f)
                                        start()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    } else if (!imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Preview Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    
                    if (body.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (!imageUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Check out this image from QuickDash: $imageUrl")
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Image"))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text("Share Link", style = MaterialTheme.typography.labelMedium)
                            }
                            
                            Button(
                                onClick = {
                                    saveImageToGallery(context, imageUrl, coroutineScope)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save to Gallery", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Release finger to dismiss",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    if (showClearFeedConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearFeedConfirmation = false },
            title = { Text("Clear Feed") },
            text = { Text("Are you sure you want to clear the feed and reset all cached messages, polls, and votes?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        userStore.saveFirebaseBlogPosts("[]")
                        userStore.saveHiddenNotifications("[]")
                        userStore.savePinnedNotifications("[]")
                        userStore.savePollVote("{}")
                    }
                    showClearFeedConfirmation = false
                }) {
                    Text("Clear Feed", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearFeedConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
