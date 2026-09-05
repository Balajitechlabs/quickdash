/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/onboarding/presentation/components
 * File: PermissionRowItem.kt
 * Description: Permission grant card showing permission status, explanation, and settings intent button.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.onboarding.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionRowItem(
    title: String,
    subtitle: String,
    iconBadgeColor: Color,
    iconTint: Color,
    imageVector: ImageVector,
    isGranted: Boolean,
    isRequired: Boolean = false,
    isOptional: Boolean = false,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF38393F),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBadgeColor)
                    .border(BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )
                    if (isRequired) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "*",
                            color = Color(0xFFFF8A80),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFFC5C6D0)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1B3B22),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Granted",
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Granted",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF81C784)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onGrant,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRequired) MaterialTheme.colorScheme.primary else Color(0xFF2A2B30),
                        contentColor = if (isRequired) Color.Black else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (isOptional) "Optional" else "Grant",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
