package com.balajitechlabs.quickdash.features.calculator.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

// ── Domain ──────────────────────────────────────────────────────────────────

private sealed class CalcKey {
    data class Digit(val ch: String) : CalcKey()
    data class Op(val ch: String) : CalcKey()
    data class ScientificOp(val name: String) : CalcKey()
    object Clear : CalcKey()
    object Backspace : CalcKey()
    object Equals : CalcKey()
    object Dot : CalcKey()
    object ToggleSign : CalcKey()
    object Percent : CalcKey()
    object Sqrt : CalcKey()
}

private fun evaluate(expression: String): String = try {
    val cleaned = expression.replace("×", "*")
        .replace("÷", "/")
        .replace("π", "3.141592653589793")
        .replace(Regex("(?<![a-zA-Z0-9.])e(?![a-zA-Z0-9.])"), "2.718281828459045")
    val parser = Parser(cleaned)
    val result = parser.parse()
    if (result.isNaN() || result.isInfinite()) "Error"
    else java.math.BigDecimal(result).setScale(8, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
} catch (_: Exception) { "Error" }

private class Parser(private val str: String) {
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
        if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
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
        val startPos = this.pos
        if (eat('('.code)) {
            x = parseExpression()
            eat(')'.code)
        } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
            while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code || ch == 'e'.code || ch == 'E'.code ||
                ((ch == '+'.code || ch == '-'.code) && (pos > 0 && (str[pos - 1] == 'e' || str[pos - 1] == 'E')))) {
                nextChar()
            }
            x = str.substring(startPos, this.pos).toDouble()
        } else if (ch >= 'a'.code && ch <= 'z'.code || ch == '√'.code) {
            while (ch >= 'a'.code && ch <= 'z'.code || ch == '√'.code) nextChar()
            val func = str.substring(startPos, this.pos)
            x = parseFactor()
            x = when (func) {
                "sqrt", "√" -> Math.sqrt(x)
                "sin" -> Math.sin(Math.toRadians(x))
                "cos" -> Math.cos(Math.toRadians(x))
                "tan" -> Math.tan(Math.toRadians(x))
                "ln" -> Math.log(x)
                "log" -> Math.log10(x)
                else -> throw RuntimeException("Unknown function: $func")
            }
        } else {
            throw RuntimeException("Unexpected: " + ch.toChar())
        }

        if (eat('^'.code)) x = Math.pow(x, parseFactor())

        return x
    }
}

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCalculatorScreen(isFloating: Boolean = false) {
    val haptic = LocalHapticFeedback.current
    var display by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(listOf<String>()) }
    var showHistory by remember { mutableStateOf(false) }
    var justEvaluated by remember { mutableStateOf(false) }
    var isScientific by remember { mutableStateOf(false) }

    fun handleKey(key: CalcKey) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        when (key) {
            CalcKey.Clear -> { display = "0"; expression = ""; justEvaluated = false }
            CalcKey.Backspace -> {
                if (justEvaluated) { display = "0"; expression = ""; justEvaluated = false; return }
                display = if (display.length > 1) display.dropLast(1) else "0"
                if (expression.isNotEmpty()) expression = expression.dropLast(1)
            }
            CalcKey.Dot -> {
                if (justEvaluated) { display = "0."; expression = "0."; justEvaluated = false; return }
                if (!display.contains('.')) { display += "."; expression += "." }
            }
            CalcKey.ToggleSign -> {
                display = if (display.startsWith('-')) display.drop(1) else "-$display"
                expression = Regex("""(-?\d+\.?\d*)$""").replace(expression) { m ->
                    val v = m.value.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                    v.negate().stripTrailingZeros().toPlainString()
                }
            }
            CalcKey.Percent -> {
                val v = display.toBigDecimalOrNull() ?: return
                val pct = v.divide(java.math.BigDecimal("100"), 12, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
                display = pct.toPlainString()
                expression = Regex("""(-?\d+\.?\d*)$""").replace(expression) { display }
            }
            CalcKey.Sqrt -> {
                val v = display.toDoubleOrNull() ?: return
                if (v < 0) { display = "Error"; return }
                val r = java.math.BigDecimal(sqrt(v)).setScale(8, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
                display = r.toPlainString()
                expression = Regex("""(-?\d+\.?\d*)$""").replace(expression) { display }
                justEvaluated = true
            }
            is CalcKey.Digit -> {
                if (justEvaluated) { expression = key.ch; display = key.ch; justEvaluated = false; return }
                if (display == "0" && key.ch != ".") {
                    display = key.ch
                    expression = expression.dropLastWhile { it.isDigit() } + key.ch
                } else { display += key.ch; expression += key.ch }
            }
            is CalcKey.Op -> {
                justEvaluated = false
                expression = expression.trimEnd().trimEnd { it in "+-×÷*/" } + key.ch
                display = "0"
            }
            is CalcKey.ScientificOp -> {
                justEvaluated = false
                if (key.name == "(") {
                    expression += "("
                    display = "0"
                } else if (key.name == ")") {
                    expression += ")"
                    display = "0"
                } else if (key.name in listOf("π", "e")) {
                    if (display == "0" || justEvaluated) {
                        display = key.name
                        expression = if (justEvaluated) key.name else expression.dropLastWhile { it.isDigit() || it == '.' } + key.name
                    } else {
                        display += key.name
                        expression += key.name
                    }
                } else {
                    if (key.name == "^") {
                        expression += "^"
                        display = "0"
                    } else {
                        expression = expression + key.name + "("
                        display = "0"
                    }
                }
            }
            CalcKey.Equals -> {
                if (expression.isBlank()) return
                val result = evaluate(expression)
                if (result != "Error") history = listOf("$expression = $result") + history.take(19)
                display = result; expression = result; justEvaluated = true
            }
        }
    }

    // Button layout
    val rows: List<List<CalcKey>> = listOf(
        listOf(CalcKey.Clear, CalcKey.ToggleSign, CalcKey.Percent, CalcKey.Op("÷")),
        listOf(CalcKey.Digit("7"), CalcKey.Digit("8"), CalcKey.Digit("9"), CalcKey.Op("×")),
        listOf(CalcKey.Digit("4"), CalcKey.Digit("5"), CalcKey.Digit("6"), CalcKey.Op("-")),
        listOf(CalcKey.Digit("1"), CalcKey.Digit("2"), CalcKey.Digit("3"), CalcKey.Op("+")),
        listOf(CalcKey.Sqrt, CalcKey.Digit("0"), CalcKey.Dot, CalcKey.Equals)
    )

    val scientificRows: List<List<CalcKey>> = listOf(
        listOf(CalcKey.ScientificOp("sin"), CalcKey.ScientificOp("cos"), CalcKey.ScientificOp("tan"), CalcKey.ScientificOp("^")),
        listOf(CalcKey.ScientificOp("ln"), CalcKey.ScientificOp("log"), CalcKey.ScientificOp("π"), CalcKey.ScientificOp("e")),
        listOf(CalcKey.ScientificOp("("), CalcKey.ScientificOp(")"), CalcKey.Sqrt, CalcKey.Percent)
    )

    val btnSize = if (isFloating) 58.dp else 70.dp
    val fontSize = if (isFloating) 18.sp else 22.sp
    val hPad = if (isFloating) 8.dp else 12.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = hPad, vertical = 4.dp)
    ) {
        // ── Display surface ──────────────────────────────────────────────
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // History toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (history.isNotEmpty()) {
                        FilterChip(
                            selected = showHistory,
                            onClick = { showHistory = !showHistory },
                            label = { Text("History", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Icon(
                                    if (showHistory) Icons.Default.HistoryToggleOff else Icons.Default.History,
                                    null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Backspace
                    FilledIconButton(
                        onClick = { handleKey(CalcKey.Backspace) },
                        shape = MaterialTheme.shapes.medium,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Backspace, contentDescription = "Backspace", modifier = Modifier.size(18.dp))
                    }
                }

                // History dropdown
                AnimatedVisibility(
                    visible = showHistory && history.isNotEmpty(),
                    enter = expandVertically(spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)) + fadeIn(),
                    exit = shrinkVertically(spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)) + fadeOut()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 120.dp)
                            .padding(top = 6.dp),
                        reverseLayout = false
                    ) {
                        items(history) { entry ->
                            Text(
                                text = entry,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp)
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Secondary expression
                AnimatedContent(
                    targetState = expression,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "expr"
                ) { expr ->
                    Text(
                        text = expr.ifEmpty { " " },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Primary display
                AnimatedContent(
                    targetState = display,
                    transitionSpec = {
                        slideInVertically { it / 4 } + fadeIn() togetherWith
                        slideOutVertically { -it / 4 } + fadeOut()
                    },
                    label = "display"
                ) { d ->
                    Text(
                        text = d,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = when {
                                d.length > 14 -> 24.sp
                                d.length > 10 -> 32.sp
                                else -> if (isFloating) 36.sp else 48.sp
                            }
                        ),
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Mode Selector Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !isScientific,
                onClick = { isScientific = false },
                label = { Text("Basic", style = MaterialTheme.typography.labelSmall) }
            )
            FilterChip(
                selected = isScientific,
                onClick = { isScientific = true },
                label = { Text("Scientific", style = MaterialTheme.typography.labelSmall) }
            )
        }

        // ── Keypad ───────────────────────────────────────────────────────────
        Spacer(modifier = Modifier.height(4.dp))

        val actualBtnSize = if (isScientific) (if (isFloating) 44.dp else 56.dp) else btnSize
        val actualFontSize = if (isScientific) (if (isFloating) 14.sp else 18.sp) else fontSize

        if (isScientific) {
            scientificRows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { key ->
                        CalcButton(
                            key = key,
                            size = actualBtnSize,
                            fontSize = actualFontSize,
                            onClick = { handleKey(key) }
                        )
                    }
                }
            }
        }

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    val isDuplicate = isScientific && (key == CalcKey.Sqrt || key == CalcKey.Percent)
                    if (isDuplicate) {
                        Spacer(modifier = Modifier.size(actualBtnSize))
                    } else {
                        CalcButton(
                            key = key,
                            size = actualBtnSize,
                            fontSize = actualFontSize,
                            onClick = { handleKey(key) }
                        )
                    }
                }
            }
        }
    }
}

// ── Button atoms ─────────────────────────────────────────────────────────────

@Composable
private fun CalcButton(
    key: CalcKey,
    size: androidx.compose.ui.unit.Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit
) {
    val label = when (key) {
        CalcKey.Clear -> "C"
        CalcKey.Backspace -> "⌫"
        CalcKey.Dot -> "."
        CalcKey.ToggleSign -> "+/-"
        CalcKey.Percent -> "%"
        CalcKey.Sqrt -> "√"
        CalcKey.Equals -> "="
        is CalcKey.Digit -> key.ch
        is CalcKey.Op -> key.ch
        is CalcKey.ScientificOp -> key.name
    }

    val isOp = key is CalcKey.Op
    val isEquals = key is CalcKey.Equals
    val isFunc = key is CalcKey.Clear || key is CalcKey.ToggleSign || key is CalcKey.Percent || key is CalcKey.Sqrt || key is CalcKey.ScientificOp

    when {
        isEquals -> Button(
            onClick = onClick,
            modifier = Modifier.size(size),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 0.dp)
        ) {
            Text(label, fontSize = fontSize, fontWeight = FontWeight.Medium)
        }

        isOp -> FilledTonalButton(
            onClick = onClick,
            modifier = Modifier.size(size),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 1.dp, pressedElevation = 0.dp)
        ) {
            Text(label, fontSize = fontSize, fontWeight = FontWeight.Medium)
        }

        isFunc -> FilledTonalButton(
            onClick = onClick,
            modifier = Modifier.size(size),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 1.dp, pressedElevation = 0.dp)
        ) {
            Text(label, fontSize = if (label.length > 2) (fontSize.value - 4).sp else fontSize, fontWeight = FontWeight.Medium)
        }

        else -> ElevatedButton(
            onClick = onClick,
            modifier = Modifier.size(size),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
        ) {
            Text(label, fontSize = fontSize, fontWeight = FontWeight.Normal)
        }
    }
}