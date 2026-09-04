/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/utils
 * File: AiTextCategorizer.kt
 * Description: EssentialX-styled component for core/utils supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

enum class TextCategory(val displayName: String) {
    URL("Web Link"),
    PHONE_NUMBER("Phone Number"),
    EMAIL("Email Address"),
    ADDRESS("Location / Address"),
    MATH_EXPRESSION("Calculation"),
    PASSWORD("Sensitive / Password"),
    PLAIN_TEXT("Text Note")
}

data class QuickAction(
    val title: String,
    val onExecute: (Context) -> Unit
)

object AiTextCategorizer {

    fun categorize(text: String): TextCategory {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return TextCategory.PLAIN_TEXT

        // URL check
        if (trimmed.startsWith("http://", ignoreCase = true) || 
            trimmed.startsWith("https://", ignoreCase = true) || 
            (trimmed.contains(".") && !trimmed.contains(" ") && trimmed.length > 4 && android.util.Patterns.WEB_URL.matcher(trimmed).matches())) {
            return TextCategory.URL
        }

        // Email check
        if (trimmed.contains("@") && android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            return TextCategory.EMAIL
        }

        // Phone number check
        if (trimmed.replace(Regex("[^0-9+]"), "").length in 7..15 && trimmed.all { it.isDigit() || it in "+-() " }) {
            return TextCategory.PHONE_NUMBER
        }

        // Password detection
        val hasUpper = trimmed.any { it.isUpperCase() }
        val hasLower = trimmed.any { it.isLowerCase() }
        val hasDigit = trimmed.any { it.isDigit() }
        val hasSpecial = trimmed.any { !it.isLetterOrDigit() }
        if (trimmed.length in 8..32 && !trimmed.contains(" ") && ((hasUpper && hasLower && hasDigit) || (hasDigit && hasSpecial))) {
            return TextCategory.PASSWORD
        }

        // Math expression check
        if (trimmed.all { it.isDigit() || it in "+-*/^=(). " } && trimmed.any { it in "+-*/" }) {
            return TextCategory.MATH_EXPRESSION
        }

        return TextCategory.PLAIN_TEXT
    }

    fun getQuickActions(text: String, category: TextCategory): List<QuickAction> {
        val actions = mutableListOf<QuickAction>()
        val trimmed = text.trim()

        when (category) {
            TextCategory.URL -> {
                actions.add(QuickAction("Open Link") { ctx ->
                    val url = if (!trimmed.startsWith("http")) "https://$trimmed" else trimmed
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                })
                actions.add(QuickAction("Share Link") { ctx ->
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, trimmed)
                    }, "Share Link").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                })
            }
            TextCategory.PHONE_NUMBER -> {
                actions.add(QuickAction("Call Number") { ctx ->
                    ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$trimmed")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                })
                actions.add(QuickAction("Send SMS") { ctx ->
                    ctx.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$trimmed")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                })
            }
            TextCategory.EMAIL -> {
                actions.add(QuickAction("Send Email") { ctx ->
                    ctx.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$trimmed")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                })
            }
            TextCategory.MATH_EXPRESSION -> {
                actions.add(QuickAction("Calculate Result") { ctx ->
                    // Express calculate trigger
                })
            }
            else -> {
                actions.add(QuickAction("Share Text") { ctx ->
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, trimmed)
                    }, "Share").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                })
            }
        }
        return actions
    }
}
