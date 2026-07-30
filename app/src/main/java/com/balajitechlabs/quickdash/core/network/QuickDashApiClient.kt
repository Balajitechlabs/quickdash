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

    suspend fun checkForUpdates(currentVersionCode: Int): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(ApiConfig.UPDATE_URL)
                .header("User-Agent", "QuickDash-App/$APP_VERSION")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val data = json.decodeFromString<UpdateInfoResponse>(bodyString)
                    return@withContext UpdateInfo(
                        hasUpdate = data.versionCode > currentVersionCode,
                        latestVersion = data.latestVersion,
                        versionCode = data.versionCode,
                        changelog = data.changelog.ifEmpty { "Bug fixes and performance improvements." },
                        apkUrl = data.apkUrl.ifEmpty { "https://github.com/Balajitechlabs/quickdash/releases/latest/download/app-universal-release.apk" },
                        sha256 = data.apkSha256,
                        isCritical = data.criticalUpdate
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch version update", e)
        }
        return@withContext UpdateInfo(
            hasUpdate = false,
            latestVersion = APP_VERSION,
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
                    val bodyString = response.body?.string() ?: ""
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
