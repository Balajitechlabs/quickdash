# QuickDash Official Release Changelog 📜

All major updates, feature additions, bug fixes, and system improvements for **QuickDash** are documented in this file.

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
