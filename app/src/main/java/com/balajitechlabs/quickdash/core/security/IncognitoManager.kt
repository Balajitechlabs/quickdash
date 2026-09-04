/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/security
 * File: IncognitoManager.kt
 * Description: EssentialX-styled component for core/security supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Zero-Trace Incognito Mode Manager (`IncognitoManager.kt`).
 * Toggles temporary suspension of clipboard history logging, search queries, and recent tool logs.
 */
object IncognitoManager {

    var isIncognitoActive by mutableStateOf(false)
        private set

    fun setIncognito(enabled: Boolean) {
        isIncognitoActive = enabled
    }

    fun toggleIncognito(): Boolean {
        isIncognitoActive = !isIncognitoActive
        return isIncognitoActive
    }

    /**
     * Applies system window security flags (`FLAG_SECURE`) to prevent screen capture
     * or recent app previews while Incognito mode is active.
     */
    fun applyWindowSecurity(window: android.view.Window, forceSecure: Boolean = false) {
        if (isIncognitoActive || forceSecure) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
