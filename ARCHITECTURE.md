# QuickDash Architecture Guide

> New to the project? Start here. This is a structural map of every folder and design decision in the codebase.

---

## What is QuickDash?

QuickDash is an **Android floating utility app** — it lives in a bubble on your screen and gives you 25+ tools (calculator, translator, QR codes, timer, notes, clipboard, AI actions, etc.) without leaving your current app.

**Tech Stack:**
- **Language**: Kotlin 2.1.x
- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **State**: DataStore (settings), Room (notes), Proto DataStore (history)
- **DI**: Hilt + manual `AppContainer`
- **Network**: OkHttp 4.12
- **Build**: Gradle 8.x with Kotlin DSL + version catalog
- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 16 (API 36)

---

## Project Map

```
quickdash/
├── app/                          ← Android app module
├── website/                      ← React + Vite showcase website
├── .github/workflows/            ← CI/CD pipelines (5 workflows)
├── .githooks/                    ← Pre-push hooks
├── gradle/                       ← Version catalog, wrapper
├── release/                      ← Release process docs & notes
├── scripts/                      ← Developer helper scripts
├── metadata/                     ← F-Droid store metadata
├── screenshots/                  ← App screenshots for stores
│
├── build.gradle.kts              ← Root build script
├── settings.gradle.kts           ← Module includes
├── gradle.properties             ← Build perf & Android settings
├── local.properties              ← Local SDK path (gitignored)
├── gradlew / gradlew.bat         ← Gradle wrapper
│
├── quickdash.jks                 ← App signing keystore
├── keystore.properties           ← Keystore credentials
├── keystore.b64 / gs.b64         ← Base64-encoded secrets for CI
├── update.json                   ← In-app update manifest (root copy)
│
├── ARCHITECTURE.md               ← This file
├── CHANGELOG.md                  ← Version history
├── README.md                     ← Project overview
├── SETUP.md                      ← Developer setup
├── CONTRIBUTING.md               ← Contribution guidelines
├── SECURITY.md                   ← Security policy
├── CODE_OF_CONDUCT.md            ← Community standards
├── LICENSE                       ← PocketOps Custom Open Source Fork License
├── PRIVACY_POLICY.md             ← Privacy policy
└── privacy_policy.html           ← Privacy policy (web)
```

---

## App Module (`app/`)

All production code lives under:
```
app/src/main/java/com/balajitechlabs/quickdash/
```

### Entry Points

| File | Purpose |
|------|---------|
| `QuickDashApplication.kt` | App startup. Initializes Hilt, Firebase, EncryptedPrefs. |
| `MainActivity.kt` | Launcher activity. Sets theme, permissions, navigation. |
| `MainViewModel.kt` | App-level state (theme mode, dynamic color, onboarding flag). |

---

### `core/` — Shared Infrastructure

#### `core/data/` — Persistence

| File | Purpose |
|------|---------|
| `UserStore.kt` | **Single source of truth for all user settings.** DataStore-backed. Every preference (theme, font, bubble, UPI IDs, etc.) flows through here. |
| `EncryptedPrefsHelper.kt` | Encrypted storage for secrets (Wi-Fi passwords, clipboard history). AndroidX Security + Tink. |
| `RemoteConfigManager.kt` | Firebase Remote Config feature flags. |
| `CryptoManager.kt` | AES encryption for backup files. |
| `HistoryRepository.kt` / `HistorySerializer.kt` | Proto DataStore serialization for usage history. |
| `OfflineBackupManager.kt` | Auto-backup settings to local storage. |
| `SecurityRepository.kt` | Biometric lock, incognito mode prefs. |
| `SettingsRepository.kt` | ⚠️ Deprecated. Will be removed in favor of UserStore. |
| `database/` | Room database (notes, DAOs, entities). |
| `migration/` | DataStore key migration helpers. |

#### `core/network/` — API Communication

| File | Purpose |
|------|---------|
| `QuickDashApiClient.kt` | **Main API client.** OkHttp-based. Fetches update info, announcements, sends feedback & crash reports. |
| `ApiConfig.kt` | Centralized API URLs and timeouts. |
| `ApiModels.kt` | Data classes: `UpdateInfo`, `Announcement`, `FeedbackRequest`, `CrashReportRequest`. |
| `FeedbackSender.kt` | Sends user feedback to the server. |
| `CrashReporter.kt` | Captures crashes, sends on next launch. |
| `WifiTransferServer.kt` | Local HTTP server for Wi-Fi file transfer. |

#### `core/services/` — Background Services

| File | Purpose |
|------|---------|
| `FloatingBubbleService.kt` | **The floating bubble.** System overlay service. Draggable, tap-to-open, double-tap-to-close. |
| `QuickTileService.kt` | Quick Settings tile to toggle the bubble. |
| `QuickTileCategoryService.kt` | Tile category provider for Android 13+. |
| `SideBarDockService.kt` | Edge sidebar for quick tool access. |
| `ShakeDetectorService.kt` | Shake-to-open gesture service. |
| `QuickDashFirebaseMessagingService.kt` | FCM push notifications. |
| `QuickDashNotificationListenerService.kt` | Reads notifications for clipboard & preview features. |

#### `core/quicktile/`

| File | Purpose |
|------|---------|
| `QrScannerTileService.kt` | QR Scanner Quick Settings tile. Opens camera from notification shade. |

#### `core/security/` — App Security Layer

| File | Purpose |
|------|---------|
| `IncognitoManager.kt` | Hides app from recents, disables screenshots. |
| `PlayIntegrityManager.kt` | Google Play Integrity API integration. |
| `SecurityGuardManager.kt` | Runtime integrity checks (root detection, emulator detection, tamper detection). |

#### `core/di/` — Dependency Injection

| File | Purpose |
|------|---------|
| `AppContainer.kt` | Manual DI interface (pre-Hilt pattern). |
| `AppContainerImpl.kt` | Implementation providing repositories, managers, API client. |
| `AppModule.kt` | Hilt module providing DataStore, OkHttp, Gson, etc. |

#### `core/ui/` — UI System

| File | Purpose |
|------|---------|
| `QuickDashApp.kt` | **Navigation host.** All screens, top bar, dashboard grid, tool routing live here. ~1200 lines. |
| `components/` | Reusable UI: `CustomComponents.kt` (glassmorphism, styled controls), `PreferenceGroup.kt` / `PreferenceItem.kt` (settings rows), `WhatsNewDialog.kt` (changelog popup), `PaymentModeSwitcher.kt` (UPI/PayPal toggle), `ExpressiveFloatingToolbar.kt`, `AccentWheelDialog.kt`, `Dimens.kt` (adaptive breakpoints), `RoundedCardContainer.kt`, `PixelPressAnimation.kt`, `GlassmorphismBlur.kt`. |
| `theme/` | **Visual identity system.** |
| `theme/Color.kt` | 11 built-in color palettes (Android Dark/Light, JetBrains, Emerald, Amber, Purple, etc.). |
| `theme/Theme.kt` | **Global theme.** Reads UserStore preferences — applies Material 3 dynamic color, dark mode, AMOLED black, typography, and shape style. |
| `theme/Type.kt` | Font system. Supports Space Grotesk, Nunito, Inter, more via Google Fonts. |
| `theme/Dimens.kt` | Adaptive sizing constants. |
| `theme/ContainerModifier.kt` | Glassmorphism blur effects for floating UI. |
| `theme/HapticEngine.kt` | Haptic feedback wrappers. |
| `theme/QuickDashMotion.kt` | Animation constants (springs, tweens). |

#### `core/utils/` — Helpers

| File | Purpose |
|------|---------|
| `UpdateManager.kt` | **Update state machine.** States: Idle → Checking → UpdateAvailable → Downloading → ReadyToInstall. |
| `UpdateDownloadWorker.kt` | Downloads APK via WorkManager (planned). |
| `AppLogger.kt` | Simple logging wrapper. |
| `LogManager.kt` | Read/write diagnostic logs. |
| `DiagnosticLogger.kt` | Persists crash info to disk. |
| `BackupRestoreManager.kt` | Export/import settings as JSON. |
| `BiometricHelper.kt` | Fingerprint / face unlock wrapper. |
| `QRCodeGenerator.kt` | ZXing-based QR bitmap generation. |
| `ShakeDetector.kt` | Accelerometer listener for shake gesture. |
| `ShareUtils.kt` | Share text/images via Android share sheet. |
| `IntentUtils.kt` | Common intent helpers (dial, browse, map, etc.). |
| `DialogLauncher.kt` | Safely open dialogs from Service context. |
| `AiTextCategorizer.kt` | On-device NLP text categorization. |
| `CrashRecoveryHandler.kt` | Graceful recovery after crash on next launch. |

---

### `features/` — App Features (23 packages)

Each feature follows this convention:
- `presentation/` — Compose screen(s) + ViewModel
- `domain/` — Business logic (models, repository interfaces)
- `data/` — Data sources (Room, network, DataStore)

| Feature | What it does | Key Files |
|---------|-------------|-----------|
| `ai/` | On-device AI: text categorization, local LLM inference | `LocalInferenceEngine.kt`, `LocalModelManager.kt` |
| `broadcast/` | Telegram bot polling for developer broadcasts | `TelegramPollerWorker.kt`, `TelegramTracker.kt` |
| `calculator/` | Math expression evaluator with history | `QuickCalculatorScreen.kt` |
| `capture/` | Screen recording & screenshot capture | `QuickCaptureScreen.kt`, `ScreenRecorderService.kt` |
| `chat/` | WhatsApp/Telegram direct message launcher | `QuickChatScreen.kt`, `QuickChatViewModel.kt` |
| `clipboard/` | Clipboard manager with history, pinning, search | `ClipboardScreen.kt`, `ClipboardViewModel.kt`, `ClipboardRepository.kt` |
| `converter/` | Unit converter (currency, length, temperature, etc.) | `QuickConverterScreen.kt` |
| `customizer/` | Theme, bubble, and accent color customizer | `BubbleCustomizerScreen.kt`, `CustomizerViewModel.kt` |
| `dashboard/` | **Main tool grid** + floating dialog | `DashboardScreen.kt`, `FloatingDialogActivity.kt` |
| `discount/` | Discount & tip calculator | `QuickDiscountScreen.kt` |
| `exchange/` | Cryptocurrency & forex rates | `QuickExchangeScreen.kt` |
| `eyedropper/` | Screen color picker (eyedropper) | `QuickColorEyedropperScreen.kt` |
| `insta/` | Social profile viewer (Instagram, GitHub) | `QuickSocialScreen.kt` |
| `notes/` | Markdown notes with Room persistence | `QuickNotesScreen.kt`, `NotesRepositoryImpl.kt`, `Note.kt` |
| `onboarding/` | First-launch wizard (8 steps) | `OnboardingScreen.kt`, `WelcomeOnboardingScreen.kt`, `steps/` (8 step files), `components/` (6 components) |
| `password/` | Password generator & strength checker | `QuickPasswordScreen.kt` |
| `pomodoro/` | Pomodoro focus timer | `QuickPomodoroScreen.kt` |
| `qr/` | **UPI QR payment**: generate, scan, history | `EnterAmountScreen.kt`, `ShowQrScreen.kt`, `SetupScreen.kt`, `QrHistoryDialog.kt`, `QrViewModel.kt`, `PaymentTargetApp.kt`, `QuickQrScannerScreen.kt` |
| `reminders/` | Reminder manager with notifications | `QuickRemindersScreen.kt` |
| `search/` | Multi-engine web search + floating WebView | `QuickSearchScreen.kt`, `QuickWebScreen.kt` |
| `settings/` | **All app settings** (~2800 lines), blog, logs | `SettingsScreen.kt`, `SettingsViewModel.kt`, `BlogPostsScreen.kt`, `BlogViewModel.kt`, `SystemLogsScreen.kt` |
| `share/` | Share receiver (other apps → QuickDash) | `ShareReceiverActivity.kt` |
| `timer/` | Countdown timer + stopwatch + alarm | `QuickTimerScreen.kt`, `QuickTimerViewModel.kt`, `TimerAlarmReceiver.kt` |
| `translator/` | Google Translate integration | `QuickTranslatorScreen.kt` |
| `voicememos/` | Voice recording & playback | `QuickVoiceMemosScreen.kt` |
| `wifi/` | Wi-Fi QR sharing + speed test | `QuickWifiScreen.kt`, `WifiHistoryDialog.kt`, `WifiViewModel.kt` |

---

### `widget/`

| File | Purpose |
|------|---------|
| `QuickDashWidget.kt` | Home screen widget (1x1). Launches app on tap. |

---

### `res/` — Resources

| Path | Contents |
|------|----------|
| `drawable/` | 70+ vector icons (SVG paths) for tools & UI |
| `layout/` | XML layouts for FloatingBubbleService |
| `mipmap-*/` | Adaptive launcher icons |
| `values/` | Strings, colors, themes, font declarations |
| `values-{de,es,fr,hi,ja,kn,mr,ta,te}/` | Translated strings (9 languages) |
| `values-night/` | Dark mode resources |
| `xml/` | Widget info, shortcuts, network security config, file paths, locale config |

---

## Website (`website/`)

A **React SPA** (Vite) with a companion **Node.js/Express server** for API data.

```
website/
├── index.html                    ← Vite entry HTML
├── vite.config.js                ← Vite build config
├── package.json                  ← Dependencies (React, React Router)
│
├── src/                          ← React source
│   ├── main.jsx                  ← App entry point + Router
│   ├── App.jsx                   ← Layout wrapper
│   ├── pages/
│   │   ├── Home.jsx              ← Landing page (hero, features, gallery, testimonials, FAQ, badges)
│   │   ├── Changelog.jsx         ← Version history with error/retry states
│   │   ├── Blog.jsx              ← Blog posts feed
│   │   ├── Docs.jsx              ← Documentation pages with error/retry states
│   │   └── Privacy.jsx           ← Privacy policy
│   ├── components/
│   │   ├── Navbar.jsx            ← Sticky navbar (blur backdrop, dark/light aware, mobile hamburger)
│   │   ├── Footer.jsx            ← Footer with GitHub Discussions, Telegram, links
│   │   ├── Layout.jsx            ← Shared layout (Navbar + content + Footer)
│   │   ├── ThemeToggle.jsx       ← Dark/light mode toggle
│   │   └── UpdatePopup.jsx       ← Version update notification popup (Escape-key dismissible)
│   └── styles/
│       └── pixel-theme.css       ← All styles: MD3 + Press Start 2P + Inter, grid, gallery, animations
│
├── server/                       ← Express API server (port 4000)
│   ├── index.js                  ← Express app: serves static + /api/reading/ endpoints
│   ├── package.json
│   ├── data/                     ← Server-side JSON data
│   │   ├── posts.json
│   │   ├── changelogs.json
│   │   └── docs.json
│   └── modules/                  ← Route handlers
│       ├── changelogs.js
│       ├── posts.js
│       ├── docs.js
│       └── updates.js
│
├── public/                       ← Static files (served as-is, no build)
│   ├── CNAME                     ← Custom domain: quickdash.balajitechlab.com
│   ├── sw.js                     ← Service Worker (PWA offline support)
│   ├── robots.txt
│   ├── sitemap.xml
│   ├── site.webmanifest          ← PWA manifest
│   ├── _redirects                ← Redirect rules
│   ├── .well-known/
│   │   └── assetlinks.json       ← Android App Links verification
│   ├── api/v1/                   ← Static JSON API endpoints
│   │   ├── update.json           ←   Version check
│   │   ├── announcement.json     ←   In-app banners
│   │   ├── stats.json            ←   Download count, GitHub stars
│   │   ├── health.json           ←   API health (min version)
│   │   └── tools.json            ←   Tool directory
│   └── assets/
│       ├── logo.svg / logo.png
│       ├── play_store.svg        ← Play Store badge
│       ├── github.svg            ← GitHub badge
│       ├── feature_graphic.jpg
│       └── gallery/              ← 15 screenshots (shot_1–15 in .png & .webp)
│
└── dist/                         ← Vite build output (deployed to GitHub Pages)
```

### How the Website Works

- **Development**: `vite dev` serves the React app; Vite proxies `/api/reading` → `localhost:4000` (Express).
- **Production**: `vite build` produces static files in `dist/`. API data is pre-bundled as JSON.
- **Deployment**: GitHub Actions pushes `dist/` to GitHub Pages on every push to `main`.

---

## CI/CD (`.github/workflows/`)

| Workflow | Trigger | What it does |
|----------|---------|-------------|
| `release.yml` | Push to `main` | Builds signed APK + AAB, creates GitHub Release with release notes |
| `deploy-website.yml` | Push to `main` | Builds Vite site, deploys to GitHub Pages |
| `update-stats.yml` | Every 6 hours | Refreshes GitHub star & download counts in `stats.json` |
| `nightly.yml` | Daily at 02:00 | Runs full test suite + lint + debug APK build |
| `dependency-scan.yml` | Weekly (Monday) | OWASP Dependency-Check vulnerability scan |

---

## Release Process (`release/`)

| File | Purpose |
|------|---------|
| `RELEASE_PROCESS.md` | **Step-by-step guide**: version bump → build → sign → verify → publish |
| `release_notes.md` | Markdown changelog for GitHub Releases |
| `release_notes.txt` | Plain-text version for in-app update display |

**Auxiliary:**
- `scripts/bump-version.sh` — Auto-increment versionCode & versionName (patch/minor/major)
- `.githooks/pre-push` — Git hook: blocks pushes containing secrets or unversioned builds
- `metadata/com.balajitechlabs.quickdash.yml` — F-Droid store metadata
- `update.json` (root) — In-app update manifest (mirrors `website/public/api/v1/update.json`)

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                   FloatingBubbleService                      │
│             (System Overlay — always on top)                  │
└────────────────────────┬────────────────────────────────────┘
                         │ tap / swipe
┌────────────────────────▼────────────────────────────────────┐
│                   FloatingDialogActivity                      │
│              (Transparent Compose Activity)                   │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                     QuickDashApp.kt                          │
│                  (Compose NavigationHost)                     │
│                                                               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐        │
│  │Dashboard │ │Calculator│ │Translator│ │  Timer   │  ...    │
│  │  Screen  │ │  Screen  │ │  Screen  │ │  Screen  │         │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘        │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐    │
│  │          Data Flow (Unidirectional)                   │    │
│  │                                                       │    │
│  │  Screen ← ViewModel ← Repository ← DataSource        │    │
│  │                               ├─ UserStore (DataStore)│    │
│  │                               ├─ Room (notes)         │    │
│  │                               ├─ OkHttp (API)         │    │
│  │                               └─ EncryptedPrefs       │    │
│  └──────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘

Website: React SPA (Vite) → static JSON API → GitHub Pages
App network: OkHttp → quickdash.balajitechlab.com/api/v1/
```

---

## Key Design Decisions

1. **No Retrofit** — Raw OkHttp. Fewer deps, full control over timeouts, interceptors, caching.
2. **Mixed DI** — Hilt for new code (`@HiltViewModel`, `@Inject`). Manual `AppContainer` for legacy code. Migration in progress.
3. **DataStore over SharedPreferences** — Coroutine-based, type-safe, async. `UserStore.kt` is the canonical settings store.
4. **Monolithic NavHost** — All 25+ screens in one `QuickDashApp.kt` compose tree. Simplifies routing, but file is ~1200 lines.
5. **On-device-first** — AI, translation, OCR all run locally. No cloud dependency, no telemetry.
6. **Static API** — The website serves JSON files as static assets. No server-side database. Easy to deploy, hard to break.

---

## Where to Start (New Contributors)

1. **`UserStore.kt`** — Understand how settings persist.
2. **`Theme.kt`** — How colors, fonts, and shapes are applied globally.
3. **A simple feature** (e.g., `QuickCalculatorScreen.kt`) — See a complete MVVM feature.
4. **`QuickDashApp.kt`** — How features are wired into the navigation graph.
5. **`QuickDashApiClient.kt`** — How the app communicates with the website API.

---

## File Size Reference

| File | Lines | Complexity |
|------|-------|-----------|
| `SettingsScreen.kt` | ~2800 | Very High — monolithic settings UI |
| `UserStore.kt` | ~1200 | High — all settings keys & defaults |
| `QuickDashApp.kt` | ~1200 | High — navigation host & tool routing |
| `MainActivity.kt` | ~400 | Medium — entry point |
| `FloatingBubbleService.kt` | ~395 | Medium — overlay service |
| `Theme.kt` | ~260 | Medium — visual system |
| `UpdateManager.kt` | ~210 | Low-Medium — state machine |
| `QuickDashApiClient.kt` | ~160 | Low — API calls |
| Most feature screens | 100–400 | Low-Medium — single-purpose |
| `proguard-rules.pro` | ~420 | Low — R8 rules, no logic |
