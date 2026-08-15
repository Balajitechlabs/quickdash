# QuickDash Official Release Changelog 📜

All major updates, feature additions, bug fixes, and system improvements for **QuickDash** are documented in this file.

## [5.2.2] - 2026-08-15 (Zero-Tracker FOSS Edition & Privacy Hardening Release) 🌿

### 🚀 ADDED & IMPROVED
- **🌿 100% Zero-Tracker FOSS Build**: Dedicated FOSS release target for F-Droid and IzzyOnDroid with zero Firebase analytics, zero Crashlytics, and zero ML Kit proprietary trackers (verified 0 trackers on Exodus Privacy).
- **🛡️ Split Architecture**: Clean separation between standard Google Play builds (`src/standard`) and pure FOSS builds (`src/foss`).
- **📷 Dynamic Proxy QR Scanner**: Tracker-free QR code scanner leveraging safe reflection and ZXing fallback.
- **🎯 4-Tool Customizable Radial Wheel**: Rapid 350ms long-press bubble tool selection.
- **📱 1-Tap Control Center Tile & Glance Widgets**: Material You dynamic home screen widgets and 1-tap quick settings tile.
- **🔐 AES-256-GCM Encrypted Backup & Restore**: Robust PBKDF2 local backup protection.

---

## [5.2.1] - 2026-08-14 (Backup & Stability Release) ⚡

### 🚀 ADDED & IMPROVED
- **🎨 Customizable Radial Bubble Wheel**: Users can now select and customize their top 4 favorite shortcut tools for the floating bubble wheel directly in Settings (`CustomizeBubbleDialog.kt` & `RadialToolCatalog.kt`).
- **📱 1-Tap Home Screen Widget Bubble Toggle**: Instant on/off floating bubble toggling directly from Glance Material You widgets (`ToggleBubbleReceiver.kt`).
- **⚡ 120Hz Animation Optimizations**: Offloaded radial bubble scale and alpha physics springs to GPU RenderThread via `Modifier.graphicsLayer` for zero-jank 120Hz rendering on Android 15 & 16.
- **📱 Glance Material You Dynamic Theming**: Integrated `androidx.glance:glance-material3:1.1.1` dynamic wallpaper palette theming across 1x1 Compact, 2x1 Quick Bar, and 2x2 Tool Hub widgets.
- **🔐 Encrypted Backup & Restore**: AES-256-GCM encrypted `.qdbackup` file export/import with PBKDF2 (100,000 iterations) passphrase key derivation, Room database notes serialization, and DataStore preferences restore.
- **🛡️ 37 CodeQL Static Analysis Security Hardening**: Addressed all CodeQL static analysis alerts including restricted WebView file/content access, explicit PendingIntents (`setPackage`), log injection sanitization, strong biometrics (`BIOMETRIC_STRONG`), absolute binary paths, and cloud backup protection.
- **🤖 GitHub Repository & CI Automation**: Added CodeRabbit configuration (`.coderabbit.yaml`), automated PR welcome contributor bot (`pr-welcome.yml`), Dependabot version grouping, and upgraded Gradle wrapper to 9.5 and `setup-java@v5`.

---

## [5.2.0] - 2026-08-14 (Unified DataStore & Architecture Release) 🚀

### 🚀 ADDED & IMPROVED
- **Unified DataStore Architecture**: Migrated all 9 ViewModels, UI Composables, background Services (`QuickTileService`), and Workers (`TelegramPollerWorker`) to `@Singleton UserStore`.
- **Target SDK 36**: Upgraded target SDK to 36 for full Android 15/16 device compatibility.
- **Enhanced Security & Incognito**: Implemented `applyWindowSecurity(window)` for system-wide `FLAG_SECURE` toggles and added `DEVICE_CREDENTIAL` (PIN/Pattern/Password) fallback unlock support.
- **Unit Test Suite Refactoring**: Updated unit test suite (`ClipboardViewModelTest`, `MainViewModelTest`, `QrViewModelTest`, `QuickTimerViewModelTest`, `SettingsViewModelTest`) for 100% passing tests.
- **Build System Cleanup**: Optimized AGP 9.2 configuration properties and removed hardcoded NDK version.

---

## [5.1.3] - 2026-07-31 (Stability & Service Robustness Release) ⚡

### 🐛 FIXED (What We Fixed)
- **Quick Settings Tile Crash**: Resolved `ForegroundServiceStartNotAllowedException` on Android 12+ / 14+ / 16 when toggling QuickDash Hub floating bubble from notification shade.
- **Service Foreground Promotion**: Wrapped `FloatingBubbleService` `startForeground()` in try-catch to prevent process crashes on restricted background starts.
- **OWASP Dependency Check Action**: Resolved Docker container `JAVA_HOME` override error in `.github/workflows/dependency-scan.yml`.
- **Nightly CI Build Pipeline**: Added automatic `google-services.json` decoding and `local.properties` generation for GitHub Actions Nightly builds.

---

## [5.1.2] - 2026-07-31 (Code Quality & Security Release) 🔒

### 🐛 FIXED (What We Fixed)
- **27 Crash-Prone Catch Blocks**: Replaced all empty `catch (_: Exception) {}` and `e.printStackTrace()` blocks with proper `Log.e()` error logging across 10 files.
- **Telegram First-Install Notification**: Fixed missing `incrementAppOpens()` call that prevented install notifications. Added device info (manufacturer, model, Android version, API level, app version) to notification message.
- **Website Stats Bar Regression**: Fixed field names in `stats.json` to match Worker API response (`downloads`, `tools`, `active_users`).
- **Website Announcement Download URL**: Fixed pointing to wrong APK filename (`QuickDash-v5.1.1` → `app-universal-release`).
- **Website Blog/Docs/Changelog Double Fetch**: Eliminated 404 round-trip by fetching `.json` directly.
- **CSS Slide-Up Animation**: Aligned `slideUp` animation with actual dialog height to prevent visual clipping.

### 🔒 SECURITY (What We Hardened)
- **Keystore Passwords Removed from Disk**: Moved signing credentials from `keystore.properties` (plaintext) to `local.properties` + CI environment variables.
- **Worker CORS Restricted**: API responses now set `access-control-allow-origin` to `quickdash.balajitechlab.com` only.

### ✅ ADDED (What We Added)
- **19 Automated Unit Tests**: ViewModel, Repository, and Room DAO tests covering MainViewModel, SettingsViewModel, ClipboardViewModel, QrViewModel, QuickTimerViewModel, HistoryRepository, SecurityRepository, and Room database migrations.
- **7 Worker API Tests**: Rate limiter (3 req/window + 429), CORS headers, OPTIONS preflight, 404 handling, stats endpoint, feedback submission.
- **Type-Safe Navigation Routes**: Sealed `QuickDashRoute` interface with 12 `@Serializable` routes and `QuickDashNavHost` composable scaffold.
- **Shared `useApi` React Hook**: Extracted and refactored Blog, Docs, and Changelog pages for consistent data fetching with loading/error states.
- **Website Accessibility**: Added `SkipLink`, focus management on route change, `aria-live="polite"` regions, and `role="status"` on dynamic content.
- **Community Files**: Created `ISSUE_TEMPLATE` (bug + feature), `PULL_REQUEST_TEMPLATE`, `CODEOWNERS`, and `FUNDING.yml`.

### 🔄 UPDATED (What We Updated)
- **Version Bump**: `5.1.1` → **`5.1.2`** (513 → **514**).
- **ProGuard Rules**: Fixed `UpdateManager**` → `UpdateManager*`, removed stale `-optimizationpasses 5`, consolidated `-dontwarn` rules.
- **Service Worker**: Updated precache to include SPA routes, removed `announcement.json`.
- **Worker `compatibility_date`**: Updated to `2026-07-30`.
- **Website `CONTRIBUTING.md`**: Fixed stale template links.

---

## [5.1.1] - 2026-07-26 (Android 16 & Google Play Release) 🛡️

### 🐛 FIXED (What We Fixed)
- **16 KB Page Alignment Crash Warning**: Fixed Google Play Console 16 KB memory page size alignment warning by adding `packaging { jniLibs { useLegacyPackaging = false } }` for native JNI `.so` libraries.
- **Glance & Protobuf SDK Warnings**: Fixed Play Console critical notes on `glance-appwidget-proto` and `glance-appwidget-external-protobuf` by upgrading `androidx.glance:glance-appwidget` from `1.1.0` to `1.1.1`.
- **AAPT Manifest Attribute Spelling**: Fixed AAPT resource compilation failure by updating XML attribute spelling to `android:resizeableActivity="true"`.
- **Mobile Website Horizontal Overflow**: Fixed right-side empty space on mobile screens by adding `max-width: 100vw`, `overflow-x: hidden !important` on `html`/`body`, and constraining `.device-glow` and `.mesh-orb-1` bounds.
- **Mobile Countdown Timer Visibility**: Fixed hidden deadline timer on mobile devices by removing `display: none` on `.s-timer` and applying a compact responsive unit layout.

### 🔄 UPDATED (What We Updated)
- **Target SDK 36 (Android 16)**: Updated target SDK from `35` to **`36`** (Android 16 API Level 36) and compile SDK to **`37`**.
- **Version Number Bump**: Updated version name to **`5.1.1`** and version code to **`511`** across `build.gradle.kts`, `README.md`, `index.html`, and `privacy.html`.
- **Technical Architecture Documentation**: Updated prerequisites and architecture specifications in `README.md` to reflect Android 16 target, 16 KB page size alignment, and Play Core APIs.

### ⚡ ADDED (What We Added)
- **Google Play In-App Updates API**: Added `com.google.android.play:app-update-ktx:2.1.0` and integrated `checkForPlayAppUpdate()` in `MainActivity.kt` for 1-tap in-app update prompts.
- **Google Play In-App Reviews API**: Added `com.google.android.play:review-ktx:2.0.2` and integrated `requestPlayInAppReview()` in `MainActivity.kt` for native 5-star rating sheets.
- **Android 15/16 Predictive Back Animations**: Added `android:enableOnBackInvokedCallback="true"` in `AndroidManifest.xml` for back-swipe gesture navigation.
- **Large Screen & Foldable Support**: Added `android:resizeableActivity="true"` for ChromeOS, tablets, and foldables.
- **Automated Firebase Crashlytics Logging**: Integrated automatic non-fatal exception recording in `AppLogger.kt` (`FirebaseCrashlytics.getInstance().recordException()`).
- **Official F-Droid Build Recipe**: Created `metadata/com.balajitechlabs.quickdash.yml` for automated F-Droid store catalog inclusion.
- **Multilingual Store Listings**: Generated translated Google Play Store listings in 5 languages (Hindi, Spanish, Portuguese, French, German) saved to `~/Desktop/PlayStore_Translations.md`.
- **Website Mobile Navigation Drawer**: Added glassmorphic slide-out drawer menu and hamburger toggle button to `website/index.html`.
- **Website Desktop QR Scan Widget**: Added a QR code scanner widget to `website/index.html` allowing desktop visitors to open the site directly on mobile.
- **Website Play Store Beta Integration**: Added a "Join Google Play Beta" hero button and dedicated card to `website/index.html`.

---

## [5.0.0] - 2026-07-24 (Major Version Release) 🚀

### 🚀 Major Highlights
- **Inbuilt QR Scanner Quick Settings Tile**: Added `QrScannerTileService` allowing users to scan payment QR codes directly from the Android Quick Settings notification shade.
- **Scan QR Code Tile Setup Action**: Added a direct setup button under Settings → Launch & Display for requesting tile addition on Android 13+.
- **Pure Icon Launcher Cards**: Replaced text and emoji labels in Quick Social Access and Quick Chat with minimalist native vector icons (`ic_instagram`, `ic_facebook`, `ic_twitter`, `ic_linkedin`, `ic_github`, `ic_whatsapp`, `ic_telegram`, `ic_signal`, `ic_sms`).
- **Stylized Custom QR Code Generation**: Defaulted circular dots and gradient color palettes for all generated payment QR codes in Quick Collect.
- **Floating Window Screenshot Action**: Added a dedicated camera button (`📷`) in the floating window top header to dynamically capture high-res screenshots of ONLY the floating container card.
- **Modal Calculator History Window**: Replaced inline calculation dropdown with a clean `AlertDialog` pop-up window featuring single-tap expression reuse.
- **Annotator Canvas Upgrades**: White canvas background by default, background color picker (White, Light Grey, Soft Yellow, Black, Dark Slate), and direct **Save as PDF** export.
- **Simplified Quick Translator**: Removed AI Assistant sections for a clean, instant, dedicated translation tool with manual paste control and official Google Translate app icon.
- **Advanced v5.0.0 Setup Screen**: Redesigned onboarding flow with step-by-step guidance for notifications, floating window overlays, and Quick Settings tile configuration.

### 🎨 UI & Layout Enhancements
- Reordered Dashboard tool grid hierarchy (Quick Collect, Quick Chat, Quick Social, Quick Translator, Smart Clipboard, Quick Notes).
- Defaulted currency converter amount to `0` and currency pair from `INR (₹)` to `USD ($)`.
- Restrained payment description note input to a compact single-line field.
- Expanded package visibility queries in `AndroidManifest.xml` for UPI payment apps (GPay, PhonePe, Paytm, BHIM).

---

## [4.4.1] - 2026-07-12
- Updated launch styles, floating window dialog animations, and biometric lock security.
- Enhanced clipboard manager with sensitive data guard and auto-clear timer options.
- Added live currency converter and scientific expression calculator.
- Expanded language support for system-wide translation.

---

## [4.4.0] - 2026-06-20
- Introduced glassmorphic Material 3 background blurs for floating dialog cards.
- Added custom shape and corner radius selectors in onboarding and settings.
- Added Room Database persistent storage for Quick Notes with Markdown rendering.
- Implemented real-time device traffic monitor.

---

## [4.1.0] - 2026-05-15 (Previous Stable Release)
- Stable release with floating bubble overlay service (`FloatingBubbleService`).
- Added Quick Collect payment QR generator for UPI and PayPal handles.
- Added direct messaging launcher for WhatsApp and Telegram.
- Integrated Glance AppWidget for home screen quick launcher.

---

## [3.0.0] - 2026-04-01
- Major architecture migration to Jetpack Compose and DataStore preferences.
- Added system-wide biometric authentication prompt.
- Introduced offline Data Backup & Restore via JSON export.

---

## [2.0.0] - 2026-02-10
- Added multi-engine web search (Google, DuckDuckGo, YouTube, GitHub, Wikipedia).
- Introduced stopwatch with lap records and countdown timer.
- Added Wi-Fi SSID QR code generator.

---

## [1.0.0] - 2026-01-01
- Initial public release of QuickDash Floating Dashboard for Android.
