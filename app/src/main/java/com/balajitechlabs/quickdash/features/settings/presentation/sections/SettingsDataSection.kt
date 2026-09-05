/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/sections
 * File: SettingsDataSection.kt
 * Description: Settings section managing local backups, data restoration, and storage clearance.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.core.ui.components.PreferenceGroup
import com.balajitechlabs.quickdash.core.ui.components.PreferenceItem

@Composable
fun SettingsDataSection(
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    onFeedback: () -> Unit,
    onOpenBackupOptions: () -> Unit
) {
    PreferenceGroup(
        title = "Data Management",
        expanded = expanded,
        onHeaderClick = onHeaderClick
    ) {
        PreferenceItem(
            title = "Backup Data",
            subtitle = "Export your settings and preferences to a JSON file",
            iconVector = Icons.Default.Upload,
            onClick = {
                onFeedback()
                onOpenBackupOptions()
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        PreferenceItem(
            title = "Restore Data",
            subtitle = "Import your settings and preferences",
            iconVector = Icons.Default.Download,
            onClick = {
                onFeedback()
                onOpenBackupOptions()
            }
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}
