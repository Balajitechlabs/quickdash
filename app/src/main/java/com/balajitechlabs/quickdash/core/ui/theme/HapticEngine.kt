/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/theme
 * File: HapticEngine.kt
 * Description: Provides centralized haptic feedback generation with vibration strengths adapted to user settings.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 *  Haptic Feedback Engine (`HapticEngine.kt`).
 * Supports vibration profiles: Crisp, Soft, Bouncy, Cyberpunk.
 */
object HapticEngine {

    enum class Profile { CRISP, SOFT, BOUNCY, CYBERPUNK }

    fun trigger(context: Context, profile: Profile = Profile.CRISP) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        audioManager?.playSoundEffect(android.media.AudioManager.FX_KEY_CLICK, 0.25f)

        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (profile) {
                Profile.CRISP -> VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE)
                Profile.SOFT -> VibrationEffect.createOneShot(30, 50)
                Profile.BOUNCY -> VibrationEffect.createWaveform(longArrayOf(0, 20, 30, 20), -1)
                Profile.CYBERPUNK -> VibrationEffect.createWaveform(longArrayOf(0, 10, 20, 10, 40), -1)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }
}
