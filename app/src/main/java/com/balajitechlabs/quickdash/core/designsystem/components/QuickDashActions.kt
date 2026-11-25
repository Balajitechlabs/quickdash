/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: core/designsystem/components
 * File: QuickDashActions.kt
 * Description: Reusable Material 3 Expressive buttons, segmented toggles, and action rows.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashShapeTokens
import com.balajitechlabs.quickdash.core.designsystem.theme.QuickDashTokens

/**
 * Native Material 3 single-choice segmented control replacing custom pill switchers.
 */
@Composable
fun <T> QuickDashSegmentedSelector(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    icon: ((T) -> ImageVector)? = null
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            SegmentedButton(
                selected = isSelected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                icon = {
                    if (icon != null) {
                        Icon(
                            imageVector = icon(option),
                            contentDescription = null,
                            modifier = Modifier.size(QuickDashTokens.Size.iconSm)
                        )
                    } else {
                        SegmentedButtonDefaults.Icon(active = isSelected)
                    }
                }
            ) {
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Standard primary action button with expressive corner radius and optional leading icon.
 */
@Composable
fun QuickDashPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = QuickDashShapeTokens.Input,
        modifier = modifier.height(QuickDashTokens.Size.buttonHeight)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(QuickDashTokens.Size.iconMd)
            )
            Spacer(modifier = Modifier.width(QuickDashTokens.Spacing.sm))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

/**
 * Reusable Copy / Share / optional Edit action row for notes, QR payloads, passwords, and clipboard items.
 */
@Composable
fun QuickDashCopyShareRow(
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null
) {
    val buttonColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(QuickDashTokens.Spacing.sm, Alignment.End)
    ) {
        if (onEdit != null) {
            FilledTonalIconButton(
                onClick = onEdit,
                colors = buttonColors,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(QuickDashTokens.Size.iconSm)
                )
            }
        }
        FilledTonalIconButton(
            onClick = onCopy,
            colors = buttonColors,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                modifier = Modifier.size(QuickDashTokens.Size.iconSm)
            )
        }
        FilledTonalIconButton(
            onClick = onShare,
            colors = buttonColors,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                modifier = Modifier.size(QuickDashTokens.Size.iconSm)
            )
        }
    }
}
