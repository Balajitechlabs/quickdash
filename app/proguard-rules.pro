# ======================================================================
# QuickDash R8 / ProGuard rules
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

# ======================================================================
# 3. BROADCAST RECEIVERS
# ======================================================================
-keep class * extends android.content.BroadcastReceiver { *; }

# ======================================================================
# 4. SERVICES
# ======================================================================
-keep class * extends android.app.Service { *; }

# ======================================================================
# 5. WORKMANAGER
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
# 7. HILT / DAGGER DI
# ======================================================================

# ======================================================================
# 8. CORE SINGLETONS & UTILITIES
# ======================================================================
-keep class com.balajitechlabs.quickdash.core.di.** { *; }
-keep class com.balajitechlabs.quickdash.core.data.UserStore { *; }
-keep class com.balajitechlabs.quickdash.core.data.RemoteConfigManager { *; }
-keep class com.balajitechlabs.quickdash.core.data.EncryptedPrefsHelper { *; }
-keep class com.balajitechlabs.quickdash.core.utils.LogManager { *; }
-keep class com.balajitechlabs.quickdash.core.utils.AppLogger { *; }
-keep class com.balajitechlabs.quickdash.core.utils.UpdateManager* { *; }
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

# ======================================================================
# 8A. BACKUP & RESTORE ENGINE (AES-256-GCM + PBKDF2)
# ======================================================================
-keep class com.balajitechlabs.quickdash.core.data.backup.** { *; }
-keepclassmembers class com.balajitechlabs.quickdash.core.data.backup.** {
    <fields>;
    <init>(...);
}

# ======================================================================
# 8B. GSON SERIALIZATION (Backup payloads & models)
# ======================================================================
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}

# ======================================================================
# 8C. RADIAL GESTURE & MODAL DIALOGS
# ======================================================================
-keep class com.balajitechlabs.quickdash.core.ui.components.RadialBubbleMenuKt { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.RadialToolItem { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.RadialSector { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.AppUpdateDialogKt { *; }
-keep class com.balajitechlabs.quickdash.features.settings.presentation.BackupRestoreDialogKt { *; }

# Telegram feature
-keep class com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker { *; }

# Repositories
-keep class com.balajitechlabs.quickdash.features.notes.data.NotesRepositoryImpl { *; }
-keep class com.balajitechlabs.quickdash.features.notes.domain.repository.NotesRepository { *; }
-keep class com.balajitechlabs.quickdash.features.settings.data.SettingsRepositoryImpl { *; }
-keep class com.balajitechlabs.quickdash.features.settings.domain.repository.SettingsRepository { *; }

# ======================================================================
# 9. KOTLINX SERIALIZATION (API models, sealed classes)
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
# 10. DATA MODELS / ENUMS / SEALED CLASSES
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
# 11. GOOGLE FONTS
# ======================================================================
-keep class androidx.compose.ui.text.googlefonts.** { *; }
-keep class androidx.compose.ui.text.font.** { *; }
-keep class com.google.android.gms.fonts.** { *; }
-dontwarn com.google.android.gms.fonts.**

# ======================================================================
# 12. ANDROIDX GRAPHICS-SHAPES
# ======================================================================
-keep class androidx.graphics.shapes.** { *; }
-dontwarn androidx.graphics.shapes.**

# ======================================================================
# 13. GSON
# ======================================================================
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }
-keep class * implements com.google.gson.TypeAdapter { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keepattributes Signature

# ======================================================================
# 14. FIREBASE / GOOGLE PLAY SERVICES
# ======================================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# ======================================================================
# 15. OKHTTP
# ======================================================================
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames interface okhttp3.internal.http2.Http2Connection$Listener

# ======================================================================
# 16. ZXING QR
# ======================================================================
-keep class com.google.zxing.** { *; }

# ======================================================================
# 17. ML KIT CODE SCANNER
# ======================================================================
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_code_scanner.** { *; }
-keep class com.google.android.gms.vision.** { *; }

# ======================================================================
# 18. ANDROIDX SECURITY / CRYPTO / TINK
# ======================================================================
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }
-keep class com.google.crypto.tink.subtle.** { *; }
-keep class com.google.crypto.tink.proto.** { *; }

# ======================================================================
# 19. JETPACK COMPOSE
# ======================================================================
-keep class androidx.compose.runtime.** { *; }
-keep class com.balajitechlabs.quickdash.**Kt { *; }
-keep class com.balajitechlabs.quickdash.**Kt$* { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.PaymentModeSwitcherKt { *; }
-keep class com.balajitechlabs.quickdash.core.ui.components.ComposableSingletons$PaymentModeSwitcherKt { *; }

# ======================================================================
# 20. KOTLIN METADATA & REFLECTION
# ======================================================================
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keep class kotlin.Metadata { *; }

# ======================================================================
# 21. PARCELABLE
# ======================================================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ======================================================================
# 22. @KEEP ANNOTATED
# ======================================================================
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# ======================================================================
# 23. COIL IMAGE LOADER
# ======================================================================
-keep class coil.** { *; }

# ======================================================================
# 24. KONFETTI
# ======================================================================
-keep class nl.dionsegijn.konfetti.** { *; }

# ======================================================================
# 25. LIFECYCLE / DATASTORE / WEBVIEW / PLAY CORE
# ======================================================================
-keep class androidx.lifecycle.** { *; }
-keep class * implements androidx.lifecycle.LifecycleObserver { *; }
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }
-keep class androidx.core.app.** { *; }
-keep class * extends android.webkit.WebViewClient { *; }
-keep class * extends android.webkit.WebChromeClient { *; }
-keep class android.webkit.** { *; }
-keep class com.google.android.play.core.** { *; }

# ======================================================================
# 26. COROUTINES
# ======================================================================
-keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlin.coroutines.** { *; }

# ======================================================================
# 27. CRASH SYMBOLICATION
# ======================================================================
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# ======================================================================
# 28. WARNING SUPPRESSION
# ======================================================================
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.internal.mlkit_code_scanner.**
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.zxing.**
-dontwarn com.google.android.gms.fonts.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn coil.**
-dontwarn nl.dionsegijn.konfetti.**
-dontwarn com.google.auto.value.**
-dontwarn javax.lang.model.**
-dontwarn autovalue.shaded.**
-dontwarn org.apache.http.**

# ======================================================================
# 29. LOG CLEANUP IN RELEASE
# ======================================================================
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ======================================================================
# 30. R8 OPTIMIZATIONS
# ======================================================================
-allowaccessmodification
