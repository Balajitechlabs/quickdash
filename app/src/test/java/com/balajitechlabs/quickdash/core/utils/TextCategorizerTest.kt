/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/utils
 * File: TextCategorizerTest.kt
 * Description: Unit tests for deterministic text classification, math evaluation, and edge cases.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TextCategorizerTest {

    @Test
    fun `categorize urls correctly`() {
        assertThat(TextCategorizer.categorize("https://balajitechlab.com")).isEqualTo(TextCategory.URL)
        assertThat(TextCategorizer.categorize("http://quickdash.balajitechlab.com/dashboard")).isEqualTo(TextCategory.URL)
    }

    @Test
    fun `categorize email addresses correctly`() {
        assertThat(TextCategorizer.categorize("admin@balajitechlab.com")).isEqualTo(TextCategory.EMAIL)
        assertThat(TextCategorizer.categorize("balajitechlabs@gmail.com")).isEqualTo(TextCategory.EMAIL)
    }

    @Test
    fun `categorize phone numbers correctly`() {
        assertThat(TextCategorizer.categorize("+91 9876543210")).isEqualTo(TextCategory.PHONE_NUMBER)
        assertThat(TextCategorizer.categorize("+1 (555) 234-5678")).isEqualTo(TextCategory.PHONE_NUMBER)
    }

    @Test
    fun `categorize address and geo coordinates`() {
        assertThat(TextCategorizer.categorize("12.9716, 77.5946")).isEqualTo(TextCategory.ADDRESS)
        assertThat(TextCategorizer.categorize("geo:12.9716,77.5946")).isEqualTo(TextCategory.ADDRESS)
        assertThat(TextCategorizer.categorize("100 Feet Road, Indiranagar, Bengaluru")).isEqualTo(TextCategory.ADDRESS)
        assertThat(TextCategorizer.categorize("MG Road, Bangalore pincode 560001")).isEqualTo(TextCategory.ADDRESS)
    }

    @Test
    fun `categorize math expressions and evaluate successfully`() {
        val expr1 = "25 * 4 + 10"
        assertThat(TextCategorizer.categorize(expr1)).isEqualTo(TextCategory.MATH_EXPRESSION)
        assertThat(TextCategorizer.evaluateMath(expr1)).isEqualTo("110")

        val expr2 = "(100 - 20) / 4"
        assertThat(TextCategorizer.categorize(expr2)).isEqualTo(TextCategory.MATH_EXPRESSION)
        assertThat(TextCategorizer.evaluateMath(expr2)).isEqualTo("20")

        val expr3 = "2 ^ 8"
        assertThat(TextCategorizer.categorize(expr3)).isEqualTo(TextCategory.MATH_EXPRESSION)
        assertThat(TextCategorizer.evaluateMath(expr3)).isEqualTo("256")
    }

    @Test
    fun `categorize password and sensitive credentials`() {
        val pass = "P@ssw0rd2026!"
        assertThat(TextCategorizer.categorize(pass)).isEqualTo(TextCategory.PASSWORD)
    }

    @Test
    fun `categorize plain text fallback`() {
        assertThat(TextCategorizer.categorize("Buy groceries for dinner tonight")).isEqualTo(TextCategory.PLAIN_TEXT)
        assertThat(TextCategorizer.categorize("")).isEqualTo(TextCategory.PLAIN_TEXT)
        assertThat(TextCategorizer.categorize("   ")).isEqualTo(TextCategory.PLAIN_TEXT)
    }

    @Test
    fun `math evaluator handles division by zero safely`() {
        val result = TextCategorizer.evaluateMath("100 / 0")
        assertThat(result).isNull()
    }
}
