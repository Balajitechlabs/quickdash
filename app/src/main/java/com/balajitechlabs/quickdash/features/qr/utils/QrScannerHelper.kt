/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr
 * File: QrScannerHelper.kt
 * Description: Universal QR & Barcode Scanner Helper powered by open-source ZXing Android Embedded.
 *              100% offline, zero Google Play Services dependency, zero reflection, 100% crash-free.
 * Developer: balajitechlabs
 */

package com.balajitechlabs.quickdash.features.qr.utils

import android.content.Context
import android.content.Intent
import com.balajitechlabs.quickdash.features.qr.presentation.QrScanActivity
import com.journeyapps.barcodescanner.ScanOptions

/**
 * Universal QR & Barcode Scanner Helper (`QrScannerHelper.kt`).
 * Powered by open-source ZXing Android Embedded.
 * Detects all 1D barcodes (EAN-13, EAN-8, UPC, Code 128, etc.) and 2D codes (QR, Aztec, Data Matrix, PDF417).
 */
object QrScannerHelper {

    private var scanCallback: ((String) -> Unit)? = null
    private var errorCallback: ((String) -> Unit)? = null

    fun defaultOptions(prompt: String = "Point camera at a QR code or barcode"): ScanOptions {
        return ScanOptions().apply {
            setPrompt(prompt)
            setBeepEnabled(true)
            setOrientationLocked(true)
            setBarcodeImageEnabled(false)
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setCaptureActivity(com.balajitechlabs.quickdash.features.qr.presentation.CustomCaptureActivity::class.java)
        }
    }

    fun startScan(
        context: Context,
        onResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        scanCallback = onResult
        errorCallback = onError

        try {
            val intent = Intent(context, QrScanActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            onError("Unable to launch camera scanner: ${e.message}")
        }
    }

    fun onScanResult(result: String?) {
        if (!result.isNullOrBlank()) {
            scanCallback?.invoke(result)
        } else {
            errorCallback?.invoke("Scanning cancelled")
        }
        clearCallbacks()
    }

    fun onScanError(error: String) {
        errorCallback?.invoke(error)
        clearCallbacks()
    }

    private fun clearCallbacks() {
        scanCallback = null
        errorCallback = null
    }
}
