/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/network
 * File: QuickDashApiClient.kt
 * Description: OkHttp client wrapper executing authenticated REST requests, release checks, and download tasks.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val versionCode: Int,
    val changelog: String,
    val apkUrl: String,
    val sha256: String = "",
    val isCritical: Boolean = false
)

data class Announcement(
    val id: Long,
    val title: String,
    val message: String,
    val actionUrl: String?
)

object QuickDashApiClient {

    private const val TAG = "QuickDashApiClient"
    private val APP_VERSION = com.balajitechlabs.quickdash.BuildConfig.VERSION_NAME

    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private fun selectBestApkUrl(assets: org.json.JSONArray?, fallbackTag: String): String {
        if (assets == null || assets.length() == 0) {
            return "https://github.com/balajitechlabs/quickdash/releases/download/v$fallbackTag/app-universal-release.apk"
        }

        var universalUrl = ""
        var arm64Url = ""
        var armeabiUrl = ""
        var anyApkUrl = ""

        for (i in 0 until assets.length()) {
            val asset = assets.optJSONObject(i) ?: continue
            val name = asset.optString("name", "").lowercase()
            val downloadUrl = asset.optString("browser_download_url", "")
            if (!name.endsWith(".apk")) continue

            if (name.contains("universal")) {
                universalUrl = downloadUrl
            } else if (name.contains("arm64")) {
                arm64Url = downloadUrl
            } else if (name.contains("armeabi")) {
                armeabiUrl = downloadUrl
            } else if (!name.contains("foss")) {
                anyApkUrl = downloadUrl
            }
        }

        // 1. Universal APK is safest across all devices & architectures
        if (universalUrl.isNotBlank()) return universalUrl

        // 2. Hardware ABI matching
        val isArm64 = android.os.Build.SUPPORTED_ABIS.any { it.contains("arm64") }
        if (isArm64 && arm64Url.isNotBlank()) return arm64Url
        if (armeabiUrl.isNotBlank()) return armeabiUrl
        if (anyApkUrl.isNotBlank()) return anyApkUrl

        val cleanTag = fallbackTag.removePrefix("v")
        return "https://github.com/balajitechlabs/quickdash/releases/download/v$cleanTag/app-universal-release.apk"
    }

    suspend fun checkForUpdates(currentVersionCode: Int, includePreRelease: Boolean = false): UpdateInfo = withContext(Dispatchers.IO) {
        val currentVersion = APP_VERSION
        val currentSemVer = com.balajitechlabs.quickdash.core.utils.SemanticVersion.parse(currentVersion)

        // 1. Primary: Direct GitHub Releases API
        try {
            val url = if (includePreRelease) {
                "https://api.github.com/repos/balajitechlabs/quickdash/releases"
            } else {
                "https://api.github.com/repos/balajitechlabs/quickdash/releases/latest"
            }

            val ghRequest = Request.Builder()
                .url(url)
                .header("User-Agent", "QuickDash-App/$APP_VERSION")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            client.newCall(ghRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body.string()
                    val targetRelease: org.json.JSONObject? = if (includePreRelease) {
                        val jsonArray = org.json.JSONArray(bodyString)
                        var bestRel: org.json.JSONObject? = null
                        var bestVer: com.balajitechlabs.quickdash.core.utils.SemanticVersion? = null
                        for (i in 0 until jsonArray.length()) {
                            val rel = jsonArray.getJSONObject(i)
                            if (rel.optBoolean("draft", false)) continue
                            val rawTag = rel.optString("tag_name", "")
                            val parsed = com.balajitechlabs.quickdash.core.utils.SemanticVersion.parse(rawTag)
                            if (bestVer == null || parsed > bestVer) {
                                bestVer = parsed
                                bestRel = rel
                            }
                        }
                        bestRel ?: if (jsonArray.length() > 0) jsonArray.getJSONObject(0) else null
                    } else {
                        org.json.JSONObject(bodyString)
                    }

                    if (targetRelease != null) {
                        val rawTag = targetRelease.optString("tag_name", "")
                        val remoteSemVer = com.balajitechlabs.quickdash.core.utils.SemanticVersion.parse(rawTag)
                        val cleanVersion = remoteSemVer.displayVersion
                        val releaseNotes = targetRelease.optString("body", "Bug fixes and performance enhancements.")
                        val isNewer = remoteSemVer > currentSemVer

                        val assets = targetRelease.optJSONArray("assets")
                        val apkUrl = selectBestApkUrl(assets, cleanVersion)

                        return@withContext UpdateInfo(
                            hasUpdate = isNewer,
                            latestVersion = cleanVersion,
                            versionCode = currentVersionCode + 1,
                            changelog = releaseNotes,
                            apkUrl = apkUrl,
                            sha256 = "",
                            isCritical = false
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "GitHub Releases check failed, attempting fallback API", e)
        }

        // 2. Fallback: Website update.json
        try {
            val request = Request.Builder()
                .url(ApiConfig.UPDATE_URL)
                .header("User-Agent", "QuickDash-App/$APP_VERSION")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body.string()
                    val data = json.decodeFromString<UpdateInfoResponse>(bodyString)
                    val remoteSemVer = com.balajitechlabs.quickdash.core.utils.SemanticVersion.parse(data.latestVersion)
                    val isNewer = remoteSemVer > currentSemVer
                    return@withContext UpdateInfo(
                        hasUpdate = isNewer,
                        latestVersion = remoteSemVer.displayVersion,
                        versionCode = data.versionCode,
                        changelog = data.changelog.ifEmpty { "Bug fixes and performance improvements." },
                        apkUrl = data.apkUrl.ifEmpty { "https://github.com/balajitechlabs/quickdash/releases/latest/download/app-universal-release.apk" },
                        sha256 = data.apkSha256,
                        isCritical = data.criticalUpdate
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback update check failed", e)
        }

        return@withContext UpdateInfo(
            hasUpdate = false,
            latestVersion = currentSemVer.displayVersion,
            versionCode = currentVersionCode,
            changelog = "",
            apkUrl = ""
        )
    }

    suspend fun fetchAnnouncement(): Announcement? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(ApiConfig.ANNOUNCEMENT_URL)
                .header("User-Agent", "QuickDash-App/$APP_VERSION")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body.string()
                    val data = json.decodeFromString<AnnouncementResponse>(bodyString)
                    return@withContext Announcement(
                        id = data.announcementId,
                        title = data.title,
                        message = data.message,
                        actionUrl = data.actionUrl.ifEmpty { null }
                    )
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "No announcement available or network unreachable")
        }
        return@withContext null
    }

    suspend fun submitFeedback(feedback: FeedbackRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = json.encodeToString(FeedbackRequest.serializer(), feedback)
            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}feedback")
                .post(body.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit feedback", e)
            return@withContext false
        }
    }

    suspend fun submitCrashReport(report: CrashReportRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = json.encodeToString(CrashReportRequest.serializer(), report)
            val request = Request.Builder()
                .url("${ApiConfig.BASE_URL}crash-report")
                .post(body.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit crash report", e)
            return@withContext false
        }
    }
}
