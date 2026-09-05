/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr/presentation
 * File: QuickQrScannerScreen.kt
 * Description: Full QR code scanner and generator screen with recent scan history and payload actions.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.presentation

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.features.qr.presentation.components.QrRecentScansSection
import com.balajitechlabs.quickdash.features.qr.presentation.components.QrResultCard
import com.balajitechlabs.quickdash.features.qr.presentation.components.QrScannerHeroPod
import com.balajitechlabs.quickdash.features.qr.utils.QrActionHelper
import com.balajitechlabs.quickdash.features.qr.utils.QrPayloadParser
import com.balajitechlabs.quickdash.features.qr.utils.QrScannerHelper
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.ScanContract

@Composable
fun QuickQrScannerScreen(
    hapticEnabled: Boolean = true
) {
    val context = LocalContext.current
    var lastScannedResult by remember { mutableStateOf<String?>(null) }
    val recentScans = remember { mutableStateListOf<String>() }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result != null && !result.contents.isNullOrBlank()) {
            lastScannedResult = result.contents
            if (!recentScans.contains(result.contents)) {
                recentScans.add(0, result.contents)
            }
            Toast.makeText(context, "Scanned successfully", Toast.LENGTH_SHORT).show()
        }
    }

    val screenImagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val stream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(stream)
                val w = bmp.width
                val h = bmp.height
                val pixels = IntArray(w * h)
                bmp.getPixels(pixels, 0, w, 0, 0, w, h)
                val source = RGBLuminanceSource(w, h, pixels)

                val hints = mapOf(
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
                val reader = MultiFormatReader().apply { setHints(hints) }

                val result = try {
                    val binaryBmp = BinaryBitmap(HybridBinarizer(source))
                    reader.decodeWithState(binaryBmp)
                } catch (_: Exception) {
                    try {
                        val globalBmp = BinaryBitmap(GlobalHistogramBinarizer(source))
                        reader.decodeWithState(globalBmp)
                    } catch (_: Exception) {
                        val invertedBmp = BinaryBitmap(HybridBinarizer(source.invert()))
                        reader.decodeWithState(invertedBmp)
                    }
                }
                lastScannedResult = result.text
                if (!recentScans.contains(result.text)) {
                    recentScans.add(0, result.text)
                }
                Toast.makeText(context, "Code detected successfully", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(context, "No readable QR code or barcode found in image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        QrScannerHeroPod(
            onOpenCameraScanner = { scanLauncher.launch(QrScannerHelper.defaultOptions()) },
            onOpenImagePicker = { screenImagePicker.launch("image/*") },
            hapticEnabled = hapticEnabled
        )

        if (lastScannedResult != null) {
            val parsedResult = remember(lastScannedResult) {
                QrPayloadParser.parse(lastScannedResult!!)
            }

            QrResultCard(
                parsedResult = parsedResult,
                onExecuteAction = { QrActionHelper.executePrimaryAction(context, parsedResult) },
                onCopyText = { QrActionHelper.copyToClipboard(context, parsedResult.raw) },
                onShareText = { QrActionHelper.shareText(context, parsedResult.raw) },
                hapticEnabled = hapticEnabled
            )
        }

        if (recentScans.isNotEmpty()) {
            QrRecentScansSection(
                recentScans = recentScans,
                onClearScans = { recentScans.clear() },
                onSelectScan = { scanText -> lastScannedResult = scanText },
                onCopyScan = { scanText -> QrActionHelper.copyToClipboard(context, scanText) },
                hapticEnabled = hapticEnabled
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}
