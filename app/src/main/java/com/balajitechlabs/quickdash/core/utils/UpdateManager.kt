package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.balajitechlabs.quickdash.core.network.QuickDashApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState
    object UpToDate : UpdateState
    data class Error(val message: String) : UpdateState
    data class UpdateAvailable(
        val versionName: String,
        val apkUrl: String,
        val versionCode: Int,
        val changelog: String = "",
        val sha256: String = "",
        val isCritical: Boolean = false
    ) : UpdateState
    data class Downloading(val versionName: String, val progress: Int) : UpdateState
    data class ReadyToInstall(val versionName: String, val fileName: String) : UpdateState
}

private const val TAG = "UpdateManager"

object UpdateManager {
    var updateState by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set

    var hasLocalApk by mutableStateOf(false)
        private set

    private var lastCheckTime: Long = 0

    private val mainHandler = Handler(Looper.getMainLooper())

    private fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        mainHandler.post {
            Toast.makeText(context, message, duration).show()
        }
    }

    fun getDownloadDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "updates")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getApkFile(context: Context, fileName: String): File {
        return File(getDownloadDir(context), fileName)
    }

    fun hasDownloadedApk(context: Context): Boolean {
        return try {
            val dir = getDownloadDir(context)
            dir.listFiles()?.any {
                it.isFile && it.name.startsWith("QuickDash-v") && it.name.endsWith(".apk")
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun deleteDownloadedApks(context: Context) {
        try {
            val dir = getDownloadDir(context)
            dir.listFiles()?.forEach {
                if (it.isFile && it.name.startsWith("QuickDash-v") && it.name.endsWith(".apk")) {
                    it.delete()
                }
            }
            try {
                val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                publicDir.listFiles()?.forEach {
                    if (it.isFile && it.name.startsWith("QuickDash-v") && it.name.endsWith(".apk")) {
                        it.delete()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete APK from public downloads", e)
            }
            hasLocalApk = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete downloaded APKs", e)
        }
    }

    fun resetState() {
        updateState = UpdateState.Idle
    }

    fun checkForUpdates(context: Context, manual: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!manual && now - lastCheckTime < 5000) return
        lastCheckTime = now

        updateState = UpdateState.Checking

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode
                }

                val apiInfo = QuickDashApiClient.checkForUpdates(currentVersionCode)
                withContext(Dispatchers.Main) {
                    if (apiInfo.hasUpdate) {
                        updateState = UpdateState.UpdateAvailable(
                            versionName = apiInfo.latestVersion,
                            apkUrl = apiInfo.apkUrl,
                            versionCode = apiInfo.versionCode,
                            changelog = apiInfo.changelog,
                            sha256 = apiInfo.sha256,
                            isCritical = apiInfo.isCritical
                        )
                    } else {
                        updateState = if (manual) UpdateState.UpToDate else UpdateState.Idle
                        hasLocalApk = hasDownloadedApk(context)
                        if (manual) {
                            Toast.makeText(context, "QuickDash is on the latest version! ✅", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update check failed", e)
                withContext(Dispatchers.Main) {
                    updateState = UpdateState.Error(e.localizedMessage ?: "Failed to check for updates")
                    hasLocalApk = hasDownloadedApk(context)
                }
            }
        }
    }

    fun startDownload(context: Context, urlStr: String, remoteVersionName: String, expectedSha256: String = "") {
        val fileName = "QuickDash-v$remoteVersionName.apk"
        val destFile = getApkFile(context, fileName)

        if (destFile.exists()) destFile.delete()

        updateState = UpdateState.Downloading(remoteVersionName, 0)

        CoroutineScope(Dispatchers.IO).launch {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlStr)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 15000
                    instanceFollowRedirects = true
                    connect()
                }

                val fileLength = connection.contentLength
                val data = ByteArray(8192)
                var total: Long = 0
                var count: Int
                var lastProgressUpdate = -1
                var lastUpdateTime = 0L

                connection.inputStream.use { input ->
                    destFile.outputStream().use { output ->
                        while (input.read(data).also { count = it } != -1) {
                            total += count
                            output.write(data, 0, count)
                            if (fileLength > 0) {
                                val progress = (total * 100 / fileLength).toInt()
                                val now = System.currentTimeMillis()
                                if (progress != lastProgressUpdate && now - lastUpdateTime > 80) {
                                    withContext(Dispatchers.Main) {
                                        updateState = UpdateState.Downloading(remoteVersionName, progress)
                                    }
                                    lastProgressUpdate = progress
                                    lastUpdateTime = now
                                }
                            }
                        }
                        output.flush()
                    }
                }

                // Verify checksum if provided
                if (expectedSha256.isNotBlank()) {
                    val computedSha = computeFileSha256(destFile)
                    if (!computedSha.equals(expectedSha256.trim(), ignoreCase = true)) {
                        Log.w(TAG, "Checksum mismatch! Expected: $expectedSha256, Computed: $computedSha")
                    }
                }

                withContext(Dispatchers.Main) {
                    updateState = UpdateState.ReadyToInstall(remoteVersionName, fileName)
                    hasLocalApk = true
                    installApk(context, fileName)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                if (destFile.exists()) destFile.delete()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    updateState = UpdateState.Error("Download failed: ${e.localizedMessage}")
                    hasLocalApk = hasDownloadedApk(context)
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun computeFileSha256(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compute SHA-256", e)
            ""
        }
    }

    fun installApk(context: Context, fileName: String) {
        try {
            val file = getApkFile(context, fileName)
            if (!file.exists()) {
                showToast(context, "APK file not found. Please download again.")
                updateState = UpdateState.Idle
                return
            }

            // Android 8.0+ (API 26+) package install permission check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    showToast(
                        context,
                        "Please enable 'Install unknown apps' permission to install QuickDash updates 📦",
                        Toast.LENGTH_LONG
                    )
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Grant read permission explicitly to matching installer activities
            val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
            for (resolveInfo in resolveInfos) {
                val pkgName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(pkgName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Install failed", e)
            showToast(context, "Install failed: ${e.localizedMessage}", Toast.LENGTH_LONG)
        }
    }
}