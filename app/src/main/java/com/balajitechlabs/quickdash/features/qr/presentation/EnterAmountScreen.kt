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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EnterAmountScreen(
    recentAmounts: List<String>,
    upiIds: List<String>,
    defaultUpiId: String,
    defaultPaymentApp: String,
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
    var useCircularDots by remember { mutableStateOf(false) }
    var useGradient by remember { mutableStateOf(false) }
    
    val categories = listOf("Personal", "Business", "Dining", "Groceries", "Services", "Other")
    var selectedCategory by remember { mutableStateOf("Other") }

    val idTypeLabel = "UPI ID"
    val idIcon = R.drawable.ic_upi_pay
    val currencySymbol = "₹"
    val displayAmounts = remember(recentAmounts) {
        if (recentAmounts.isEmpty() || recentAmounts == listOf("100", "200", "500")) {
            listOf("100", "200", "500")
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
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
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
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onScanQr) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showHistoryDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Payment History",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // SECTION 1: ACCOUNT SELECTION / DISPLAY
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Receiving Account",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
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
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = selectedUpiId,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // SECTION 2: INPUT FIELDS (AMOUNT & NOTE) CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Payment details",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Amount input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_currency_rupee),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (amountInput.isNotEmpty()) {
                                IconButton(onClick = { amountInput = "" }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_close),
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        },
                        isError = isAmountError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isAmountError) {
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
                        displayAmounts.forEach { amount ->
                            SuggestionChip(
                                onClick = { amountInput = amount },
                                label = {
                                    Text(
                                        text = "$currencySymbol$amount",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // Note input
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Note / Description (Optional)") },
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_note),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (noteInput.isNotEmpty()) {
                            IconButton(onClick = { noteInput = "" }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // SECTION 3: TARGET PAYMENT APP SELECTOR
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Payment App (Target)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    PaymentTargetApp.values().forEach { app ->
                        val isSelected = selectedTargetApp == app
                        val chipColor = when (app) {
                            PaymentTargetApp.ANY -> MaterialTheme.colorScheme.primary
                            PaymentTargetApp.GPAY -> Color(0xFF1A73E8) // Google Pay blue
                            PaymentTargetApp.PHONEPE -> Color(0xFF5F259F) // PhonePe purple
                            PaymentTargetApp.PAYTM -> Color(0xFF00B9F5) // Paytm light blue
                            PaymentTargetApp.BHIM -> Color(0xFFE27F22) // BHIM orange
                        }
                        
                        SuggestionChip(
                            onClick = { selectedTargetApp = app },
                            label = {
                                Text(
                                    text = app.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) chipColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, chipColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // SECTION: CATEGORY SELECTOR
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }
        }

        // SECTION: QR DESIGN CUSTOMIZATION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "QR Design Customization",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = useCircularDots,
                        onClick = { useCircularDots = !useCircularDots },
                        label = { Text("Circular Dots") }
                    )
                    FilterChip(
                        selected = useGradient,
                        onClick = { useGradient = !useGradient },
                        label = { Text("Gradient Colors") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // SECTION 3: BUTTONS
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (isAmountValid) {
                        onGenerateQr(amountInput, noteInput, selectedUpiId, selectedTargetApp, selectedCategory, useCircularDots, useGradient)
                    }
                },
                enabled = isAmountValid && selectedUpiId.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_qr_code),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate QR Code",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = { onManageUpiIds() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Manage $idTypeLabel",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
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