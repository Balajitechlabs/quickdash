package com.balajitechlabs.quickdash.features.password.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Tool #15: Quick Password & Hash Generator.
 * Generate ultra-secure passwords, PINs, and SHA-256 hashes.
 */
@Composable
fun QuickPasswordScreen(isFloating: Boolean = false) {
    val context = LocalContext.current

    var passwordLength by remember { mutableFloatStateOf(16f) }
    var includeSymbols by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf("") }
    var inputHashText by remember { mutableStateOf("") }
    var sha256Output by remember { mutableStateOf("") }

    fun generatePassword() {
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        val charPool = StringBuilder(uppercase + lowercase).apply {
            if (includeNumbers) append(numbers)
            if (includeSymbols) append(symbols)
        }.toString()

        val random = SecureRandom()
        val password = StringBuilder()
        val len = passwordLength.toInt()
        for (i in 0 until len) {
            password.append(charPool[random.nextInt(charPool.length)])
        }
        generatedPassword = password.toString()
    }

    fun computeSha256(text: String) {
        if (text.isEmpty()) {
            sha256Output = ""
            return
        }
        val bytes = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
        sha256Output = bytes.joinToString("") { "%02x".format(it) }
    }

    if (generatedPassword.isEmpty()) {
        generatePassword()
    }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label to Clipboard! 🔒", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .then(if (isFloating) Modifier.fillMaxWidth().wrapContentHeight() else Modifier.fillMaxSize())
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Quick Password & Hash",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generated Password Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "GENERATED PASSWORD", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = generatedPassword,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
                IconButton(onClick = { generatePassword() }) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Regenerate")
                }
                IconButton(onClick = { copyToClipboard(generatedPassword, "Password") }) {
                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Controls Pod
        RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Length: ${passwordLength.toInt()} characters",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = passwordLength,
                    onValueChange = {
                        passwordLength = it
                        generatePassword()
                    },
                    valueRange = 8f..32f,
                    steps = 23
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Numbers (0-9)")
                    Switch(
                        checked = includeNumbers,
                        onCheckedChange = {
                            includeNumbers = it
                            generatePassword()
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Symbols (!@#$)")
                    Switch(
                        checked = includeSymbols,
                        onCheckedChange = {
                            includeSymbols = it
                            generatePassword()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SHA-256 Hasher
        OutlinedTextField(
            value = inputHashText,
            onValueChange = {
                inputHashText = it
                computeSha256(it)
            },
            label = { Text("Compute SHA-256 Hash") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        if (sha256Output.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sha256Output,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { copyToClipboard(sha256Output, "SHA-256 Hash") }) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy Hash")
                    }
                }
            }
        }
    }
}
