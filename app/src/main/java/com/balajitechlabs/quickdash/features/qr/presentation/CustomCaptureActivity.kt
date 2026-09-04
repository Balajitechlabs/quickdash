/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr
 * File: CustomCaptureActivity.kt
 * Description: Custom EssentialX-styled Barcode & QR Camera Scanner Activity
 * Developer: balajitechlabs
 */

package com.balajitechlabs.quickdash.features.qr.presentation

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.balajitechlabs.quickdash.R
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class CustomCaptureActivity : CaptureActivity() {

    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private var isTorchOn = false

    override fun initializeContent(): DecoratedBarcodeView {
        setContentView(R.layout.activity_custom_qr_scanner)
        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner)
        return barcodeScannerView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val btnBack = findViewById<View>(R.id.btn_scanner_back)
        btnBack?.setOnClickListener {
            finish()
        }

        val btnFlash = findViewById<ImageButton>(R.id.btn_scanner_flash)
        if (!hasFlash()) {
            btnFlash?.visibility = View.GONE
        } else {
            btnFlash?.setOnClickListener {
                isTorchOn = !isTorchOn
                if (isTorchOn) {
                    barcodeScannerView.setTorchOn()
                    btnFlash.setImageResource(R.drawable.ic_flash_on)
                } else {
                    barcodeScannerView.setTorchOff()
                    btnFlash.setImageResource(R.drawable.ic_flash_off)
                }
            }
        }
    }

    private fun hasFlash(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }
}
