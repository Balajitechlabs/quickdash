/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/network
 * File: ApiModels.kt
 * Description: EssentialX-styled component for core/network supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.network

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class UpdateInfoResponse(
    @SerialName("version_code") val versionCode: Int,
    @SerialName("latest_version") val latestVersion: String,
    @SerialName("min_version_code") val minVersionCode: Int = 500,
    val changelog: String = "",
    @SerialName("apk_url") val apkUrl: String = "",
    @SerialName("apk_size_bytes") val apkSizeBytes: Long = 0L,
    @SerialName("apk_sha256") val apkSha256: String = "",
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("critical_update") val criticalUpdate: Boolean = false
)

@Keep
@Serializable
data class AnnouncementResponse(
    @SerialName("announcement_id") val announcementId: Long,
    val title: String,
    val message: String,
    @SerialName("action_url") val actionUrl: String = "",
    @SerialName("action_text") val actionText: String = "",
    val priority: String = "normal",
    val dismissible: Boolean = true
)

@Keep
@Serializable
data class FeedbackRequest(
    val app_version: String,
    val device: String,
    val android_version: String,
    val message: String,
    val rating: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Keep
@Serializable
data class CrashReportRequest(
    val id: String,
    val app_version: String,
    val version_code: Int,
    val device: String,
    val android_version: String,
    val stacktrace: String,
    val last_action: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
