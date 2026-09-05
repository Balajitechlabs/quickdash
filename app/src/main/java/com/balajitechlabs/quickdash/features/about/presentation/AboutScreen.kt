/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/about/presentation
 * File: AboutScreen.kt
 * Description: Developer brand screen featuring social links, project architecture notes, and update actions.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.about.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.utils.UpdateManager
import com.balajitechlabs.quickdash.core.utils.UpdateState
import com.balajitechlabs.quickdash.features.about.presentation.components.AboutAppHeader
import com.balajitechlabs.quickdash.features.about.presentation.components.AboutFeaturesCard
import com.balajitechlabs.quickdash.features.about.presentation.components.AboutFloatingWebsiteBubble
import com.balajitechlabs.quickdash.features.about.presentation.components.AboutLegalCard
import com.balajitechlabs.quickdash.features.about.presentation.components.AboutUpToDateSheet
import com.balajitechlabs.quickdash.features.about.presentation.components.AboutUpdaterCard
import com.balajitechlabs.quickdash.features.about.presentation.components.DeveloperProfileHeader
import com.balajitechlabs.quickdash.features.about.presentation.components.SocialPillsGrid
import com.balajitechlabs.quickdash.features.about.presentation.dialogs.AboutLicenseDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("AboutApp") }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showUpToDateSheet by remember { mutableStateOf(false) }
    val userStore = remember { UserStore(context) }
    val isPreReleaseChannel by userStore.includePreRelease.collectAsStateWithLifecycle(initialValue = false)

    val updateState = UpdateManager.updateState

    LaunchedEffect(updateState) {
        if (updateState is UpdateState.UpToDate && updateState.isManual) {
            showUpToDateSheet = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    SegmentedButton(
                        selected = selectedTab == "AboutApp",
                        onClick = {
                            com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                            selectedTab = "AboutApp"
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text("About QuickDash", fontWeight = FontWeight.Bold) },
                        icon = { SegmentedButtonDefaults.Icon(active = selectedTab == "AboutApp") }
                    )
                    SegmentedButton(
                        selected = selectedTab == "AboutMe",
                        onClick = {
                            com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                            selectedTab = "AboutMe"
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text("About Me", fontWeight = FontWeight.Bold) },
                        icon = { SegmentedButtonDefaults.Icon(active = selectedTab == "AboutMe") }
                    )
                }
            }

            if (selectedTab == "AboutMe") {
                item {
                    DeveloperProfileHeader()
                }

                item {
                    Text(
                        text = "Communities & Developer Profiles",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp, start = 4.dp)
                    )
                }

                item {
                    SocialPillsGrid(context = context)
                }
            } else {
                item {
                    AboutAppHeader()
                }

                item {
                    AboutUpdaterCard(
                        updateState = updateState,
                        isPreReleaseChannel = isPreReleaseChannel,
                        onShowUpToDateSheet = { showUpToDateSheet = true }
                    )
                }

                item {
                    AboutFeaturesCard(context = context)
                }

                item {
                    AboutLegalCard(
                        context = context,
                        onShowLicense = { showLicenseDialog = true }
                    )
                }
            }
        }

        AboutFloatingWebsiteBubble(context = context)
    }

    if (showLicenseDialog) {
        AboutLicenseDialog(onDismissRequest = { showLicenseDialog = false })
    }

    if (showUpToDateSheet && updateState is UpdateState.UpToDate) {
        AboutUpToDateSheet(
            upToDate = updateState,
            isPreReleaseChannel = isPreReleaseChannel,
            onDismissRequest = { showUpToDateSheet = false }
        )
    }
}
