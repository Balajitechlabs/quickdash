/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/clipboard/presentation/components
 * File: ClipboardActionParser.kt
 * Description: Semantic classifier parsing clipboard text for URLs, phone numbers, UPI IDs, OTPs, and sensitive data.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.clipboard.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector

data class ActionableItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val intent: Intent
)

fun parseClipboardContent(text: String, context: Context): List<ActionableItem> {
    val items = mutableListOf<ActionableItem>()
    val trimmed = text.trim()

    // 0. OTP / Verification Code Detection (4-8 digits)
    val otpRegex = Regex("""\b(\d{4,8})\b""")
    val lowerText = trimmed.lowercase()
    val isOtpContext = lowerText.contains("otp") || lowerText.contains("code") || lowerText.contains("verification") ||
            lowerText.contains("password") || lowerText.contains("login") || lowerText.contains("pin")
    if (isOtpContext || (trimmed.all { it.isDigit() } && trimmed.length in 4..8)) {
        val otpMatch = otpRegex.find(trimmed)
        if (otpMatch != null) {
            val code = otpMatch.groupValues[1]
            val copyIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, code)
            }
            items.add(
                ActionableItem(
                    label = "Copy OTP: $code",
                    value = code,
                    icon = Icons.Filled.ContentCopy,
                    intent = copyIntent
                )
            )
        }
    }

    // 1. Phone number detection
    val phoneRegex = Regex("\\+?[0-9][0-9\\s-]{7,14}[0-9]")
    phoneRegex.findAll(trimmed).forEach { match ->
        val matchedPhone = match.value
        val digits = matchedPhone.filter { it.isDigit() }
        if (digits.length >= 8) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$matchedPhone")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            items.add(
                ActionableItem(
                    label = "Call $matchedPhone",
                    value = matchedPhone,
                    icon = Icons.Filled.Call,
                    intent = intent
                )
            )

            // Direct WhatsApp Chat Pill
            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            items.add(
                ActionableItem(
                    label = "WhatsApp $matchedPhone",
                    value = matchedPhone,
                    icon = Icons.Filled.Share,
                    intent = waIntent
                )
            )
        }
    }

    // 2. Email detection
    val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}")
    emailRegex.findAll(trimmed).forEach { match ->
        val matchedEmail = match.value
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$matchedEmail")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        items.add(
            ActionableItem(
                label = "Email $matchedEmail",
                value = matchedEmail,
                icon = Icons.Filled.Email,
                intent = intent
            )
        )
    }

    // 3. Link detection
    val urlRegex = Regex("(https?://[^\\s]+|www\\.[^\\s]+)")
    urlRegex.findAll(trimmed).forEach { match ->
        val matchedUrl = match.value
        val finalUrl = if (matchedUrl.startsWith("www.")) "https://$matchedUrl" else matchedUrl
        val uri = Uri.parse(finalUrl)
        val host = uri.host?.lowercase() ?: ""

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val (label, icon) = when {
            host.contains("youtube.com") || host.contains("youtu.be") -> {
                intent.setPackage("com.google.android.youtube")
                "Open YouTube" to Icons.Filled.PlayArrow
            }
            host.contains("maps.google") || host.contains("google.com/maps") || host.contains("maps.app.goo.gl") -> {
                intent.setPackage("com.google.android.apps.maps")
                "Open Maps" to Icons.Filled.LocationOn
            }
            host.contains("play.google.com") -> {
                intent.setPackage("com.android.vending")
                "Open Play Store" to Icons.Filled.Info
            }
            host.contains("instagram.com") -> {
                intent.setPackage("com.instagram.android")
                "Open Instagram" to Icons.Filled.Share
            }
            host.contains("twitter.com") || host.contains("x.com") -> {
                intent.setPackage("com.twitter.android")
                "Open X / Twitter" to Icons.Filled.Share
            }
            finalUrl.startsWith("upi:") -> {
                "UPI Payment" to Icons.Filled.QrCode
            }
            else -> "Browse Link" to Icons.AutoMirrored.Filled.OpenInNew
        }

        try {
            val pm = context.packageManager
            if (intent.`package` != null && intent.resolveActivity(pm) == null) {
                intent.setPackage(null)
            }
        } catch (_: Exception) {
            intent.setPackage(null)
        }

        items.add(
            ActionableItem(
                label = label,
                value = matchedUrl,
                icon = icon,
                intent = intent
            )
        )
    }

    return items
}

fun isLuhnValid(number: String): Boolean {
    val cleanNumber = number.filter { it.isDigit() }
    if (cleanNumber.length < 13 || cleanNumber.length > 19) return false
    var sum = 0
    var alternate = false
    for (i in cleanNumber.length - 1 downTo 0) {
        var n = cleanNumber[i] - '0'
        if (alternate) {
            n *= 2
            if (n > 9) n = (n % 10) + 1
        }
        sum += n
        alternate = !alternate
    }
    return sum % 10 == 0
}

fun isSensitive(text: String): Boolean {
    val lower = text.lowercase()
    if (lower.contains("password") || lower.contains("key") || lower.contains("token") ||
        lower.contains("secret") || lower.contains("pwd") || lower.contains("pin") ||
        (text.length > 25 && !text.contains(" ") && !text.contains("/"))) {
        return true
    }
    val ccRegex = Regex("\\b\\d{13,19}\\b")
    val cleanDigits = text.filter { it.isDigit() || it.isWhitespace() }.replace("\\s+".toRegex(), "")
    val ccMatch = ccRegex.find(cleanDigits)
    if (ccMatch != null && isLuhnValid(ccMatch.value)) {
        return true
    }
    val hasOtpKeyword = lower.contains("otp") || lower.contains("code") || lower.contains("verification") ||
            lower.contains("passcode") || lower.contains("one-time")
    val digitRegex = Regex("\\b\\d{4,8}\\b")
    if (hasOtpKeyword && digitRegex.containsMatchIn(text)) {
        return true
    }
    return false
}
