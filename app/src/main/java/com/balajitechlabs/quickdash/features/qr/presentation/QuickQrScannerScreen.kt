/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr
 * File: QuickQrScannerScreen.kt
 * Description: Authentic EssentialX pitch-black Universal QR & Barcode scanner companion
 * Developer: balajitechlabs
 */

package com.balajitechlabs.quickdash.features.qr.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import com.balajitechlabs.quickdash.core.ui.playClickVibration
import com.balajitechlabs.quickdash.core.ui.theme.ColorUtil
import com.balajitechlabs.quickdash.features.qr.utils.QrScannerHelper

/**
 * Authentic EssentialX QR & Barcode Scanner Companion Screen.
 * Developer: balajitechlabs
 */
@Composable
fun QuickQrScannerScreen(
    hapticEnabled: Boolean = true
) {
    val context = LocalContext.current
    var lastScannedResult by remember { mutableStateOf<String?>(null) }

    val scanLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = com.journeyapps.barcodescanner.ScanContract()
    ) { result ->
        if (result != null && !result.contents.isNullOrBlank()) {
            lastScannedResult = result.contents
            Toast.makeText(context, "Scanned successfully ✓", Toast.LENGTH_SHORT).show()
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

        // Scanner Hero Pod
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF38393F),
            border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2B30)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "QR & Barcode Scanner",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Instant camera barcode and QR recognition",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp
                    ),
                    color = Color(0xFFC5C6D0),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        playClickVibration(context, hapticEnabled)
                        scanLauncher.launch(QrScannerHelper.defaultOptions())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000000),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0xFF44474F))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open Camera Scanner",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // On-Screen / Screenshot QR Detector
                val screenImagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        try {
                            val stream = context.contentResolver.openInputStream(uri)
                            val bmp = android.graphics.BitmapFactory.decodeStream(stream)
                            val w = bmp.width
                            val h = bmp.height
                            val pixels = IntArray(w * h)
                            bmp.getPixels(pixels, 0, w, 0, 0, w, h)
                            val source = com.google.zxing.RGBLuminanceSource(w, h, pixels)

                            val hints = mapOf(
                                com.google.zxing.DecodeHintType.TRY_HARDER to true,
                                com.google.zxing.DecodeHintType.POSSIBLE_FORMATS to listOf(
                                    com.google.zxing.BarcodeFormat.QR_CODE,
                                    com.google.zxing.BarcodeFormat.DATA_MATRIX,
                                    com.google.zxing.BarcodeFormat.AZTEC,
                                    com.google.zxing.BarcodeFormat.PDF_417,
                                    com.google.zxing.BarcodeFormat.EAN_13,
                                    com.google.zxing.BarcodeFormat.EAN_8,
                                    com.google.zxing.BarcodeFormat.UPC_A,
                                    com.google.zxing.BarcodeFormat.UPC_E,
                                    com.google.zxing.BarcodeFormat.CODE_128,
                                    com.google.zxing.BarcodeFormat.CODE_39,
                                    com.google.zxing.BarcodeFormat.CODE_93,
                                    com.google.zxing.BarcodeFormat.ITF,
                                    com.google.zxing.BarcodeFormat.CODABAR
                                )
                            )
                            val reader = com.google.zxing.MultiFormatReader().apply { setHints(hints) }

                            val result = try {
                                val binaryBmp = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
                                reader.decodeWithState(binaryBmp)
                            } catch (_: Exception) {
                                try {
                                    val globalBmp = com.google.zxing.BinaryBitmap(com.google.zxing.common.GlobalHistogramBinarizer(source))
                                    reader.decodeWithState(globalBmp)
                                } catch (_: Exception) {
                                    // Try inverted for dark-mode QR codes
                                    val invertedBmp = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source.invert()))
                                    reader.decodeWithState(invertedBmp)
                                }
                            }
                            lastScannedResult = result.text
                            Toast.makeText(context, "Code detected successfully! ✓", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "No readable QR code or barcode found in image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        playClickVibration(context, hapticEnabled)
                        screenImagePicker.launch("image/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0xFF44474F))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open On-Screen QR Detector",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Result Card (EssentialX Style)
        if (lastScannedResult != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF38393F),
                border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scanned Result",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFB0C6FF)
                        )

                        IconButton(
                            onClick = {
                                playClickVibration(context, hapticEnabled)
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("QR Code", lastScannedResult!!))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = lastScannedResult!!,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = Color.White
                    )
                }
            }
        }

        // Bottom clearance for floating Back pill
        Spacer(modifier = Modifier.height(120.dp))
    }
}
