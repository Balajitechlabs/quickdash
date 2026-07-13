# ======================================================================
# QuickDash R8 / ProGuard rules — Stable Release v4.4.0
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
# Room uses annotation processors — keep generated _Impl classes
-keep class **_Impl { *; }
-keep class **Dao_Impl { *; }

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
-keep class com.balajitechlabs.quickdash.core.utils.UpdateState { *; }
-keep class com.balajitechlabs.quickdash.core.utils.UpdateState$* { *; }
-keep class com.balajitechlabs.quickdash.core.utils.ShakeDetector { *; }
-keep class com.balajitechlabs.quickdash.core.utils.QRCodeGenerator { *; }
-keep class com.balajitechlabs.quickdash.core.utils.BackupRestoreManager { *; }
-keep class com.balajitechlabs.quickdash.core.utils.BiometricHelper { *; }
-keep class com.balajitechlabs.quickdash.core.utils.ShareUtils { *; }
-keep class com.balajitechlabs.quickdash.core.utils.DialogLauncher { *; }
-keep class com.balajitechlabs.quickdash.core.utils.GoogleDriveSyncManager { *; }
-keep class com.balajitechlabs.quickdash.core.utils.DiagnosticLogger { *; }
-keep class com.balajitechlabs.quickdash.core.utils.IntentUtilsKt { *; }
# MySavedStateRegistryOwner is used by FloatingBubbleService to host
# Compose overlays — R8 strips it as unused because it's only referenced
# within the same file via private visibility.
-keep class com.balajitechlabs.quickdash.core.services.MySavedStateRegistryOwner { *; }
# BackupCrypto uses reflection for encryption key derivation
-keep class com.balajitechlabs.quickdash.core.utils.BackupCrypto { *; }

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
-keep class com.balajitechlabs.quickdash.features.clipboard.presentation.ActionableItem { *; }
-keep class com.balajitechlabs.quickdash.features.insta.presentation.GithubProfileCache { *; }
-keep class com.balajitechlabs.quickdash.features.qr.presentation.QrHistoryItem { *; }
-keep class com.balajitechlabs.quickdash.features.dashboard.presentation.ToolDef { *; }
-keep class com.balajitechlabs.quickdash.features.dashboard.presentation.ToolDef$* { *; }

# Enums used throughout CustomComponents (SwitchStyle, SliderStyle, ShapeStyle)
# and PaymentTargetApp.  R8 strips enum valueOf/values() which are used
# by DataStore and JSON deserialization.
-keep class com.balajitechlabs.quickdash.features.qr.presentation.PaymentTargetApp { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.SwitchStyle { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.SliderStyle { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.ShapeStyle { *; }
-keep class com.balajitechlabs.quickdash.features.dashboard.presentation.QuickTool { *; }
# CalcKey is a private sealed class — R8 inlines object subclasses and
# strips the hierarchy, breaking when-exhaustiveness at runtime.
-keep class com.balajitechlabs.quickdash.features.calculator.presentation.CalcKey { *; }
-keep class com.balajitechlabs.quickdash.features.calculator.presentation.CalcKey$* { *; }
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
# Keep both with and without allowobfuscation to ensure R8 full mode
# preserves generic signatures of anonymous TypeToken subclasses.
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
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
# 15b. ML KIT CODE SCANNER (play-services-code-scanner)
# ======================================================================
# GmsBarcodeScanning uses internal GMS classes loaded via reflection.
# Without these rules R8 strips com.google.android.gms.internal.mlkit_code_scanner.*
# causing NullPointerException at GmsBarcodeScanning.getClient() call time.
# This was the PRIMARY crash on first launch in release builds.
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_code_scanner.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.internal.mlkit_code_scanner.**

# ======================================================================
# 16. ANDROIDX SECURITY / BIOMETRIC / DATASTORE (CRITICAL)
# ======================================================================
# EncryptedSharedPreferences uses reflection for AES key generation via Tink.
# Without keeping these classes, release builds crash immediately on first
# launch with a NoClassDefFoundError inside EncryptedPrefsHelper.init().
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**
-dontwarn androidx.biometric.**
-dontwarn androidx.datastore.**
# Keep the ENTIRE Tink crypto library — EncryptedSharedPreferences uses
# Tink internally via reflection and class-loading.  Stripping any Tink
# class causes a hard crash on encrypted prefs initialization in release.
-keep class com.google.crypto.tink.** { *; }
-keep class com.google.crypto.tink.subtle.** { *; }
-keep class com.google.crypto.tink.proto.** { *; }
-dontwarn com.google.crypto.tink.**

# ======================================================================
# 17. JETPACK COMPOSE RUNTIME STABILITY (CRITICAL)
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
# Keep all Compose top-level composable functions and their lambdas.
# R8 can strip Compose function references it considers unreachable due
# to inlining by the Compose compiler plugin.
-keep class com.balajitechlabs.quickdash.**Kt { *; }
-keep class com.balajitechlabs.quickdash.**Kt$* { *; }
# Keep PaymentModeSwitcher Composable (stripped by R8 as unused)
-keep class com.balajitechlabs.quickdash.core.ui.components.PaymentModeSwitcherKt { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.ComposableSingletons$PaymentModeSwitcherKt { *; }

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

# ======================================================================
# 28. ADDITIONAL SAFETY RULES FOR STARTUP CRASHES
# ======================================================================
# Keep Kotlinx Coroutines internals that might be stripped incorrectly by R8.
# These are required for the coroutine dispatcher machinery to function in
# the release build — stripping them causes silent hangs or immediate crashes.
-keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlin.coroutines.** { *; }

# Keep DataStore classes (prevents initialization crashes when minified).
# DataStore uses reflection to find the singleton instance via the
# preferencesDataStore delegate — stripping classes breaks this.
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }
-keep class androidx.datastore.preferences.core.** { *; }

# Keep Lifecycle classes — required by Activity and Compose runtime.
-keep class androidx.lifecycle.** { *; }
-keep class * implements androidx.lifecycle.LifecycleObserver { *; }

# Keep AndroidX Core App internals
-keep class androidx.core.app.** { *; }

# Keep all Fragments and Activities implicitly
-keep class * extends androidx.fragment.app.Fragment { *; }
-keep class * extends android.app.Activity { *; }

# ======================================================================
# 29. ANDROIDX SAVEDSTATE / LIFECYCLE (FLOATING SERVICE)
# ======================================================================
# MySavedStateRegistryOwner in FloatingBubbleService implements
# SavedStateRegistryOwner.  Without these rules R8 strips the interface
# implementations, causing a ClassCastException in the Compose overlay.
-keep class androidx.savedstate.** { *; }
-dontwarn androidx.savedstate.**
-keep class * implements androidx.savedstate.SavedStateRegistryOwner { *; }

# ======================================================================
# 30. WEBVIEW (QuickWebScreen)
# ======================================================================
# WebViewClient / WebChromeClient subclasses are instantiated at runtime
# via the WebView native bridge. R8 will strip anonymous subclasses.
-keep class * extends android.webkit.WebViewClient { *; }
-keep class * extends android.webkit.WebChromeClient { *; }
-keep class android.webkit.** { *; }
-dontwarn android.webkit.**
