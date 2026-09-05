/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/clipboard/presentation/components
 * File: ClipboardItemCard.kt
 * Description: Card displaying clipboard snippet content, timestamp, action chips, and pin toggle.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.clipboard.presentation.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ClipboardItemCard(
    item: String,
    isPinned: Boolean,
    sensitive: Boolean,
    revealed: Boolean,
    onToggleReveal: () -> Unit,
    onTogglePin: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (sensitive && !revealed)
                Color(0xFF3B2424)
            else Color(0xFF38393F)
        ),
        border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = if (sensitive && !revealed) "Sensitive content hidden" else item,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                maxLines = if (revealed || !sensitive) 8 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = sensitive) { onToggleReveal() }
            )

            if (sensitive) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (revealed) "Tap to hide" else "Tap to reveal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable { onToggleReveal() }
                )
            }

            if (!sensitive || revealed) {
                val actions = remember(item) { parseClipboardContent(item, context) }
                if (actions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(actions, key = { it.label }) { action ->
                            AssistChip(
                                onClick = {
                                    try {
                                        context.startActivity(action.intent)
                                    } catch (e: Exception) {
                                        try {
                                            action.intent.setPackage(null)
                                            context.startActivity(action.intent)
                                        } catch (ex: Exception) {
                                            Log.e("QuickDash", "Error executing clipboard action: ${ex.message}", ex)
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = action.label,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFB0C6FF)
                                    )
                                },
                                label = {
                                    Text(
                                        text = action.label,
                                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFF1E2024),
                                    labelColor = Color.White
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = Color(0xFF44474F)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF44474F).copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (sensitive) {
                    IconButton(
                        onClick = {
                            com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                            onToggleReveal()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (revealed) "Hide sensitive text" else "Reveal sensitive text",
                            modifier = Modifier.size(18.dp),
                            tint = if (revealed) MaterialTheme.colorScheme.primary else Color(0xFFC5C6D0)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(
                    onClick = {
                        com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                        onTogglePin()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = if (isPinned) "Unpin" else "Pin",
                        modifier = Modifier.size(18.dp),
                        tint = if (isPinned) Color(0xFFB0C6FF) else Color(0xFFC5C6D0)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
                        onShare()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.Share, "Share", modifier = Modifier.size(18.dp), tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        com.balajitechlabs.quickdash.core.ui.playSuccessVibration(context, true)
                        onCopy()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, "Copy", modifier = Modifier.size(18.dp), tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        com.balajitechlabs.quickdash.core.ui.playHeavyVibration(context, true, 26L)
                        onDelete()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.Delete, "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
