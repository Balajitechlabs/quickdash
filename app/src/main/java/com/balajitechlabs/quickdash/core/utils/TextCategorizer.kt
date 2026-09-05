/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/utils
 * File: TextCategorizer.kt
 * Description: Deterministic text categorization and context-aware quick action dispatcher.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.math.BigDecimal
import java.math.RoundingMode

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

object TextCategorizer {

    private val COORD_REGEX = Regex("""^-?\d{1,2}\.\d+,\s*-?\d{1,3}\.\d+$""")
    private val ADDRESS_KEYWORDS = Regex("""(?i)\b(street|st\.|road|rd\.|avenue|ave\.|nagar|layout|cross|lane|blvd|pincode|pin code|zip code)\b""")
    private val EMAIL_REGEX = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")
    private val URL_REGEX = Regex("""^(https?://)?([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}(/.*)?$""")
    private val MATH_OPERATOR_REGEX = Regex("""(\d|\))\s*[+\-*/^%]\s*(\d|\()""")

    fun categorize(text: String): TextCategory {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return TextCategory.PLAIN_TEXT

        // Geo URI or Geographic Coordinates or Street Address
        if (trimmed.startsWith("geo:", ignoreCase = true) ||
            COORD_REGEX.matches(trimmed) ||
            (ADDRESS_KEYWORDS.containsMatchIn(trimmed) && trimmed.length >= 10)
        ) {
            return TextCategory.ADDRESS
        }

        // URL check
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            (!trimmed.contains(" ") && URL_REGEX.matches(trimmed))
        ) {
            return TextCategory.URL
        }

        // Email check
        if (EMAIL_REGEX.matches(trimmed)) {
            return TextCategory.EMAIL
        }

        // Phone number check (must have 7 to 15 digits, no alphabetic or math multiplication/division characters)
        val digitsOnly = trimmed.replace(Regex("[^0-9]"), "")
        if (digitsOnly.length in 7..15 &&
            trimmed.all { it.isDigit() || it in "+-() " } &&
            (trimmed.startsWith("+") || trimmed.contains(" ") || trimmed.contains("-") || digitsOnly.length >= 10)
        ) {
            return TextCategory.PHONE_NUMBER
        }

        // Math expression check: must contain a binary operator between numbers or brackets
        if (trimmed.length >= 3 &&
            MATH_OPERATOR_REGEX.containsMatchIn(trimmed) &&
            trimmed.all { it.isDigit() || it in "+-*/^=(). %" } &&
            evaluateMath(trimmed) != null
        ) {
            return TextCategory.MATH_EXPRESSION
        }

        // Password / Sensitive Token detection
        val hasUpper = trimmed.any { it.isUpperCase() }
        val hasLower = trimmed.any { it.isLowerCase() }
        val hasDigit = trimmed.any { it.isDigit() }
        val hasSpecial = trimmed.any { !it.isLetterOrDigit() }
        if (trimmed.length in 8..32 && !trimmed.contains(" ") &&
            ((hasUpper && hasLower && hasDigit) || (hasDigit && hasSpecial))
        ) {
            return TextCategory.PASSWORD
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
                    runCatching {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    }
                })
                actions.add(QuickAction("Share Link") { ctx ->
                    runCatching {
                        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, trimmed)
                        }, "Share Link").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    }
                })
            }
            TextCategory.PHONE_NUMBER -> {
                actions.add(QuickAction("Call Number") { ctx ->
                    runCatching {
                        ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$trimmed")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    }
                })
                actions.add(QuickAction("Send SMS") { ctx ->
                    runCatching {
                        ctx.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$trimmed")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    }
                })
            }
            TextCategory.EMAIL -> {
                actions.add(QuickAction("Send Email") { ctx ->
                    runCatching {
                        ctx.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$trimmed")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    }
                })
            }
            TextCategory.ADDRESS -> {
                actions.add(QuickAction("View on Map") { ctx ->
                    val mapUri = if (trimmed.startsWith("geo:", ignoreCase = true)) {
                        Uri.parse(trimmed)
                    } else {
                        Uri.parse("geo:0,0?q=" + Uri.encode(trimmed))
                    }
                    runCatching {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, mapUri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    }
                })
                actions.add(QuickAction("Share Location") { ctx ->
                    runCatching {
                        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, trimmed)
                        }, "Share Location").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    }
                })
            }
            TextCategory.MATH_EXPRESSION -> {
                actions.add(QuickAction("Calculate Result") { ctx ->
                    val result = evaluateMath(trimmed) ?: "Error"
                    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    clipboard?.setPrimaryClip(ClipData.newPlainText("Calculation Result", result))
                    Toast.makeText(ctx, "Result: $result (copied)", Toast.LENGTH_SHORT).show()
                })
                actions.add(QuickAction("Open Calculator") { ctx ->
                    runCatching {
                        val calcIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_CALCULATOR)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        ctx.startActivity(calcIntent)
                    }
                })
            }
            else -> {
                actions.add(QuickAction("Share Text") { ctx ->
                    runCatching {
                        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, trimmed)
                        }, "Share").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    }
                })
            }
        }
        return actions
    }

    fun evaluateMath(expression: String): String? = runCatching {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")
        if (sanitized.isEmpty()) return null
        val parser = MathParser(sanitized)
        val result = parser.parse()
        if (result.isNaN() || result.isInfinite()) null
        else BigDecimal(result).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }.getOrNull()

    private class MathParser(private val str: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw IllegalArgumentException("Unexpected character: " + ch.toChar())
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> x /= parseFactor()
                    eat('%'.code) -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else {
                throw IllegalArgumentException("Unexpected: " + ch.toChar())
            }

            if (eat('^'.code)) x = Math.pow(x, parseFactor())
            return x
        }
    }
}
