/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/settings/presentation/sections
 * File: SettingsPaymentSection.kt
 * Description: Settings section configuring default UPI handles, PayPal links, and payee names.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.settings.presentation.sections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.core.ui.components.PreferenceGroup
import com.balajitechlabs.quickdash.core.ui.components.PreferenceItem

@Composable
fun SettingsPaymentSection(
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    onManageUpiIds: () -> Unit,
    activeDefaultPaymentApp: String,
    onSaveDefaultPaymentApp: (String) -> Unit,
    onFeedback: () -> Unit
) {
    var payAppExpanded by remember { mutableStateOf(false) }

    PreferenceGroup(
        title = "Payments & UPI",
        expanded = expanded,
        onHeaderClick = onHeaderClick
    ) {
        PreferenceItem(
            title = "Manage UPI IDs",
            subtitle = "Configure your payment UPI IDs and display name",
            iconVector = Icons.Default.Payment,
            onClick = {
                onFeedback()
                onManageUpiIds()
            }
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "Default Target Payment App",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Preselect target app when generating Quick Collect payment QRs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                OutlinedButton(
                    onClick = { payAppExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (activeDefaultPaymentApp) {
                            "ANY" -> "Any Payment App"
                            "GPAY" -> "Google Pay"
                            "PHONEPE" -> "PhonePe"
                            "PAYTM" -> "Paytm"
                            "BHIM" -> "BHIM"
                            else -> "Any Payment App"
                        }
                    )
                }
                DropdownMenu(
                    expanded = payAppExpanded,
                    onDismissRequest = { payAppExpanded = false }
                ) {
                    listOf(
                        "ANY" to "Any Payment App",
                        "GPAY" to "Google Pay",
                        "PHONEPE" to "PhonePe",
                        "PAYTM" to "Paytm",
                        "BHIM" to "BHIM"
                    ).forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                payAppExpanded = false
                                onSaveDefaultPaymentApp(code)
                            }
                        )
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}
