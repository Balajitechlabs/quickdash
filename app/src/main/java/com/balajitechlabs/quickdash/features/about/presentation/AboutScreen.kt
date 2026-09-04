/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: about
 * File: AboutScreen.kt
 * Description: About tab — Dynamic dual tabs [ About QuickDash | About Me ] for balajitechlabs
 * Developer: balajitechlabs
 */

package com.balajitechlabs.quickdash.features.about.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.BuildConfig
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import com.balajitechlabs.quickdash.core.utils.UpdateManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val customForkLicenseText = """
PocketOps Custom Open Source Fork License

Copyright (c) 2026 Aakarsh (L192) / IIXII™

TERMS AND CONDITIONS FOR FORKED DERIVATIVES, REPRODUCTION, AND DISTRIBUTION

1. DEFINITIONS
- "Author" refers to Aakarsh (L192) / IIXII™.
- "Original Software" refers to the official PocketOps Android application source code and binaries.
- "Fork" or "Derivative" refers to any modified version of the Original Software, created by a third party.
- "Publish" or "Distribute" refers to making the Software or Derivatives available to third parties via app stores, websites, direct file transfers, or any other media.

2. LOCAL & PERSONAL USE
You are permitted to download, compile, modify, and use the Software locally and privately on your personal devices without restriction.

3. REDISTRIBUTION OF ORIGINAL SOFTWARE
You are permitted to redistribute the original, unmodified Software, provided that:
- You clearly identify it as an unofficial distribution of the Original Software.
- You do NOT use the original branding, name (PocketOps), or package name (l192.aakarsh.pocketops) for any public-facing redistribution or app store listing.
- You must include this license file and clearly credit the Author as the original creator.

4. RESTRICTIONS ON DERIVATIVES & PUBLISHING
You may NOT Publish or Distribute any Forked Derivative work unless it meets the requirement of containing Major Functional Changes.
- Minor Changes (such as changing the name, logo, icons, colors, or visual theme; modifying the package name; bug fixes, refactoring, etc.) DO NOT grant permission to Publish or Distribute.
- All Forked Derivative works must use a unique, non-confusing name and package name to ensure they are not mistaken for the Original Software.
- "Major Functional Changes" require that new features and functional modifications constitute at least 30% of the entire codebase and functionality of the project.
- Bug fixes, refactoring, and UI/theme adjustments DO NOT count toward the 30% threshold. Only entirely new features or substantial functional expansions qualify.
- The Author (Aakarsh/L192) reserves the right to serve as the final arbiter in determining if the 30% functional change threshold has been met.

5. SHARE-ALIKE AND OPEN SOURCE FORCE (COPYLEFT)
If you meet the 30% Major Functional Changes threshold and choose to Publish or Distribute a Forked Derivative work, you are legally obligated to:
- Keep the entire source code of your Forked Derivative work fully public and open-source. You cannot close the source code, sell it privately, or hide your modifications.
- Distribute your Forked Derivative work under this exact same license, ensuring that any downstream users have the same rights and restrictions.
- Platform Requirement: If the Forked Derivative is hosted on a public repository platform (such as GitHub), it must be managed via that platform’s native "Fork" mechanism to maintain a transparent, verifiable link to the original repository.

6. PROTECTION OF AUTHORSHIP
Any permitted Forked Derivative work must:
- Retain all original copyright notices, credits, and trademarks pointing to the Author.
- Prominently state inside the application (within the "About" or "Settings" menu) that it is a "Fork of IIXII™ property" and include a clear link to the Original Software repository.
- Include a comprehensive changelog within the repository detailing all functional modifications.

7. REVOCATION AND COMPLIANCE
- Any permission granted under this license is contingent upon continuous compliance. Failure to adhere to the terms herein shall result in an immediate, automatic revocation of all rights granted, without prejudice to the Author's right to pursue further legal action.

8. OWNER EXCEPTIONS AND CUSTOM GRANTS
The Author (Aakarsh / L192) reserves the sole right to grant exceptions, waive any restrictions, or offer custom licensing terms at their absolute discretion. Any such custom permission must be requested and obtained in writing via email exclusively at 192aakarsh@gmail.com.

9. NO WARRANTY
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
""".trimIndent()

private data class SocialPill(
    val label: String,
    val handle: String,
    val url: String,
    val badgeColor: Color,
    val iconTint: Color,
    val imageVector: ImageVector? = null,
    val iconRes: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("AboutApp") } // "AboutApp" is first, "AboutMe" is second
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showUpToDateSheet by remember { mutableStateOf(false) }
    val userStore = remember { com.balajitechlabs.quickdash.core.data.UserStore(context) }
    val isPreReleaseChannel by userStore.includePreRelease.collectAsStateWithLifecycle(initialValue = false)

    val updateState = UpdateManager.updateState

    LaunchedEffect(updateState) {
        if (updateState is com.balajitechlabs.quickdash.core.utils.UpdateState.UpToDate && updateState.isManual) {
            showUpToDateSheet = true
        }
    }

    val socialPills = listOf(
        SocialPill("QuickDash Website", "quickdash.balajitechlab.com", "https://balajitechlab.com/go/quickdash", Color(0xFF2A2B30), Color.White, imageVector = Icons.Rounded.Language),
        SocialPill("Portfolio Website", "balajitechlab.com", "https://balajitechlab.com", Color(0xFF2A2B30), Color.White, imageVector = Icons.Rounded.Public),
        SocialPill("GitHub Profile", "github.com/balajitechlabs", "https://balajitechlab.com/go/github", Color(0xFF2A2B30), Color.White, iconRes = R.drawable.ic_github),
        SocialPill("Play Store", "balajitechlabs Developer Apps", "https://balajitechlab.com/go/playstore", Color(0xFF2A2B30), Color.White, imageVector = Icons.Rounded.Shop),
        SocialPill("LinkedIn", "linkedin.com/in/balajitechlabs", "https://balajitechlab.com/go/linkedin", Color(0xFF2A2B30), Color.White, imageVector = Icons.Rounded.Work),
        SocialPill("X / Twitter", "@balajitechlabs", "https://balajitechlab.com/go/x", Color(0xFF2A2B30), Color.White, imageVector = Icons.Rounded.Share),
        SocialPill("GitLab", "gitlab.com/balajitechlabs", "https://balajitechlab.com/go/gitlab", Color(0xFF2A2B30), Color.White, imageVector = Icons.Rounded.Code),
        SocialPill("Reddit Community", "r/balajitechlabs", "https://balajitechlab.com/go/reddit", Color(0xFF2A2B30), Color.White, imageVector = Icons.Rounded.Forum),
        SocialPill("Telegram Community", "t.me/QuickDash Group", "https://balajitechlab.com/go/quickdash-community", Color(0xFF2A2B30), Color.White, iconRes = R.drawable.ic_telegram),
        SocialPill("Email", "admin@balajitechlab.com", "mailto:admin@balajitechlab.com", Color(0xFF2A2B30), Color.White, imageVector = Icons.Rounded.Email)
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000000))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dynamic Header Segmented Tabs: [ 🚀 About QuickDash | 👨‍💻 About Me ]
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    SegmentedButton(
                        selected = selectedTab == "AboutApp",
                        onClick = { selectedTab = "AboutApp" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text("About QuickDash", fontWeight = FontWeight.Bold) },
                        icon = { SegmentedButtonDefaults.Icon(active = selectedTab == "AboutApp") }
                    )
                    SegmentedButton(
                        selected = selectedTab == "AboutMe",
                        onClick = { selectedTab = "AboutMe" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text("About Me", fontWeight = FontWeight.Bold) },
                        icon = { SegmentedButtonDefaults.Icon(active = selectedTab == "AboutMe") }
                    )
                }
            }

            if (selectedTab == "AboutMe") {
                // ── ABOUT ME TAB ──────────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_developer_avatar),
                            contentDescription = "balajitechlabs avatar",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "balajitechlabs 🇮🇳",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "||BTL||™",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Social & Community Profiles Listed One-by-One (No Horizontal Scrolling)
                item {
                    Text(
                        text = "Communities & Developer Profiles",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp)
                    )
                }

                items(socialPills) { pill ->
                    Surface(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pill.url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E2024),
                        border = BorderStroke(1.dp, Color(0xFF38393F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2A2B30)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (pill.iconRes != null) {
                                    Icon(
                                        painter = painterResource(pill.iconRes),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else if (pill.imageVector != null) {
                                    Icon(
                                        imageVector = pill.imageVector,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pill.label,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = pill.handle,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = Color(0xFFC5C6D0)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = null,
                                tint = Color(0xFF8E9099),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                // ── ABOUT QUICKDASH TAB ──────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.app_logo),
                            contentDescription = "QuickDash logo",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "QuickDash",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFC5C6D0)
                        )
                    }
                }

                // EssentialX-style In-App Updates Card with Release Channel Toggle
                item {
                    val updateState = UpdateManager.updateState
                    RoundedCardContainer(
                        containerColor = Color(0xFF1E2024),
                        cornerRadius = 20.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "In-App Updates",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "balajitechlabs/quickdash",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFC5C6D0)
                                )
                            }

                            // Dynamic Update State UI
                            when (updateState) {
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.Checking -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Checking GitHub Releases...", color = Color.White, fontSize = 13.sp)
                                    }
                                }
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.UpToDate -> {
                                    val upToDate = updateState
                                    Surface(
                                        onClick = { showUpToDateSheet = true },
                                        color = Color(0xFF1B2E1E),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF81C784), modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                if (upToDate.isAheadOfLatest) {
                                                    Text("Preview Build (v${upToDate.currentVersion}) ✅", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                                    Text("Ahead of latest public release (v${upToDate.latestVersion}) ▾", color = Color(0xFF81C784), fontSize = 11.sp)
                                                } else {
                                                    Text("You are on the latest version (v${upToDate.currentVersion})! ✅", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                                    Text("Tap to view release notes & details ▾", color = Color(0xFF81C784), fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { UpdateManager.checkForUpdates(context, manual = true, includePreRelease = isPreReleaseChannel) },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Check Again")
                                    }
                                }
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.UpdateAvailable -> {
                                    val info = updateState
                                    Surface(
                                        color = Color(0xFF1E2024),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF2A2B30))
                                                        .border(BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f)), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Rounded.Download, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text("Update Available: v${info.versionName.removePrefix("v")}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text("Tap below to inspect changelogs & download", color = Color(0xFFC5C6D0), fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            UpdateManager.showUpdateSheet = true
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Rounded.Download, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("View Changelog & Install v${info.versionName.removePrefix("v")}", fontWeight = FontWeight.Bold)
                                    }
                                }
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.Downloading -> {
                                    val dl = updateState
                                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Downloading v${dl.versionName.removePrefix("v")}...", color = Color.White, fontSize = 12.sp)
                                            Text("${dl.progress}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        LinearProgressIndicator(
                                            progress = { dl.progress / 100f },
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                        )
                                    }
                                }
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.ReadyToInstall -> {
                                    val ready = updateState
                                    Button(
                                        onClick = {
                                            UpdateManager.installApk(context, ready.fileName)
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Rounded.SystemUpdate, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Install Update (v${ready.versionName.removePrefix("v")})", fontWeight = FontWeight.Bold)
                                    }
                                }
                                is com.balajitechlabs.quickdash.core.utils.UpdateState.Error -> {
                                    val err = updateState
                                    Text("Update check error: ${err.message}", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                    Button(
                                        onClick = { UpdateManager.checkForUpdates(context, manual = true, includePreRelease = isPreReleaseChannel) },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Retry")
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { UpdateManager.checkForUpdates(context, manual = true, includePreRelease = isPreReleaseChannel) },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Check for Updates", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // App Features Card
                item {
                    RoundedCardContainer(
                        containerColor = Color(0xFF1E2024),
                        cornerRadius = 20.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "About The Application",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "QuickDash is an all-in-one floating productivity companion featuring 15+ daily tools: Quick Collect, Quick Chat, Smart Clipboard, Quick Notes, Voice Memos, Wi-Fi Hub, Pomodoro, and more.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/balajitechlabs/quickdash")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2A2B30),
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.8f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_github),
                                    contentDescription = "GitHub Repository",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "View on GitHub (balajitechlabs/quickdash)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Legal & License Card
                item {
                    RoundedCardContainer(
                        containerColor = Color(0xFF1E2024),
                        cornerRadius = 20.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Legal & Open Source",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Fork of IIXII™ property • PocketOps Custom Fork License",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showLicenseDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("View License", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://balajitechlab.com/privacy")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Privacy Policy", fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Forked & Maintained by ||BTL||™ (balajitechlabs)\nOriginal Property © 2026 Aakarsh (L192) / IIXII™",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Floating QuickDash Website Bubble — icon-only at rest, expands on tap then navigates
        val fabScope = rememberCoroutineScope()
        var fabExpanded by remember { mutableStateOf(false) }

        Surface(
            onClick = {
                if (fabExpanded) {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://quickdash.balajitechlab.com"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    fabExpanded = false
                } else {
                    fabExpanded = true
                    fabScope.launch {
                        delay(2200)
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://quickdash.balajitechlab.com"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        fabExpanded = false
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 20.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = if (fabExpanded) 16.dp else 14.dp,
                    vertical = 14.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Crossfade(targetState = fabExpanded, label = "fab_icon") { expanded ->
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.Language else Icons.Rounded.Public,
                        contentDescription = "QuickDash Website",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = expandHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "quickdash.balajitechlab.com",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }

    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = { Text("Custom Open Source License", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = customForkLicenseText,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicenseDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showUpToDateSheet && updateState is com.balajitechlabs.quickdash.core.utils.UpdateState.UpToDate) {
        val upToDate = updateState
        ModalBottomSheet(
            onDismissRequest = { showUpToDateSheet = false },
            containerColor = Color(0xFF1E2024),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = if (upToDate.isAheadOfLatest) "Preview / Development Build" else "You're on the Latest Build!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF2A2B30)
                ) {
                    val badgeText = if (upToDate.isAheadOfLatest) {
                        "QuickDash v${upToDate.currentVersion} • Ahead of latest public v${upToDate.latestVersion}"
                    } else {
                        "QuickDash v${upToDate.currentVersion} • ${if (isPreReleaseChannel) "Beta (Pre-Release)" else "Stable Release"}"
                    }
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                if (upToDate.changelog.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF141518),
                        border = BorderStroke(1.dp, Color(0xFF2A2B30)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "What's New in this Release",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = upToDate.changelog,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC5C6D0),
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Your application is fully updated with all the latest tools, design refinements, and performance upgrades. No new updates are available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC5C6D0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { showUpToDateSheet = false },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Great, Got It!", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/balajitechlabs/quickdash/releases")
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Release History on GitHub", fontSize = 12.sp)
                }
            }
        }
    }
}
