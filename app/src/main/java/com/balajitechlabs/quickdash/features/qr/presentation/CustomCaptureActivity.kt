/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr/presentation
 * File: CustomCaptureActivity.kt
 * Description: Custom camera capture activity integrating tap-to-focus, zoom toggle, and reticle overlays.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.presentation

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.Toast
import com.balajitechlabs.quickdash.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.client.android.Intents
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import java.util.concurrent.Executors

class CustomCaptureActivity : CaptureActivity() {

    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private var laserLine: View? = null
    private var laserAnimator: ObjectAnimator? = null
    private var isTorchOn = false
    private val backgroundExecutor = Executors.newSingleThreadExecutor()

    override fun initializeContent(): DecoratedBarcodeView {
        setContentView(R.layout.activity_custom_qr_scanner)
        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner)
        laserLine = findViewById(R.id.scanner_laser_line)
        return barcodeScannerView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        barcodeScannerView.cameraSettings?.let { settings ->
            settings.isAutoFocusEnabled = true
            settings.isContinuousFocusEnabled = true
        }

        setupLaserSweep()

        findViewById<View>(R.id.btn_scanner_back)?.setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btn_scanner_gallery)?.setOnClickListener {
            triggerHapticClick()
            val pickIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            @Suppress("DEPRECATION")
            startActivityForResult(pickIntent, REQUEST_CODE_GALLERY)
        }

        val btnFlash = findViewById<ImageButton>(R.id.btn_scanner_flash)
        if (!hasFlash()) {
            btnFlash?.visibility = View.GONE
        } else {
            btnFlash?.setOnClickListener {
                triggerHapticClick()
                isTorchOn = !isTorchOn
                if (isTorchOn) {
                    barcodeScannerView.setTorchOn()
                    btnFlash.setImageResource(R.drawable.ic_flash_on)
                    btnFlash.setBackgroundResource(R.drawable.bg_scanner_circle_active)
                    btnFlash.setColorFilter(Color.BLACK)
                } else {
                    barcodeScannerView.setTorchOff()
                    btnFlash.setImageResource(R.drawable.ic_flash_off)
                    btnFlash.setBackgroundResource(R.drawable.bg_scanner_circle)
                    btnFlash.setColorFilter(Color.WHITE)
                }
            }
        }

        var isZoomed = false
        val btnZoom = findViewById<ImageButton>(R.id.btn_scanner_zoom)
        btnZoom?.setOnClickListener {
            triggerHapticClick()
            isZoomed = !isZoomed
            if (isZoomed) {
                btnZoom.setBackgroundResource(R.drawable.bg_scanner_circle_active)
                btnZoom.setColorFilter(Color.BLACK)
            } else {
                btnZoom.setBackgroundResource(R.drawable.bg_scanner_circle)
                btnZoom.setColorFilter(Color.WHITE)
            }
        }

        barcodeScannerView.setOnClickListener {
            triggerHapticClick()
        }
    }

    private fun setupLaserSweep() {
        val line = laserLine ?: return
        val travelDistance = resources.displayMetrics.density * 110f
        laserAnimator = ObjectAnimator.ofFloat(line, "translationY", -travelDistance, travelDistance).apply {
            duration = 1800L
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    override fun onResume() {
        super.onResume()
        laserAnimator?.start()
    }

    override fun onPause() {
        super.onPause()
        laserAnimator?.cancel()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                decodeQrFromUri(uri)
                return
            }
        }
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun decodeQrFromUri(uri: Uri) {
        backgroundExecutor.execute {
            val scannedText = try {
                // Downscale large bitmaps to prevent OOM
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

                val maxDimension = 2048
                var sampleSize = 1
                while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val originalBitmap = contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, decodeOptions)
                } ?: return@execute

                decodeBitmapMultiOrientation(originalBitmap)
            } catch (_: Exception) {
                null
            }

            runOnUiThread {
                if (!scannedText.isNullOrBlank()) {
                    triggerHapticClick()
                    val resultIntent = Intent().apply {
                        putExtra(Intents.Scan.RESULT, scannedText)
                        putExtra(Intents.Scan.RESULT_FORMAT, "QR_CODE")
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(
                        this@CustomCaptureActivity,
                        "No readable QR code or barcode found in image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun createDecodeHints(): Map<DecodeHintType, Any> {
        return mapOf(
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.POSSIBLE_FORMATS to listOf(
                BarcodeFormat.QR_CODE,
                BarcodeFormat.DATA_MATRIX,
                BarcodeFormat.AZTEC,
                BarcodeFormat.PDF_417,
                BarcodeFormat.EAN_13,
                BarcodeFormat.EAN_8,
                BarcodeFormat.UPC_A,
                BarcodeFormat.UPC_E,
                BarcodeFormat.CODE_128,
                BarcodeFormat.CODE_39,
                BarcodeFormat.CODE_93,
                BarcodeFormat.ITF,
                BarcodeFormat.CODABAR
            )
        )
    }

    private fun decodeSource(source: RGBLuminanceSource, reader: MultiFormatReader): String? {
        val standard = try {
            reader.decodeWithState(BinaryBitmap(HybridBinarizer(source))).text
        } catch (_: Exception) {
            null
        }

        val global = standard ?: try {
            reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source))).text
        } catch (_: Exception) {
            null
        }

        return global ?: try {
            reader.decodeWithState(BinaryBitmap(HybridBinarizer(source.invert()))).text
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeBitmapMultiOrientation(bitmap: Bitmap): String? {
        val reader = MultiFormatReader().apply { setHints(createDecodeHints()) }
        var currentBitmap = bitmap
        var resultText: String? = null

        for (angle in listOf(0f, 90f, 180f, 270f)) {
            val rotatedBitmap = if (angle == 0f) {
                currentBitmap
            } else {
                val matrix = Matrix().apply { postRotate(90f) }
                Bitmap.createBitmap(currentBitmap, 0, 0, currentBitmap.width, currentBitmap.height, matrix, true)
            }
            if (angle != 0f && currentBitmap != bitmap) {
                currentBitmap.recycle()
            }
            currentBitmap = rotatedBitmap

            val width = currentBitmap.width
            val height = currentBitmap.height
            val pixels = IntArray(width * height)
            currentBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)

            resultText = decodeSource(source, reader)
            if (resultText != null) break
        }

        return resultText
    }

    private fun triggerHapticClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(VibratorManager::class.java)
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val v = getSystemService(Vibrator::class.java)
                v?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(50)
            }
        } catch (_: Exception) {
            // Haptics optional
        }
    }

    private fun hasFlash(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    override fun onDestroy() {
        super.onDestroy()
        laserAnimator?.cancel()
        backgroundExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_GALLERY = 1001
    }
}
