package com.balajitechlabs.quickdash.features.ai

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import java.io.File

/**
 * Manages exactly 2 on-device AI model downloads.
 *
 * Both models are from Google's litert-community on HuggingFace (public, no auth token needed).
 * Both are in MediaPipe .task format — the only format supported by LlmInference on Android.
 *
 * Download IDs are stored in SharedPreferences so they survive screen navigation.
 * File destination uses setDestinationInExternalFilesDir() — correct for Android 10+ scoped storage.
 */
object LocalModelManager {

    // ─── Model definitions ────────────────────────────────────────────────────

    data class ModelInfo(
        val id: String,
        val name: String,
        val tagline: String,           // One-line capability summary
        val sizeLabel: String,         // "~600 MB"
        val sizeBytes: Long,
        val downloadUrl: String,
        val filename: String,
        val recommended: Boolean = false
    )

    data class DownloadProgress(
        val downloadedBytes: Long,
        val totalBytes: Long,
        val percent: Int,
        val speedBytesPerSec: Long,    // Rolling speed from DownloadManager
        val downloadId: Long
    ) {
        val etaSeconds: Long
            get() = if (speedBytesPerSec > 0) (totalBytes - downloadedBytes) / speedBytesPerSec else -1L
    }

    sealed class ModelStatus {
        object NotDownloaded : ModelStatus()
        data class Downloading(val progress: DownloadProgress) : ModelStatus()
        object Downloaded : ModelStatus()
        data class Failed(val reason: String) : ModelStatus()
    }

    /**
     * The 2 production-ready models.
     * Both: Google litert-community · Public HuggingFace repos · MediaPipe .task format.
     * No HuggingFace token required.
     */
    val AVAILABLE_MODELS = listOf(
        ModelInfo(
            id = "gemma3-1b-it",
            name = "Gemma 3 1B",
            tagline = "Fastest • Great for grammar, rephrasing & summaries",
            sizeLabel = "~600 MB",
            sizeBytes = 630_000_000L,
            downloadUrl = "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task",
            filename = "gemma3-1b-it-int4.task",
            recommended = false
        ),
        ModelInfo(
            id = "gemma2-2b-it",
            name = "Gemma 2 2B",
            tagline = "Best quality • Deeper reasoning, code & long-form tasks",
            sizeLabel = "~1.1 GB",
            sizeBytes = 1_150_000_000L,
            downloadUrl = "https://huggingface.co/litert-community/Gemma2-2B-IT/resolve/main/Gemma2-2B-IT_multi-prefill-seq_q8_ekv2048.task",
            filename = "gemma2-2b-it.task",
            recommended = true
        )
    )

    // ─── SharedPreferences keys ───────────────────────────────────────────────

    private const val PREFS = "qdash_model_prefs"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun saveId(ctx: Context, modelId: String, dlId: Long) =
        prefs(ctx).edit().putLong("dl_$modelId", dlId).apply()

    fun storedId(ctx: Context, modelId: String): Long =
        prefs(ctx).getLong("dl_$modelId", -1L)

    private fun clearId(ctx: Context, modelId: String) =
        prefs(ctx).edit().remove("dl_$modelId").apply()

    // ─── Model file helpers ───────────────────────────────────────────────────

    fun modelDir(ctx: Context): File {
        val dir = ctx.getExternalFilesDir("ai_models") ?: File(ctx.filesDir, "ai_models")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** Returns the model file if it exists and is at least 10 MB (guards against stubs). */
    fun modelFile(ctx: Context, modelId: String): File? {
        val m = AVAILABLE_MODELS.find { it.id == modelId } ?: return null
        val f = File(modelDir(ctx), m.filename)
        return if (f.exists() && f.length() > 10_485_760L) f else null
    }

    fun isReady(ctx: Context, modelId: String) = modelFile(ctx, modelId) != null

    // ─── Status ───────────────────────────────────────────────────────────────

    fun status(ctx: Context, modelId: String): ModelStatus {
        // 1. Check disk first
        if (isReady(ctx, modelId)) {
            clearId(ctx, modelId)
            return ModelStatus.Downloaded
        }
        // 2. Check stored download ID
        val dlId = storedId(ctx, modelId)
        if (dlId != -1L) return queryDmStatus(ctx, dlId)
        return ModelStatus.NotDownloaded
    }

    private fun queryDmStatus(ctx: Context, dlId: Long): ModelStatus {
        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val q = DownloadManager.Query().setFilterById(dlId)
        val c: Cursor = dm.query(q)
        if (!c.moveToFirst()) { c.close(); return ModelStatus.NotDownloaded }

        val st = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
        val done = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
        val total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        val reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
        c.close()

        return when (st) {
            DownloadManager.STATUS_PENDING ->
                ModelStatus.Downloading(DownloadProgress(0, total.coerceAtLeast(1), 0, 0, dlId))
            DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PAUSED -> {
                val pct = if (total > 0) ((done * 100) / total).toInt() else 0
                ModelStatus.Downloading(DownloadProgress(done, total.coerceAtLeast(1), pct, 0, dlId))
            }
            DownloadManager.STATUS_SUCCESSFUL -> ModelStatus.Downloaded
            DownloadManager.STATUS_FAILED -> ModelStatus.Failed(friendlyError(reason))
            else -> ModelStatus.NotDownloaded
        }
    }

    // ─── Download control ─────────────────────────────────────────────────────

    /** Enqueues a download. Returns the DownloadManager ID (> 0) or -1 on failure. */
    fun startDownload(ctx: Context, modelId: String): Long {
        val m = AVAILABLE_MODELS.find { it.id == modelId } ?: return -1L

        // Cancel previous attempt
        val prev = storedId(ctx, modelId)
        if (prev != -1L) runCatching {
            (ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).remove(prev)
        }
        File(modelDir(ctx), m.filename).delete()   // Remove partial file

        val req = DownloadManager.Request(Uri.parse(m.downloadUrl)).apply {
            setTitle("QuickDash · Downloading ${m.name}")
            setDescription("${m.sizeLabel} AI model — on-device, private")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            // ✅ Correct for Android 10+ scoped storage — no WRITE_EXTERNAL_STORAGE needed
            setDestinationInExternalFilesDir(ctx, "ai_models", m.filename)
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
            setAllowedOverMetered(true)
            setAllowedOverRoaming(false)
        }

        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val dlId = dm.enqueue(req)
        saveId(ctx, modelId, dlId)   // Persist → survives recomposition & navigation
        return dlId
    }

    fun cancelDownload(ctx: Context, modelId: String) {
        val dlId = storedId(ctx, modelId)
        if (dlId != -1L) runCatching {
            (ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).remove(dlId)
        }
        clearId(ctx, modelId)
        val m = AVAILABLE_MODELS.find { it.id == modelId } ?: return
        File(modelDir(ctx), m.filename).delete()
    }

    fun deleteModel(ctx: Context, modelId: String) {
        val m = AVAILABLE_MODELS.find { it.id == modelId } ?: return
        File(modelDir(ctx), m.filename).delete()
        clearId(ctx, modelId)
    }

    // ─── Formatting helpers ───────────────────────────────────────────────────

    fun fmtBytes(b: Long): String = when {
        b >= 1_000_000_000L -> "%.1f GB".format(b / 1e9)
        b >= 1_000_000L     -> "%.0f MB".format(b / 1e6)
        b >= 1_000L         -> "%.0f KB".format(b / 1e3)
        else                -> "$b B"
    }

    fun fmtSpeed(bytesPerSec: Long): String = when {
        bytesPerSec >= 1_000_000L -> "%.1f MB/s".format(bytesPerSec / 1e6)
        bytesPerSec >= 1_000L     -> "%.0f KB/s".format(bytesPerSec / 1e3)
        else                      -> ""
    }

    fun fmtEta(etaSec: Long): String = when {
        etaSec <= 0        -> ""
        etaSec < 60        -> "~${etaSec}s left"
        etaSec < 3600      -> "~${etaSec / 60}min left"
        else               -> "~${etaSec / 3600}hr left"
    }

    fun totalStorageUsed(ctx: Context): String {
        val total = modelDir(ctx).listFiles()?.sumOf { it.length() } ?: 0L
        return if (total > 0) fmtBytes(total) else ""
    }

    private fun friendlyError(reason: Int): String = when (reason) {
        DownloadManager.ERROR_INSUFFICIENT_SPACE  -> "Not enough storage space. Free up space and retry."
        DownloadManager.ERROR_FILE_ERROR          -> "File write error. Check available storage."
        DownloadManager.ERROR_HTTP_DATA_ERROR     -> "Server returned bad data. Retry."
        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "HTTP error. Check internet and retry."
        403                                       -> "Access denied (403). The model URL may have moved."
        404                                       -> "Model not found (404). URL may have changed."
        else                                      -> "Download failed (error $reason). Check internet and retry."
    }
}
