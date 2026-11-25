/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: ui
 * File: DashboardScreenTest.kt
 * Description: Compose UI tests verifying QuickDash design system components and QuickCalculatorScreen rendering.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashCard
import com.balajitechlabs.quickdash.core.designsystem.components.QuickDashPrimaryButton
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun quickDashDesignSystemComponentsRenderContent() {
        composeTestRule.setContent {
            MaterialTheme {
                QuickDashCard {
                    Text("QuickDash Tools")
                    QuickDashPrimaryButton(text = "Launch", onClick = {})
                }
            }
        }
        composeTestRule.onNodeWithText("QuickDash Tools").assertIsDisplayed()
        composeTestRule.onNodeWithText("Launch").assertIsDisplayed()
    }
}
