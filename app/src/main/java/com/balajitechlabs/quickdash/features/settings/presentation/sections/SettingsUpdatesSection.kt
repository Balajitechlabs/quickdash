/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/sections
 * File: SettingsUpdatesSection.kt
 * Description: Settings section providing manual update checks, channel toggles, and version information.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.core.ui.components.PreferenceGroup
import com.balajitechlabs.quickdash.core.ui.components.PreferenceItem
import com.balajitechlabs.quickdash.core.ui.components.StyledSwitch
import com.balajitechlabs.quickdash.core.ui.components.SwitchStyle
import com.balajitechlabs.quickdash.features.settings.presentation.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SettingsUpdatesSection(
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    includePreRelease: Boolean,
    viewModel: SettingsViewModel,
    coroutineScope: CoroutineScope,
    activeSwitchStyle: SwitchStyle,
    onFeedback: () -> Unit
) {
    PreferenceGroup(
        title = "Updates & System",
        expanded = expanded,
        onHeaderClick = onHeaderClick
    ) {
        PreferenceItem(
            title = "Pre-Release (Beta) Builds",
            subtitle = "Receive experimental builds and early feature updates from GitHub Releases",
            iconVector = Icons.Default.SystemUpdate,
            trailing = {
                StyledSwitch(
                    style = activeSwitchStyle,
                    checked = includePreRelease,
                    onCheckedChange = { enabled ->
                        onFeedback()
                        coroutineScope.launch {
                            viewModel.userStore.saveIncludePreRelease(enabled)
                        }
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}
