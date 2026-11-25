/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: core/utils
 * File: SemanticVersionTest.kt
 * Description: Unit tests verifying SemanticVersion parsing, ordering, and prerelease comparison.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SemanticVersionTest {

    @Test
    fun `parse compares major minor patch versions accurately`() {
        val v524 = SemanticVersion.parse("v5.2.4")
        val v525 = SemanticVersion.parse("5.2.5")
        assertThat(v525).isGreaterThan(v524)
    }

    @Test
    fun `stable release is newer than prerelease of same version`() {
        val beta = SemanticVersion.parse("5.3.0-beta1")
        val stable = SemanticVersion.parse("5.3.0")
        assertThat(stable).isGreaterThan(beta)
    }
}
