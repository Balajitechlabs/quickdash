# QuickDash Official Release Changelog 📜

All major updates, feature additions, bug fixes, and system improvements for **QuickDash** are documented in this file.

---

## [5.0.1] - 2026-07-26 (Android 16 & Google Play Release) 🛡️

### 🐛 FIXED (What We Fixed)
- **16 KB Page Alignment Crash Warning**: Fixed Google Play Console 16 KB memory page size alignment warning by adding `packaging { jniLibs { useLegacyPackaging = false } }` for native JNI `.so` libraries.
- **Glance & Protobuf SDK Warnings**: Fixed Play Console critical notes on `glance-appwidget-proto` and `glance-appwidget-external-protobuf` by upgrading `androidx.glance:glance-appwidget` from `1.1.0` to `1.1.1`.
- **AAPT Manifest Attribute Spelling**: Fixed AAPT resource compilation failure by updating XML attribute spelling to `android:resizeableActivity="true"`.
- **Mobile Website Horizontal Overflow**: Fixed right-side empty space on mobile screens by adding `max-width: 100vw`, `overflow-x: hidden !important` on `html`/`body`, and constraining `.device-glow` and `.mesh-orb-1` bounds.
- **Mobile Countdown Timer Visibility**: Fixed hidden deadline timer on mobile devices by removing `display: none` on `.s-timer` and applying a compact responsive unit layout.

### 🔄 UPDATED (What We Updated)
- **Target SDK 36 (Android 16)**: Updated target SDK from `35` to **`36`** (Android 16 API Level 36) and compile SDK to **`37`**.
- **Version Number Bump**: Updated version name to **`5.0.1`** and version code to **`501`** across `build.gradle.kts`, `README.md`, `index.html`, and `privacy.html`.
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
