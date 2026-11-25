/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: core/designsystem/theme
 * File: QuickDashHaptics.kt
 * Description: Centralized, API-safe haptic feedback helper using VibratorManager on Android 12+.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.designsystem.theme

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Unified haptic feedback helper replacing legacy duplicated vibration utilities.
 */
object QuickDashHaptics {

    enum class Style {
        TICK,
        CLICK,
        HEAVY,
        SUCCESS,
        ERROR
    }

    fun perform(
        context: Context,
        style: Style = Style.CLICK,
        enabled: Boolean = true,
        playSound: Boolean = false
    ) {
        if (!enabled) return

        if (playSound) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.25f)
        }

        val vibrator = resolveVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        val effect = when (style) {
            Style.TICK -> VibrationEffect.createOneShot(10L, 80)
            Style.CLICK -> VibrationEffect.createOneShot(18L, VibrationEffect.DEFAULT_AMPLITUDE)
            Style.HEAVY -> VibrationEffect.createOneShot(28L, 220)
            Style.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0L, 15L, 60L, 20L), -1)
            Style.ERROR -> VibrationEffect.createWaveform(longArrayOf(0L, 35L, 50L, 35L), -1)
        }
        try {
            vibrator.vibrate(effect)
        } catch (_: Exception) {
            // Best-effort haptics; ignore OEM/sandbox restrictions.
        }
    }

    private fun resolveVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
