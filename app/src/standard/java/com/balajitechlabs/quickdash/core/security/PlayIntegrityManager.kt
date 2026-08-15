package com.balajitechlabs.quickdash.core.security

import android.content.Context
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlinx.coroutines.tasks.await

/**
 * Google Play Integrity API Manager.
 * Cryptographically verifies binary integrity and device verdict.
 */
object PlayIntegrityManager {

    private const val TAG = "PlayIntegrityManager"
    // Optional Cloud Project Number for server-side decryption token binding
    private const val CLOUD_PROJECT_NUMBER = 1056492348572L 

    suspend fun requestIntegrityToken(context: Context, nonce: String): String? {
        return try {
            val integrityManager = IntegrityManagerFactory.create(context)
            val request = IntegrityTokenRequest.builder()
                .setCloudProjectNumber(CLOUD_PROJECT_NUMBER)
                .setNonce(nonce)
                .build()

            val response = integrityManager.requestIntegrityToken(request).await()
            val token = response.token()
            Log.d(TAG, "Successfully fetched Play Integrity token")
            token
        } catch (e: Exception) {
            Log.w(TAG, "Play Integrity check failed or unverified environment: ${e.message}")
            null
        }
    }
}
