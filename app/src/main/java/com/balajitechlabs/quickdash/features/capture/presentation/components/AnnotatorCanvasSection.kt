/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/capture/presentation/components
 * File: AnnotatorCanvasSection.kt
 * Description: Drawing canvas providing pens, highlighters, shapes, and color pickers for screenshot markup.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.capture.presentation.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnnotatorCanvasSection(
    context: Context,
    modifier: Modifier = Modifier
) {
    val paths = remember { mutableStateListOf<LinePath>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Color(0xFFFF3B30)) }
    var strokeWidth by remember { mutableFloatStateOf(8f) }
    var isEraser by remember { mutableStateOf(false) }
    var canvasBgColor by remember { mutableStateOf(Color.White) }
    val bgColors = listOf(Color.White, Color(0xFFF5F5F7), Color(0xFFFFFDE7), Color(0xFF121212), Color(0xFF1E293B))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Doodle & Canvas",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC5C6D0)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isEraser = !isEraser },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isEraser) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent)
                    ) {
                        Icon(Icons.Filled.Edit, "Eraser", tint = if (isEraser) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { if (paths.isNotEmpty()) paths.removeAt(paths.lastIndex) }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, "Undo")
                    }
                    TextButton(
                        onClick = { paths.clear() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00),
                    Color(0xFF34C759), Color(0xFF007AFF), Color(0xFFAF52DE),
                    Color.White, Color.Black
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(if (selectedColor == color && !isEraser) 28.dp else 24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color && !isEraser) 2.5.dp else 1.dp,
                                color = if (selectedColor == color && !isEraser) MaterialTheme.colorScheme.primary else Color.Gray.copy(0.5f),
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color; isEraser = false }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Stroke", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(42.dp))
                Slider(
                    value = strokeWidth,
                    onValueChange = { strokeWidth = it },
                    valueRange = 3f..30f,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("Canvas Bg:", style = MaterialTheme.typography.labelSmall)
                bgColors.forEach { color ->
                    val isSelected = canvasBgColor == color
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .clickable { canvasBgColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(canvasBgColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), RoundedCornerShape(12.dp))
                    .pointerInput(canvasBgColor) {
                        detectDragGestures(
                            onDragStart = { offset -> currentPath = listOf(offset) },
                            onDrag = { change, _ -> currentPath = currentPath + change.position },
                            onDragEnd = {
                                if (currentPath.size >= 2) {
                                    val drawColor = if (isEraser) canvasBgColor else selectedColor
                                    val drawWidth = if (isEraser) strokeWidth * 3f else strokeWidth
                                    paths.add(LinePath(currentPath, drawColor, drawWidth))
                                }
                                currentPath = emptyList()
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    paths.forEach { lp -> drawLinePath(lp.path, lp.color, lp.strokeWidth) }
                    if (currentPath.size >= 2) {
                        val drawColor = if (isEraser) canvasBgColor else selectedColor
                        val drawWidth = if (isEraser) strokeWidth * 3f else strokeWidth
                        drawLinePath(currentPath, drawColor, drawWidth)
                    }
                }

                if (paths.isEmpty() && currentPath.isEmpty()) {
                    Text(
                        "Draw here…",
                        color = if (canvasBgColor == Color.White || canvasBgColor == Color(0xFFF5F5F7) || canvasBgColor == Color(0xFFFFFDE7)) Color(0xFF888888) else Color(0xFFAAAAAA),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (paths.isEmpty()) Toast.makeText(context, "Canvas is empty! Draw something first.", Toast.LENGTH_SHORT).show()
                        else saveCanvasToGallery(context, paths, canvasBgColor)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Save Image", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        if (paths.isEmpty()) Toast.makeText(context, "Canvas is empty! Draw something first.", Toast.LENGTH_SHORT).show()
                        else saveCanvasToPdf(context, paths, canvasBgColor)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Save as PDF", fontSize = 12.sp)
                }
            }
        }
    }
}
