package com.balajitechlabs.quickdash.features.qr.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.data.UserStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    upiIds: List<String>,
    defaultUpiId: String?,
    payeeName: String?,
    usePaypal: Boolean = false,
    onSaveUpiIds: (List<String>, String, String) -> Unit
) {
    val currentUpiIds = remember(upiIds) { mutableStateListOf(*upiIds.toTypedArray()) }
    var newUpiInput by remember { mutableStateOf("") }
    var newPayeeNameInput by remember { mutableStateOf(payeeName ?: "") }

    var isExpanded by remember { mutableStateOf(true) }
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
    val idPlaceholder = if (usePaypal) "192aakarsh" else "name@bank"
    val idIcon = if (usePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Setup your $idTypeLabel",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // SECTION 1: ACTIVE CONFIGURATION & ADDED IDS
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
                    text = "Connected Divisions / Accounts (${currentUpiIds.size}/3)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                for (i in 0 until 3) {
                    val name = "Division ${i + 1}"
                    val isDefault = if (i < currentUpiIds.size) currentUpiIds[i] == selectedDefaultUpiId else false
                    val defaultLabel = if (isDefault) " (Default)" else ""
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(idIcon),
                            contentDescription = null,
                            tint = if (i < currentUpiIds.size) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (i < currentUpiIds.size) "$name$defaultLabel: ${currentUpiIds[i]}" else "$name: [Not Defined]",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (i < currentUpiIds.size) FontWeight.Medium else FontWeight.Normal,
                            color = if (i < currentUpiIds.size) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        )
                        if (i < currentUpiIds.size) {
                            IconButton(
                                onClick = {
                                    pendingDeleteIndex = i
                                    showDeleteDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete),
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (currentUpiIds.size > 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Set Default Selection",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedDefaultUpiId,
                            onValueChange = {},
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Icon(
                                    painter = if (dropdownExpanded) painterResource(R.drawable.ic_keyboard_arrow_up)
                                    else painterResource(R.drawable.ic_keyboard_arrow_down),
                                    contentDescription = null
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

        // SECTION 2: ADD & EDIT FORM CARD
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
                    text = "Add Division / Account Details",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (currentUpiIds.size < 3) {
                    val isIdValid = if (usePaypal) {
                        newUpiInput.isNotBlank() && !newUpiInput.contains("@") && !newUpiInput.contains(" ")
                    } else {
                        newUpiInput.trim().matches(Regex("^[a-zA-Z0-9.\\-_]+@[a-zA-Z0-9.\\-_]+$"))
                    }
                    val isDuplicate = currentUpiIds.contains(newUpiInput)

                    val addId = {
                        if (isIdValid && !isDuplicate && newUpiInput.isNotEmpty()) {
                            currentUpiIds.add(newUpiInput)
                            if (selectedDefaultUpiId.isEmpty()) {
                                selectedDefaultUpiId = newUpiInput
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
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (newUpiInput.isNotEmpty() && isIdValid && !isDuplicate) {
                                    IconButton(onClick = addId) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_add_upi_id),
                                            contentDescription = "Add ID",
                                            tint = MaterialTheme.colorScheme.primary
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
                                text = "Invalid $idTypeLabel format",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Maximum of 3 ${idTypeLabel}s configured.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Payee Name Field
                OutlinedTextField(
                    value = newPayeeNameInput,
                    onValueChange = { if (it.length <= 30) newPayeeNameInput = it },
                    label = { Text("Display Name (Optional)") },
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_person),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (newPayeeNameInput.isNotEmpty()) {
                            IconButton(onClick = { newPayeeNameInput = "" }) {
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

        Spacer(modifier = Modifier.height(4.dp))

        // Save button
        Button(
            onClick = {
                val finalDefault = if (selectedDefaultUpiId.isNotEmpty() && currentUpiIds.contains(selectedDefaultUpiId)) {
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
                .height(50.dp)
        ) {
            Text(
                "Save & Continue",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
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
