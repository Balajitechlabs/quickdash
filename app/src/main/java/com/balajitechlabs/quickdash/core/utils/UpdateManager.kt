package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.balajitechlabs.quickdash.core.network.QuickDashApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState
    data class Error(val message: String) : UpdateState
    data class UpdateAvailable(val versionName: String, val apkUrl: String, val versionCode: Int) : UpdateState
    data class Downloading(val versionName: String, val progress: Int) : UpdateState
    data class ReadyToInstall(val versionName: String, val fileName: String) : UpdateState
}

object UpdateManager {
    var updateState by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set

    var hasLocalApk by mutableStateOf(false)
        private set

    private var lastCheckTime: Long = 0

    private fun getDownloadDir(context: Context): File {
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
            } catch (_: Exception) {}
            hasLocalApk = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                    updateState = UpdateState.UpdateAvailable(
                        versionName = apiInfo.latestVersion,
                        apkUrl = apiInfo.apkUrl,
                        versionCode = apiInfo.versionCode
                    )
                    return@launch
                }

                updateState = UpdateState.Idle
                hasLocalApk = hasDownloadedApk(context)
                if (manual) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "QuickDash is up to date! ✅", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateState = UpdateState.Error(e.localizedMessage ?: "Failed to check for updates")
                hasLocalApk = hasDownloadedApk(context)
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
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.connect()

                val fileLength = connection.contentLength
                val inputStream = connection.inputStream
                val outputStream = destFile.outputStream()

                val data = ByteArray(4096)
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
                        if (progress != lastProgressUpdate && now - lastUpdateTime > 100) {
                            updateState = UpdateState.Downloading(remoteVersionName, progress)
                            lastProgressUpdate = progress
                            lastUpdateTime = now
                        }
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                connection.disconnect()

                updateState = UpdateState.ReadyToInstall(remoteVersionName, fileName)
            } catch (e: Exception) {
                e.printStackTrace()
                if (destFile.exists()) destFile.delete()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
                updateState = UpdateState.Idle
                hasLocalApk = hasDownloadedApk(context)
            }
        }
    }

    fun installApk(context: Context, fileName: String) {
        try {
            val file = getApkFile(context, fileName)
            if (!file.exists()) {
                Toast.makeText(context, "APK file not found. Please download again.", Toast.LENGTH_SHORT).show()
                updateState = UpdateState.Idle
                return
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
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Install failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}