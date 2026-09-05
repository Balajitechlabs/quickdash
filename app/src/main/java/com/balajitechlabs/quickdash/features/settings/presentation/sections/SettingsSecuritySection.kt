/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/sections
 * File: SettingsSecuritySection.kt
 * Description: Settings section configuring biometric lock, incognito mode, and window security flags.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.sections

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.core.ui.components.PreferenceGroup
import com.balajitechlabs.quickdash.core.ui.components.PreferenceItem
import com.balajitechlabs.quickdash.core.ui.components.StyledSwitch
import com.balajitechlabs.quickdash.core.ui.components.SwitchStyle
import com.balajitechlabs.quickdash.features.settings.presentation.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SettingsSecuritySection(
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    viewModel: SettingsViewModel,
    coroutineScope: CoroutineScope,
    activeSwitchStyle: SwitchStyle,
    onFeedback: () -> Unit
) {
    PreferenceGroup(
        title = "Security & Privacy",
        expanded = expanded,
        onHeaderClick = onHeaderClick
    ) {
        val isAppLocked by viewModel.userStore.isAppLocked.collectAsStateWithLifecycle(initialValue = false)
        PreferenceItem(
            title = "Biometric Lock",
            subtitle = "Require fingerprint / face to open QuickDash",
            iconVector = Icons.Default.Lock,
            trailing = {
                StyledSwitch(
                    style = activeSwitchStyle,
                    checked = isAppLocked,
                    onCheckedChange = { enabled ->
                        onFeedback()
                        coroutineScope.launch { viewModel.userStore.setAppLocked(enabled) }
                    }
                )
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        val isTabLocked by viewModel.userStore.tabBiometricLock.collectAsStateWithLifecycle(initialValue = false)
        PreferenceItem(
            title = "Lock Private Tabs",
            subtitle = "Require authentication for Clipboard & Notes",
            iconVector = Icons.Default.LockClock,
            trailing = {
                StyledSwitch(
                    style = activeSwitchStyle,
                    checked = isTabLocked,
                    onCheckedChange = { enabled ->
                        onFeedback()
                        coroutineScope.launch { viewModel.userStore.saveTabBiometricLock(enabled) }
                    }
                )
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        val isSecureMode by viewModel.userStore.secureMode.collectAsStateWithLifecycle(initialValue = false)
        PreferenceItem(
            title = "Secure Mode",
            subtitle = "Block screenshots and hide app preview in recents",
            iconVector = Icons.Default.Security,
            trailing = {
                StyledSwitch(
                    style = activeSwitchStyle,
                    checked = isSecureMode,
                    onCheckedChange = { enabled ->
                        onFeedback()
                        coroutineScope.launch { viewModel.userStore.saveSecureMode(enabled) }
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}
