/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/dashboard/presentation/dialogs
 * File: CustomizeFavoritesDialog.kt
 * Description: Modal dialog enabling users to pin, unpin, and reorder dashboard favorite tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.dashboard.presentation.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.core.ui.QuickTool
import com.balajitechlabs.quickdash.features.dashboard.presentation.model.SpotlightToolItem

@Composable
fun CustomizeFavoritesDialog(
    currentFavorites: List<QuickTool>,
    allTools: List<SpotlightToolItem>,
    onSave: (List<QuickTool>) -> Unit,
    onDismiss: () -> Unit
) {
    var workingList by remember { mutableStateOf(currentFavorites) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E2024),
        title = {
            Text(
                text = "Customize Favorites",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                Text(
                    text = "Reorder or remove your top squircle tools:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC5C6D0),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(workingList) { index, tool ->
                        val toolInfo = allTools.find { it.tool == tool }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF38393F),
                            border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = toolInfo?.title ?: tool.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    modifier = Modifier.weight(1f)
                                )

                                Row {
                                    if (index > 0) {
                                        IconButton(
                                            onClick = {
                                                val list = workingList.toMutableList()
                                                val item = list.removeAt(index)
                                                list.add(index - 1, item)
                                                workingList = list
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move Up", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    if (index < workingList.size - 1) {
                                        IconButton(
                                            onClick = {
                                                val list = workingList.toMutableList()
                                                val item = list.removeAt(index)
                                                list.add(index + 1, item)
                                                workingList = list
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move Down", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            workingList = workingList.filter { it != tool }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = Color(0xFFFFB4AB), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        workingList = listOf(QuickTool.UPI, QuickTool.CHAT, QuickTool.CLIPBOARD, QuickTool.NOTES, QuickTool.CAPTURE, QuickTool.WIFI, QuickTool.PASSWORD)
                    }
                ) {
                    Text("Reset", color = Color(0xFFB0C6FF))
                }
                Button(
                    onClick = {
                        onSave(workingList)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38393F)),
                    border = BorderStroke(1.dp, Color(0xFF44474F))
                ) {
                    Text("Save", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFC5C6D0))
            }
        }
    )
}
