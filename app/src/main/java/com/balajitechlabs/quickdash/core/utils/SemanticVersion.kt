/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/utils
 * File: SemanticVersion.kt
 * Description: Semantic versioning parser, comparator, and formatter for in-app updates.
 * Developer: balajitechlabs
 */

package com.balajitechlabs.quickdash.core.utils

/**
 * Robust, semantic versioning model supporting standard semver (major.minor.patch),
 * pre-release channels (alpha, beta, rc), and CI build numbers.
 */
data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String? = null,
    val buildNumber: Int? = null,
    val raw: String = ""
) : Comparable<SemanticVersion> {

    /**
     * Clean version string for user-facing UI, e.g. "5.2.2" or "5.2.3-beta.1".
     * Strips internal CI run numbers (like -78) so users see clean version names.
     */
    val displayVersion: String
        get() = if (preRelease != null) {
            "$major.$minor.$patch-$preRelease"
        } else {
            "$major.$minor.$patch"
        }

    override fun compareTo(other: SemanticVersion): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        if (patch != other.patch) return patch.compareTo(other.patch)

        // A stable release without pre-release is NEWER than a pre-release
        // e.g. 5.2.2 > 5.2.2-beta.1
        if (preRelease == null && other.preRelease != null) return 1
        if (preRelease != null && other.preRelease == null) return -1

        // If both have pre-release identifiers, compare them
        if (preRelease != null && other.preRelease != null) {
            val cmp = preRelease.compareTo(other.preRelease)
            if (cmp != 0) return cmp
        }

        // If both are otherwise identical and both have build numbers, compare build numbers
        if (buildNumber != null && other.buildNumber != null) {
            return buildNumber.compareTo(other.buildNumber)
        }

        return 0
    }

    companion object {
        /**
         * Parses any version string safely (e.g. "v5.2.2", "QuickDash v5.2.2-78", "5.3.0-beta.1").
         */
        fun parse(versionInfo: String?): SemanticVersion {
            if (versionInfo.isNullOrBlank()) {
                return SemanticVersion(0, 0, 0, raw = "")
            }

            try {
                // Strip "QuickDash", "v", leading/trailing whitespace
                val clean = versionInfo.trim()
                    .removePrefix("QuickDash")
                    .trim()
                    .removePrefix("v")
                    .trim()

                // Split at "-" into base semver and suffix
                val parts = clean.split("-", limit = 2)
                val baseParts = parts[0].split(".")

                val major = baseParts.getOrNull(0)?.toIntOrNull() ?: 0
                val minor = baseParts.getOrNull(1)?.toIntOrNull() ?: 0
                val patch = baseParts.getOrNull(2)?.toIntOrNull() ?: 0

                var preRelease: String? = null
                var buildNumber: Int? = null

                if (parts.size > 1) {
                    val suffix = parts[1].trim()
                    val asInt = suffix.toIntOrNull()
                    if (asInt != null) {
                        // Pure integer suffix (e.g. "78" from GitHub run number)
                        buildNumber = asInt
                    } else {
                        // Suffix has letters (e.g. "beta.1", "alpha", "rc-2")
                        preRelease = suffix
                    }
                }

                return SemanticVersion(major, minor, patch, preRelease, buildNumber, clean)
            } catch (_: Exception) {
                return SemanticVersion(0, 0, 0, raw = versionInfo)
            }
        }
    }
}
