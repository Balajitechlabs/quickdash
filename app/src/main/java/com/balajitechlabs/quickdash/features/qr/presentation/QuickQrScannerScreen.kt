/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/qr/presentation
 * File: QuickQrScannerScreen.kt
 * Description: QR code and barcode scanner screen with off-main-thread gallery decoding, recent scan history, and payload actions.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.presentation

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashTokens
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun QuickQrScannerScreen(
    hapticEnabled: Boolean = true
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var lastScannedResult by remember { mutableStateOf<String?>(null) }
    val recentScans = remember { mutableStateListOf<String>() }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val contents = result?.contents
        if (!contents.isNullOrBlank()) {
            lastScannedResult = contents
            if (!recentScans.contains(contents)) {
                recentScans.add(0, contents)
            }
            Toast.makeText(context, "Scanned successfully", Toast.LENGTH_SHORT).show()
        }
    }

    val screenImagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val decoded = withContext(Dispatchers.Default) {
                decodeBarcodeFromUri(context, uri)
            }
            if (!decoded.isNullOrBlank()) {
                lastScannedResult = decoded
                if (!recentScans.contains(decoded)) {
                    recentScans.add(0, decoded)
                }
                Toast.makeText(context, "Code detected successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No readable QR code or barcode found in image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                horizontal = QuickDashTokens.Spacing.lg,
                vertical = QuickDashTokens.Spacing.sm
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(QuickDashTokens.Spacing.lg)
    ) {
        Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.sm))

        QrScannerHeroPod(
            onOpenCameraScanner = { scanLauncher.launch(QrScannerHelper.defaultOptions()) },
            onOpenImagePicker = { screenImagePicker.launch("image/*") },
            hapticEnabled = hapticEnabled
        )

        lastScannedResult?.let { currentScan ->
            val parsedResult = remember(currentScan) {
                QrPayloadParser.parse(currentScan)
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

        Spacer(modifier = Modifier.height(QuickDashTokens.Spacing.xxl))
    }
}

private fun decodeBarcodeFromUri(context: Context, uri: Uri): String? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    } ?: return null

    val maxDim = 1600
    var sampleSize = 1
    while (bounds.outWidth / sampleSize > maxDim || bounds.outHeight / sampleSize > maxDim) {
        sampleSize *= 2
    }

    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bmp = context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, decodeOpts)
    } ?: return null

    return try {
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
            reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
        } catch (_: Exception) {
            try {
                reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source)))
            } catch (_: Exception) {
                reader.decodeWithState(BinaryBitmap(HybridBinarizer(source.invert())))
            }
        }
        result.text
    } catch (_: Exception) {
        null
    } finally {
        bmp.recycle()
    }
}
