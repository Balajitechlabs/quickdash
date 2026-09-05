/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr/presentation
 * File: SetupScreen.kt
 * Description: Configuration screen for setting up primary merchant UPI ID and payee details.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import com.balajitechlabs.quickdash.features.qr.presentation.QrViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    upiIds: List<String>,
    defaultUpiId: String?,
    payeeName: String?,
    usePaypal: Boolean = false,
    viewModel: QrViewModel = hiltViewModel(),
    onSaveUpiIds: (List<String>, String, String) -> Unit
) {
    val currentUpiIds = remember(upiIds) { mutableStateListOf(*upiIds.toTypedArray()) }
    var newUpiInput by remember { mutableStateOf("") }
    var newPayeeNameInput by remember { mutableStateOf(payeeName ?: "") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteIndex by remember { mutableStateOf(-1) }

    var selectedDefaultUpiId by remember(defaultUpiId, currentUpiIds) {
        mutableStateOf(
            if (!defaultUpiId.isNullOrBlank() && currentUpiIds.contains(defaultUpiId)) defaultUpiId 
            else currentUpiIds.firstOrNull() ?: ""
        )
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val idTypeLabel = if (usePaypal) "PayPal ID" else "UPI ID"
    val idPlaceholder = if (usePaypal) "username" else "name@bank"
    val idIcon = if (usePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay

    val coroutineScope = rememberCoroutineScope()
    val launchStyleFlow = viewModel.launchStyle.collectAsStateWithLifecycle(initialValue = "FLOATING")
    var isFloating by remember(launchStyleFlow.value) { mutableStateOf(launchStyleFlow.value == "FLOATING") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Centered Header
        Text(
            text = "Setup your $idTypeLabel",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // SECTION 1: CONFIGURED ACCOUNTS LIST (CLEAN MATERIAL CARD)
        RoundedCardContainer(
            containerColor = Color(0xFF38393F),
            cornerRadius = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Configured Accounts",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (currentUpiIds.isEmpty()) {
                    Text(
                        text = "No accounts configured yet. Enter your $idTypeLabel below to begin collecting payments instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    currentUpiIds.forEachIndexed { i, id ->
                        val isDefault = id == selectedDefaultUpiId
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF2A2B30),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(idIcon),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = id,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                                if (isDefault) {
                                    Text(
                                        text = "Primary Default",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    pendingDeleteIndex = i
                                    showDeleteDialog = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete),
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (currentUpiIds.size > 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = "Default: $selectedDefaultUpiId",
                                onValueChange = {},
                                readOnly = true,
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    Icon(
                                        painter = if (dropdownExpanded) painterResource(R.drawable.ic_keyboard_arrow_up)
                                        else painterResource(R.drawable.ic_keyboard_arrow_down),
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                currentUpiIds.forEach { id ->
                                    DropdownMenuItem(
                                        text = { Text(text = id) },
                                        onClick = {
                                            selectedDefaultUpiId = id
                                            dropdownExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION 2: ADD DIVISION / ACCOUNT DETAILS
        RoundedCardContainer(
            containerColor = Color(0xFF38393F),
            cornerRadius = 20.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Account Details",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                if (currentUpiIds.size < 3) {
                    val isIdValid = if (usePaypal) {
                        newUpiInput.isNotBlank() && !newUpiInput.contains("@") && !newUpiInput.contains(" ")
                    } else {
                        newUpiInput.trim().matches(Regex("^[a-zA-Z0-9.\\-_]+@[a-zA-Z0-9.\\-_]+$"))
                    }
                    val isDuplicate = currentUpiIds.contains(newUpiInput.trim())

                    val addId = {
                        val trimmed = newUpiInput.trim()
                        if (isIdValid && !isDuplicate && trimmed.isNotEmpty()) {
                            currentUpiIds.add(trimmed)
                            if (selectedDefaultUpiId.isEmpty()) {
                                selectedDefaultUpiId = trimmed
                            }
                            newUpiInput = ""
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = newUpiInput,
                            onValueChange = { if (it.length <= 50) newUpiInput = it },
                            label = { Text("New $idTypeLabel", maxLines = 1) },
                            placeholder = { Text(idPlaceholder) },
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(idIcon),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (newUpiInput.isNotEmpty() && isIdValid && !isDuplicate) {
                                    IconButton(onClick = addId) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_add_upi_id),
                                            contentDescription = "Add ID",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            isError = (!isIdValid && newUpiInput.isNotEmpty()) || isDuplicate,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { addId() })
                        )

                        if (!isIdValid && newUpiInput.isNotEmpty()) {
                            Text(
                                text = "Invalid $idTypeLabel format (example: name@bank)",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Maximum of 3 ${idTypeLabel}s reached.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Payee Name Field
                OutlinedTextField(
                    value = newPayeeNameInput,
                    onValueChange = { if (it.length <= 30) newPayeeNameInput = it },
                    label = { Text("Payee Display Name (Optional)") },
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_person),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (newPayeeNameInput.isNotEmpty()) {
                            IconButton(onClick = { newPayeeNameInput = "" }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = "Clear",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // SECTION 3: COMPACT FLOATING WINDOW SWITCH (NO REDUNDANT TEXT)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF38393F)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_quickdash_tile),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Floating Window Mode",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.White
                    )
                }
                Switch(
                    checked = isFloating,
                    onCheckedChange = { newVal ->
                        isFloating = newVal
                        coroutineScope.launch {
                            viewModel.saveLaunchStyle(if (isFloating) "FLOATING" else "FULL_SCREEN")
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF2A2B30),
                        uncheckedBorderColor = Color(0xFF44474F)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // SECTION 4: SAVE BUTTON
        Button(
            onClick = {
                val finalDefault = if (selectedDefaultUpiId.isNotBlank() && currentUpiIds.contains(selectedDefaultUpiId)) {
                    selectedDefaultUpiId
                } else {
                    currentUpiIds.firstOrNull() ?: ""
                }
                onSaveUpiIds(currentUpiIds, newPayeeNameInput, finalDefault)
            },
            enabled = currentUpiIds.isNotEmpty(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                "Save & Continue",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Delete confirmation dialog
    if (showDeleteDialog && pendingDeleteIndex != -1 && pendingDeleteIndex < currentUpiIds.size) {
        val upiToBeRemoved = currentUpiIds[pendingDeleteIndex]
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove $idTypeLabel?") },
            text = { Text("Are you sure you want to remove $upiToBeRemoved from QuickDash?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val removedId = currentUpiIds[pendingDeleteIndex]
                        currentUpiIds.removeAt(pendingDeleteIndex)
                        if (selectedDefaultUpiId == removedId) {
                            selectedDefaultUpiId = currentUpiIds.firstOrNull() ?: ""
                        }
                        showDeleteDialog = false
                        pendingDeleteIndex = -1
                    }
                ) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        pendingDeleteIndex = -1
                    }
                ) { Text("Cancel") }
            }
        )
    }
}
