# ======================================================================
# QuickDash R8 / ProGuard rules — Stable Release v4.3.0
# ======================================================================
# These rules are carefully audited to prevent runtime crashes in the
# minified release APK.  Every rule has a comment explaining WHY it is
# needed so future developers can safely prune obsolete entries.

# ======================================================================
# 1. APP ENTRY POINTS (registered in AndroidManifest.xml)
# ======================================================================
# These classes are instantiated by the Android framework via reflection.
-keep class com.balajitechlabs.quickdash.QuickDashApplication { *; }
-keep class com.balajitechlabs.quickdash.MainActivity { *; }
-keep class com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity { *; }
-keep class com.balajitechlabs.quickdash.core.services.FloatingBubbleService { *; }
-keep class com.balajitechlabs.quickdash.core.services.QuickDashFirebaseMessagingService { *; }
-keep class com.balajitechlabs.quickdash.core.services.QuickDashNotificationListenerService { *; }
-keep class com.balajitechlabs.quickdash.core.quicktile.QuickTileService { *; }

# ======================================================================
# 2. GLANCE APP WIDGET (registered in AndroidManifest.xml as receiver)
# ======================================================================
# GlanceAppWidgetReceiver and GlanceAppWidget are instantiated by the
# system via the <receiver> tag.  R8 strips them if not kept.
-keep class com.balajitechlabs.quickdash.widget.QuickDashWidget { *; }
-keep class com.balajitechlabs.quickdash.widget.QuickDashWidgetReceiver { *; }
# Keep all Glance internals that use reflection for content providers
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# ======================================================================
# 3. BROADCAST RECEIVERS & ALARM RECEIVERS
# ======================================================================
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class com.balajitechlabs.quickdash.features.timer.presentation.TimerAlarmReceiver { *; }

# ======================================================================
# 4. SERVICES (FloatingBubble, NotificationListener, etc.)
# ======================================================================
-keep class * extends android.app.Service { *; }

# ======================================================================
# 5. WORKMANAGER / BACKGROUND WORKERS
# ======================================================================
# WorkManager uses reflection to instantiate Worker classes.
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
# Explicitly keep our TelegramPollerWorker (it's the primary worker)
-keep class com.balajitechlabs.quickdash.features.broadcast.data.TelegramPollerWorker { *; }

# ======================================================================
# 6. ROOM DATABASE, ENTITIES, AND DAOs
# ======================================================================
-keep class com.balajitechlabs.quickdash.core.data.database.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# ======================================================================
# 7. DEPENDENCY INJECTION CONTAINER & CORE SINGLETONS
# ======================================================================
# AppContainer interface and its implementation use lazy delegation.
# R8 can inline and strip the interface methods, breaking the cast.
-keep class com.balajitechlabs.quickdash.core.di.** { *; }

# Core singletons / object classes that are accessed from multiple
# compilation units.  R8 may strip companion objects or init blocks.
-keep class com.balajitechlabs.quickdash.core.data.UserStore { *; }
-keep class com.balajitechlabs.quickdash.core.data.RemoteConfigManager { *; }
-keep class com.balajitechlabs.quickdash.core.data.EncryptedPrefsHelper { *; }
-keep class com.balajitechlabs.quickdash.core.utils.LogManager { *; }
-keep class com.balajitechlabs.quickdash.core.utils.AppLogger { *; }
-keep class com.balajitechlabs.quickdash.core.utils.UpdateManager** { *; }
-keep class com.balajitechlabs.quickdash.core.utils.ShakeDetector { *; }
-keep class com.balajitechlabs.quickdash.core.utils.QRCodeGenerator { *; }
-keep class com.balajitechlabs.quickdash.core.utils.BackupRestoreManager { *; }
-keep class com.balajitechlabs.quickdash.core.utils.BiometricHelper { *; }
-keep class com.balajitechlabs.quickdash.core.utils.ShareUtils { *; }
-keep class com.balajitechlabs.quickdash.core.utils.DialogLauncher { *; }
-keep class com.balajitechlabs.quickdash.core.utils.GoogleDriveSyncManager { *; }

# Telegram feature singletons
-keep class com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker { *; }

# Repositories (accessed via interface delegation in AppContainerImpl)
-keep class com.balajitechlabs.quickdash.features.notes.data.NotesRepositoryImpl { *; }
-keep class com.balajitechlabs.quickdash.features.notes.domain.repository.NotesRepository { *; }
-keep class com.balajitechlabs.quickdash.features.settings.data.SettingsRepositoryImpl { *; }
-keep class com.balajitechlabs.quickdash.features.settings.domain.repository.SettingsRepository { *; }

# ======================================================================
# 8. DATA MODELS / ENUMS / SEALED CLASSES
# ======================================================================
# Keep all data models used for JSON serialization and DataStore state
-keep class com.balajitechlabs.quickdash.core.data.** { *; }
-keep class com.balajitechlabs.quickdash.features.**.domain.model.** { *; }
-keep class com.balajitechlabs.quickdash.features.**.UiState** { *; }
-keep class com.balajitechlabs.quickdash.**UiState** { *; }

# Feature-specific data classes
-keep class com.balajitechlabs.quickdash.features.wifi.presentation.WifiEntry { *; }
-keep class com.balajitechlabs.quickdash.features.chat.presentation.Country { *; }
-keep class com.balajitechlabs.quickdash.features.notes.domain.model.Note { *; }

# Enums used throughout CustomComponents (SwitchStyle, SliderStyle, ShapeStyle)
# and PaymentTargetApp.  R8 strips enum valueOf/values() which are used
# by DataStore and JSON deserialization.
-keep class com.balajitechlabs.quickdash.features.qr.presentation.PaymentTargetApp { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.SwitchStyle { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.SliderStyle { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.ShapeStyle { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ======================================================================
# 9. GOOGLE FONTS (CRITICAL — #1 CRASH SOURCE IN RELEASE BUILDS)
# ======================================================================
# The Google Fonts provider uses reflection to resolve the GMS font
# content provider.  R8 strips the provider class + the font certificate
# array resource, causing an immediate crash on theme initialization.
-keep class androidx.compose.ui.text.googlefonts.** { *; }
-keep class androidx.compose.ui.text.font.** { *; }
# Keep the GMS font provider class that is resolved via reflection
-keep class com.google.android.gms.fonts.** { *; }
-dontwarn com.google.android.gms.fonts.**

# ======================================================================
# 10. ANDROIDX GRAPHICS-SHAPES (RoundedPolygon, Star, etc.)
# ======================================================================
# Used in CustomComponents.kt for Squircle/Smooth shape styles.
# The library uses internal reflection for path morph operations.
-keep class androidx.graphics.shapes.** { *; }
-dontwarn androidx.graphics.shapes.**

# ======================================================================
# 11. GSON / JSON ADAPTERS (CRITICAL — TypeToken reflection)
# ======================================================================
# Gson uses Class.getGenericSuperclass() on anonymous TypeToken
# subclasses (e.g. object : TypeToken<List<String>>() {}) to extract
# generic type info at runtime.  R8 strips the generic signature
# from these anonymous classes, causing Gson to return raw Object
# instead of the parameterized type → ClassCastException at runtime.
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }
-keep class * implements com.google.gson.TypeAdapter { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keep class * extends com.google.gson.TypeAdapter { *; }
-keep class * extends com.google.gson.TypeAdapterFactory { *; }
# Keep generic signatures on all classes (needed by Gson TypeToken)
-keepattributes Signature

# ======================================================================
# 12. FIREBASE / GOOGLE PLAY SERVICES
# ======================================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ======================================================================
# 13. ONESIGNAL (kept even though disabled — prevents missing-class warnings)
# ======================================================================
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**

# ======================================================================
# 14. OKHTTP / RETROFIT / OKIO / COROUTINES
# ======================================================================
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn kotlinx.coroutines.**

# ======================================================================
# 15. ZXING QR GENERATION
# ======================================================================
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# ======================================================================
# 16. ANDROIDX SECURITY / BIOMETRIC / DATASTORE
# ======================================================================
# EncryptedSharedPreferences uses reflection for AES key generation.
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**
-dontwarn androidx.biometric.**
-dontwarn androidx.datastore.**
# Keep the Tink crypto library used internally by EncryptedSharedPreferences
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# ======================================================================
# 17. JETPACK COMPOSE RUNTIME STABILITY
# ======================================================================
# Compose compiler generates $stable metadata fields and uses reflection
# for recomposition scoping.  Stripping these causes random crashes.
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.runtime.**
# Keep Compose UI internals that resolve layout nodes
-dontwarn androidx.compose.ui.**
-dontwarn androidx.compose.material3.**
-dontwarn androidx.compose.foundation.**
-dontwarn androidx.compose.animation.**

# ======================================================================
# 18. KOTLIN METADATA & REFLECTION
# ======================================================================
# Keep Kotlin metadata annotations so that reflection on Kotlin classes
# (used by Gson, Room, DataStore, etc.) works correctly.
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ======================================================================
# 19. KEEP PARCELABLE CREATORS (required for Intent extras)
# ======================================================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ======================================================================
# 20. KEEP @Keep ANNOTATED CLASSES
# ======================================================================
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# ======================================================================
# 21. COIL IMAGE LOADER
# ======================================================================
-keep class coil.** { *; }
-dontwarn coil.**

# ======================================================================
# 22. GOOGLE API CLIENT (Drive SDK, even if disabled)
# ======================================================================
-dontwarn com.google.api.client.**
-dontwarn com.google.auth.**
-dontwarn com.google.api.services.**
-dontwarn org.apache.http.**
-dontwarn org.apache.commons.**

# ======================================================================
# 23. KONFETTI (Confetti animation library)
# ======================================================================
-keep class nl.dionsegijn.konfetti.** { *; }
-dontwarn nl.dionsegijn.konfetti.**

# ======================================================================
# 24. R8 OPTIMIZATION SETTINGS
# ======================================================================
-allowaccessmodification
-optimizationpasses 3

# ======================================================================
# 25. LOGGING CLEANUP IN RELEASE
# ======================================================================
# IMPORTANT: Only strip android.util.Log calls. Do NOT strip our custom
# LogManager or AppLogger — they are used for crash reporting via Telegram.
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
# NOTE: We intentionally keep Log.w() and Log.e() so that warning/error
# logs are still available in production for crash diagnostics.

# ======================================================================
# 26. CRASH SYMBOLICATION
# ======================================================================
# Preserve source file & line info for Firebase Crashlytics and Telegram
# crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ======================================================================
# 27. PALETTE / APPCOMPAT
# ======================================================================
-dontwarn androidx.palette.**
-dontwarn androidx.appcompat.**
