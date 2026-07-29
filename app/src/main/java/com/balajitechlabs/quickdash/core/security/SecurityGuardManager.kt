package com.balajitechlabs.quickdash.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * 🔒 Biometric Guard & Security Manager (`SecurityGuardManager.kt`).
 * Handles fingerprint and face unlock authentication for sensitive features (Clipboard, Passwords, Notes).
 */
object SecurityGuardManager {

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
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
            // Fallback: grant access if biometrics are not configured
            onSuccess()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
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
