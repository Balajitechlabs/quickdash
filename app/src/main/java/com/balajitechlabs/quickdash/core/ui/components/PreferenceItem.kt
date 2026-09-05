/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/components
 * File: PreferenceItem.kt
 * Description: Standardized settings row composable with icon, title, subtitle, and action control slots.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.theme.ColorUtil

/**
 * EssentialX / QuickDash Material 3 Expressive Preference Row.
 * Features:
 * - Deterministic pastel circle icon badge with vibrant tint
 * - Crisp bold typography and high-contrast subtitle
 * - Clean trailing action slots (Switch, Badge, Arrow)
 *
 * Developer: balajitechlabs
 */
@Composable
fun PreferenceItem(
    title: String,
    subtitle: String? = null,
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    iconColor: Color? = null,
    iconContainerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val pastelBg = iconContainerColor ?: Color(0xFF2A2B30)
    val vibrantIcon = iconColor ?: Color.White

    val leadingContent: (@Composable () -> Unit)? = when {
        iconVector != null -> ({
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(pastelBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = title,
                    tint = vibrantIcon,
                    modifier = Modifier.size(20.dp)
                )
            }
        })
        iconRes != null -> ({
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(pastelBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    tint = vibrantIcon,
                    modifier = Modifier.size(20.dp)
                )
            }
        })
        else -> null
    }

    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = Color.White
            )
        },
        supportingContent = if (subtitle != null) ({
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = Color(0xFFC5C6D0)
            )
        }) else null,
        leadingContent = leadingContent,
        trailingContent = trailing,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick)
                else Modifier
            ),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}