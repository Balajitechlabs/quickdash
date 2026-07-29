# ======================================================================
# QuickDash R8 / ProGuard rules — Stable Release v5.1.1
# ======================================================================

# ======================================================================
# 1. APP ENTRY POINTS (registered in AndroidManifest.xml)
# ======================================================================
-keep class com.balajitechlabs.quickdash.QuickDashApplication { *; }
-keep class com.balajitechlabs.quickdash.MainActivity { *; }
-keep class com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity { *; }
-keep class com.balajitechlabs.quickdash.core.services.FloatingBubbleService { *; }
-keep class com.balajitechlabs.quickdash.core.services.QuickDashFirebaseMessagingService { *; }
-keep class com.balajitechlabs.quickdash.core.services.QuickDashNotificationListenerService { *; }
-keep class com.balajitechlabs.quickdash.core.quicktile.QuickTileService { *; }
-keep class com.balajitechlabs.quickdash.core.quicktile.QrScannerTileService { *; }

# ======================================================================
# 2. GLANCE APP WIDGET
# ======================================================================
-keep class com.balajitechlabs.quickdash.widget.QuickDashWidget { *; }
-keep class com.balajitechlabs.quickdash.widget.QuickDashWidgetReceiver { *; }
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# ======================================================================
# 3. BROADCAST RECEIVERS & ALARM RECEIVERS
# ======================================================================
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class com.balajitechlabs.quickdash.features.timer.presentation.TimerAlarmReceiver { *; }

# ======================================================================
# 4. SERVICES
# ======================================================================
-keep class * extends android.app.Service { *; }

# ======================================================================
# 5. WORKMANAGER / BACKGROUND WORKERS
# ======================================================================
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.balajitechlabs.quickdash.features.broadcast.data.TelegramPollerWorker { *; }

# ======================================================================
# 6. ROOM DATABASE
# ======================================================================
-keep class com.balajitechlabs.quickdash.core.data.database.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class **_Impl { *; }
-keep class **Dao_Impl { *; }

# ======================================================================
# 7. DI CONTAINER & CORE SINGLETONS
# ======================================================================
-keep class com.balajitechlabs.quickdash.core.di.** { *; }
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
-keep class com.balajitechlabs.quickdash.core.utils.DiagnosticLogger { *; }
-keep class com.balajitechlabs.quickdash.core.utils.IntentUtilsKt { *; }
-keep class com.balajitechlabs.quickdash.core.utils.CrashRecoveryHandler { *; }
-keep class com.balajitechlabs.quickdash.core.utils.UpdateDownloadWorker { *; }
-keep class com.balajitechlabs.quickdash.core.services.MySavedStateRegistryOwner { *; }
-keep class com.balajitechlabs.quickdash.core.utils.BackupCrypto { *; }
-keep class com.balajitechlabs.quickdash.core.network.QuickDashApiClient { *; }
-keep class com.balajitechlabs.quickdash.core.network.ApiConfig { *; }

# Telegram feature singletons
-keep class com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker { *; }

# Repositories
-keep class com.balajitechlabs.quickdash.features.notes.data.NotesRepositoryImpl { *; }
-keep class com.balajitechlabs.quickdash.features.notes.domain.repository.NotesRepository { *; }
-keep class com.balajitechlabs.quickdash.features.settings.data.SettingsRepositoryImpl { *; }
-keep class com.balajitechlabs.quickdash.features.settings.domain.repository.SettingsRepository { *; }

# ======================================================================
# 8. KOTLINX SERIALIZATION MODELS (ApiModels, sealed classes)
# ======================================================================
-keep,includedescriptorclasses class com.balajitechlabs.quickdash.**$$serializer { *; }
-keepclassmembers class com.balajitechlabs.quickdash.** {
    *** Companion;
}
-keepclasseswithmembers class com.balajitechlabs.quickdash.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.balajitechlabs.quickdash.core.network.UpdateInfoResponse { *; }
-keep class com.balajitechlabs.quickdash.core.network.AnnouncementResponse { *; }
-keep class com.balajitechlabs.quickdash.core.network.FeedbackRequest { *; }
-keep class com.balajitechlabs.quickdash.core.network.CrashReportRequest { *; }

# ======================================================================
# 9. DATA MODELS / ENUMS / SEALED CLASSES
# ======================================================================
-keep class com.balajitechlabs.quickdash.core.data.** { *; }
-keep class com.balajitechlabs.quickdash.features.**.domain.model.** { *; }
-keep class com.balajitechlabs.quickdash.features.**.UiState** { *; }
-keep class com.balajitechlabs.quickdash.**UiState** { *; }
-keep class com.balajitechlabs.quickdash.features.wifi.presentation.WifiEntry { *; }
-keep class com.balajitechlabs.quickdash.features.chat.presentation.Country { *; }
-keep class com.balajitechlabs.quickdash.features.notes.domain.model.Note { *; }
-keep class com.balajitechlabs.quickdash.features.clipboard.presentation.ActionableItem { *; }
-keep class com.balajitechlabs.quickdash.features.insta.presentation.GithubProfileCache { *; }
-keep class com.balajitechlabs.quickdash.features.qr.presentation.QrHistoryItem { *; }
-keep class com.balajitechlabs.quickdash.features.dashboard.presentation.ToolDef { *; }
-keep class com.balajitechlabs.quickdash.features.dashboard.presentation.ToolDef$* { *; }
-keep class com.balajitechlabs.quickdash.features.qr.presentation.PaymentTargetApp { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.SwitchStyle { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.SliderStyle { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.ShapeStyle { *; }
-keep class com.balajitechlabs.quickdash.features.dashboard.presentation.QuickTool { *; }
-keep class com.balajitechlabs.quickdash.features.calculator.presentation.CalcKey { *; }
-keep class com.balajitechlabs.quickdash.features.calculator.presentation.CalcKey$* { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ======================================================================
# 10. GOOGLE FONTS (CRITICAL — crash on theme init if stripped)
# ======================================================================
-keep class androidx.compose.ui.text.googlefonts.** { *; }
-keep class androidx.compose.ui.text.font.** { *; }
-keep class com.google.android.gms.fonts.** { *; }
-dontwarn com.google.android.gms.fonts.**

# ======================================================================
# 11. ANDROIDX GRAPHICS-SHAPES (Squircle/Smooth shape styles)
# ======================================================================
-keep class androidx.graphics.shapes.** { *; }
-dontwarn androidx.graphics.shapes.**

# ======================================================================
# 12. GSON / TypeToken (CRITICAL — generic signature reflection)
# ======================================================================
-keep class com.google.gson.** { *; }
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
-keepattributes Signature

# ======================================================================
# 13. FIREBASE / GOOGLE PLAY SERVICES
# ======================================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ======================================================================
# 14. OKHTTP
# ======================================================================
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames interface okhttp3.internal.http2.Http2Connection$Listener
-dontwarn okhttp3.**
-dontwarn okio.**

# ======================================================================
# 15. ZXING QR GENERATION
# ======================================================================
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# ======================================================================
# 16. ML KIT CODE SCANNER (primary crash source in release builds)
# ======================================================================
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_code_scanner.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.internal.mlkit_code_scanner.**

# ======================================================================
# 17. ANDROIDX SECURITY / CRYPTO / TINK (CRITICAL — EncryptedPrefs)
# ======================================================================
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**
-dontwarn androidx.biometric.**
-keep class com.google.crypto.tink.** { *; }
-keep class com.google.crypto.tink.subtle.** { *; }
-keep class com.google.crypto.tink.proto.** { *; }
-dontwarn com.google.crypto.tink.**

# ======================================================================
# 18. JETPACK COMPOSE RUNTIME
# ======================================================================
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.runtime.**
-dontwarn androidx.compose.ui.**
-dontwarn androidx.compose.material3.**
-dontwarn androidx.compose.foundation.**
-dontwarn androidx.compose.animation.**
-keep class com.balajitechlabs.quickdash.**Kt { *; }
-keep class com.balajitechlabs.quickdash.**Kt$* { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.PaymentModeSwitcherKt { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.ComposableSingletons$PaymentModeSwitcherKt { *; }

# ======================================================================
# 19. KOTLIN METADATA & REFLECTION
# ======================================================================
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ======================================================================
# 20. PARCELABLE CREATORS
# ======================================================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ======================================================================
# 21. @Keep ANNOTATED CLASSES
# ======================================================================
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# ======================================================================
# 22. COIL IMAGE LOADER
# ======================================================================
-keep class coil.** { *; }
-dontwarn coil.**

# ======================================================================
# 23. KONFETTI
# ======================================================================
-keep class nl.dionsegijn.konfetti.** { *; }
-dontwarn nl.dionsegijn.konfetti.**

# ======================================================================
# 24. R8 OPTIMIZATION
# ======================================================================
-allowaccessmodification
-optimizationpasses 5

# ======================================================================
# 25. LOGGING CLEANUP IN RELEASE
# ======================================================================
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ======================================================================
# 26. CRASH SYMBOLICATION
# ======================================================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ======================================================================
# 27. ANDROIDX LIFECYCLE / DATASTORE / SAVEDSTATE / WEBVIEW / PLAY CORE
# ======================================================================
-keep class androidx.lifecycle.** { *; }
-keep class * implements androidx.lifecycle.LifecycleObserver { *; }
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }
-keep class androidx.core.app.** { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }
-keep class * extends android.app.Activity { *; }
-keep class androidx.savedstate.** { *; }
-dontwarn androidx.savedstate.**
-keep class * implements androidx.savedstate.SavedStateRegistryOwner { *; }
-keep class * extends android.webkit.WebViewClient { *; }
-keep class * extends android.webkit.WebChromeClient { *; }
-keep class android.webkit.** { *; }
-dontwarn android.webkit.**
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**
-dontwarn androidx.palette.**
-dontwarn androidx.appcompat.**

# ======================================================================
# 28. COROUTINES (prevents silent hangs in release)
# ======================================================================
-keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlin.coroutines.** { *; }
-dontwarn kotlinx.**

# ======================================================================
# 29. CLEANUP — suppress non-actionable warnings
# ======================================================================
-dontwarn javax.lang.model.**
-dontwarn autovalue.shaded.**
-dontwarn com.google.auto.value.**
-dontwarn com.squareup.javapoet.**
-dontwarn org.apache.http.**
-dontwarn org.apache.commons.**
