package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.balajitechlabs.quickdash.R
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    fun shareQrCode(
        context: Context,
        qrBitmap: Bitmap,
        name: String,
        upiId: String,
        amount: String,
        showUpiId: Boolean,
        usePaypal: Boolean = false
    ) {
        val imageFile = generateShareableImage(context, qrBitmap, name, upiId, amount, showUpiId, usePaypal) ?: return
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share QR Code"))
    }

    fun sharePaymentLink(
        context: Context,
        payUrl: String,
        amount: String,
        payeeName: String,
        usePaypal: Boolean = false
    ) {
        val currencySymbol = if (usePaypal) "$" else "₹"
        val amountText = if (amount.isNotBlank()) "$currencySymbol$amount" else "Any Amount"
        val shareText = if (amount.isNotBlank()) {
            "Pay $amountText to $payeeName using this link:\n$payUrl"
        } else {
            "Pay to $payeeName using this link:\n$payUrl"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Payment Link"))
    }

    private fun generateShareableImage(
        context: Context,
        qrBitmap: Bitmap,
        name: String,
        upiId: String,
        amount: String,
        showUpiId: Boolean,
        usePaypal: Boolean = false
    ): File? {
        val currencySymbol = if (usePaypal) "$" else "₹"
        val idTypeLabel = if (usePaypal) "PayPal ID" else "UPI ID"
        try {
            val width = 800
            val height = 1050
            val bitmap = createBitmap(width, height)
            val canvas = Canvas(bitmap)

            // Background
            canvas.drawColor("#f8f9fb".toColorInt())

            val textPaint = Paint().apply {
                color = Color.BLACK
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            // Define QR Code geometry (perfectly centered vertically)
            val qrSize = 520
            val qrLeft = (width - qrSize) / 2
            val qrTop = (height - qrSize) / 2 - 20
            val qrDestRect = Rect(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize)
            canvas.drawBitmap(qrBitmap, null, qrDestRect, null)

            // Draw Payee Name above (if it exists)
            if (name.isNotBlank()) {
                textPaint.textSize = 36f
                textPaint.isFakeBoldText = false
                textPaint.color = "#4c566a".toColorInt()
                canvas.drawText(name, width / 2f, qrTop - 110f, textPaint)
            }

            // Always draw "Scan to Pay" exactly above the QR code
            textPaint.textSize = 55f
            textPaint.isFakeBoldText = true
            textPaint.color = "#4c566a".toColorInt()
            canvas.drawText("Scan to Pay", width / 2f, qrTop - 35f, textPaint)

            // Draw raw ID directly below the QR code (UPI ID or PayPal ID)
            if (showUpiId) {
                textPaint.textSize = 32f
                textPaint.isFakeBoldText = false
                textPaint.color = "#7b88a1".toColorInt()
                canvas.drawText(upiId, width / 2f, qrTop + qrSize + 60f, textPaint)
            }

            // If amount is set, draw it below the ID
            if (amount.isNotBlank()) {
                textPaint.textSize = 75f
                textPaint.isFakeBoldText = true
                textPaint.color = "#1a1a1a".toColorInt()
                canvas.drawText("$currencySymbol$amount", width / 2f, qrTop + qrSize + 150f, textPaint)
            }

            // Draw centered footer text at the bottom: QuickDash (without logo)
            val footerText = "QuickDash"
            val footerPaint = Paint().apply {
                color = "#4c566a".toColorInt()
                textSize = 34f
                isAntiAlias = true
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val footerY = height - 70f
            canvas.drawText(footerText, width / 2f, footerY, footerPaint)

            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "share_qr.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}