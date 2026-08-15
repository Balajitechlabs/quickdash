package com.balajitechlabs.quickdash.features.settings.presentation

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.data.backup.BackupManager
import com.balajitechlabs.quickdash.core.data.backup.BackupResult
import com.balajitechlabs.quickdash.core.data.backup.RestoreStrategy
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backupManager = remember { BackupManager(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Export, 1: Import
    var isProcessing by remember { mutableStateOf(false) }

    // Export State
    var usePasswordProtection by remember { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Import State
    var pendingImportBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isFileEncrypted by remember { mutableStateOf(false) }
    var importPassword by remember { mutableStateOf("") }
    var restoreStrategy by remember { mutableStateOf(RestoreStrategy.MERGE) }
    var showPasswordPrompt by remember { mutableStateOf(false) }

    // File Launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            isProcessing = true
            scope.launch {
                val result = try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        val pass = if (usePasswordProtection && exportPassword.isNotBlank()) exportPassword.trim() else null
                        backupManager.exportBackup(pass, outputStream)
                    } ?: BackupResult.Error("Could not write to destination file.")
                } catch (e: Exception) {
                    BackupResult.Error("Export failed: ${e.localizedMessage ?: e.message}", e)
                }

                isProcessing = false
                when (result) {
                    is BackupResult.Success -> {
                        Toast.makeText(
                            context,
                            "Backup exported successfully! (${result.notesCount} notes, ${result.preferencesCount} settings)",
                            Toast.LENGTH_LONG
                        ).show()
                        onDismissRequest()
                    }
                    is BackupResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isProcessing = true
            scope.launch {
                val bytes = try {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } catch (e: Exception) {
                    null
                }

                isProcessing = false
                if (bytes == null || bytes.isEmpty()) {
                    Toast.makeText(context, "Could not read selected backup file.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                pendingImportBytes = bytes
                val (isValid, isEnc) = backupManager.inspectBackup(bytes)
                if (!isValid) {
                    Toast.makeText(context, "Invalid or unsupported backup format.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                isFileEncrypted = isEnc
                if (isEnc) {
                    importPassword = ""
                    showPasswordPrompt = true
                } else {
                    // Directly restore plaintext
                    isProcessing = true
                    val result = backupManager.importBackup(null, bytes, restoreStrategy)
                    isProcessing = false
                    when (result) {
                        is BackupResult.Success -> {
                            Toast.makeText(
                                context,
                                "Restored successfully! (${result.notesCount} notes, ${result.preferencesCount} settings)",
                                Toast.LENGTH_LONG
                            ).show()
                            onDismissRequest()
                        }
                        is BackupResult.Error -> {
                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text("Encrypted Backup & Restore", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("AES-256-GCM Secure Archive", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedTab = 0 },
                        color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudUpload,
                                contentDescription = null,
                                tint = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export Backup",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = if (selectedTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedTab = 1 },
                        color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudDownload,
                                contentDescription = null,
                                tint = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Restore",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = if (selectedTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // ── EXPORT TAB ─────────────────────────────────────────
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (usePasswordProtection) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                        contentDescription = null,
                                        tint = if (usePasswordProtection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Encrypt with Password", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                                Switch(
                                    checked = usePasswordProtection,
                                    onCheckedChange = { usePasswordProtection = it }
                                )
                            }

                            AnimatedVisibility(visible = usePasswordProtection) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    OutlinedTextField(
                                        value = exportPassword,
                                        onValueChange = { exportPassword = it },
                                        label = { Text("Backup Passphrase") },
                                        placeholder = { Text("Enter a secure password") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        trailingIcon = {
                                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    )
                                    Text(
                                        "Required to restore this backup on another device.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            val extension = if (usePasswordProtection) "qdbackup" else "json"
                            exportLauncher.launch("quickdash_backup_$time.$extension")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isProcessing && (!usePasswordProtection || exportPassword.length >= 4)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Backup File", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // ── RESTORE TAB ────────────────────────────────────────
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Conflict Strategy", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { restoreStrategy = RestoreStrategy.MERGE }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = restoreStrategy == RestoreStrategy.MERGE,
                                    onClick = { restoreStrategy = RestoreStrategy.MERGE }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Merge with existing notes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("Keeps current notes and adds backup notes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { restoreStrategy = RestoreStrategy.REPLACE }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = restoreStrategy == RestoreStrategy.REPLACE,
                                    onClick = { restoreStrategy = RestoreStrategy.REPLACE }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Replace all data", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("Overwrites existing notes and settings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            importLauncher.launch(arrayOf("*/*", "application/octet-stream", "application/json"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Backup File (.qdbackup / .json)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )

    // Password Unlock Prompt Dialog for Encrypted Imports
    // Password Unlock Prompt Dialog for Encrypted Imports
    if (showPasswordPrompt && pendingImportBytes != null) {
        AlertDialog(
            onDismissRequest = { showPasswordPrompt = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Encrypted Backup", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "This backup archive is encrypted with AES-256-GCM. Please enter the passphrase used to create it:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = importPassword,
                        onValueChange = { importPassword = it },
                        label = { Text("Passphrase") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bytes = pendingImportBytes ?: return@Button
                        showPasswordPrompt = false
                        isProcessing = true
                        scope.launch {
                            val result = backupManager.importBackup(importPassword.trim(), bytes, restoreStrategy)
                            isProcessing = false
                            when (result) {
                                is BackupResult.Success -> {
                                    Toast.makeText(
                                        context,
                                        "Decrypted and restored successfully! (${result.notesCount} notes, ${result.preferencesCount} settings)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onDismissRequest()
                                }
                                is BackupResult.Error -> {
                                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    enabled = importPassword.isNotBlank() && !isProcessing
                ) {
                    Text("Unlock & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordPrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
