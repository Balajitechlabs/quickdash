/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/capture/presentation/components
 * File: AnnotatorCanvasUtils.kt
 * Description: Geometry and bitmap rendering utilities for path drawing, stroke smoothing, and export.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.capture.presentation.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import java.io.OutputStream

data class LinePath(val path: List<Offset>, val color: Color, val strokeWidth: Float)

fun DrawScope.drawLinePath(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float
) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val midX = (prev.x + curr.x) / 2f
            val midY = (prev.y + curr.y) / 2f
            quadraticTo(prev.x, prev.y, midX, midY)
        }
        lineTo(points.last().x, points.last().y)
    }
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

fun saveCanvasToGallery(context: Context, paths: List<LinePath>, bgColor: Color) {
    try {
        val width = 1200
        val height = 900
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(bgColor.toArgb())

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (lp in paths) {
            paint.color = lp.color.toArgb()
            paint.strokeWidth = lp.strokeWidth * 2.5f
            val pts = lp.path
            if (pts.size >= 2) {
                val androidPath = android.graphics.Path()
                androidPath.moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) {
                    val prev = pts[i - 1]
                    val curr = pts[i]
                    val midX = (prev.x + curr.x) / 2f
                    val midY = (prev.y + curr.y) / 2f
                    androidPath.quadTo(prev.x, prev.y, midX, midY)
                }
                androidPath.lineTo(pts.last().x, pts.last().y)
                canvas.drawPath(androidPath, paint)
            }
        }

        val filename = "QuickDash_Annotate_${System.currentTimeMillis()}.png"
        val cv = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QuickDash")
            }
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { s: OutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, s)
            }
            Toast.makeText(context, "Saved: Pictures/QuickDash/$filename", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun saveCanvasToPdf(context: Context, paths: List<LinePath>, bgColor: Color) {
    try {
        val width = 1080
        val height = 1440
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(bgColor.toArgb())

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (lp in paths) {
            paint.color = lp.color.toArgb()
            paint.strokeWidth = lp.strokeWidth * 3f
            val pts = lp.path
            if (pts.size >= 2) {
                val androidPath = android.graphics.Path()
                androidPath.moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) {
                    val prev = pts[i - 1]
                    val curr = pts[i]
                    val midX = (prev.x + curr.x) / 2f
                    val midY = (prev.y + curr.y) / 2f
                    androidPath.quadTo(prev.x, prev.y, midX, midY)
                }
                androidPath.lineTo(pts.last().x, pts.last().y)
                canvas.drawPath(androidPath, paint)
            }
        }

        pdfDocument.finishPage(page)

        val filename = "QuickDash_Annotate_${System.currentTimeMillis()}.pdf"
        val cv = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/QuickDash")
            }
        }
        val targetUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
        val uri = context.contentResolver.insert(targetUri, cv)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { s: OutputStream ->
                pdfDocument.writeTo(s)
            }
            Toast.makeText(context, "Saved PDF: Download/QuickDash/$filename", Toast.LENGTH_LONG).show()
        }
        pdfDocument.close()
    } catch (e: Exception) {
        Toast.makeText(context, "PDF export failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
