/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data
 * File: UserStore.kt
 * Description: Central DataStore preferences repository managing all user customizations, feature flags, and UI states.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.balajitechlabs.quickdash.core.data.prefs.PreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserStore(context: Context) : UserStoreTools(context) {

    companion object {
        val UPI_ID_KEY get() = PreferencesKeys.UPI_ID_KEY
        val UPI_IDS_KEY get() = PreferencesKeys.UPI_IDS_KEY
        val DEFAULT_UPI_ID_KEY get() = PreferencesKeys.DEFAULT_UPI_ID_KEY
        val PAYEE_NAME_KEY get() = PreferencesKeys.PAYEE_NAME_KEY
        val RECENT_AMOUNTS_KEY get() = PreferencesKeys.RECENT_AMOUNTS_KEY
        val SHOW_UPI_ID_KEY get() = PreferencesKeys.SHOW_UPI_ID_KEY
        val THEME_MODE_KEY get() = PreferencesKeys.THEME_MODE_KEY
        val DYNAMIC_COLOR_KEY get() = PreferencesKeys.DYNAMIC_COLOR_KEY
        val ANALYTICS_ENABLED_KEY get() = PreferencesKeys.ANALYTICS_ENABLED_KEY
        val HAS_REPORTED_INSTALL_KEY get() = PreferencesKeys.HAS_REPORTED_INSTALL_KEY
        val LAST_ACTIVE_DATE_KEY get() = PreferencesKeys.LAST_ACTIVE_DATE_KEY
        val IS_ONBOARDING_COMPLETE_KEY get() = PreferencesKeys.IS_ONBOARDING_COMPLETE_KEY
        val BUBBLE_ENABLED_KEY get() = PreferencesKeys.BUBBLE_ENABLED_KEY
        val LAST_TELEGRAM_UPDATE_ID_KEY get() = PreferencesKeys.LAST_TELEGRAM_UPDATE_ID_KEY
        val CHAT_DEFAULT_CODE_KEY get() = PreferencesKeys.CHAT_DEFAULT_CODE_KEY
        val CHAT_DEFAULT_ISO_KEY get() = PreferencesKeys.CHAT_DEFAULT_ISO_KEY
        val CHAT_HISTORY_KEY get() = PreferencesKeys.CHAT_HISTORY_KEY
        val CHAT_PAUSE_HISTORY_KEY get() = PreferencesKeys.CHAT_PAUSE_HISTORY_KEY
        val NOTES_HISTORY_KEY get() = PreferencesKeys.NOTES_HISTORY_KEY
        val CLIPBOARD_HISTORY_KEY get() = PreferencesKeys.CLIPBOARD_HISTORY_KEY
        val WIFI_SSID_KEY get() = PreferencesKeys.WIFI_SSID_KEY
        val WIFI_PASSWORD_KEY get() = PreferencesKeys.WIFI_PASSWORD_KEY
        val APP_LOCKED_KEY get() = PreferencesKeys.APP_LOCKED_KEY
        val DISPLAY_MODE_KEY get() = PreferencesKeys.DISPLAY_MODE_KEY
        val USE_PAYPAL_KEY get() = PreferencesKeys.USE_PAYPAL_KEY
        val PAYPAL_IDS_KEY get() = PreferencesKeys.PAYPAL_IDS_KEY
        val DEFAULT_PAYPAL_ID_KEY get() = PreferencesKeys.DEFAULT_PAYPAL_ID_KEY
        val HAPTIC_ENABLED_KEY get() = PreferencesKeys.HAPTIC_ENABLED_KEY
        val HAPTIC_LEVEL_KEY get() = PreferencesKeys.HAPTIC_LEVEL_KEY
        val TOTAL_APP_OPENS_KEY get() = PreferencesKeys.TOTAL_APP_OPENS_KEY
        val TOTAL_QR_GENERATED_KEY get() = PreferencesKeys.TOTAL_QR_GENERATED_KEY
        val TOTAL_NOTES_SAVED_KEY get() = PreferencesKeys.TOTAL_NOTES_SAVED_KEY
        val LAUNCH_STYLE_KEY get() = PreferencesKeys.LAUNCH_STYLE_KEY
        val SEED_COLOR_KEY get() = PreferencesKeys.SEED_COLOR_KEY
        val PALETTE_STYLE_KEY get() = PreferencesKeys.PALETTE_STYLE_KEY
        val SWITCH_STYLE_KEY get() = PreferencesKeys.SWITCH_STYLE_KEY
        val SLIDER_STYLE_KEY get() = PreferencesKeys.SLIDER_STYLE_KEY
        val SHAPE_STYLE_KEY get() = PreferencesKeys.SHAPE_STYLE_KEY
        val CORNER_RADIUS_KEY get() = PreferencesKeys.CORNER_RADIUS_KEY
        val BORDER_WIDTH_KEY get() = PreferencesKeys.BORDER_WIDTH_KEY
        val FONT_FAMILY_KEY get() = PreferencesKeys.FONT_FAMILY_KEY
        val FONT_SCALE_KEY get() = PreferencesKeys.FONT_SCALE_KEY
        val CONFETTI_ENABLED_KEY get() = PreferencesKeys.CONFETTI_ENABLED_KEY
        val CONFETTI_TYPE_KEY get() = PreferencesKeys.CONFETTI_TYPE_KEY
        val SHOW_SHADOW_KEY get() = PreferencesKeys.SHOW_SHADOW_KEY
        val SECURE_MODE_KEY get() = PreferencesKeys.SECURE_MODE_KEY
        val MAX_BRIGHTNESS_KEY get() = PreferencesKeys.MAX_BRIGHTNESS_KEY
        val EMOJI_HEADER_KEY get() = PreferencesKeys.EMOJI_HEADER_KEY
        val APP_LANGUAGE_KEY get() = PreferencesKeys.APP_LANGUAGE_KEY
        val SEARCH_HISTORY_KEY get() = PreferencesKeys.SEARCH_HISTORY_KEY
        val WIFI_HISTORY_KEY get() = PreferencesKeys.WIFI_HISTORY_KEY
        val LAST_SEEN_VERSION_KEY get() = PreferencesKeys.LAST_SEEN_VERSION_KEY
        val BIOMETRIC_LOCK_KEY get() = PreferencesKeys.BIOMETRIC_LOCK_KEY
        val TAB_BIOMETRIC_LOCK_KEY get() = PreferencesKeys.TAB_BIOMETRIC_LOCK_KEY
        val CLIPBOARD_AUTOCLEAN_INTERVAL_KEY get() = PreferencesKeys.CLIPBOARD_AUTOCLEAN_INTERVAL_KEY
        val CUSTOM_SEARCH_ENGINES_KEY get() = PreferencesKeys.CUSTOM_SEARCH_ENGINES_KEY
        val FIREBASE_BLOG_POSTS_KEY get() = PreferencesKeys.FIREBASE_BLOG_POSTS_KEY
        val NOTIFICATION_HISTORY_KEY get() = PreferencesKeys.NOTIFICATION_HISTORY_KEY
        val SHAKE_TO_OPEN_KEY get() = PreferencesKeys.SHAKE_TO_OPEN_KEY
        val SHAKE_TO_TRIGGER_KEY get() = PreferencesKeys.SHAKE_TO_TRIGGER_KEY
        val SHAKE_MODE_KEY get() = PreferencesKeys.SHAKE_MODE_KEY
        val SHAKE_SENSITIVITY_KEY get() = PreferencesKeys.SHAKE_SENSITIVITY_KEY
        val HAPTIC_DURATION_KEY get() = PreferencesKeys.HAPTIC_DURATION_KEY
        val CUSTOM_BACKUP_PATH_KEY get() = PreferencesKeys.CUSTOM_BACKUP_PATH_KEY
        val BUBBLE_SIZE_DP_KEY get() = PreferencesKeys.BUBBLE_SIZE_DP_KEY
        val BUBBLE_OPACITY_ALPHA_KEY get() = PreferencesKeys.BUBBLE_OPACITY_ALPHA_KEY
        val BUBBLE_GLOW_COLOR_HEX_KEY get() = PreferencesKeys.BUBBLE_GLOW_COLOR_HEX_KEY
        val USE_DYNAMIC_WALLPAPER_COLOR_KEY get() = PreferencesKeys.USE_DYNAMIC_WALLPAPER_COLOR_KEY
        val SOUND_EFFECTS_ENABLED_KEY get() = PreferencesKeys.SOUND_EFFECTS_ENABLED_KEY
        val LAST_CLIPBOARD_CLEAN_TIME_KEY get() = PreferencesKeys.LAST_CLIPBOARD_CLEAN_TIME_KEY
        val QR_USE_EMOJI_OVERLAY_KEY get() = PreferencesKeys.QR_USE_EMOJI_OVERLAY_KEY
        val WIFI_HOTSPOT_MODE_KEY get() = PreferencesKeys.WIFI_HOTSPOT_MODE_KEY
        val SHOW_IMAGE_PREVIEWS_KEY get() = PreferencesKeys.SHOW_IMAGE_PREVIEWS_KEY
        val ADVANCED_THUMBNAIL_KEY get() = PreferencesKeys.ADVANCED_THUMBNAIL_KEY
        val GOOGLE_PROFILE_NAME_KEY get() = PreferencesKeys.GOOGLE_PROFILE_NAME_KEY
        val GOOGLE_PROFILE_PHOTO_KEY get() = PreferencesKeys.GOOGLE_PROFILE_PHOTO_KEY
        val DRIVE_BACKUP_LINK_KEY get() = PreferencesKeys.DRIVE_BACKUP_LINK_KEY
        val POLL_VOTES_KEY get() = PreferencesKeys.POLL_VOTES_KEY
        val HIDDEN_NOTIFICATIONS_KEY get() = PreferencesKeys.HIDDEN_NOTIFICATIONS_KEY
        val RADIAL_CUSTOM_TOOLS_KEY get() = PreferencesKeys.RADIAL_CUSTOM_TOOLS_KEY
        val PINNED_NOTIFICATIONS_KEY get() = PreferencesKeys.PINNED_NOTIFICATIONS_KEY
        val QR_HISTORY_KEY get() = PreferencesKeys.QR_HISTORY_KEY
        val GITHUB_ACCESS_TOKEN_KEY get() = PreferencesKeys.GITHUB_ACCESS_TOKEN_KEY
        val DEFAULT_PAYMENT_APP_KEY get() = PreferencesKeys.DEFAULT_PAYMENT_APP_KEY
        val CLIPBOARD_CLEAR_DELAY_KEY get() = PreferencesKeys.CLIPBOARD_CLEAR_DELAY_KEY
        val CLIPBOARD_PINNED_KEY get() = PreferencesKeys.CLIPBOARD_PINNED_KEY
        val TIMER_HISTORY_KEY get() = PreferencesKeys.TIMER_HISTORY_KEY
        val GOOGLE_PROFILE_EMAIL_KEY get() = PreferencesKeys.GOOGLE_PROFILE_EMAIL_KEY
        val SERVER_CREDENTIALS_KEY get() = PreferencesKeys.SERVER_CREDENTIALS_KEY
        val SHOW_TOOL_DESCRIPTIONS_KEY get() = PreferencesKeys.SHOW_TOOL_DESCRIPTIONS_KEY
        val PINNED_TOOLS_KEY get() = PreferencesKeys.PINNED_TOOLS_KEY
        val BIOMETRIC_GUARD_ENABLED_KEY get() = PreferencesKeys.BIOMETRIC_GUARD_ENABLED_KEY
        val TOOL_ORDER_KEY get() = PreferencesKeys.TOOL_ORDER_KEY
        val FAVORITE_TOOLS_KEY get() = PreferencesKeys.FAVORITE_TOOLS_KEY
        val INCOGNITO_MODE_KEY get() = PreferencesKeys.INCOGNITO_MODE_KEY
        val VIBRATION_STRENGTH_KEY get() = PreferencesKeys.VIBRATION_STRENGTH_KEY
        val INCLUDE_PRE_RELEASE_KEY get() = PreferencesKeys.INCLUDE_PRE_RELEASE_KEY
    }

    // ── Security & Biometric Locks ──────────────────────────────────────────
    val isAppLocked: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_LOCKED_KEY] ?: false
    }

    suspend fun setAppLocked(locked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCKED_KEY] = locked
        }
    }

    val biometricLock: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BIOMETRIC_LOCK_KEY] ?: false
    }

    suspend fun saveBiometricLock(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_LOCK_KEY] = enabled
        }
    }

    val tabBiometricLock: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TAB_BIOMETRIC_LOCK_KEY] ?: false
    }

    suspend fun saveTabBiometricLock(locked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TAB_BIOMETRIC_LOCK_KEY] = locked
        }
    }

    val biometricGuardEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BIOMETRIC_GUARD_ENABLED_KEY] ?: false
    }

    suspend fun setBiometricGuardEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_GUARD_ENABLED_KEY] = enabled
        }
    }

    val secureMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SECURE_MODE_KEY] ?: true
    }

    suspend fun saveSecureMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SECURE_MODE_KEY] = enabled
        }
    }

    val incognitoMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.INCOGNITO_MODE_KEY] ?: false
    }

    suspend fun setIncognitoMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCOGNITO_MODE_KEY] = enabled
        }
    }

    // ── Shake & Gesture Triggers ────────────────────────────────────────────
    val shakeToOpen: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHAKE_TO_OPEN_KEY] ?: false
    }

    suspend fun saveShakeToOpen(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHAKE_TO_OPEN_KEY] = enabled }
    }

    suspend fun setShakeToOpen(enabled: Boolean) = saveShakeToOpen(enabled)

    val shakeToTrigger: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHAKE_TO_TRIGGER_KEY] ?: false
    }

    suspend fun saveShakeToTrigger(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHAKE_TO_TRIGGER_KEY] = enabled }
    }

    val shakeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHAKE_MODE_KEY] ?: "DOUBLE"
    }

    suspend fun saveShakeMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.SHAKE_MODE_KEY] = mode }
    }

    val shakeSensitivity: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHAKE_SENSITIVITY_KEY] ?: "MEDIUM"
    }

    suspend fun saveShakeSensitivity(sensitivity: String) {
        context.dataStore.edit { it[PreferencesKeys.SHAKE_SENSITIVITY_KEY] = sensitivity }
    }
}
