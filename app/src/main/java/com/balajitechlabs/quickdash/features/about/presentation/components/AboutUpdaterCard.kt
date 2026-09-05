/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/about/presentation/components
 * File: AboutUpdaterCard.kt
 * Description: In-app updater card initiating GitHub release checks and displaying version info.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.about.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import com.balajitechlabs.quickdash.core.utils.UpdateManager
import com.balajitechlabs.quickdash.core.utils.UpdateState

@Composable
fun AboutUpdaterCard(
    updateState: UpdateState,
    isPreReleaseChannel: Boolean,
    onShowUpToDateSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    RoundedCardContainer(
        containerColor = Color(0xFF1E2024),
        cornerRadius = 20.dp,
        modifier = modifier.fillMaxWidth()
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

            when (updateState) {
                is UpdateState.Checking -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Checking GitHub Releases...", color = Color.White, fontSize = 13.sp)
                    }
                }
                is UpdateState.UpToDate -> {
                    Surface(
                        onClick = onShowUpToDateSheet,
                        color = Color(0xFF1B2E1E),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF81C784), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            val aheadTitle = "Preview Build (v${updateState.currentVersion})"
                            val aheadSub = "Ahead of public release (v${updateState.latestVersion})"
                            val latestTitle = "Latest version (v${updateState.currentVersion})"
                            val latestSub = "Tap to view release notes and details"
                            Column {
                                if (updateState.isAheadOfLatest) {
                                    Text(
                                        text = aheadTitle,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = aheadSub,
                                        color = Color(0xFF81C784),
                                        fontSize = 11.sp
                                    )
                                } else {
                                    Text(
                                        text = latestTitle,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = latestSub,
                                        color = Color(0xFF81C784),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            UpdateManager.checkForUpdates(
                                context = context,
                                manual = true,
                                includePreRelease = isPreReleaseChannel
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check Again")
                    }
                }
                is UpdateState.UpdateAvailable -> {
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
                                    Text("Update Available: v${updateState.versionName.removePrefix("v")}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        Text("View Changelog & Install v${updateState.versionName.removePrefix("v")}", fontWeight = FontWeight.Bold)
                    }
                }
                is UpdateState.Downloading -> {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Downloading v${updateState.versionName.removePrefix("v")}...", color = Color.White, fontSize = 12.sp)
                            Text("${updateState.progress}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        LinearProgressIndicator(
                            progress = { updateState.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
                is UpdateState.ReadyToInstall -> {
                    Button(
                        onClick = {
                            UpdateManager.installApk(context, updateState.fileName)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.SystemUpdate, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Install Update (v${updateState.versionName.removePrefix("v")})", fontWeight = FontWeight.Bold)
                    }
                }
                is UpdateState.Error -> {
                    Text("Update check error: ${updateState.message}", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
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
