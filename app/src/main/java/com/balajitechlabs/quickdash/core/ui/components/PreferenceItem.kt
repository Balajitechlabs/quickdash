package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 ListItem-backed preference row.
 * Supports an icon (tinted on a tonal surface), headline, optional supporting text,
 * and an optional trailing slot (Switch, IconButton, Text, etc.).
 */
@Composable
fun PreferenceItem(
    title: String,
    subtitle: String? = null,
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val leadingContent: (@Composable () -> Unit)? = when {
        iconVector != null -> ({
            Surface(
                color = iconContainerColor,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        })
        iconRes != null -> ({
            Surface(
                color = iconContainerColor,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        })
        else -> null
    }

    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = if (subtitle != null) ({
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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