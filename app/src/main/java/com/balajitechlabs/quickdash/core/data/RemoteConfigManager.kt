/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: RemoteConfigManager.kt
 * Description: Remote configuration stub — Firebase removed. Future: use own backend or GitHub-hosted JSON config.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

/**
 * Stub for future remote config. Firebase Remote Config was removed.
 * Replace with a GitHub-hosted JSON file or your own backend endpoint.
 */
object RemoteConfigManager {
    fun fetchAndActivate() {
        // No-op stub
    }

    fun getString(key: String, default: String = ""): String = default
    fun getBoolean(key: String, default: Boolean = false): Boolean = default
    fun getLong(key: String, default: Long = 0L): Long = default
}
