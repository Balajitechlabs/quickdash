/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr
 * File: EnterAmountScreen.kt
 * Description: EssentialX-styled component for features/qr supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.R
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EnterAmountScreen(
    recentAmounts: List<String>,
    upiIds: List<String>,
    defaultUpiId: String,
    defaultPaymentApp: String,
    usePaypal: Boolean = false,
    isFloating: Boolean = false,
    qrHistoryJson: String,
    onClearQrHistory: () -> Unit,
    onScanQr: () -> Unit,
    onGenerateQr: (String, String, String, PaymentTargetApp, String, Boolean, Boolean) -> Unit,
    onManageUpiIds: () -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var selectedUpiId by remember(upiIds, defaultUpiId) {
        mutableStateOf(if (upiIds.contains(defaultUpiId)) defaultUpiId else upiIds.firstOrNull() ?: "")
    }
    var expanded by remember { mutableStateOf(false) }
    var selectedTargetApp by remember(defaultPaymentApp) {
        mutableStateOf(
            try { PaymentTargetApp.valueOf(defaultPaymentApp) }
            catch (e: Exception) { PaymentTargetApp.ANY }
        )
    }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var useCircularDots by remember { mutableStateOf(true) }
    var useGradient by remember { mutableStateOf(true) }
    
    val categories = listOf("Personal", "Business", "Dining", "Groceries", "Services", "Other")
    var selectedCategory by remember { mutableStateOf("Other") }
 
    val idTypeLabel = if (usePaypal) "PayPal ID" else "UPI ID"
    val idIcon = if (usePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay
    val currencySymbol = if (usePaypal) "$" else "₹"
    val displayAmounts = remember(recentAmounts, usePaypal) {
        if (recentAmounts.isEmpty() || recentAmounts == listOf("100", "200", "500")) {
            if (usePaypal) listOf("10", "20", "50") else listOf("100", "200", "500")
        } else {
            recentAmounts
        }
    }
 
    // Amount validation (Declared at the top level of composable)
    val amountDouble = amountInput.toDoubleOrNull()
    val isAmountValid = amountInput.isEmpty() || (amountDouble != null && amountDouble > 0)
    val isAmountError = !isAmountValid && amountInput.isNotEmpty()
 
    Column(
        modifier = Modifier
            .then(if (isFloating) Modifier.fillMaxWidth().wrapContentHeight() else Modifier.fillMaxSize())
            .background(Color(0xFF000000))
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Generate Payment QR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onScanQr) {
                    Icon(
                        imageVector = Icons.Rounded.QrCodeScanner,
                        contentDescription = "Scan QR",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { showHistoryDialog = true }) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = "Payment History",
                        tint = Color.White
                    )
                }
            }
        }

        // SECTION 1: ACCOUNT SELECTION / DISPLAY
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF38393F)
            ),
            border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Receiving Account",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (upiIds.size > 1) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedUpiId,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select $idTypeLabel") },
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Icon(
                                    painter = if (expanded) painterResource(R.drawable.ic_keyboard_arrow_up)
                                    else painterResource(R.drawable.ic_keyboard_arrow_down),
                                    contentDescription = null
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(idIcon),
                                    contentDescription = idTypeLabel,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            upiIds.forEach { id ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = if (id == defaultUpiId) "$id (Default)" else id,
                                            fontWeight = if (id == defaultUpiId) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    onClick = {
                                        selectedUpiId = id
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(idIcon),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = selectedUpiId,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // SECTION 2: INPUT FIELDS (AMOUNT) CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF38393F)
            ),
            border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Payment details",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Amount input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amountInput = newValue
                            }
                        },
                        label = { Text("Amount (Optional)", color = Color(0xFFC5C6D0)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF44474F),
                            unfocusedBorderColor = Color(0xFF44474F).copy(alpha = 0.6f),
                            focusedContainerColor = Color(0xFF1E2024),
                            unfocusedContainerColor = Color(0xFF1E2024),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        leadingIcon = { Text(currencySymbol, color = Color.White, modifier = Modifier.padding(start = 12.dp)) },
                        trailingIcon = {
                            if (amountInput.isNotEmpty()) {
                                IconButton(onClick = { amountInput = "" }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_close),
                                        contentDescription = "Clear",
                                        tint = Color(0xFFC5C6D0)
                                    )
                                }
                            }
                        },
                        isError = isAmountError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (usePaypal) {
                        Text(
                            text = "PayPal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    } else if (isAmountError) {
                        Text(
                            text = "Please enter a valid amount",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // Recent amount chips
                if (displayAmounts.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        displayAmounts.take(3).forEach { amount ->
                            SuggestionChip(
                                onClick = { 
                                    amountInput = amount
                                    onGenerateQr(amount, "", selectedUpiId, selectedTargetApp, selectedCategory, useCircularDots, useGradient)
                                },
                                label = {
                                    Text(
                                        text = "$currencySymbol$amount",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color(0xFF1E2024),
                                    labelColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // SECTION 3: BUTTONS
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (isAmountValid) {
                        onGenerateQr(amountInput, "", selectedUpiId, selectedTargetApp, selectedCategory, useCircularDots, useGradient)
                    }
                },
                enabled = isAmountValid && selectedUpiId.isNotEmpty(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38393F),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFF44474F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.QrCode,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate QR Code",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = { onManageUpiIds() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color(0xFF44474F))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Manage $idTypeLabel",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    if (showHistoryDialog) {
        QrHistoryDialog(
            historyJson = qrHistoryJson,
            onClearHistory = onClearQrHistory,
            onDismiss = { showHistoryDialog = false }
        )
    }
}