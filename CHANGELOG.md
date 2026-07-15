# Changelog

All notable changes to **QuickDash** will be documented in this file.

## [4.4.1] - 2026-07-15
### Fixed
- **removeLast() NoSuchMethodError Crash:** Replaced Java 21 `removeLast()` calls in navigation stack and logger with standard `removeAt(list.lastIndex)` to support older Android versions.
- **Timer Exact-Alarm Security Exception:** Integrated runtime checks for `canScheduleExactAlarms()` and structured exception catching. Timer alarms now fall back gracefully to inexact alarms instead of throwing a `SecurityException` on Android 13/14+.
- **Quick Web Layout & Cookies:** Resolved WebView loading issues by eliminating manual layout param constraints, and enabled full cookie, mixed-content, and URL handling support.
- **In-App Auto-Updates:** Integrated prompt dialogs, download progress indicators, and installer handoffs directly within the main App scaffold.

## [4.4.0] - 2026-07-13
### Added
- **Refactored Clipboard Layout:** Upgraded ClipboardItemCard to a vertical column layout. Long clipboard items now occupy full width, resolving overlapping and text-squishing issues.
- **Native Vector Logo in Control Center:** Overwrote the Settings Tile icon with the official double-layered vector brand icon.
- **Package Visibility Queries:** Fused package visibility rules in AndroidManifest.xml to allow seamless JSON backup transfers to WhatsApp, Telegram, and other messaging channels.
- **Backup & Restore Toast Feedback:** Integrated visual Toast notifications confirming success or failure for local data backups and restores.
- **Play Protect R8 WebView Fix:** Redesigned WebView keep rules in R8 to prevent anonymous subclasses from getting stripped in release builds.

## [4.3.0] - 2026-07-12
### Added
- **Dynamic Centered Window Animations:** Overhauled the main floating window transition animations using `SizeTransform` and spring animations for a sleek, minimizing center-reveal effect when switching tools.
- **Data Backup & Restore 💾:** Added a robust Data Management system in Settings. Users can now export all their preferences, notes, and configurations into a portable JSON file, and restore them anytime.
- **Diagnostic Crash Logger 🐛:** Integrated a comprehensive in-app crash reporter. It intercepts uncaught exceptions and saves them to local disk for easy viewing and sharing in the System Logs screen.
- **WhatsApp Live QR Scanner 📷:** Integrated Google ML Kit Barcode Scanning directly into the Quick Chat screen. Scan a friend's WhatsApp QR code to instantly launch their chat without saving their contact.
- **PayPal Switcher 💳:** Upgraded the Quick Collect screen with a beautiful animated top-bar toggle. Instantly switch between generating UPI QR codes and PayPal.me payment links.
- **Quick Web (Floating Browser) 🌐:** Added a new floating web browser tool. Open and browse any URL in a lightweight, resizable floating window without ever leaving your current app.

---

## [4.2.1] - 2026-07-12
### Added
- **Premium Onboarding Redesign (0-8 Flow):** Overhauled step-by-step onboarding flow featuring premium Welcome intro, Location/Notification permission checks, Quick Collect UPI setup, Theme Mode, Corners & Shapes, Font selections, and complete confetti splash.
- **Custom Fonts Choice (Space Grotesk, Poppins, Nunito):** Integrated dynamic typography system into Onboarding and Settings, letting users personalize their text previewed in real-time.
- **Glance App Widget Integration:** Built a beautiful circular 1x1 Glance App Shortcut home-screen widget to launch QuickDash overlay with a single click. Added app shortcuts for Scan QR, Smart Clipboard, and Full-screen.
- **Quick Collect & Divisions Customizations:** Renamed connected accounts to Division Slots 1, 2, and 3, defining configure/delete/default actions. Starts empty on new installations. Wiped all hardcoded fallback IDs. Added custom category selection (groceries, dining, business) and filterable history periods (Today, This Week, This Month).
- **Clipboard Layout Crash Prevention:** Fixed `IllegalStateException` asking for intrinsic measurements of SubcomposeLayout (LazyRow inside ListItem) by building a fully customized and highly responsive Compose Row layout. Added a dedicated Pinned clipboard history section with Pin, Share, Copy, and Delete actions.
- **Quick Chat & Predefined Templates:** Added a premium tabbed selector for WhatsApp, Telegram, Signal, and SMS with tab-specific custom pre-filled message templates. Supports direct Telegram Wizard username or phone number launches. Direct flag click triggers country search dialog.
- **Stopwatch History & Saved Wi-Fi QR:** Added a dedicated history screen for stopwatch lap lists and countdown alarms. Wi-Fi history logs now show a scan-to-connect QR popup when clicked.
- **Support & Direct Pay Upgrades:** Renamed donation support options to Razorpay and Direct Pay. Configured Direct Pay UPI to use `241120067@ybl` across all direct intents.
- **Compliances & Privacy Enhancements:** Completely removed Google Sign-in and Google Drive cloud storage backups. Disabled OneSignal SDK initialization to preserve absolute device anonymity and block external notification popups. Enforced strict cleartext traffic enforcements.

---

## [4.0.1] - 2026-07-06
### Added
- **Support & Donate (Razorpay):** Seamlessly integrated a new Donation option at the bottom of the Settings screen, linking directly to the developer's Razorpay for quick and secure support.
- **Dedicated System Logs UI:** A powerful new developer tool added in Settings. Now includes a 1-click "Copy to Clipboard" feature for instant and flawless troubleshooting.
- **Dynamic Floating UI:** The core Floating UI Window (QuickDash overlay) has been redesigned to dynamically wrap and adjust its height based entirely on its internal content, eliminating dead space on your screen.
- **In-App App Rating System:** Added a clean 5-Star rating dialog integrated directly into the Settings screen, which instantly relays your feedback and score straight to the developer's Telegram Bot.

### Improved
- **Instantly Clickable Links:** Any URLs or links saved to your Clipboard History or Blog Posts section are now immediately styled and clickable inside the app! No more manual copying and pasting into a browser.
- **Crashlytics Pipeline:** Solidified the Gradle build structure to properly enforce the Crashlytics mapping file generation specifically during final Release builds, ensuring 100% crash report legibility in Firebase.

---

## [3.2.0] - 2026-07-05
### Added
- **Shake-to-Trigger:** Physically shake your device to instantly launch the QuickDash overlay. Configurable in settings.
- **Custom Search Engines:** Define your own custom search providers (like Wikipedia or custom wikis) in the Quick Search screen.
- **System Logs Viewer:** Real-time terminal-like debugging and troubleshooting viewer accessible from Developer Settings.
- **Biometric App Lock:** Secure your app behind fingerprint or face unlock.
- **GitHub Action CI/CD:** Complete automated release pipeline support.

### Fixed
- Fixed unresponsiveness issues during long-running background polling.

---

## [2.2.6] - 2026-07-01
### Added
- **Room Database Migration:** Overhauled the Notes architecture. Migrated notes from DataStore JSON string to a fully persistent Room Database, making data crash-safe and improving read/write speeds.
- **Giant Confetti:** Overhauled onboarding confetti to cover the entire screen for a better UX.
- **Share QuickDash:** Added a share button in the Settings screen to let users share the app link easily.
- **Privacy Policy Link:** Added to Settings for compliance with data collection guidelines.

### Fixed
- **Room Compilation:** Fixed Room KSP compilation errors ("unexpected jvm signature V") on newer Kotlin versions by enabling Room Kotlin code generation and using explicit return types.
- **Biometric Fix:** Handling all biometric lockout and hardware error codes natively to prevent blank screen hangs.
- **Bubble Navigation Issue:** Switched Floating Bubble Intent from `FLAG_ACTIVITY_CLEAR_TASK` to `FLAG_ACTIVITY_REORDER_TO_FRONT` to prevent wiping the back stack.
- **Web Search Crash:** Added `FLAG_ACTIVITY_NEW_TASK` to the Web Search Intent to allow searches to launch properly from the floating bubble and quick tile contexts.
- **Update Check Loop:** Cleaned up duplicate `checkForUpdates` calls and correctly updated the `versionCode` in `update.json` to 18 to fix the update trigger loop.
- **Wi-Fi Password Security:** Encrypted the stored Wi-Fi password using AndroidX `security-crypto` (EncryptedSharedPreferences) instead of plain-text DataStore.
- **Telegram Hardcoding:** Removed hardcoded Telegram Bot token; injected via `BuildConfig` using `local.properties`.

### Changed
- Added missing `POST_NOTIFICATIONS` and `FOREGROUND_SERVICE` permissions in AndroidManifest.xml.

---

## [2.1.0] - 2026-05-15
### Added
- **11 Beautiful Themes:** Introduced a comprehensive theming engine supporting 11 distinct color palettes.
- **Dynamic Color (Material 3):** Added support for Android 12+ wallpaper-based dynamic coloring.
- **Telegram Broadcast Receiver:** Added background worker to fetch global announcements from the developer's Telegram Bot without relying on Firebase.

### Fixed
- Stabilized the Floating Bubble to prevent it from disappearing during intensive memory operations.
- Fixed an issue where the clipboard history would fail to record long text entries.

---

## [2.0.0] - 2026-04-10
### Added
- **Complete UI Overhaul:** Switched entirely to Jetpack Compose with Material 3 design language.
- **App Statistics:** Added a dashboard in settings to track Total App Opens, QR Codes Generated, and Notes Saved.
- **Haptic Feedback:** Implemented system-wide haptics for a premium feel.
- **Biometric App Lock:** Secure your app behind fingerprint/face unlock.

### Changed
- Migrated legacy SharedPreferences to DataStore Preferences for better asynchronous data handling.

---

## [1.5.0] - 2026-02-28
### Added
- **Quick Instagram Search:** Instantly look up Instagram profiles right from the floating bubble.
- **Quick Web Search:** Search Google instantly without opening a heavy browser first.
- **Clipboard Manager:** Automatically track copied items for quick retrieval.

---

## [1.0.0] - Initial Release
### Added
- **Floating Bubble Service:** The core of QuickDash. A system-wide floating head to access tools.
- **QR Code Generator:** Convert Wi-Fi credentials, UPI IDs, and custom text into scannable QR codes instantly.
- **Quick Notes:** Jot down thoughts quickly.
- **UPI Quick Pay:** Store and display your UPI QR code for fast payments.
