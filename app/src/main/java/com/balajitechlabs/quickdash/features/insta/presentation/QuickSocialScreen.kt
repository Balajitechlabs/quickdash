/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/insta/presentation
 * File: QuickSocialScreen.kt
 * Description: Utility screen facilitating rapid access to social profile links and link formatting.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.insta.presentation


import com.google.gson.JsonParser
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.data.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

// Cache model to avoid hitting GitHub API rates
data class GithubProfileCache(
    val login: String,
    val name: String,
    val avatarUrl: String,
    val bio: String,
    val followers: Int,
    val following: Int,
    val blogUrl: String,
    val githubUrl: String,
    val readmeContent: String,
    val followersList: List<Pair<String, String>> // list of Pair(login, avatarUrl)
)

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSocialScreen(
    mainViewModel: com.balajitechlabs.quickdash.MainViewModel,
    onDismiss: () -> Unit,
    socialViewModel: QuickSocialViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }

    // Available platforms
    val platforms = remember { listOf("Instagram", "Facebook", "X (Twitter)", "LinkedIn", "GitHub") }
    var selectedPlatform by remember { mutableStateOf("Instagram") }

    val githubState by androidx.lifecycle.compose.collectAsStateWithLifecycle(socialViewModel.githubState)
    val showGithubProfile = githubState.showProfile
    val githubLoading = githubState.isLoading
    val githubError = githubState.error
    val currentGithubProfile = githubState.profile

    val cleanedUsername = username.trim().removePrefix("@").trim()
    val isValid = cleanedUsername.isNotEmpty() && !cleanedUsername.contains(" ")

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Social Profile Deep-Linker",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Enter a username and target platform to search or profile directly.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Platform selector chips with minimalist icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            platforms.forEach { platform ->
                val isSelected = selectedPlatform == platform
                val iconRes = when (platform) {
                    "Instagram" -> R.drawable.ic_instagram
                    "Facebook" -> R.drawable.ic_facebook
                    "X (Twitter)" -> R.drawable.ic_twitter
                    "LinkedIn" -> R.drawable.ic_linkedin
                    "GitHub" -> R.drawable.ic_github
                    else -> R.drawable.ic_globe
                }
                val brandColor = when (platform) {
                    "Instagram" -> Color(0xFFE4405F)
                    "Facebook" -> Color(0xFF1877F2)
                    "X (Twitter)" -> Color(0xFF1DA1F2)
                    "LinkedIn" -> Color(0xFF0A66C2)
                    else -> MaterialTheme.colorScheme.primary
                }
                Card(
                    onClick = { selectedPlatform = platform; socialViewModel.clearError() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) brandColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) brandColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = platform,
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Profile Username", maxLines = 1) },
            placeholder = { Text("e.g. balajitechlabs") },
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.AlternateEmail,
                    contentDescription = "Username Prefix",
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (username.isNotEmpty()) {
                    IconButton(onClick = { username = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (githubError != null && selectedPlatform == "GitHub") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = githubError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Open Profile button
        Button(
            onClick = {
                if (isValid) {
                    if (selectedPlatform == "GitHub") {
                        socialViewModel.fetchGithubProfile(cleanedUsername)
                    } else {
                        // Handle native redirects
                        val (appUri, browserUri, pkg) = when (selectedPlatform) {
                            "Instagram" -> Triple(
                                Uri.parse("https://instagram.com/_u/$cleanedUsername"),
                                Uri.parse("https://instagram.com/$cleanedUsername"),
                                "com.instagram.android"
                            )
                            "Facebook" -> Triple(
                                Uri.parse("fb://facewebmodal/f?href=https://www.facebook.com/$cleanedUsername"),
                                Uri.parse("https://www.facebook.com/$cleanedUsername"),
                                "com.facebook.katana"
                            )
                            "X (Twitter)" -> Triple(
                                Uri.parse("twitter://user?screen_name=$cleanedUsername"),
                                Uri.parse("https://twitter.com/$cleanedUsername"),
                                "com.twitter.android"
                            )
                            "LinkedIn" -> Triple(
                                Uri.parse("linkedin://profile/$cleanedUsername"),
                                Uri.parse("https://www.linkedin.com/in/$cleanedUsername"),
                                "com.linkedin.android"
                            )
                            else -> Triple(null, null, null)
                        }

                        if (browserUri != null) {
                            val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
                                if (pkg != null) setPackage(pkg)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                            }
                        }
                        onDismiss()
                    }
                }
            },
            enabled = isValid && !githubLoading,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (githubLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(
                    if (selectedPlatform == "GitHub") "Scan Profile" else "Open Platform Profile",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // GitHub dialog pop-up overlay
    if (showGithubProfile && currentGithubProfile != null) {
        val profile = currentGithubProfile
        AlertDialog(
            onDismissRequest = { socialViewModel.dismissProfile() },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = profile.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { socialViewModel.dismissProfile() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Header card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = profile.avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Column {
                            Text(text = "@${profile.login}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(text = "${profile.followers} Followers • ${profile.following} Following", style = MaterialTheme.typography.bodySmall)
                            
                            if (profile.blogUrl.isNotBlank()) {
                                Row(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clickable {
                                            val blog = if (!profile.blogUrl.startsWith("http")) "https://${profile.blogUrl}" else profile.blogUrl
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(blog)))
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = profile.blogUrl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Bio Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Bio", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(profile.bio, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Followers list view
                    if (profile.followersList.isNotEmpty()) {
                        Column {
                            Text("Followers", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(profile.followersList) { (fLogin, fAvatar) ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(56.dp)
                                            .clickable {
                                                socialViewModel.fetchGithubProfile(fLogin)
                                            }
                                    ) {
                                        AsyncImage(
                                            model = fAvatar,
                                            contentDescription = fLogin,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = fLogin,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // README Preview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("README.md Preview", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            // Simple raw markdown cleaning to display formatted preview
                            val cleanedReadme = remember(profile.readmeContent) {
                                profile.readmeContent
                                    .replace(Regex("<[^>]*>"), "") // Remove HTML tags
                                    .replace(Regex("#+\\s+"), "") // Remove headers formatting
                                    .replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1") // Simplify links
                                    .replace(Regex("`"), "") // Remove inline code ticks
                                    .take(800) + (if (profile.readmeContent.length > 800) "\n\n...(more content available on GitHub)" else "")
                            }
                            Text(
                                text = cleanedReadme,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(profile.githubUrl)))
                    }
                ) {
                    @Suppress("DEPRECATION") Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Github Profile")
                }
            }
        )
    }
}
