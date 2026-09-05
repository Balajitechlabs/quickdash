/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/timer/presentation
 * File: QuickTimerScreen.kt
 * Description: Multi-timer and stopwatch tool with background notification alerts and preset durations.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.timer.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.balajitechlabs.quickdash.features.timer.presentation.components.CountdownContent
import com.balajitechlabs.quickdash.features.timer.presentation.components.StopwatchContent
import com.balajitechlabs.quickdash.features.timer.presentation.components.TimerHistoryContent

@Composable
fun QuickTimerScreen(
    viewModel: QuickTimerViewModel = hiltViewModel(),
    isFloating: Boolean = false
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SegmentedButton(
                selected = selectedTab == 0,
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                    selectedTab = 0
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                label = { Text("Stopwatch") },
                icon = { SegmentedButtonDefaults.Icon(active = selectedTab == 0) }
            )
            SegmentedButton(
                selected = selectedTab == 1,
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                    selectedTab = 1
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                label = { Text("Timer") },
                icon = { SegmentedButtonDefaults.Icon(active = selectedTab == 1) }
            )
            SegmentedButton(
                selected = selectedTab == 2,
                onClick = {
                    com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                    selectedTab = 2
                },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                label = { Text("History") },
                icon = { SegmentedButtonDefaults.Icon(active = selectedTab == 2) }
            )
        }

        AnimatedContent(targetState = selectedTab, label = "timer_tab_content") { tab ->
            when (tab) {
                0 -> StopwatchContent(viewModel = viewModel, scope = scope, isFloating = isFloating)
                1 -> CountdownContent(viewModel = viewModel, scope = scope, isFloating = isFloating)
                else -> TimerHistoryContent(viewModel = viewModel, isFloating = isFloating)
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}
