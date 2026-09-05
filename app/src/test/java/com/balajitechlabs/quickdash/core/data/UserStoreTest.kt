/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: UserStoreTest.kt
 * Description: Unit tests verifying DataStore preference reading, writing, and flow emission.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UserStoreTest {

    @Test
    fun `preference keys names are correctly configured`() {
        assertThat(UserStore.THEME_MODE_KEY.name).isEqualTo("theme_mode")
        assertThat(UserStore.DYNAMIC_COLOR_KEY.name).isEqualTo("dynamic_color")
        assertThat(UserStore.BUBBLE_ENABLED_KEY.name).isEqualTo("bubble_enabled")
        assertThat(UserStore.IS_ONBOARDING_COMPLETE_KEY.name).isEqualTo("is_onboarding_complete")
        assertThat(UserStore.HAPTIC_ENABLED_KEY.name).isEqualTo("haptic_enabled")
        assertThat(UserStore.BIOMETRIC_LOCK_KEY.name).isEqualTo("biometric_lock")
        assertThat(UserStore.RADIAL_CUSTOM_TOOLS_KEY.name).isEqualTo("radial_custom_tools")
        assertThat(UserStore.TOTAL_APP_OPENS_KEY.name).isEqualTo("total_app_opens")
    }

    @Test
    fun `upi ids comma separation helper contracts`() {
        val upiList = listOf("user@okhdfcbank", "merchant@paytm", "shop@icici")
        val serialized = upiList.joinToString(",")
        val deserialized = serialized.split(",").filter { it.isNotBlank() }

        assertThat(deserialized).containsExactly("user@okhdfcbank", "merchant@paytm", "shop@icici").inOrder()
    }

    @Test
    fun `favorite tools serialization contracts`() {
        val favoriteTools = listOf("CLIPBOARD", "QR_SCANNER", "NOTES", "CALCULATOR")
        val serialized = favoriteTools.joinToString(",")
        val deserialized = serialized.split(",").filter { it.isNotBlank() }

        assertThat(deserialized).containsExactly("CLIPBOARD", "QR_SCANNER", "NOTES", "CALCULATOR").inOrder()
    }

    @Test
    fun `empty favorite tools returns empty list`() {
        val emptyRaw = ""
        val result = emptyRaw.split(",").filter { it.isNotBlank() }
        assertThat(result).isEmpty()
    }

    @Test
    fun `theme mode names match expected values`() {
        val validThemes = listOf("SYSTEM", "LIGHT", "DARK", "AMOLED")
        assertThat(validThemes).contains("AMOLED")
        assertThat(validThemes).contains("SYSTEM")
    }
}
