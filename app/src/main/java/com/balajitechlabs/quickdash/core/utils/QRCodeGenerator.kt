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
        width: Int, 
        height: Int,
        qrColor: Int = Color.BLACK,
        centerEmoji: String? = null,
        qrGradientColors: Pair<Int, Int>? = null,
        useCircularDots: Boolean = false
    ): Bitmap {
        val hints = HashMap<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.MARGIN, 1)
        }
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            width,
            height,
            hints
        )
        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background
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

        val dotSize = matrixWidth.toFloat() / bitMatrix.width.toFloat()

        for (y in 0 until matrixHeight) {
            for (x in 0 until matrixWidth) {
                if (bitMatrix[x, y]) {
                    val left = x * dotSize
                    val top = y * dotSize
                    val right = left + dotSize
                    val bottom = top + dotSize

                    val isFinderPattern = (x in 0..6 && y in 0..6) ||
                            (x >= bitMatrix.width - 7 && y in 0..6) ||
                            (x in 0..6 && y >= bitMatrix.height - 7)

                    if (useCircularDots && !isFinderPattern) {
                        val centerX = left + dotSize / 2f
                        val centerY = top + dotSize / 2f
                        val radius = (dotSize / 2f) * 0.85f
                        canvas.drawCircle(centerX, centerY, radius, paint)
                    } else {
                        canvas.drawRect(left, top, right, bottom, paint)
                    }
                }
            }
        }

        // Draw center overlay (emoji if provided, otherwise launcher logo)
        if (!centerEmoji.isNullOrBlank()) {
            val logoSize = (width * 0.20).toInt()
            val logoLeft = (width - logoSize) / 2
            val logoTop = (height - logoSize) / 2
            val logoRight = logoLeft + logoSize
            val logoBottom = logoTop + logoSize

            val borderSize = (logoSize * 0.15).toInt()
            val cardRect = RectF(
                (logoLeft - borderSize).toFloat(),
                (logoTop - borderSize).toFloat(),
                (logoRight + borderSize).toFloat(),
                (logoBottom + borderSize).toFloat()
            )
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(cardRect, borderSize.toFloat() * 1.5f, borderSize.toFloat() * 1.5f, cardPaint)

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = logoSize.toFloat() * 0.85f
                textAlign = Paint.Align.CENTER
            }
            val x = width / 2f
            val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(centerEmoji, x, y, textPaint)
        } else {
            val logoDrawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)
            if (logoDrawable != null) {
                val logoSize = (width * 0.20).toInt() // Logo occupies 20% of QR width
                val logoLeft = (width - logoSize) / 2
                val logoTop = (height - logoSize) / 2
                val logoRight = logoLeft + logoSize
                val logoBottom = logoTop + logoSize

                // Background circle/card for logo to clear QR code elements under it
                val borderSize = (logoSize * 0.10).toInt()
                val cardRect = RectF(
                    (logoLeft - borderSize).toFloat(),
                    (logoTop - borderSize).toFloat(),
                    (logoRight + borderSize).toFloat(),
                    (logoBottom + borderSize).toFloat()
                )
                val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(cardRect, borderSize.toFloat() * 1.5f, borderSize.toFloat() * 1.5f, cardPaint)

                // Draw logo inside
                val padding = (logoSize * 0.10).toInt()
                logoDrawable.setBounds(
                    logoLeft + padding,
                    logoTop + padding,
                    logoRight - padding,
                    logoBottom - padding
                )
                logoDrawable.draw(canvas)
            }
        }

        return bitmap
    }
}