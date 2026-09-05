# QuickDash Architecture Guide

Welcome to the QuickDash Architecture Guide. This document provides a structural map of every architectural layer, package hierarchy, and design decision in the codebase.

For an exhaustive, auto-generated inventory of every file with lines of code and purpose, see [docs/FILE_MAP.md](docs/FILE_MAP.md).
For the algorithmic audit of text classification, QR parsing, and cryptography, see [docs/AlgorithmAudit.md](docs/AlgorithmAudit.md).

---

## 1. What is QuickDash?

QuickDash is an **Android floating productivity suite** built with Jetpack Compose. It resides as an overlay bubble on your screen, granting instant access to 25+ productivity tools (calculator, translator, QR payments and scanner, timer, notes, clipboard, Wi-Fi generator, and contextual share actions) without leaving your active app.

### Technical Stack
- **Language**: Kotlin 2.4.10
- **UI Framework**: Jetpack Compose with Material Design 3 Expressive
- **Architecture**: Clean Architecture + MVVM / UDF (Unidirectional Data Flow)
- **State & Storage**: Jetpack DataStore (UserStore preferences), Room Database (offline notes), SharedPreferences with hardware-backed Android Keystore AES-GCM encryption (EncryptedPrefsHelper)
- **Dependency Injection**: Pure Dagger Hilt via KSP
- **Network**: OkHttp 5.5.0 + GitHub Releases API
- **Barcode & QR Engine**: ZXing Core 3.5.3 + ZXing Android Embedded 4.3.0
- **Build System**: Gradle 9.3 with Kotlin DSL and Version Catalog (`gradle/libs.versions.toml`)
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 16 / Vanilla Ice Cream (API 36)

---

## 2. Directory Layout

```
quickdash/
├── app/ # Android application module
├── docs/ # Engineering documentation, file maps, and setup guides
│ ├── SETUP.md # Local developer environment setup
│ ├── FILE_MAP.md # Auto-generated source map with LOC and descriptions
│ ├── AlgorithmAudit.md # Cryptography and parser correctness specifications
│ └── file_manifest.json# Source of truth for file descriptions
├── tools/ # Automation and CI synchronization scripts
│ └── sync_file_headers.py # Deterministic header and file map generator
├── .github/ # Community health standards, CI/CD, and workflows
│ ├── CODE_OF_CONDUCT.md # Contributor Covenant Code of Conduct
│ ├── SECURITY.md # Vulnerability reporting and security policies
│ └── workflows/ # Continuous integration and release workflows
├── .githooks/ # Pre-push verification hooks
├── gradle/ # Version catalog and wrapper
├── ARCHITECTURE.md # This system architecture specification
├── CONTRIBUTING.md # Contributor onboarding rubric and guidelines
├── THIRD_PARTY_NOTICES.md # Open-source licenses and attribution
├── LICENSE # PocketOps Custom Open Source Fork License (||BTL||™)
└── PRIVACY_POLICY.md # Offline-first privacy guarantee
```

---

## 3. Application Architecture (`app/src/main/java`)

Package namespace: `com.balajitechlabs.quickdash`

### 3.1 Application Entry Points
| File | Responsibility |
|---|---|
| `QuickDashApplication.kt` | Application entry point configuring Hilt dependency injection, notification channels, and Telegram crash telemetry. |
| `MainActivity.kt` | Root launcher activity configuring Edge-to-Edge window insets, theme application, and initial navigation. |
| `MainViewModel.kt` | App-level ViewModel observing theme mode, dynamic color toggles, and onboarding progression. |

---

### 3.2 Core Infrastructure (`core/`)

#### `core/data/` — Storage & Persistence
- **`UserStore.kt`**: Single source of truth for all user preferences, feature toggles, and appearance states. Backed by Jetpack DataStore preferences with reactive `Flow` emissions.
- **`CryptoManager.kt`**: Android Keystore AES-256-GCM authenticated encryption engine ensuring unique 12-byte IVs and tamper rejection (`AEADBadTagException`).
- **`EncryptedPrefsHelper.kt`**: Hardware-backed encrypted storage for sensitive key-value pairs (Wi-Fi passwords and secure tokens).
- **`HistoryRepository.kt`**: Repository querying and persisting unified tool activity, searches, and clipboard events.
- **`BackupManager.kt`**: Encrypted full-data backup engine with AES-256-GCM, Room note serialization, and atomic JSON export/restoration.
- **`database/`**: Room database layer consisting of `AppDatabase`, `NoteDao`, and `NoteEntity`.
- **`prefs/PreferencesKeys.kt`**: Type-safe DataStore preference key declarations.

#### `core/network/` — API & Telemetry
- **`QuickDashApiClient.kt`**: Centralized OkHttp client executing authenticated REST requests, release checks, and download tasks.
- **`ApiConfig.kt` & `ApiModels.kt`**: Centralized endpoint definitions and response data classes.
- **`CrashReporter.kt`**: Captures unhandled exceptions and dispatches sanitized diagnostic telemetry to the Telegram monitoring bot.
- **`FeedbackSender.kt`**: Dispatches user diagnostics and feedback to developer support.
- **`WifiTransferServer.kt`**: Embedded HTTP server facilitating local peer-to-peer file transfer over Wi-Fi.

#### `core/services/` — Foreground & Background Services
- **`FloatingBubbleService.kt`**: Foreground system overlay service rendering the draggable floating bubble and handling radial gestures.
- **`QuickTileService.kt`**: Quick Settings tile service toggling the floating bubble overlay.
- **`QuickTileCategoryService.kt`**: Tile category provider for Android 13+.
- **`SideBarDockService.kt`**: Edge-screen docked sidebar service providing slide-out access to favorite tools.
- **`ShakeDetectorService.kt`**: Sensor service monitoring accelerometer events to trigger the overlay on device shake.
- **`QuickDashNotificationListenerService.kt`**: Notification listener monitoring incoming notifications for quick replies and clipboard capture.

#### `core/ui/` — Design System & Shared Components
- **`QuickDashApp.kt`**: Composable navigation scaffold orchestrating bottom bar tabs, floating toolbar, theme container, and transitions.
- **`QuickTool.kt`**: Comprehensive registry of all 25+ productivity tools with titles, icons, and routes.
- **`components/`**: Modular controls including `EssentialsFloatingToolbar`, `StyledControls`, `PreferenceGroup`, `PreferenceItem`, `RoundedCardContainer`, `GlassmorphismBlur`, `PixelPressAnimation`, `FavoriteCarousel`, and `AppUpdateDialog`.
- **`theme/`**: Theme engine providing `Theme.kt`, `Color.kt` (including pitch-black AMOLED overrides), `Type.kt` (Google Sans Flex), `Dimens.kt`, `HapticEngine.kt`, and `QuickDashMotion.kt`.

#### `core/utils/` — Utility Engines
- **`TextCategorizer.kt`**: Deterministic text classification engine parsing URLs, phone numbers, emails, addresses, math expressions, and passwords, wired with quick action dispatchers.
- **`UpdateManager.kt`**: GitHub Releases update state machine managing checking, downloading, and installation states.
- **`QRCodeGenerator.kt`**: ZXing bitmap generator producing monochrome and custom colored QR codes.
- **`BiometricHelper.kt`**: BiometricPrompt wrapper coordinating fingerprint and face recognition.
- **`AppLogger.kt`**: Unified diagnostic logging utility.

---

### 3.3 Feature Modules (`features/`)

Each feature module is encapsulated under `features/<name>/`:
- **`about/`**: Developer brand presentation, social links, project architecture, and manual update triggers.
- **`calculator/`**: Floating scientific and standard calculator with expression evaluation and history.
- **`clipboard/`**: Encrypted clipboard manager with auto-capture, search filtering, pinning, and auto-cleanup.
- **`dashboard/`**: FloatingDialogActivity document window hosting the primary tool grid and Spotlight launcher.
- **`notes/`**: Scratchpad markdown notes backed by Room offline persistence.
- **`qr/`**: UPI payment QR generation, custom camera scanner with zoom toggle and tap-to-focus, scan history, and semantic payload parsing (`QrPayloadParser`).
- **`settings/`**: Modularized settings screen composed of dedicated section components (`SettingsSecuritySection`, `SettingsDataSection`, `SettingsUpdatesSection`, `SettingsCommunitySection`).
- **`timer/`**: Multi-timer, stopwatch, and alarm scheduler with background BroadcastReceiver alerts.
- **`wifi/`**: Wi-Fi network credential sharing, history dialog, and connectable QR generator.
- **`voicememos/`**, **`converter/`**, **`password/`**, **`pomodoro/`**, **`search/`**, **`translator/`**, **`chat/`**, **`capture/`**, **`broadcast/`**, **`share/`**: Single-purpose productivity tools.

---

## 4. Unidirectional Data Flow (UDF) Architecture

```
┌─────────────────────────────────────────────────────────────┐
│ FloatingBubbleService │
│ (System WindowManager Overlay - Always on Top) │
└──────────────────────────────┬──────────────────────────────┘
 │ Tap / Radial Gesture
┌──────────────────────────────▼──────────────────────────────┐
│ FloatingDialogActivity │
│ (Document-isolated Compose Window) │
└──────────────────────────────┬──────────────────────────────┘
 │
┌──────────────────────────────▼──────────────────────────────┐
│ QuickDashApp.kt │
│ (Root Scaffold & Navigation) │
│ │
│ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ │
│ │ DashboardTab │ │ SettingsTab │ │ AboutTab │ │
│ └──────────────┘ └──────────────┘ └──────────────┘ │
│ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Unidirectional Data Pipeline │ │
│ │ │ │
│ │ UI Composables ◄── ViewModel ◄── Repositories │ │
│ │ │ │ │ │ │
│ │ Intents StateFlow Data Sources │ │
│ │ │ │ │ │ │
│ │ ▼ ▼ ▼ │ │
│ │ Actions ──────► UserStore / Room / OkHttp │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Algorithmic Correctness & Verification

All core algorithms are verified via automated unit test suites:
- **`TextCategorizerTest`**: Validates classification across URLs, phone numbers, geographic coordinates, email addresses, and arithmetic formulas.
- **`QrPayloadParserTest`**: Validates parsing of UPI payment URIs with encoded parameters, Wi-Fi credentials, vCards, and GTIN product barcodes.
- **`CryptoManagerTest`**: Validates AES-256-GCM encryption, unique initialization vectors per operation, authentication tag integrity, and wrong-key rejection.
- **`BackupManagerTest`**: Validates encrypted JSON backup export and atomic round-trip restoration.

Detailed test procedures and input spaces are maintained in [docs/AlgorithmAudit.md](docs/AlgorithmAudit.md).
