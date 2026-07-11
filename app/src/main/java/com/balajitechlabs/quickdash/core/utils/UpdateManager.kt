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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateState {
    object Idle : UpdateState
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

    /**
     * Get the app-private downloads directory. This is where APKs are saved
     * so that FileProvider can always access and serve them without any storage permissions.
     */
    private fun getDownloadDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "updates")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Get the APK file for a specific version.
     */
    fun getApkFile(context: Context, fileName: String): File {
        return File(getDownloadDir(context), fileName)
    }

    /**
     * Check if any QuickDash APK exists in our private download directory.
     */
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

    /**
     * Delete ALL downloaded QuickDash APKs from private directory.
     */
    fun deleteDownloadedApks(context: Context) {
        try {
            val dir = getDownloadDir(context)
            dir.listFiles()?.forEach {
                if (it.isFile && it.name.startsWith("QuickDash-v") && it.name.endsWith(".apk")) {
                    it.delete()
                }
            }
            // Also clean public downloads (leftover from old versions)
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

    /**
     * Check for updates by fetching latest release from GitHub API.
     */
    fun checkForUpdates(context: Context, manual: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!manual && now - lastCheckTime < 5000) return
        lastCheckTime = now

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val currentVersionName = packageInfo.versionName ?: "0.0.0"
                // Assuming version names are like "3.2.4"
                
                val url = URL("https://api.github.com/repos/balajitechlabs/quickdash/releases/latest")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                val responseText = connection.inputStream.bufferedReader().readText()
                connection.disconnect()

                val json = JSONObject(responseText)
                val tagName = json.optString("tag_name", "") // e.g., "v3.2.4"
                val remoteVersionName = tagName.removePrefix("v")
                
                var apkUrl = ""
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk")) {
                            apkUrl = asset.optString("browser_download_url", "")
                            break
                        }
                    }
                }

                val activeFileName = "QuickDash-v$remoteVersionName.apk"
                
                // Compare versions (simple string compare works if lengths are equal, e.g. 3.2.4 > 3.2.3, but better to compare components)
                val isNewer = isVersionNewer(currentVersionName, remoteVersionName)
                
                // Clean obsolete APKs
                cleanObsoleteApks(context, if (isNewer) activeFileName else null)

                if (isNewer && apkUrl.isNotEmpty()) {
                    val localFile = getApkFile(context, activeFileName)
                    if (localFile.exists() && localFile.length() > 0) {
                        updateState = UpdateState.ReadyToInstall(remoteVersionName, activeFileName)
                    } else {
                        updateState = UpdateState.UpdateAvailable(remoteVersionName, apkUrl, 0)
                    }
                } else {
                    updateState = UpdateState.Idle
                    hasLocalApk = hasDownloadedApk(context)
                    if (manual) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "You are on the latest version", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                hasLocalApk = hasDownloadedApk(context)
                if (manual) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Failed to check for updates.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun isVersionNewer(current: String, remote: String): Boolean {
        val currParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val remParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(currParts.size, remParts.size)) {
            val c = currParts.getOrElse(i) { 0 }
            val r = remParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    /**
     * Download the APK from the GitHub release URL directly to app-private storage.
     */
    fun startDownload(context: Context, urlStr: String, remoteVersionName: String) {
        val fileName = "QuickDash-v$remoteVersionName.apk"
        val destFile = getApkFile(context, fileName)

        // Delete any existing file first
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
                        // Throttle progress updates to avoid UI stutter
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

    /**
     * Launch the system Package Installer for the given APK file.
     * Uses FileProvider to generate a content:// URI that the installer can read.
     */
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

    /**
     * Delete APKs that don't match the currently-needed update file.
     */
    private fun cleanObsoleteApks(context: Context, activeUpdateFileName: String?) {
        try {
            val dir = getDownloadDir(context)
            dir.listFiles()?.forEach {
                if (it.isFile && it.name.startsWith("QuickDash-v") && it.name.endsWith(".apk")) {
                    if (activeUpdateFileName != null && it.name == activeUpdateFileName) {
                        return@forEach // Keep this one
                    }
                    it.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}