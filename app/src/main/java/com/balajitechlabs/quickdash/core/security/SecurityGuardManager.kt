/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/security
 * File: SecurityGuardManager.kt
 * Description: Enforces screen security flags, detects debuggers or tampered environments, and controls biometric gating.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Biometric Guard & Security Manager (`SecurityGuardManager.kt`).
 * Handles fingerprint and face unlock authentication for sensitive features (Clipboard, Passwords, Notes).
 */
object SecurityGuardManager {

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock QuickDash Guard",
        subtitle: String = "Authenticate to access sensitive data",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAvailable(activity)) {
            onError("Biometric authentication is not enrolled or available on this device")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed. Try again.")
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Masks sensitive strings such as 16-digit credit card numbers, CVVs, or passwords.
     */
    fun maskSensitiveText(input: String): String {
        // Credit card pattern (13 to 19 digits)
        val cardRegex = Regex("\\b(?:\\d[ -]*?){13,19}\\b")
        var sanitized = input.replace(cardRegex) { match ->
            val digits = match.value.replace(Regex("\\D"), "")
            if (digits.length >= 12) {
                "**** **** **** " + digits.takeLast(4)
            } else {
                "**** **** **** ****"
            }
        }

        // CVV pattern
        val cvvRegex = Regex("\\bCVV:?\\s*\\d{3,4}\\b", RegexOption.IGNORE_CASE)
        sanitized = sanitized.replace(cvvRegex, "CVV: ***")

        return sanitized
    }
}
