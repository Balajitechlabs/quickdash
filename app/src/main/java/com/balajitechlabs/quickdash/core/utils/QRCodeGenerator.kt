/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/utils
 * File: QRCodeGenerator.kt
 * Description: EssentialX-styled component for core/utils supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.balajitechlabs.quickdash.R

object QRCodeGenerator {

    @Throws(WriterException::class)
    fun generateQRCode(
        context: Context,
        text: String,
        width: Int = 512,
        height: Int = 512,
        qrColor: Int = Color.BLACK,
        centerEmoji: String? = null,
        qrGradientColors: Pair<Int, Int>? = null,
        useCircularDots: Boolean = false,
        addBrandingFooter: Boolean = false
    ): Bitmap {
        val hints = HashMap<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.MARGIN, 1)
        }

        // Fast ZXing matrix encoding
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            512,
            512,
            hints
        )

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        val footerHeight = if (addBrandingFooter) 60 else 0
        val totalHeight = matrixHeight + footerHeight

        val bitmap = Bitmap.createBitmap(matrixWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = qrColor
            style = Paint.Style.FILL
        }

        if (qrGradientColors != null) {
            val (startColor, endColor) = qrGradientColors
            paint.shader = android.graphics.LinearGradient(
                0f, 0f, matrixWidth.toFloat(), matrixHeight.toFloat(),
                startColor, endColor,
                android.graphics.Shader.TileMode.CLAMP
            )
        }

        // Fast pixel rendering via array or module grid
        val pixels = IntArray(matrixWidth * matrixHeight)
        for (y in 0 until matrixHeight) {
            val offset = y * matrixWidth
            for (x in 0 until matrixWidth) {
                pixels[offset + x] = if (bitMatrix[x, y]) qrColor else Color.WHITE
            }
        }
        bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)

        // Center badge cutout
        val badgeSize = (matrixWidth * 0.22f).toInt()
        val badgeLeft = (matrixWidth - badgeSize) / 2f
        val badgeTop = (matrixHeight - badgeSize) / 2f
        val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeSize, badgeTop + badgeSize)

        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE0E0E0.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        val cornerRadius = badgeSize * 0.25f
        canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, badgeBgPaint)
        canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, badgeBorderPaint)

        // Draw Center Logo / Emoji
        if (!centerEmoji.isNullOrBlank()) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = badgeSize * 0.65f
                textAlign = Paint.Align.CENTER
            }
            val x = matrixWidth / 2f
            val y = matrixHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(centerEmoji, x, y, textPaint)
        } else {
            val logo = ContextCompat.getDrawable(context, R.drawable.app_logo)
                ?: ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)
            if (logo != null) {
                val pad = (badgeSize * 0.15f).toInt()
                logo.setBounds(
                    (badgeLeft + pad).toInt(),
                    (badgeTop + pad).toInt(),
                    (badgeLeft + badgeSize - pad).toInt(),
                    (badgeTop + badgeSize - pad).toInt()
                )
                logo.draw(canvas)
            }
        }

        // Optional footer branding: "Made with QuickDash  ||BTL||™"
        if (addBrandingFooter) {
            val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF424242.toInt()
                textSize = 20f
                textAlign = Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            canvas.drawText("Made with QuickDash  ||BTL||™", matrixWidth / 2f, matrixHeight + 38f, brandPaint)
        }

        return bitmap
    }
}