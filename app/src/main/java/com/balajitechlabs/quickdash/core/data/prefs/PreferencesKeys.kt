/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/data/prefs
 * File: PreferencesKeys.kt
 * Description: Defines type-safe DataStore preference keys for theming, security, bubble controls, and tool options.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.data.prefs

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey


object PreferencesKeys {

    // Appearance & Theming
    val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    val DISPLAY_MODE_KEY = stringPreferencesKey("display_mode")
    val LAUNCH_STYLE_KEY = stringPreferencesKey("launch_style")
    val SEED_COLOR_KEY = stringPreferencesKey("seed_color")
    val PALETTE_STYLE_KEY = stringPreferencesKey("palette_style")
    val SWITCH_STYLE_KEY = stringPreferencesKey("switch_style")
    val SLIDER_STYLE_KEY = stringPreferencesKey("slider_style")
    val SHAPE_STYLE_KEY = stringPreferencesKey("shape_style")
    val CORNER_RADIUS_KEY = floatPreferencesKey("corner_radius")
    val BORDER_WIDTH_KEY = floatPreferencesKey("border_width")
    val FONT_FAMILY_KEY = stringPreferencesKey("font_family_key")
    val FONT_SCALE_KEY = floatPreferencesKey("font_scale")
    val CONFETTI_ENABLED_KEY = booleanPreferencesKey("confetti_enabled")
    val CONFETTI_TYPE_KEY = stringPreferencesKey("confetti_type")
    val SHOW_SHADOW_KEY = booleanPreferencesKey("show_shadow")
    val MAX_BRIGHTNESS_KEY = booleanPreferencesKey("max_brightness")
    val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
    val EMOJI_HEADER_KEY = stringPreferencesKey("emoji_header")

    // Security & Biometric Locks
    val APP_LOCKED_KEY = booleanPreferencesKey("app_locked")
    val BIOMETRIC_LOCK_KEY = booleanPreferencesKey("biometric_lock")
    val TAB_BIOMETRIC_LOCK_KEY = booleanPreferencesKey("tab_biometric_lock")
    val BIOMETRIC_GUARD_ENABLED_KEY = booleanPreferencesKey("biometric_guard_enabled")
    val SECURE_MODE_KEY = booleanPreferencesKey("secure_mode")
    val INCOGNITO_MODE_KEY = booleanPreferencesKey("incognito_mode")

    // Payments & Identifiers
    val UPI_ID_KEY = stringPreferencesKey("upi_id")
    val UPI_IDS_KEY = stringPreferencesKey("upi_ids")
    val DEFAULT_UPI_ID_KEY = stringPreferencesKey("default_upi_id")
    val PAYEE_NAME_KEY = stringPreferencesKey("payee_name")
    val RECENT_AMOUNTS_KEY = stringPreferencesKey("recent_amounts")
    val SHOW_UPI_ID_KEY = booleanPreferencesKey("show_upi_id")
    val USE_PAYPAL_KEY = booleanPreferencesKey("use_paypal")
    val PAYPAL_IDS_KEY = stringPreferencesKey("paypal_ids")
    val DEFAULT_PAYPAL_ID_KEY = stringPreferencesKey("default_paypal_id")
    val DEFAULT_PAYMENT_APP_KEY = stringPreferencesKey("default_payment_app")

    // Floating Bubble & Gestures
    val BUBBLE_ENABLED_KEY = booleanPreferencesKey("bubble_enabled")
    val BUBBLE_SIZE_DP_KEY = floatPreferencesKey("bubble_size_dp")
    val BUBBLE_OPACITY_ALPHA_KEY = floatPreferencesKey("bubble_opacity_alpha")
    val BUBBLE_GLOW_COLOR_HEX_KEY = stringPreferencesKey("bubble_glow_color_hex")
    val USE_DYNAMIC_WALLPAPER_COLOR_KEY = booleanPreferencesKey("use_dynamic_wallpaper_color")
    val SHAKE_TO_OPEN_KEY = booleanPreferencesKey("shake_to_open")
    val SHAKE_TO_TRIGGER_KEY = booleanPreferencesKey("shake_to_trigger")
    val SHAKE_MODE_KEY = stringPreferencesKey("shake_mode")
    val SHAKE_SENSITIVITY_KEY = stringPreferencesKey("shake_sensitivity")

    // Haptics & Sound
    val HAPTIC_ENABLED_KEY = booleanPreferencesKey("haptic_enabled")
    val HAPTIC_LEVEL_KEY = stringPreferencesKey("haptic_level")
    val HAPTIC_DURATION_KEY = floatPreferencesKey("haptic_duration")
    val VIBRATION_STRENGTH_KEY = intPreferencesKey("vibration_strength")
    val SOUND_EFFECTS_ENABLED_KEY = booleanPreferencesKey("sound_effects_enabled")

    // Dashboard Tools & Navigation
    val RADIAL_CUSTOM_TOOLS_KEY = stringPreferencesKey("radial_custom_tools")
    val PINNED_TOOLS_KEY = stringPreferencesKey("pinned_tools")
    val TOOL_ORDER_KEY = stringPreferencesKey("tool_order")
    val FAVORITE_TOOLS_KEY = stringPreferencesKey("favorite_tools")
    val SHOW_TOOL_DESCRIPTIONS_KEY = booleanPreferencesKey("show_tool_descriptions")
    val SHOW_IMAGE_PREVIEWS_KEY = booleanPreferencesKey("show_image_previews")
    val ADVANCED_THUMBNAIL_KEY = booleanPreferencesKey("advanced_thumbnail")

    // Feature Data Stores
    val CHAT_DEFAULT_CODE_KEY = stringPreferencesKey("chat_default_code")
    val CHAT_DEFAULT_ISO_KEY = stringPreferencesKey("chat_default_iso")
    val CHAT_HISTORY_KEY = stringPreferencesKey("chat_history")
    val CHAT_PAUSE_HISTORY_KEY = booleanPreferencesKey("chat_pause_history")
    val NOTES_HISTORY_KEY = stringPreferencesKey("notes_history")
    val CLIPBOARD_HISTORY_KEY = stringPreferencesKey("clipboard_history")
    val CLIPBOARD_PINNED_KEY = stringPreferencesKey("clipboard_pinned")
    val CLIPBOARD_CLEAR_DELAY_KEY = longPreferencesKey("clipboard_clear_delay")
    val CLIPBOARD_AUTOCLEAN_INTERVAL_KEY = stringPreferencesKey("clipboard_autoclean_interval")
    val LAST_CLIPBOARD_CLEAN_TIME_KEY = longPreferencesKey("last_clipboard_clean_time")
    val WIFI_SSID_KEY = stringPreferencesKey("wifi_ssid")
    val WIFI_PASSWORD_KEY = stringPreferencesKey("wifi_password")
    val WIFI_HISTORY_KEY = stringPreferencesKey("wifi_history")
    val WIFI_HOTSPOT_MODE_KEY = booleanPreferencesKey("wifi_hotspot_mode")
    val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history")
    val CUSTOM_SEARCH_ENGINES_KEY = stringPreferencesKey("custom_search_engines")
    val QR_HISTORY_KEY = stringPreferencesKey("qr_history")
    val QR_USE_EMOJI_OVERLAY_KEY = booleanPreferencesKey("qr_use_emoji_overlay")
    val TIMER_HISTORY_KEY = stringPreferencesKey("timer_history")

    // Notifications & Polling
    val NOTIFICATION_HISTORY_KEY = stringPreferencesKey("notification_history")
    val POLL_VOTES_KEY = stringPreferencesKey("poll_votes")
    val HIDDEN_NOTIFICATIONS_KEY = stringPreferencesKey("hidden_notifications")
    val PINNED_NOTIFICATIONS_KEY = stringPreferencesKey("pinned_notifications")
    val LAST_TELEGRAM_UPDATE_ID_KEY = longPreferencesKey("last_telegram_update_id")

    // Backup & Sync
    val CUSTOM_BACKUP_PATH_KEY = stringPreferencesKey("custom_backup_path")
    val DRIVE_BACKUP_LINK_KEY = stringPreferencesKey("drive_backup_link")
    val GOOGLE_PROFILE_NAME_KEY = stringPreferencesKey("google_profile_name")
    val GOOGLE_PROFILE_PHOTO_KEY = stringPreferencesKey("google_profile_photo")
    val GOOGLE_PROFILE_EMAIL_KEY = stringPreferencesKey("google_profile_email")
    val SERVER_CREDENTIALS_KEY = stringPreferencesKey("server_credentials")
    val GITHUB_ACCESS_TOKEN_KEY = stringPreferencesKey("github_access_token")

    // System & Metrics
    val TOTAL_APP_OPENS_KEY = longPreferencesKey("total_app_opens")
    val TOTAL_QR_GENERATED_KEY = longPreferencesKey("total_qr_generated")
    val TOTAL_NOTES_SAVED_KEY = longPreferencesKey("total_notes_saved")
    val ANALYTICS_ENABLED_KEY = booleanPreferencesKey("analytics_enabled")
    val HAS_REPORTED_INSTALL_KEY = booleanPreferencesKey("has_reported_install")
    val LAST_ACTIVE_DATE_KEY = stringPreferencesKey("last_active_date")
    val IS_ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("is_onboarding_complete")
    val LAST_SEEN_VERSION_KEY = stringPreferencesKey("last_seen_version")
    val INCLUDE_PRE_RELEASE_KEY = booleanPreferencesKey("include_pre_release")
    val FIREBASE_BLOG_POSTS_KEY = stringPreferencesKey("firebase_blog_posts")
}

