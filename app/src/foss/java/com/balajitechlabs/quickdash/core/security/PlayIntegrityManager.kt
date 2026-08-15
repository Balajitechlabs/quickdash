package com.balajitechlabs.quickdash.core.security

import android.content.Context

/**
 * 🌿 FOSS Edition PlayIntegrityManager.
 * Zero-tracker no-op implementation.
 */
object PlayIntegrityManager {
    @Suppress("UNUSED_PARAMETER")
    suspend fun requestIntegrityToken(context: Context, nonce: String): String? = null
}
