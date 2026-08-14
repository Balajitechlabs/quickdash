package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
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
import android.util.Log
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

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
                if (apiInfo.hasUpdate) {
                    CoroutineScope(Dispatchers.Main).launch {
                        updateState = UpdateState.UpdateAvailable(
                            versionName = apiInfo.latestVersion,
                            apkUrl = apiInfo.apkUrl,
                            versionCode = apiInfo.versionCode,
                            changelog = apiInfo.changelog,
                            isCritical = apiInfo.isCritical
                        )
                    }
                    return@launch
                }

                CoroutineScope(Dispatchers.Main).launch {
                    updateState = if (manual) UpdateState.UpToDate else UpdateState.Idle
                    hasLocalApk = hasDownloadedApk(context)
                    if (manual) {
                        Toast.makeText(context, "QuickDash is on the latest version! ✅", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update check failed", e)
                CoroutineScope(Dispatchers.Main).launch {
                    updateState = UpdateState.Error(e.localizedMessage ?: "Failed to check for updates")
                    hasLocalApk = hasDownloadedApk(context)
                }
            }
        }
    }

    fun startDownload(context: Context, urlStr: String, remoteVersionName: String) {
        val fileName = "QuickDash-v$remoteVersionName.apk"
        val destFile = getApkFile(context, fileName)

        if (destFile.exists()) destFile.delete()

        updateState = UpdateState.Downloading(remoteVersionName, 0)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlStr)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = true
                connection.connect()

                val fileLength = connection.contentLength
                val inputStream = connection.inputStream
                val outputStream = destFile.outputStream()

                val data = ByteArray(8192)
                var total: Long = 0
                var count: Int
                var lastProgressUpdate = -1
                var lastUpdateTime = 0L

                while (inputStream.read(data).also { count = it } != -1) {
                    total += count
                    outputStream.write(data, 0, count)
                    if (fileLength > 0) {
                        val progress = (total * 100 / fileLength).toInt()
                        val now = System.currentTimeMillis()
                        if (progress != lastProgressUpdate && now - lastUpdateTime > 80) {
                            CoroutineScope(Dispatchers.Main).launch {
                                updateState = UpdateState.Downloading(remoteVersionName, progress)
                            }
                            lastProgressUpdate = progress
                            lastUpdateTime = now
                        }
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                connection.disconnect()

                CoroutineScope(Dispatchers.Main).launch {
                    updateState = UpdateState.ReadyToInstall(remoteVersionName, fileName)
                    hasLocalApk = true
                    // Automatically trigger install prompt when download completes
                    installApk(context, fileName)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                if (destFile.exists()) destFile.delete()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    updateState = UpdateState.Error("Download failed: ${e.localizedMessage}")
                    hasLocalApk = hasDownloadedApk(context)
                }
            }
        }
    }

    fun installApk(context: Context, fileName: String) {
        try {
            val file = getApkFile(context, fileName)
            if (!file.exists()) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "APK file not found. Please download again.", Toast.LENGTH_SHORT).show()
                    updateState = UpdateState.Idle
                }
                return
            }

            // Android 8.0+ (API 26+) package install permission check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            context,
                            "Please enable 'Install unknown apps' permission to install QuickDash updates 📦",
                            Toast.LENGTH_LONG
                        ).show()
                    }
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
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Grant read permission explicitly
            val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
            for (resolveInfo in resolveInfos) {
                val pkgName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(pkgName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Install failed", e)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Install failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}