/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/calculator/domain
 * File: CalculatorEngineTest.kt
 * Description: Unit tests verifying CalculatorEngine expression evaluation, operator precedence, and state reduction.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.calculator.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun `evaluate respects operator precedence and parentheses`() {
        val result = CalculatorEngine.evaluate("2 + 3 × 4 - (10 ÷ 2)")
        assertThat(result).isEqualTo("9")
    }

    @Test
    fun `evaluate handles division by zero as Error`() {
        val result = CalculatorEngine.evaluate("12 ÷ 0")
        assertThat(result).isEqualTo("Error")
    }

    @Test
    fun `evaluate handles scientific functions and constants`() {
        val result = CalculatorEngine.evaluate("sin(30) + sqrt(16)")
        assertThat(result).isEqualTo("4.5")
    }

    @Test
    fun `reduce appends digits, evaluates equals, and records history`() {
        var state = CalculatorState()
        state = CalculatorEngine.reduce(state, CalcKey.Digit("7"))
        state = CalculatorEngine.reduce(state, CalcKey.Op("×"))
        state = CalculatorEngine.reduce(state, CalcKey.Digit("6"))
        state = CalculatorEngine.reduce(state, CalcKey.Equals)

        assertThat(state.display).isEqualTo("42")
        assertThat(state.justEvaluated).isTrue()
        assertThat(state.history).containsExactly("7×6 = 42")
    }

    @Test
    fun `reduce Clear resets display and expression`() {
        val initial = CalculatorState(display = "123", expression = "100+23", justEvaluated = true)
        val cleared = CalculatorEngine.reduce(initial, CalcKey.Clear)

        assertThat(cleared.display).isEqualTo("0")
        assertThat(cleared.expression).isEmpty()
        assertThat(cleared.justEvaluated).isFalse()
    }
}
