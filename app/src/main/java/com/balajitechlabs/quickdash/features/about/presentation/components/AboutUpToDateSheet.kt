/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/about/presentation/components
 * File: AboutUpToDateSheet.kt
 * Description: Bottom sheet confirming the application is currently running the latest release.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.about.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.utils.UpdateState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUpToDateSheet(
    upToDate: UpdateState.UpToDate,
    isPreReleaseChannel: Boolean,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
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
                onClick = onDismissRequest,
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
