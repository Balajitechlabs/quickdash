/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: UserStoreUi.kt
 * Description: UI, theme, bubble, typography, and haptic feedback preferences manager.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.balajitechlabs.quickdash.core.data.prefs.PreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class UserStoreUi(context: Context) : UserStoreBase(context) {

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE_KEY] ?: "AMOLED"
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE_KEY] = mode
        }
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLOR_KEY] ?: true
    }

    suspend fun saveDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR_KEY] = enabled
        }
    }

    val launchStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAUNCH_STYLE_KEY] ?: "FLOATING_DIALOG"
    }

    suspend fun saveLaunchStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAUNCH_STYLE_KEY] = style
        }
    }

    val displayMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DISPLAY_MODE_KEY] ?: "FLOATING_DIALOG"
    }

    suspend fun saveDisplayMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISPLAY_MODE_KEY] = mode
        }
    }

    val seedColor: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SEED_COLOR_KEY] ?: "#1E88E5"
    }

    suspend fun saveSeedColor(color: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SEED_COLOR_KEY] = color
        }
    }

    val paletteStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PALETTE_STYLE_KEY] ?: "TonalSpot"
    }

    suspend fun savePaletteStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PALETTE_STYLE_KEY] = style
        }
    }

    val switchStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SWITCH_STYLE_KEY] ?: "MaterialYou"
    }

    suspend fun saveSwitchStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SWITCH_STYLE_KEY] = style
        }
    }

    val sliderStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SLIDER_STYLE_KEY] ?: "MaterialYou"
    }

    suspend fun saveSliderStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SLIDER_STYLE_KEY] = style
        }
    }

    val shapeStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHAPE_STYLE_KEY] ?: "Rounded"
    }

    suspend fun saveShapeStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHAPE_STYLE_KEY] = style
        }
    }

    val cornerRadius: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CORNER_RADIUS_KEY] ?: 16f
    }

    suspend fun saveCornerRadius(radius: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CORNER_RADIUS_KEY] = radius
        }
    }

    val borderWidth: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BORDER_WIDTH_KEY] ?: 1f
    }

    suspend fun saveBorderWidth(width: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BORDER_WIDTH_KEY] = width
        }
    }

    val fontFamilyKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_FAMILY_KEY] ?: "SPACE_GROTESK"
    }

    suspend fun saveFontFamilyKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_FAMILY_KEY] = key
        }
    }

    val fontScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_SCALE_KEY] ?: 1f
    }

    suspend fun saveFontScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SCALE_KEY] = scale
        }
    }

    val confettiEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CONFETTI_ENABLED_KEY] ?: true
    }

    suspend fun saveConfettiEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONFETTI_ENABLED_KEY] = enabled
        }
    }

    val confettiType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CONFETTI_TYPE_KEY] ?: "Default"
    }

    suspend fun saveConfettiType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONFETTI_TYPE_KEY] = type
        }
    }

    val showShadow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_SHADOW_KEY] ?: true
    }

    suspend fun saveShowShadow(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_SHADOW_KEY] = show
        }
    }

    val maxBrightness: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MAX_BRIGHTNESS_KEY] ?: true
    }

    suspend fun saveMaxBrightness(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_BRIGHTNESS_KEY] = enabled
        }
    }

    val emojiHeader: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EMOJI_HEADER_KEY] ?: ""
    }

    suspend fun saveEmojiHeader(emoji: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMOJI_HEADER_KEY] = emoji
        }
    }

    val bubbleEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BUBBLE_ENABLED_KEY] ?: false
    }

    suspend fun setBubbleEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUBBLE_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveBubbleEnabled(enabled: Boolean) = setBubbleEnabled(enabled)

    val bubbleSizeDp: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BUBBLE_SIZE_DP_KEY] ?: 60f
    }

    suspend fun saveBubbleSizeDp(sizeDp: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUBBLE_SIZE_DP_KEY] = sizeDp
        }
    }

    val bubbleOpacityAlpha: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BUBBLE_OPACITY_ALPHA_KEY] ?: 0.9f
    }

    suspend fun saveBubbleOpacityAlpha(alpha: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUBBLE_OPACITY_ALPHA_KEY] = alpha
        }
    }

    val bubbleGlowColorHex: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BUBBLE_GLOW_COLOR_HEX_KEY] ?: "#3DDC84"
    }

    suspend fun saveBubbleGlowColorHex(colorHex: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUBBLE_GLOW_COLOR_HEX_KEY] = colorHex
        }
    }

    val useDynamicWallpaperColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USE_DYNAMIC_WALLPAPER_COLOR_KEY] ?: true
    }

    suspend fun saveUseDynamicWallpaperColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_DYNAMIC_WALLPAPER_COLOR_KEY] = enabled
        }
    }

    val soundEffectsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED_KEY] ?: true
    }

    suspend fun saveSoundEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED_KEY] = enabled
        }
    }

    val showImagePreviews: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_IMAGE_PREVIEWS_KEY] ?: true
    }

    suspend fun saveShowImagePreviews(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_IMAGE_PREVIEWS_KEY] = show
        }
    }

    val advancedThumbnail: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ADVANCED_THUMBNAIL_KEY] ?: true
    }

    suspend fun saveAdvancedThumbnail(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ADVANCED_THUMBNAIL_KEY] = enabled
        }
    }

    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAPTIC_ENABLED_KEY] ?: true
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_ENABLED_KEY] = enabled
        }
    }

    val hapticLevel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAPTIC_LEVEL_KEY] ?: "Crisp"
    }

    suspend fun saveHapticLevel(level: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_LEVEL_KEY] = level
        }
    }

    val hapticDuration: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAPTIC_DURATION_KEY] ?: 15f
    }

    suspend fun saveHapticDuration(duration: Float) {
        context.dataStore.edit { it[PreferencesKeys.HAPTIC_DURATION_KEY] = duration }
    }

    val vibrationStrength: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIBRATION_STRENGTH_KEY] ?: 100
    }

    suspend fun saveVibrationStrength(strength: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_STRENGTH_KEY] = strength
        }
    }
}
