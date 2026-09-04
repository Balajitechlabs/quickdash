/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/utils
 * File: UpdateDownloadWorker.kt
 * Description: EssentialX-styled component for core/utils supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class UpdateDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val apkUrl = inputData.getString("apk_url") ?: return@withContext Result.failure()
        val expectedSha256 = inputData.getString("sha256") ?: ""
        val versionName = inputData.getString("version_name") ?: "update"

        return@withContext try {
            val client = OkHttpClient()
            val request = Request.Builder().url(apkUrl).build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.retry()

            val file = File(applicationContext.cacheDir, "QuickDash-$versionName.apk")
            response.body.byteStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (expectedSha256.isNotEmpty()) {
                val actualSha256 = computeSha256(file)
                if (!actualSha256.equals(expectedSha256, ignoreCase = true)) {
                    file.delete()
                    Log.e("UpdateDownloadWorker", "SHA-256 verification failed!")
                    return@withContext Result.failure()
                }
            }

            Log.i("UpdateDownloadWorker", "Downloaded APK verified successfully: ${file.absolutePath}")
            Result.success()
        } catch (e: Exception) {
            Log.e("UpdateDownloadWorker", "Failed to download update", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun computeSha256(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
