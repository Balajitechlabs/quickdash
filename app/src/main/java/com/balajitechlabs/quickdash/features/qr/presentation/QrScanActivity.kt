/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr
 * File: QrScanActivity.kt
 * Description: Universal ZXing camera scanner launcher activity
 * Developer: balajitechlabs
 */

package com.balajitechlabs.quickdash.features.qr.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.balajitechlabs.quickdash.features.qr.utils.QrScannerHelper
import com.journeyapps.barcodescanner.ScanContract

class QrScanActivity : ComponentActivity() {

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result != null && !result.contents.isNullOrBlank()) {
            QrScannerHelper.onScanResult(result.contents)
        } else {
            QrScannerHelper.onScanResult(null)
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanLauncher.launch(QrScannerHelper.defaultOptions())
    }
}
