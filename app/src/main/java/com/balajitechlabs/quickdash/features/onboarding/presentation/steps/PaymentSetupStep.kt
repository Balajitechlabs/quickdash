package com.balajitechlabs.quickdash.features.onboarding.presentation.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.features.onboarding.presentation.components.OnboardingScaffold

@Composable
fun PaymentSetupStep(
    upiIds: List<String>,
    defaultUpiId: String?,
    payeeName: String?,
    onSave: (List<String>, String, String) -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val currentIds = remember(upiIds) { mutableStateListOf(*upiIds.toTypedArray()) }
    var newIdInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf(payeeName ?: "") }
    var usePaypal by remember { mutableStateOf(false) }
    var selectedDefault by remember(defaultUpiId, currentIds) {
        mutableStateOf(
            if (!defaultUpiId.isNullOrBlank() && currentIds.contains(defaultUpiId)) defaultUpiId
            else currentIds.firstOrNull() ?: ""
        )
    }

    val idTypeLabel = if (usePaypal) "PayPal" else "UPI"
    val idPlaceholder = if (usePaypal) "your_username" else "name@bank"

    val isIdValid = if (usePaypal) {
        newIdInput.isNotBlank() && !newIdInput.contains("@") && !newIdInput.contains(" ")
    } else {
        newIdInput.trim().matches(Regex("^[a-zA-Z0-9.\\-_]+@[a-zA-Z0-9.\\-_]+$"))
    }
    val isDuplicate = currentIds.contains(newIdInput)

    fun addId() {
        if (isIdValid && !isDuplicate && newIdInput.isNotEmpty() && currentIds.size < 3) {
            currentIds.add(newIdInput)
            if (selectedDefault.isEmpty()) selectedDefault = newIdInput
            newIdInput = ""
        }
    }

    OnboardingScaffold(
        stepTitle = "Set Up Quick Collect",
        stepSubtitle = "Add your $idTypeLabel ID to generate instant payment QR codes. You can skip this and add later.",
        currentStep = 2,
        totalSteps = 7,
        showBack = true,
        showSkip = true,
        onBack = onBack,
        onSkip = onSkip
    ) {
        // UPI / PayPal toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !usePaypal,
                onClick = { usePaypal = false },
                label = { Text("UPI", fontWeight = FontWeight.SemiBold) },
                leadingIcon = if (!usePaypal) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = usePaypal,
                onClick = { usePaypal = true },
                label = { Text("PayPal", fontWeight = FontWeight.SemiBold) },
                leadingIcon = if (usePaypal) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current IDs
        if (currentIds.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Added ${idTypeLabel} IDs (${currentIds.size}/3)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    currentIds.forEachIndexed { index, id ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (usePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay
                                ),
                                contentDescription = null,
                                tint = if (id == selectedDefault) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = id + if (id == selectedDefault) " (Default)" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (id == selectedDefault) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            FilledTonalIconButton(
                                onClick = {
                                    currentIds.removeAt(index)
                                    if (selectedDefault == id) {
                                        selectedDefault = currentIds.firstOrNull() ?: ""
                                    }
                                },
                                modifier = Modifier.size(32.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = "\u2715",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (currentIds.size > 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Default:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        currentIds.forEach { id ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                RadioButton(
                                    selected = id == selectedDefault,
                                    onClick = { selectedDefault = id },
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = id,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Add new ID
        if (currentIds.size < 3) {
            OutlinedTextField(
                value = newIdInput,
                onValueChange = { if (it.length <= 50) newIdInput = it },
                label = { Text("New $idTypeLabel ID") },
                placeholder = { Text(idPlaceholder) },
                shape = RoundedCornerShape(14.dp),
                leadingIcon = {
                    Icon(
                        painter = painterResource(
                            if (usePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (newIdInput.isNotEmpty() && isIdValid && !isDuplicate) {
                        IconButton(onClick = { addId() }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add_upi_id),
                                contentDescription = "Add ID",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                isError = (!isIdValid && newIdInput.isNotEmpty()) || isDuplicate,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { addId() })
            )

            if (newIdInput.isNotEmpty() && !isIdValid) {
                Text(
                    text = "Invalid $idTypeLabel format",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
            if (isDuplicate) {
                Text(
                    text = "This ID is already added",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        } else {
            Text(
                text = "Maximum 3 IDs configured.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Payee name
        OutlinedTextField(
            value = nameInput,
            onValueChange = { if (it.length <= 30) nameInput = it },
            label = { Text("Display Name (Optional)") },
            placeholder = { Text("Your Name") },
            shape = RoundedCornerShape(14.dp),
            supportingText = {
                Text(
                    text = "${nameInput.length}/30",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                val finalDefault = if (selectedDefault.isNotEmpty() && currentIds.contains(selectedDefault)) {
                    selectedDefault
                } else {
                    currentIds.firstOrNull() ?: ""
                }
                onSave(currentIds, nameInput, finalDefault)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large,
            enabled = currentIds.isNotEmpty()
        ) {
            Text(
                text = "Continue",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
