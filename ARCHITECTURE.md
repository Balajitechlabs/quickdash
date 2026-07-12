# QuickDash Architecture & Project Structure

This document outlines the entire architectural layout of the **QuickDash** codebase. QuickDash is built using modern Android development practices, emphasizing a clean feature-based modularity using Jetpack Compose, DataStore, and Room.

## Tree Overview

```text
app/src/main/java/com/balajitechlabs/quickdash/
├── MainActivity.kt                            # Entry point of the app. Requests permissions and starts the QuickDashService.
├── QuickDashApplication.kt                    # Application class for global initializations (Crashlytics, AppContainer).
│
├── core/                                      # Core utilities, services, and shared data logic
│   ├── data/
│   │   ├── EncryptedPrefsHelper.kt            # Handles secure storage of sensitive data like Wi-Fi passwords.
│   │   ├── RemoteConfigManager.kt             # Manages feature flags (currently deprecated/disabled for privacy).
│   │   ├── UserStore.kt                       # Single source of truth for user preferences using DataStore.
│   │   └── database/
│   │       ├── AppDatabase.kt                 # Room Database configuration for persistent storage.
│   │       ├── NoteDao.kt                     # Data Access Object for CRUD operations on Notes.
│   │       └── NoteEntity.kt                  # Room entity defining the schema for a Note.
│   │
│   ├── di/
│   │   ├── AppContainer.kt                    # Interface for manual dependency injection container.
│   │   └── AppContainerImpl.kt                # Concrete implementation providing singletons (e.g., UserStore, NoteDao).
│   │
│   ├── quicktile/
│   │   └── QuickTileService.kt                # Android Quick Settings Tile to launch the dashboard instantly.
│   │
│   ├── services/
│   │   ├── FloatingBubbleService.kt           # (Deprecated) Old floating head service.
│   │   ├── QuickDashFirebaseMessagingService.kt # Handles push notifications (disabled for privacy compliance).
│   │   └── QuickDashNotificationListenerService.kt # Listens to system notifications to display in the drawer.
│   │
│   ├── ui/
│   │   ├── QuickDashApp.kt                    # The core Jetpack Compose Navigation Host and Floating Window layout engine.
│   │   ├── components/                        # Reusable UI components across the app.
│   │   │   ├── CustomComponents.kt            # Specialized glassmorphic backgrounds and buttons.
│   │   │   ├── PreferenceGroup.kt             # Settings group header component.
│   │   │   ├── PreferenceItem.kt              # Standardized row component for settings.
│   │   │   └── WhatsNewDialog.kt              # Dialog to display release notes (Changelog) on updates.
│   │   └── theme/                             # Material 3 Design System
│   │       ├── Color.kt                       # 11 distinct color palettes.
│   │       ├── ContainerModifier.kt           # Blur and glassmorphic modifier utility.
│   │       ├── Theme.kt                       # Global app theme and Dynamic Color implementation.
│   │       └── Type.kt                        # Typography and custom font selections.
│   │
│   └── utils/
│       ├── AppLogger.kt                       # Wrapper for standard Android Log.
│       ├── BackupRestoreManager.kt            # Core logic to export/import UserStore preferences to JSON.
│       ├── BiometricHelper.kt                 # Wrapper for AndroidX BiometricPrompt for app locking.
│       ├── DiagnosticLogger.kt                # Intercepts uncaught crashes and saves them to private storage.
│       ├── DialogLauncher.kt                  # Utility to launch floating dialogs safely from Service context.
│       ├── GoogleDriveSyncManager.kt          # (Deprecated) Handled cloud backups, removed for privacy.
│       ├── LogManager.kt                      # Utility to read/write log files.
│       ├── QRCodeGenerator.kt                 # Uses ZXing to generate QR codes from strings.
│       ├── ShakeDetector.kt                   # SensorEventListener for "Shake-to-Launch" feature.
│       ├── ShareUtils.kt                      # Utility to format and share text/images to other apps.
│       └── UpdateManager.kt                   # Polls GitHub API for new APK releases.
│
├── features/                                  # Feature modules containing presentation and domain logic
│   ├── broadcast/
│   │   ├── data/TelegramPollerWorker.kt       # WorkManager job that polls Telegram for global announcements.
│   │   └── domain/TelegramTracker.kt          # Helper to send 5-star ratings or logs directly to the Developer.
│   │
│   ├── calculator/
│   │   └── presentation/QuickCalculatorScreen.kt # Expression evaluation and mathematical history tracking.
│   │
│   ├── chat/
│   │   └── presentation/QuickChatScreen.kt    # WhatsApp/Telegram direct message launcher and QR Scanner.
│   │
│   ├── clipboard/
│   │   └── presentation/ClipboardScreen.kt    # Pinned and historic clipboard tracking.
│   │
│   ├── dashboard/
│   │   ├── presentation/DashboardScreen.kt    # The main grid of tool icons.
│   │   └── presentation/FloatingDialogActivity.kt # Transparent activity for launching certain full-screen intents.
│   │
│   ├── insta/
│   │   └── presentation/QuickSocialScreen.kt  # Instagram and GitHub profile rapid viewer.
│   │
│   ├── notes/
│   │   ├── data/NotesRepositoryImpl.kt        # Concrete repository fetching notes from Room.
│   │   ├── domain/model/Note.kt               # Domain model mapping to NoteEntity.
│   │   ├── domain/repository/NotesRepository.kt # Interface for note operations.
│   │   └── presentation/QuickNotesScreen.kt   # Markdown-supported Notes UI.
│   │
│   ├── onboarding/
│   │   └── presentation/OnboardingScreen.kt   # 8-step welcome wizard for new installs.
│   │
│   ├── qr/
│   │   ├── presentation/EnterAmountScreen.kt  # Dynamic UI for entering amounts for PayPal/UPI.
│   │   ├── presentation/PaymentTargetApp.kt   # Data class representing division targets.
│   │   ├── presentation/QrHistoryDialog.kt    # List of historically generated QR payments.
│   │   ├── presentation/SetupScreen.kt        # Initial setup form for Division Slots.
│   │   └── presentation/ShowQrScreen.kt       # Renders the final generated QR code.
│   │
│   ├── search/
│   │   ├── presentation/QuickSearchScreen.kt  # Multi-engine search tool.
│   │   └── presentation/QuickWebScreen.kt     # Floating WebView browser.
│   │
│   ├── settings/
│   │   ├── data/SettingsRepositoryImpl.kt     # Interacts with UserStore for settings.
│   │   ├── domain/repository/SettingsRepository.kt
│   │   ├── presentation/BlogPostsScreen.kt    # Fetches remote articles/content.
│   │   ├── presentation/SettingsScreen.kt     # The main configurations and preferences UI.
│   │   └── presentation/SystemLogsScreen.kt   # Developer UI for viewing crash reports.
│   │
│   ├── timer/
│   │   ├── presentation/QuickTimerScreen.kt   # Stopwatch and Countdown logic.
│   │   └── presentation/TimerAlarmReceiver.kt # Broadcast receiver triggered when countdown finishes.
│   │
│   └── wifi/
│       ├── presentation/QuickWifiScreen.kt    # Extracts active network details to generate Wi-Fi QR.
│       └── presentation/WifiHistoryDialog.kt  # Logs of previously shared networks.
│
└── widget/
    └── QuickDashWidget.kt                     # AppWidgetProvider for the 1x1 Home Screen circular launch widget.
```
