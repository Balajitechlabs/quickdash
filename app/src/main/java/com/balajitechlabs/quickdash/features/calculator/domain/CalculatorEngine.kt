/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/calculator/domain
 * File: CalculatorEngine.kt
 * Description: Pure-Kotlin expression parser and state reducer for standard and scientific calculator operations.
 *              Kept framework-free so calculator logic is deterministic and unit-testable.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.calculator.domain

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

sealed class CalcKey {
    data class Digit(val ch: String) : CalcKey()
    data class Op(val ch: String) : CalcKey()
    data class ScientificOp(val name: String) : CalcKey()
    data object Clear : CalcKey()
    data object Backspace : CalcKey()
    data object Equals : CalcKey()
    data object Dot : CalcKey()
    data object ToggleSign : CalcKey()
    data object Percent : CalcKey()
    data object Sqrt : CalcKey()
}

data class CalculatorState(
    val display: String = "0",
    val expression: String = "",
    val history: List<String> = emptyList(),
    val justEvaluated: Boolean = false
)

object CalculatorEngine {

    private val trailingNumberRegex = Regex("""(-?\d+\.?\d*)$""")
    private val eulerConstantRegex = Regex("""(?<![a-zA-Z0-9.])e(?![a-zA-Z0-9.])""")

    fun reduce(state: CalculatorState, key: CalcKey): CalculatorState {
        return when (key) {
            CalcKey.Clear -> state.copy(display = "0", expression = "", justEvaluated = false)

            CalcKey.Backspace -> {
                if (state.justEvaluated) {
                    state.copy(display = "0", expression = "", justEvaluated = false)
                } else {
                    state.copy(
                        display = if (state.display.length > 1) state.display.dropLast(1) else "0",
                        expression = if (state.expression.isNotEmpty()) state.expression.dropLast(1) else ""
                    )
                }
            }

            CalcKey.Dot -> {
                if (state.justEvaluated) {
                    state.copy(display = "0.", expression = "0.", justEvaluated = false)
                } else if (!state.display.contains('.')) {
                    state.copy(display = state.display + ".", expression = state.expression + ".")
                } else {
                    state
                }
            }

            CalcKey.ToggleSign -> {
                val nextDisplay = if (state.display.startsWith('-')) {
                    state.display.drop(1)
                } else {
                    "-${state.display}"
                }
                val nextExpression = trailingNumberRegex.replace(state.expression) { match ->
                    val value = match.value.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    value.negate().stripTrailingZeros().toPlainString()
                }
                state.copy(display = nextDisplay, expression = nextExpression)
            }

            CalcKey.Percent -> {
                val value = state.display.toBigDecimalOrNull() ?: return state
                val pct = value.divide(BigDecimal("100"), 12, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
                val nextExpression = trailingNumberRegex.replace(state.expression) { pct }
                state.copy(display = pct, expression = nextExpression)
            }

            CalcKey.Sqrt -> {
                val value = state.display.toDoubleOrNull() ?: return state
                if (value < 0) return state.copy(display = "Error")
                val result = BigDecimal(sqrt(value))
                    .setScale(8, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
                val nextExpression = trailingNumberRegex.replace(state.expression) { result }
                state.copy(display = result, expression = nextExpression, justEvaluated = true)
            }

            is CalcKey.Digit -> {
                if (state.justEvaluated) {
                    state.copy(display = key.ch, expression = key.ch, justEvaluated = false)
                } else if (state.display == "0" && key.ch != ".") {
                    state.copy(
                        display = key.ch,
                        expression = state.expression.dropLastWhile { it.isDigit() } + key.ch
                    )
                } else {
                    state.copy(
                        display = state.display + key.ch,
                        expression = state.expression + key.ch
                    )
                }
            }

            is CalcKey.Op -> state.copy(
                display = "0",
                expression = state.expression.trimEnd().trimEnd { it in "+-×÷*/" } + key.ch,
                justEvaluated = false
            )

            is CalcKey.ScientificOp -> reduceScientific(state, key.name)

            CalcKey.Equals -> {
                if (state.expression.isBlank()) return state
                val result = evaluate(state.expression)
                val nextHistory = if (result != "Error") {
                    listOf("${state.expression} = $result") + state.history.take(19)
                } else {
                    state.history
                }
                state.copy(
                    display = result,
                    expression = result,
                    history = nextHistory,
                    justEvaluated = true
                )
            }
        }
    }

    private fun reduceScientific(state: CalculatorState, name: String): CalculatorState {
        return when (name) {
            "(", ")" -> state.copy(
                display = "0",
                expression = state.expression + name,
                justEvaluated = false
            )
            "π", "e" -> {
                if (state.display == "0" || state.justEvaluated) {
                    val nextExpr = if (state.justEvaluated) {
                        name
                    } else {
                        state.expression.dropLastWhile { it.isDigit() || it == '.' } + name
                    }
                    state.copy(display = name, expression = nextExpr, justEvaluated = false)
                } else {
                    state.copy(
                        display = state.display + name,
                        expression = state.expression + name,
                        justEvaluated = false
                    )
                }
            }
            "^" -> state.copy(
                display = "0",
                expression = state.expression + "^",
                justEvaluated = false
            )
            else -> state.copy(
                display = "0",
                expression = "${state.expression}$name(",
                justEvaluated = false
            )
        }
    }

    fun evaluate(expression: String): String = try {
        val cleaned = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", Math.PI.toString())
            .replace(eulerConstantRegex, Math.E.toString())
        val result = ExpressionParser(cleaned).parse()
        if (result.isNaN() || result.isInfinite()) {
            "Error"
        } else {
            BigDecimal(result)
                .setScale(8, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString()
        }
    } catch (_: Exception) {
        "Error"
    }
}

private class ExpressionParser(private val str: String) {
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
        if (pos < str.length) throw IllegalArgumentException("Unexpected: ${ch.toChar()}")
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
        } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
            while (
                ch in '0'.code..'9'.code ||
                ch == '.'.code ||
                ch == 'e'.code ||
                ch == 'E'.code ||
                ((ch == '+'.code || ch == '-'.code) && pos > 0 && (str[pos - 1] == 'e' || str[pos - 1] == 'E'))
            ) {
                nextChar()
            }
            x = str.substring(startPos, pos).toDouble()
        } else if (ch in 'a'.code..'z'.code || ch == '√'.code) {
            while (ch in 'a'.code..'z'.code || ch == '√'.code) nextChar()
            val func = str.substring(startPos, pos)
            x = parseFactor()
            x = when (func) {
                "sqrt", "√" -> sqrt(x)
                "sin" -> sin(Math.toRadians(x))
                "cos" -> cos(Math.toRadians(x))
                "tan" -> tan(Math.toRadians(x))
                "ln" -> ln(x)
                "log" -> log10(x)
                else -> throw IllegalArgumentException("Unknown function: $func")
            }
        } else {
            throw IllegalArgumentException("Unexpected: ${ch.toChar()}")
        }

        if (eat('^'.code)) x = x.pow(parseFactor())
        return x
    }
}
