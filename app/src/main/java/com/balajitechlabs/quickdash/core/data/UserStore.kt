package com.balajitechlabs.quickdash.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserStore(private val context: Context) {

    init {
        EncryptedPrefsHelper.init(context)
    }

    companion object {
        val UPI_ID_KEY = stringPreferencesKey("upi_id") // Legacy
        val UPI_IDS_KEY = stringPreferencesKey("upi_ids") // New: Comma separated list
        val DEFAULT_UPI_ID_KEY = stringPreferencesKey("default_upi_id") // New default selection
        val PAYEE_NAME_KEY = stringPreferencesKey("payee_name")
        val RECENT_AMOUNTS_KEY = stringPreferencesKey("recent_amounts")
        val SHOW_UPI_ID_KEY = booleanPreferencesKey("show_upi_id")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        // New analytics enabled flag (default true)
        val ANALYTICS_ENABLED_KEY = booleanPreferencesKey("analytics_enabled")
        val HAS_REPORTED_INSTALL_KEY = booleanPreferencesKey("has_reported_install")
        val LAST_ACTIVE_DATE_KEY = stringPreferencesKey("last_active_date")
        val IS_ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("is_onboarding_complete")
        val BUBBLE_ENABLED_KEY = booleanPreferencesKey("bubble_enabled")
        val LAST_TELEGRAM_UPDATE_ID_KEY = longPreferencesKey("last_telegram_update_id")
        
        // Quick Chat Additions
        val CHAT_DEFAULT_CODE_KEY = stringPreferencesKey("chat_default_code")
        val CHAT_DEFAULT_ISO_KEY = stringPreferencesKey("chat_default_iso")
        val CHAT_HISTORY_KEY = stringPreferencesKey("chat_history")
        val CHAT_PAUSE_HISTORY_KEY = booleanPreferencesKey("chat_pause_history")
        // Advanced Features Additions
        val NOTES_HISTORY_KEY = stringPreferencesKey("notes_history")
        val CLIPBOARD_HISTORY_KEY = stringPreferencesKey("clipboard_history")
        // Duplicate SEARCH_HISTORY_KEY removed
        val WIFI_SSID_KEY = stringPreferencesKey("wifi_ssid")
        val WIFI_PASSWORD_KEY = stringPreferencesKey("wifi_password")
        val APP_LOCKED_KEY = booleanPreferencesKey("app_locked")
        val DISPLAY_MODE_KEY = stringPreferencesKey("display_mode")

        // New Upgrades Additions
        val HAPTIC_ENABLED_KEY = booleanPreferencesKey("haptic_enabled")
        val HAPTIC_LEVEL_KEY = stringPreferencesKey("haptic_level")
        val TOTAL_APP_OPENS_KEY = longPreferencesKey("total_app_opens")
        val TOTAL_QR_GENERATED_KEY = longPreferencesKey("total_qr_generated")
        val TOTAL_NOTES_SAVED_KEY = longPreferencesKey("total_notes_saved")

        // ImageToolbox & Launch Mode Settings
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
        val SECURE_MODE_KEY = booleanPreferencesKey("secure_mode")
        val MAX_BRIGHTNESS_KEY = booleanPreferencesKey("max_brightness")
        val EMOJI_HEADER_KEY = stringPreferencesKey("emoji_header")
        val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")

        // Phase 3: Search history + Wi-Fi credential history
        val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history")
        val WIFI_HISTORY_KEY = stringPreferencesKey("wifi_history")
        val LAST_SEEN_VERSION_KEY = stringPreferencesKey("last_seen_version")
        val BIOMETRIC_LOCK_KEY = booleanPreferencesKey("biometric_lock")
        val TAB_BIOMETRIC_LOCK_KEY = booleanPreferencesKey("tab_biometric_lock")
        val CLIPBOARD_AUTOCLEAN_INTERVAL_KEY = stringPreferencesKey("clipboard_autoclean_interval")
        val CUSTOM_SEARCH_ENGINES_KEY = stringPreferencesKey("custom_search_engines")
        val FIREBASE_BLOG_POSTS_KEY = stringPreferencesKey("firebase_blog_posts")
        val NOTIFICATION_HISTORY_KEY = stringPreferencesKey("notification_history")
        val SHAKE_TO_OPEN_KEY = booleanPreferencesKey("shake_to_open")
        val SHAKE_TO_TRIGGER_KEY = booleanPreferencesKey("shake_to_trigger")
        val HAPTIC_DURATION_KEY = floatPreferencesKey("haptic_duration")
        val CUSTOM_BACKUP_PATH_KEY = stringPreferencesKey("custom_backup_path")
        val LAST_CLIPBOARD_CLEAN_TIME_KEY = longPreferencesKey("last_clipboard_clean_time")
        val QR_USE_EMOJI_OVERLAY_KEY = booleanPreferencesKey("qr_use_emoji_overlay")
        val WIFI_HOTSPOT_MODE_KEY = booleanPreferencesKey("wifi_hotspot_mode")
        val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
        val ONESIGNAL_ID_KEY = stringPreferencesKey("onesignal_id")
        val SHOW_IMAGE_PREVIEWS_KEY = booleanPreferencesKey("show_image_previews")
        
        // Google Drive / Profile
        val GOOGLE_PROFILE_NAME_KEY = stringPreferencesKey("google_profile_name")
        val GOOGLE_PROFILE_PHOTO_KEY = stringPreferencesKey("google_profile_photo")
        val DRIVE_BACKUP_LINK_KEY = stringPreferencesKey("drive_backup_link")

        // Notification states
        val POLL_VOTES_KEY = stringPreferencesKey("poll_votes")
        val HIDDEN_NOTIFICATIONS_KEY = stringPreferencesKey("hidden_notifications")
        val PINNED_NOTIFICATIONS_KEY = stringPreferencesKey("pinned_notifications")
        val QR_HISTORY_KEY = stringPreferencesKey("qr_history")
        val GITHUB_ACCESS_TOKEN_KEY = stringPreferencesKey("github_access_token")
        val DEFAULT_PAYMENT_APP_KEY = stringPreferencesKey("default_payment_app")
        val CLIPBOARD_CLEAR_DELAY_KEY = longPreferencesKey("clipboard_clear_delay")
        val CLIPBOARD_PINNED_KEY = stringPreferencesKey("clipboard_pinned")
        val TIMER_HISTORY_KEY = stringPreferencesKey("timer_history")
        val GOOGLE_PROFILE_EMAIL_KEY = stringPreferencesKey("google_profile_email")
        val SERVER_CREDENTIALS_KEY = stringPreferencesKey("server_credentials")
    }

    val fcmToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FCM_TOKEN_KEY] ?: ""
    }

    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[FCM_TOKEN_KEY] = token
        }
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { it[APP_LANGUAGE_KEY] ?: "English" }
    suspend fun saveAppLanguage(lang: String) {
        context.dataStore.edit { it[APP_LANGUAGE_KEY] = lang }
    }

    // --- Google Drive Profile & Backup Link ---
    val googleProfileName: Flow<String> = context.dataStore.data.map { it[GOOGLE_PROFILE_NAME_KEY] ?: "" }
    suspend fun saveGoogleProfileName(name: String) {
        context.dataStore.edit { it[GOOGLE_PROFILE_NAME_KEY] = name }
    }

    val googleProfilePhoto: Flow<String> = context.dataStore.data.map { it[GOOGLE_PROFILE_PHOTO_KEY] ?: "" }
    suspend fun saveGoogleProfilePhoto(url: String) {
        context.dataStore.edit { it[GOOGLE_PROFILE_PHOTO_KEY] = url }
    }

    val driveBackupLink: Flow<String> = context.dataStore.data.map { it[DRIVE_BACKUP_LINK_KEY] ?: "" }
    suspend fun saveDriveBackupLink(link: String) {
        context.dataStore.edit { it[DRIVE_BACKUP_LINK_KEY] = link }
    }

    val googleProfileEmail: Flow<String> = context.dataStore.data.map { it[GOOGLE_PROFILE_EMAIL_KEY] ?: "" }
    suspend fun saveGoogleProfileEmail(email: String) {
        context.dataStore.edit { it[GOOGLE_PROFILE_EMAIL_KEY] = email }
    }

    suspend fun clearGoogleProfile() {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_PROFILE_NAME_KEY] = ""
            preferences[GOOGLE_PROFILE_PHOTO_KEY] = ""
            preferences[GOOGLE_PROFILE_EMAIL_KEY] = ""
            preferences[DRIVE_BACKUP_LINK_KEY] = ""
        }
    }

    val onesignalId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ONESIGNAL_ID_KEY] ?: ""
    }

    suspend fun saveOnesignalId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[ONESIGNAL_ID_KEY] = id
        }
    }

    val showImagePreviews: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_IMAGE_PREVIEWS_KEY] ?: true
    }

    suspend fun saveShowImagePreviews(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_IMAGE_PREVIEWS_KEY] = show
        }
    }

    val tabBiometricLock: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TAB_BIOMETRIC_LOCK_KEY] ?: false
    }

    suspend fun saveTabBiometricLock(locked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TAB_BIOMETRIC_LOCK_KEY] = locked
        }
    }

    val upiIds: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val rawIds = preferences[UPI_IDS_KEY]
        if (!rawIds.isNullOrBlank()) {
            rawIds.split(",").filter { it.isNotBlank() }
        } else {
            val legacyId = preferences[UPI_ID_KEY]
            if (!legacyId.isNullOrBlank()) listOf(legacyId) else emptyList()
        }
    }

    val defaultUpiId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_UPI_ID_KEY] ?: ""
    }

    val upiId: Flow<String?> = upiIds.map { it.firstOrNull() }

    val payeeName: Flow<String?> =
        context.dataStore.data.map { preferences -> preferences[PAYEE_NAME_KEY] ?: "" }

    val recentAmounts: Flow<List<String>> =
        context.dataStore.data.map { preferences ->
            val serialized = preferences[RECENT_AMOUNTS_KEY] ?: "100,200,500"
            serialized.split(",").filter { it.isNotBlank() }
        }

    val showUpiId: Flow<Boolean> =
        context.dataStore.data.map { preferences -> preferences[SHOW_UPI_ID_KEY] ?: true }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "AMOLED"
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY] ?: false
    }
    // Flow for analytics enabled flag
    val analyticsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ANALYTICS_ENABLED_KEY] ?: true
    }

    suspend fun isAnalyticsEnabled(): Boolean {
        return analyticsEnabled.first()
    }

    val hasReportedInstall: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_REPORTED_INSTALL_KEY] ?: false
    }
    // Save analytics enabled flag
    suspend fun saveAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ANALYTICS_ENABLED_KEY] = enabled
        }
    }

    val lastActiveDate: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_ACTIVE_DATE_KEY] ?: ""
    }

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_ONBOARDING_COMPLETE_KEY] ?: false
    }

    val bubbleEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BUBBLE_ENABLED_KEY] ?: true
    }

    val lastTelegramUpdateId: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_TELEGRAM_UPDATE_ID_KEY] ?: 0L
    }

    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAPTIC_ENABLED_KEY] ?: true
    }

    val hapticLevel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[HAPTIC_LEVEL_KEY] ?: "Crisp"
    }

    val totalAppOpens: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[TOTAL_APP_OPENS_KEY] ?: 0L
    }

    val totalQrGenerated: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[TOTAL_QR_GENERATED_KEY] ?: 0L
    }

    val totalNotesSaved: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[TOTAL_NOTES_SAVED_KEY] ?: 0L
    }

    suspend fun saveUpiIds(ids: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[UPI_IDS_KEY] = ids.joinToString(",")
            if (ids.isNotEmpty()) {
                preferences[UPI_ID_KEY] = ids.first()
                val currentDefault = preferences[DEFAULT_UPI_ID_KEY]
                if (currentDefault == null || !ids.contains(currentDefault)) {
                    preferences[DEFAULT_UPI_ID_KEY] = ids.first()
                }
            } else {
                preferences.remove(UPI_ID_KEY)
                preferences.remove(DEFAULT_UPI_ID_KEY)
            }
        }
    }

    suspend fun saveDefaultUpiId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_UPI_ID_KEY] = id
        }
    }

    suspend fun saveUpiId(id: String) {
        saveUpiIds(listOf(id))
    }

    suspend fun savePayeeName(name: String) {
        context.dataStore.edit { preferences -> preferences[PAYEE_NAME_KEY] = name }
    }

    suspend fun saveShowUpiId(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHOW_UPI_ID_KEY] = show }
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }


    suspend fun saveRecentAmount(amount: String) {
        if (amount.isBlank()) return
        context.dataStore.edit { preferences ->
            val currentList =
                (preferences[RECENT_AMOUNTS_KEY] ?: "100,200,500")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .toMutableList()
            currentList.remove(amount)
            currentList.add(0, amount)
            val newList = currentList.take(3)
            preferences[RECENT_AMOUNTS_KEY] = newList.joinToString(",")
        }
    }

    // Quick Chat flows & methods
    val chatDefaultCode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CHAT_DEFAULT_CODE_KEY] ?: "91"
    }

    val chatDefaultIso: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CHAT_DEFAULT_ISO_KEY] ?: "IN"
    }

    val chatHistory: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[CHAT_HISTORY_KEY]
        if (!raw.isNullOrBlank()) {
            raw.split(";").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }

    val chatPauseHistory: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CHAT_PAUSE_HISTORY_KEY] ?: false
    }

    suspend fun saveChatDefaultCountry(code: String, iso: String) {
        context.dataStore.edit { preferences ->
            preferences[CHAT_DEFAULT_CODE_KEY] = code
            preferences[CHAT_DEFAULT_ISO_KEY] = iso
        }
    }

    suspend fun saveChatNumberToHistory(number: String, flag: String) {
        context.dataStore.edit { preferences ->
            val paused = preferences[CHAT_PAUSE_HISTORY_KEY] ?: false
            if (!paused) {
                val current = (preferences[CHAT_HISTORY_KEY] ?: "")
                    .split(";")
                    .filter { it.isNotBlank() }
                    .toMutableList()
                val entry = "$number:$flag"
                current.remove(entry)
                current.add(0, entry)
                preferences[CHAT_HISTORY_KEY] = current.take(20).joinToString(";")
            }
        }
    }

    suspend fun clearChatHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(CHAT_HISTORY_KEY)
        }
    }

    suspend fun saveChatPauseHistory(pause: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHAT_PAUSE_HISTORY_KEY] = pause
        }
    }

    suspend fun saveDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    suspend fun setHasReportedInstall() {
        context.dataStore.edit { preferences ->
            preferences[HAS_REPORTED_INSTALL_KEY] = true
        }
    }

    suspend fun setLastActiveDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_ACTIVE_DATE_KEY] = date
        }
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETE_KEY] = true
        }
    }

    suspend fun setBubbleEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BUBBLE_ENABLED_KEY] = enabled
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveHapticLevel(level: String) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_LEVEL_KEY] = level
        }
    }

    suspend fun incrementAppOpens() {
        context.dataStore.edit { preferences ->
            val current = preferences[TOTAL_APP_OPENS_KEY] ?: 0L
            preferences[TOTAL_APP_OPENS_KEY] = current + 1
        }
    }

    suspend fun incrementQrGenerated() {
        context.dataStore.edit { preferences ->
            val current = preferences[TOTAL_QR_GENERATED_KEY] ?: 0L
            preferences[TOTAL_QR_GENERATED_KEY] = current + 1
        }
    }

    suspend fun incrementNotesSaved() {
        context.dataStore.edit { preferences ->
            val current = preferences[TOTAL_NOTES_SAVED_KEY] ?: 0L
            preferences[TOTAL_NOTES_SAVED_KEY] = current + 1
        }
    }

    suspend fun setAppLocked(locked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCKED_KEY] = locked
        }
    }

    suspend fun clearClipboardHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(CLIPBOARD_HISTORY_KEY)
        }
    }

    // Duplicate clearSearchHistory removed

    suspend fun setLastTelegramUpdateId(updateId: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_TELEGRAM_UPDATE_ID_KEY] = updateId
        }
    }

    // Advanced Features Flows & Methods
    val notesHistory: Flow<String> = flow {
        emit(EncryptedPrefsHelper.getString(NOTES_HISTORY_KEY.name, "[]") ?: "[]")
    }

    suspend fun saveNotesHistory(json: String) {
        EncryptedPrefsHelper.putString(NOTES_HISTORY_KEY.name, json)
    }

    val clipboardHistory: Flow<String> = flow {
        emit(EncryptedPrefsHelper.getString(CLIPBOARD_HISTORY_KEY.name, "[]") ?: "[]")
    }

    suspend fun saveClipboardHistory(json: String) {
        EncryptedPrefsHelper.putString(CLIPBOARD_HISTORY_KEY.name, json)
    }

    val clipboardPinned: Flow<String> = flow {
        emit(EncryptedPrefsHelper.getString(CLIPBOARD_PINNED_KEY.name, "[]") ?: "[]")
    }

    suspend fun saveClipboardPinned(json: String) {
        EncryptedPrefsHelper.putString(CLIPBOARD_PINNED_KEY.name, json)
    }

    val timerHistory: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TIMER_HISTORY_KEY] ?: "[]"
    }

    suspend fun saveTimerHistory(json: String) {
        context.dataStore.edit { preferences ->
            preferences[TIMER_HISTORY_KEY] = json
        }
    }

    // Duplicate searchHistory flow and addSearchHistory removed

    val wifiSsid: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WIFI_SSID_KEY] ?: ""
    }

    val wifiPassword: Flow<String> = flow {
        emit(EncryptedPrefsHelper.getString("wifi_password", "") ?: "")
    }

    suspend fun saveWifiCredentials(ssid: String, password: String) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_SSID_KEY] = ssid
        }
        EncryptedPrefsHelper.putString("wifi_password", password)
    }

    val isAppLocked: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[APP_LOCKED_KEY] ?: false
    }

    // Custom UI settings
    val launchStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAUNCH_STYLE_KEY] ?: "FULL_SCREEN"
    }

    val displayMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DISPLAY_MODE_KEY] ?: "FULL_SCREEN"
    }

    suspend fun saveDisplayMode(mode: String) {
        context.dataStore.edit { preferences -> preferences[DISPLAY_MODE_KEY] = mode }
    }

    val seedColor: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SEED_COLOR_KEY] ?: "#1E88E5"
    }

    val paletteStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PALETTE_STYLE_KEY] ?: "TonalSpot"
    }

    val switchStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SWITCH_STYLE_KEY] ?: "MaterialYou"
    }

    val sliderStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SLIDER_STYLE_KEY] ?: "MaterialYou"
    }

    val shapeStyle: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SHAPE_STYLE_KEY] ?: "Rounded"
    }

    val cornerRadius: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[CORNER_RADIUS_KEY] ?: 16f
    }

    val borderWidth: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[BORDER_WIDTH_KEY] ?: 1f
    }

    val fontFamilyKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FONT_FAMILY_KEY] ?: "system"
    }

    val fontScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[FONT_SCALE_KEY] ?: 1f
    }

    val confettiEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CONFETTI_ENABLED_KEY] ?: true
    }

    val confettiType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CONFETTI_TYPE_KEY] ?: "Default"
    }

    val showShadow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_SHADOW_KEY] ?: true
    }

    val secureMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SECURE_MODE_KEY] ?: false
    }

    val maxBrightness: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MAX_BRIGHTNESS_KEY] ?: false
    }

    val emojiHeader: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[EMOJI_HEADER_KEY] ?: "🚀"
    }



    val biometricLock: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BIOMETRIC_LOCK_KEY] ?: false
    }

    val clipboardAutocleanInterval: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CLIPBOARD_AUTOCLEAN_INTERVAL_KEY] ?: "OFF"
    }

    val shakeToOpen: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHAKE_TO_OPEN_KEY] ?: false
    }

    val shakeToTrigger: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHAKE_TO_TRIGGER_KEY] ?: false
    }

    val customSearchEngines: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_SEARCH_ENGINES_KEY] ?: "[]"
    }

    val firebaseBlogPosts: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FIREBASE_BLOG_POSTS_KEY] ?: "[]"
    }

    val notificationHistory: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_HISTORY_KEY] ?: "[]"
    }

    val qrUseEmojiOverlay: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[QR_USE_EMOJI_OVERLAY_KEY] ?: false
    }

    val wifiHotspotMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[WIFI_HOTSPOT_MODE_KEY] ?: false
    }

    val hapticDuration: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[HAPTIC_DURATION_KEY] ?: 15f
    }

    val customBackupPath: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_BACKUP_PATH_KEY]
    }

    val lastClipboardCleanTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_CLIPBOARD_CLEAN_TIME_KEY] ?: 0L
    }

    suspend fun saveLaunchStyle(style: String) {
        context.dataStore.edit { preferences -> preferences[LAUNCH_STYLE_KEY] = style }
    }

    suspend fun saveSeedColor(color: String) {
        context.dataStore.edit { preferences -> preferences[SEED_COLOR_KEY] = color }
    }

    suspend fun savePaletteStyle(style: String) {
        context.dataStore.edit { preferences -> preferences[PALETTE_STYLE_KEY] = style }
    }

    suspend fun saveSwitchStyle(style: String) {
        context.dataStore.edit { preferences -> preferences[SWITCH_STYLE_KEY] = style }
    }

    suspend fun saveSliderStyle(style: String) {
        context.dataStore.edit { preferences -> preferences[SLIDER_STYLE_KEY] = style }
    }

    suspend fun saveShapeStyle(style: String) {
        context.dataStore.edit { preferences -> preferences[SHAPE_STYLE_KEY] = style }
    }

    suspend fun saveCornerRadius(radius: Float) {
        context.dataStore.edit { preferences -> preferences[CORNER_RADIUS_KEY] = radius }
    }

    suspend fun saveBorderWidth(width: Float) {
        context.dataStore.edit { preferences -> preferences[BORDER_WIDTH_KEY] = width }
    }

    suspend fun saveFontFamilyKey(key: String) {
        context.dataStore.edit { preferences -> preferences[FONT_FAMILY_KEY] = key }
    }

    suspend fun saveFontScale(scale: Float) {
        context.dataStore.edit { preferences -> preferences[FONT_SCALE_KEY] = scale }
    }

    suspend fun saveConfettiEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[CONFETTI_ENABLED_KEY] = enabled }
    }

    suspend fun saveConfettiType(type: String) {
        context.dataStore.edit { preferences -> preferences[CONFETTI_TYPE_KEY] = type }
    }

    suspend fun saveShowShadow(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHOW_SHADOW_KEY] = show }
    }

    suspend fun saveSecureMode(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[SECURE_MODE_KEY] = enabled }
    }

    suspend fun saveMaxBrightness(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[MAX_BRIGHTNESS_KEY] = enabled }
    }

    suspend fun saveEmojiHeader(emoji: String) {
        context.dataStore.edit { preferences -> preferences[EMOJI_HEADER_KEY] = emoji }
    }


    suspend fun saveBiometricLock(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[BIOMETRIC_LOCK_KEY] = enabled }
    }

    suspend fun setClipboardAutocleanInterval(interval: String) {
        context.dataStore.edit { preferences -> preferences[CLIPBOARD_AUTOCLEAN_INTERVAL_KEY] = interval }
    }

    suspend fun setShakeToOpen(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHAKE_TO_OPEN_KEY] = enabled }
    }

    suspend fun saveCustomSearchEngines(json: String) {
        context.dataStore.edit { preferences -> preferences[CUSTOM_SEARCH_ENGINES_KEY] = json }
    }

    suspend fun saveFirebaseBlogPosts(json: String) {
        context.dataStore.edit { preferences -> preferences[FIREBASE_BLOG_POSTS_KEY] = json }
    }

    suspend fun saveNotificationHistory(json: String) {
        context.dataStore.edit { preferences -> preferences[NOTIFICATION_HISTORY_KEY] = json }
    }

    suspend fun saveQrUseEmojiOverlay(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[QR_USE_EMOJI_OVERLAY_KEY] = enabled }
    }

    suspend fun saveWifiHotspotMode(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[WIFI_HOTSPOT_MODE_KEY] = enabled }
    }

    suspend fun saveShakeToTrigger(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHAKE_TO_TRIGGER_KEY] = enabled }
    }

    suspend fun saveHapticDuration(duration: Float) {
        context.dataStore.edit { preferences -> preferences[HAPTIC_DURATION_KEY] = duration }
    }

    suspend fun saveCustomBackupPath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path == null) {
                preferences.remove(CUSTOM_BACKUP_PATH_KEY)
            } else {
                preferences[CUSTOM_BACKUP_PATH_KEY] = path
            }
        }
    }

    suspend fun saveLastClipboardCleanTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_CLIPBOARD_CLEAN_TIME_KEY] = time
        }
    }

    val pollVotes: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[POLL_VOTES_KEY] ?: "{}"
    }

    suspend fun savePollVote(json: String) {
        context.dataStore.edit { preferences ->
            preferences[POLL_VOTES_KEY] = json
        }
    }

    val hiddenNotifications: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[HIDDEN_NOTIFICATIONS_KEY] ?: "[]"
    }

    suspend fun saveHiddenNotifications(json: String) {
        context.dataStore.edit { preferences ->
            preferences[HIDDEN_NOTIFICATIONS_KEY] = json
        }
    }

    val pinnedNotifications: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PINNED_NOTIFICATIONS_KEY] ?: "[]"
    }

    suspend fun savePinnedNotifications(json: String) {
        context.dataStore.edit { preferences ->
            preferences[PINNED_NOTIFICATIONS_KEY] = json
        }
    }

        // Search History implementation
        val searchHistory: Flow<String> = context.dataStore.data.map { preferences ->
            preferences[SEARCH_HISTORY_KEY] ?: "[]"
        }

        suspend fun addSearchHistory(query: String) {
            if (query.isBlank()) return
            context.dataStore.edit { preferences ->
                val raw = preferences[SEARCH_HISTORY_KEY] ?: "[]"
                val list = try {
                    com.google.gson.Gson().fromJson<List<String>>(
                        raw,
                        object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                    )?.toMutableList() ?: mutableListOf()
                } catch (e: Exception) { mutableListOf() }
                list.remove(query)
                list.add(0, query)
                preferences[SEARCH_HISTORY_KEY] = com.google.gson.Gson().toJson(list.take(50))
            }
        }

        suspend fun clearSearchHistory() {
            context.dataStore.edit { preferences ->
                preferences[SEARCH_HISTORY_KEY] = "[]"
            }
        }

    // ── Wi-Fi Credential History ──────────────────────────────────────────────
    val wifiHistory: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WIFI_HISTORY_KEY] ?: "[]"
    }

    suspend fun addWifiHistory(ssid: String, password: String, securityType: String = "WPA/WPA2") {
        context.dataStore.edit { preferences ->
            val current = try {
                val raw = preferences[WIFI_HISTORY_KEY] ?: "[]"
                com.google.gson.JsonParser.parseString(raw).asJsonArray
            } catch (e: Exception) { com.google.gson.JsonArray() }
            
            // Find existing entry
            var existingEntry: com.google.gson.JsonObject? = null
            val filtered = com.google.gson.JsonArray()
            current.forEach { el ->
                val obj = el.asJsonObject
                if (obj.get("ssid")?.asString == ssid) {
                    existingEntry = obj
                } else {
                    filtered.add(el)
                }
            }
            
            val count = if (existingEntry != null) {
                (existingEntry!!.get("shareCount")?.asInt ?: 0) + 1
            } else {
                1
            }
            
            val newEntry = com.google.gson.JsonObject().apply {
                addProperty("ssid", ssid)
                addProperty("password", password)
                addProperty("securityType", securityType)
                addProperty("shareCount", count)
                addProperty("lastSharedAt", java.lang.System.currentTimeMillis())
                addProperty("savedAt", existingEntry?.get("savedAt")?.asLong ?: java.lang.System.currentTimeMillis())
            }
            
            val newArray = com.google.gson.JsonArray()
            newArray.add(newEntry)
            filtered.forEach { newArray.add(it) }
            
            // Keep up to 50 entries
            val trimmed = com.google.gson.JsonArray()
            newArray.take(50).forEach { trimmed.add(it) }
            
            preferences[WIFI_HISTORY_KEY] = trimmed.toString()
        }
    }

    suspend fun removeWifiHistoryEntry(ssid: String) {
        context.dataStore.edit { preferences ->
            val current = try {
                com.google.gson.JsonParser.parseString(
                    preferences[WIFI_HISTORY_KEY] ?: "[]"
                ).asJsonArray
            } catch (e: Exception) { com.google.gson.JsonArray() }
            val filtered = com.google.gson.JsonArray()
            current.forEach { el ->
                if (el.asJsonObject.get("ssid")?.asString != ssid) filtered.add(el)
            }
            preferences[WIFI_HISTORY_KEY] = filtered.toString()
        }
    }

    suspend fun clearWifiHistory() {
        context.dataStore.edit { preferences ->
            preferences[WIFI_HISTORY_KEY] = "[]"
        }
    }

    val lastSeenVersion: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_SEEN_VERSION_KEY] ?: ""
    }

    suspend fun saveLastSeenVersion(version: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SEEN_VERSION_KEY] = version
        }
    }

    // QR History
    val qrHistory: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[QR_HISTORY_KEY] ?: "[]"
    }

    suspend fun saveQrHistoryItem(amount: String, note: String, upiId: String, targetApp: String, category: String) {
        val newEntry = com.google.gson.JsonObject().apply {
            addProperty("amount", amount)
            addProperty("note", note)
            addProperty("upiId", upiId)
            addProperty("targetApp", targetApp)
            addProperty("category", category)
            addProperty("timestamp", java.lang.System.currentTimeMillis())
        }
        context.dataStore.edit { preferences ->
            val current = try {
                val raw = preferences[QR_HISTORY_KEY] ?: "[]"
                com.google.gson.JsonParser.parseString(raw).asJsonArray
            } catch (e: Exception) { com.google.gson.JsonArray() }
            
            val newArray = com.google.gson.JsonArray()
            newArray.add(newEntry)
            current.forEach { newArray.add(it) }
            
            val trimmed = com.google.gson.JsonArray()
            newArray.take(100).forEach { trimmed.add(it) }
            preferences[QR_HISTORY_KEY] = trimmed.toString()
        }
    }

    suspend fun clearQrHistory() {
        context.dataStore.edit { preferences ->
            preferences[QR_HISTORY_KEY] = "[]"
        }
    }

    // GitHub Access Token
    val githubAccessToken: Flow<String> = kotlinx.coroutines.flow.flow {
        emit(EncryptedPrefsHelper.getString("github_access_token", "") ?: "")
    }

    suspend fun saveGithubAccessToken(token: String) {
        EncryptedPrefsHelper.putString("github_access_token", token)
    }

    // Default Payment App
    val defaultPaymentApp: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_PAYMENT_APP_KEY] ?: "ANY"
    }

    suspend fun saveDefaultPaymentApp(app: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_PAYMENT_APP_KEY] = app
        }
    }

    // Clipboard Clear Delay
    val clipboardClearDelay: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[CLIPBOARD_CLEAR_DELAY_KEY] ?: -1L
    }

    suspend fun saveClipboardClearDelay(delayMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[CLIPBOARD_CLEAR_DELAY_KEY] = delayMs
        }
    }

    // Server Credentials
    val serverCredentials: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SERVER_CREDENTIALS_KEY] ?: "{}"
    }

    suspend fun saveServerCredentials(json: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_CREDENTIALS_KEY] = json
        }
    }
}