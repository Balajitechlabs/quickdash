/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/network
 * File: ApiConfig.kt
 * Description: EssentialX-styled component for core/network supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.network

object ApiConfig {
    const val BASE_URL = "https://quickdash.balajitechlab.com/api/v1/"
    const val GITHUB_API_URL = "https://api.github.com/repos/balajitechlabs/quickdash/"

    const val TIMEOUT_SECONDS = 15L
    const val MAX_RETRIES = 3

    val UPDATE_URL = "${BASE_URL}update.json"
    val ANNOUNCEMENT_URL = "${BASE_URL}announcement.json"
    val STATS_URL = "${BASE_URL}stats.json"
    val HEALTH_URL = "${BASE_URL}health.json"
    val TOOLS_URL = "${BASE_URL}tools.json"
}
