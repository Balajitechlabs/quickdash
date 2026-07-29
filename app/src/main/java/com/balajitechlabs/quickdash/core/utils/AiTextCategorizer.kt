package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

enum class TextCategory(val emoji: String, val displayName: String) {
    URL("🔗", "Web Link"),
    PHONE_NUMBER("📞", "Phone Number"),
    EMAIL("📧", "Email Address"),
    ADDRESS("📍", "Location / Address"),
    MATH_EXPRESSION("🧮", "Calculation"),
    PASSWORD("🔑", "Sensitive / Password"),
    PLAIN_TEXT("📝", "Text Note")
}

data class QuickAction(
    val title: String,
    val iconEmoji: String,
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

        // Phone check
        if (trimmed.length in 7..16 && trimmed.all { it.isDigit() || it == '+' || it == '-' || it == ' ' || it == '(' || it == ')' }) {
            return TextCategory.PHONE_NUMBER
        }

        // Password heuristic (high entropy / complex mix)
        if (trimmed.length in 8..32 && !trimmed.contains(" ") && 
            trimmed.any { it.isDigit() } && trimmed.any { it.isUpperCase() } && trimmed.any { !it.isLetterOrDigit() }) {
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
                actions.add(QuickAction("Open Link", "🌐") { ctx ->
                    val url = if (!trimmed.startsWith("http")) "https://$trimmed" else trimmed
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                })
                actions.add(QuickAction("Share Link", "📤") { ctx ->
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, trimmed)
                    }, "Share Link").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                })
            }
            TextCategory.PHONE_NUMBER -> {
                actions.add(QuickAction("Call Number", "📞") { ctx ->
                    ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$trimmed")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                })
                actions.add(QuickAction("Send SMS", "💬") { ctx ->
                    ctx.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$trimmed")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                })
            }
            TextCategory.EMAIL -> {
                actions.add(QuickAction("Send Email", "✉️") { ctx ->
                    ctx.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$trimmed")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                })
            }
            TextCategory.MATH_EXPRESSION -> {
                actions.add(QuickAction("Calculate Result", "🧮") { ctx ->
                    // Express calculate trigger
                })
            }
            else -> {
                actions.add(QuickAction("Share Text", "📤") { ctx ->
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
