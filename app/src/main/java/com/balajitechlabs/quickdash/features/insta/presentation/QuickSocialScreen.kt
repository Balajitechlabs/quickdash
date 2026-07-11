package com.balajitechlabs.quickdash.features.insta.presentation

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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSocialScreen(
    userStore: UserStore,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    
    // Available platforms
    val platforms = listOf("Instagram", "Facebook", "X (Twitter)", "LinkedIn", "GitHub")
    var selectedPlatform by remember { mutableStateOf("Instagram") }
    
    // GitHub API states
    var showGithubProfile by remember { mutableStateOf(false) }
    var githubLoading by remember { mutableStateOf(false) }
    var githubError by remember { mutableStateOf<String?>(null) }
    var currentGithubProfile by remember { mutableStateOf<GithubProfileCache?>(null) }
    
    // In-memory cache for recursively loaded follower profiles
    val profileCache = remember { mutableMapOf<String, GithubProfileCache>() }

    val cleanedUsername = username.trim().removePrefix("@").trim()
    val isValid = cleanedUsername.isNotEmpty() && !cleanedUsername.contains(" ")

    // Fetch GitHub details helper
    fun fetchGithubProfile(targetUsername: String) {
        githubLoading = true
        githubError = null
        
        // Check cache first
        if (profileCache.containsKey(targetUsername.lowercase())) {
            currentGithubProfile = profileCache[targetUsername.lowercase()]
            githubLoading = false
            showGithubProfile = true
            return
        }

        scope.launch {
            try {
                // Run network operations in IO thread and get result
                val result = withContext(Dispatchers.IO) {
                    try {
                        val token = userStore.githubAccessToken.first()
                        
                        // 1. Fetch user general details
                        val userUrl = URL("https://api.github.com/users/$targetUsername")
                        val userConn = userUrl.openConnection() as HttpURLConnection
                        userConn.requestMethod = "GET"
                        userConn.connectTimeout = 5000
                        userConn.readTimeout = 5000
                        if (token.isNotBlank()) {
                            userConn.setRequestProperty("Authorization", "token $token")
                        }
                        
                        val responseCode = userConn.responseCode
                        if (responseCode == 403) {
                            return@withContext Result.failure<GithubProfileCache>(Exception("RATE_LIMIT"))
                        } else if (responseCode == 404) {
                            return@withContext Result.failure<GithubProfileCache>(Exception("NOT_FOUND"))
                        } else if (responseCode != 200) {
                            return@withContext Result.failure<GithubProfileCache>(Exception("ERROR_$responseCode"))
                        }
                        
                        val userJsonStr = userConn.inputStream.bufferedReader().use { it.readText() }
                        val userObj = JsonParser.parseString(userJsonStr).asJsonObject
                        
                        val login = userObj.get("login")?.asString ?: targetUsername
                        val name = userObj.get("name")?.asString ?: login
                        val avatarUrl = userObj.get("avatar_url")?.asString ?: ""
                        val bio = userObj.get("bio")?.asString ?: "No bio description."
                        val followers = userObj.get("followers")?.asInt ?: 0
                        val following = userObj.get("following")?.asInt ?: 0
                        val blogUrl = userObj.get("blog")?.asString ?: ""
                        val githubUrl = userObj.get("html_url")?.asString ?: "https://github.com/$login"
                        
                        // 2. Fetch README markdown preview
                        var readmeContent = "No profile README found."
                        val readmeUrls = listOf(
                            URL("https://raw.githubusercontent.com/$login/$login/main/README.md"),
                            URL("https://raw.githubusercontent.com/$login/$login/master/README.md")
                        )
                        for (url in readmeUrls) {
                            try {
                                val conn = url.openConnection() as HttpURLConnection
                                conn.connectTimeout = 3000
                                conn.readTimeout = 3000
                                if (conn.responseCode == 200) {
                                    readmeContent = conn.inputStream.bufferedReader().use { it.readText() }
                                    break
                                }
                            } catch (e: Exception) {
                                // try next URL
                            }
                        }

                        // 3. Fetch followers list
                        val followersList = mutableListOf<Pair<String, String>>()
                        try {
                            val fUrl = URL("https://api.github.com/users/$login/followers?per_page=12")
                            val fConn = fUrl.openConnection() as HttpURLConnection
                            fConn.requestMethod = "GET"
                            fConn.connectTimeout = 3000
                            fConn.readTimeout = 3000
                            if (token.isNotBlank()) {
                                fConn.setRequestProperty("Authorization", "token $token")
                            }
                            if (fConn.responseCode == 200) {
                                val fJsonStr = fConn.inputStream.bufferedReader().use { it.readText() }
                                val fArr = JsonParser.parseString(fJsonStr).asJsonArray
                                fArr.forEach { el ->
                                    val fObj = el.asJsonObject
                                    val fLogin = fObj.get("login")?.asString ?: ""
                                    val fAvatar = fObj.get("avatar_url")?.asString ?: ""
                                    if (fLogin.isNotBlank()) {
                                        followersList.add(Pair(fLogin, fAvatar))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        val cacheItem = GithubProfileCache(
                            login = login,
                            name = name,
                            avatarUrl = avatarUrl,
                            bio = bio,
                            followers = followers,
                            following = following,
                            blogUrl = blogUrl,
                            githubUrl = githubUrl,
                            readmeContent = readmeContent,
                            followersList = followersList
                        )
                        Result.success(cacheItem)
                    } catch (e: Exception) {
                        Result.failure<GithubProfileCache>(e)
                    }
                }

                // Update Compose state on the MAIN thread
                if (result.isSuccess) {
                    val cacheItem = result.getOrThrow()
                    profileCache[cacheItem.login.lowercase()] = cacheItem
                    currentGithubProfile = cacheItem
                    githubError = null
                    showGithubProfile = true
                } else {
                    val ex = result.exceptionOrNull()
                    val msg = ex?.message ?: ""
                    githubError = when {
                        msg == "RATE_LIMIT" -> "Rate limit exceeded. Add a token in settings or try again later."
                        msg == "NOT_FOUND" -> "GitHub user not found."
                        msg.startsWith("ERROR_") -> "GitHub API error: ${msg.substring(6)}"
                        else -> "Connection error. Please check your internet connection."
                    }
                    showGithubProfile = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                githubError = "Connection error. Please check your internet connection."
                showGithubProfile = false
            } finally {
                githubLoading = false
            }
        }
    }

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

        // Platform selector chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            platforms.forEach { platform ->
                val isSelected = selectedPlatform == platform
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedPlatform = platform; githubError = null },
                    label = { Text(platform) }
                )
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
                    imageVector = Icons.Default.AlternateEmail,
                    contentDescription = "Username Prefix",
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (username.isNotEmpty()) {
                    IconButton(onClick = { username = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
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
                        fetchGithubProfile(cleanedUsername)
                    } else {
                        // Handle native redirects
                        val (appUri, browserUri, pkg) = when (selectedPlatform) {
                            "Instagram" -> Triple(
                                Uri.parse("http://instagram.com/_u/$cleanedUsername"),
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
        val profile = currentGithubProfile!!
        AlertDialog(
            onDismissRequest = { showGithubProfile = false },
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
                    IconButton(onClick = { showGithubProfile = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
                                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
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
                                                fetchGithubProfile(fLogin)
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
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Github Profile")
                }
            }
        )
    }
}
