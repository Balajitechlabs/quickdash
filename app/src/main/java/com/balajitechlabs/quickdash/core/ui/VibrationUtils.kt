/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui
 * File: VibrationUtils.kt
 * Description: Standardized tactile haptic feedback and acoustic click helper for buttons, sliders, and gestures.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

private const val TAG = "VibrationUtils"

/**
 * Standard click feedback with physical haptic impulse and subtle audio click,
 * matching the tactile feedback felt in the Settings screen.
 */
fun playClickVibration(context: Context, hapticEnabled: Boolean = true, durationMs: Long = 18L) {
    if (!hapticEnabled || durationMs <= 0L) return
    try {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.25f)

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            val safeDuration = durationMs.coerceIn(10L, 50L)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(safeDuration, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(safeDuration)
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to play click vibration", e)
    }
}

/**
 * Stronger haptic pulse for confirmations, item saves, and long-presses.
 */
fun playHeavyVibration(context: Context, hapticEnabled: Boolean = true, durationMs: Long = 32L) {
    if (!hapticEnabled || durationMs <= 0L) return
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            val safeDuration = durationMs.coerceIn(20L, 80L)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(safeDuration, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(safeDuration)
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to play heavy vibration", e)
    }
}

/**
 * Double pulse waveform for successes, QR captures, and completed actions.
 */
fun playSuccessVibration(context: Context, hapticEnabled: Boolean = true) {
    if (!hapticEnabled) return
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 16, 50, 22), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 16, 50, 22), -1)
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to play success vibration", e)
    }
}

fun playExplosionVibration(context: Context, hapticEnabled: Boolean = true) {
    if (!hapticEnabled) return
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 15, 80, 24), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 15, 80, 24), -1)
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to play explosion vibration", e)
    }
}
