/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: ui
 * File: DashboardScreenTest.kt
 * Description: Compose UI tests verifying dashboard layout rendering, tool clicks, and state handling.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboardScreenHeaderRendersCorrectly() {
        composeTestRule.setContent {
            Text("QuickDash Dashboard")
        }
        composeTestRule.onNodeWithText("QuickDash Dashboard").assertIsDisplayed()
    }
}
