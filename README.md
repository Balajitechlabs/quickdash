<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" width="140" alt="QuickDash Logo" style="border-radius: 20%; box-shadow: 0 4px 8px rgba(0,0,0,0.2); margin-bottom: 20px;"/>

# QuickDash ⚡

**The Ultimate Floating Utility Hub & Quick Settings Productivity Suite for Android**
<br>
*Never switch apps again. All your essential tools floating beautifully on your screen.*

<br>

[![Download APK](https://img.shields.io/badge/Download%20APK-2ea44f?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Balajitechlabs/quickdash/releases/latest)
[![Obtainium](https://img.shields.io/badge/Add%20to-Obtainium-000000?style=for-the-badge&logo=github&logoColor=white)](obtainium://app/https://github.com/Balajitechlabs/quickdash)

**Latest Tag: `v5.0.1-17`**

<br>

![Version](https://img.shields.io/badge/Version-5.0.1-blue?style=for-the-badge)
![Stars](https://img.shields.io/github/stars/Balajitechlabs/quickdash?style=for-the-badge&logo=github&color=gold)
![F-Droid](https://img.shields.io/badge/F--Droid-Ready-3DDC84?style=for-the-badge&logo=f-droid&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue.svg?logo=kotlin&style=for-the-badge)
![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg?logo=android&style=for-the-badge)
![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-purple.svg?style=for-the-badge)
![License](https://img.shields.io/badge/License-Open_Source-red.svg?style=for-the-badge)

</div>

---

## 🚀 What's New in v5.0.1

QuickDash **v5.0.1** is an official maintenance release focusing on **Android 16 readiness**, **16 KB memory page size alignment**, **Google Play Core API integrations**, and **F-Droid distribution support**!

- 🎯 **Android 16 Readiness (Target SDK 36)**: Compiled against `targetSdk = 36` (Android 16 API Level 36) with full 64-bit/32-bit ABI compatibility.
- ⚡ **16 KB Memory Page Size Alignment**: Enabled uncompressed 16 KB page alignment for all native C/C++ libraries (`useLegacyPackaging = false`), eliminating crash warnings on modern Android 16 devices.
- 🔄 **Google Play In-App Updates API**: Integrated `com.google.android.play:app-update-ktx:2.1.0`. Users get 1-tap in-app prompts when a new release is available on Play Store.
- ⭐ **Google Play In-App Reviews API**: Integrated `com.google.android.play:review-ktx:2.0.2`. Requests a native 5-star rating sheet directly inside the app.
- 🧩 **Upgraded Jetpack Glance Engine**: Upgraded `androidx.glance:glance-appwidget` to `1.1.1` to resolve Play Console dependency warnings.
- 📱 **Android 15/16 Predictive Back Animations**: Enabled `android:enableOnBackInvokedCallback="true"` for smooth back-swipe gesture animations.
- 📱 **Large Screen & Foldable Support**: Enabled `android:resizeableActivity="true"` in `AndroidManifest.xml` for ChromeOS, tablets, and foldables.
- 🤖 **F-Droid Distribution Recipe**: Added official F-Droid build recipe metadata at `metadata/com.balajitechlabs.quickdash.yml`.

---

## 🚀 What's New in v5.0.0

QuickDash **v5.0.0** is a major release introducing powerful system-wide Quick Settings Tiles, minimalist vector icon launchers, custom QR styling, and floating window screenshot tools!

- 💳 **Minimalist Quick Collect**: Universal UPI & PayPal QR generators with stylized circular dots and custom gradient styling.
- 📷 **Inbuilt QR Scanner Quick Tile**: Add a dedicated **Scan QR Code** tile to your Android notification shade. Tapping it instantly opens Google ML Kit scanner on behalf of QuickDash.
- 💬 **Pure Icon Social & Chat Hub**: Minimalist app icon launchers for WhatsApp, Telegram, Signal, Instagram, Facebook, X, LinkedIn, and GitHub.
- 📸 **Floating Window Screenshot Button**: Dedicated camera action button (`📷`) in the floating window top bar to capture high-res screenshots of ONLY the floating widget container.
- 🧮 **Modal Calculator History**: Calculation history presented in a clean modal pop-up window with single-tap expression reuse.
- 🎨 **White Canvas & PDF Export Annotator**: Draw annotations on a clean white canvas, choose background colors, and export directly as PDF.

---

## ✨ Core Features

### 🟢 Floating Dashboard & System Overlays
- **System-Wide Overlay:** Accessible over any app, game, or video.
- **Glassmorphic Material 3 UI:** Snappy Jetpack Compose animations with dark mode, system accent colors, and custom shapes.
- **Biometric Security:** Lock your floating dashboard using native fingerprint or face authentication.
- **100% On-Device Privacy:** No cloud tracking or ad SDKs. Your payment handles, notes, and clipboard history stay strictly on your phone.

### 🛠️ Integrated Utility Suite
1. **💳 Quick Collect:** Instant UPI & PayPal payment QR generator with custom dot patterns, gradient themes, and preset amount chips.
2. **💬 Quick Chat:** Prefilled direct chat launcher for WhatsApp, Telegram, Signal, and SMS.
3. **📸 Quick Social Access:** Minimalist vector icon launcher for Instagram, Facebook, X, LinkedIn, and GitHub profiles.
4. **🌐 Quick Translator:** Instant language translation using Google Translate API with Text-To-Speech.
5. **📋 Smart Clipboard:** Track copied text, pin sensitive passwords, and auto-clean clipboard contents.
6. **📝 Quick Notes:** Crash-safe offline notes powered by Room Database with markdown support.
7. **💱 Quick Converter:** Live currency exchange rate converter (INR ₹ to USD $ default) and unit converter.
8. **🛠️ Quick Capture:** Screen recorder with foreground service notifications and canvas annotator.
9. **🔍 Quick Search:** Multi-engine web search (Google, DuckDuckGo, YouTube, GitHub, Wikipedia).
10. **📶 Quick Wi-Fi:** Encrypted QR generator for sharing Wi-Fi credentials effortlessly.
11. **🧮 Quick Calculator:** Scientific expression calculator with modal history dialog.
12. **⏱️ Quick Timer:** Stopwatch with lap records and countdown timer.

---

## 🏗️ Technical Architecture & Specifications

- **Language & Runtime:** 100% Kotlin 2.2 with Jetpack Compose & Material Design 3 Expressive
- **Target SDK:** API 36 (Android 16) · **Compile SDK:** API 37 · **Min SDK:** API 24 (Android 7.0+)
- **Memory & ABI Compatibility:** 16 KB page-aligned native library packaging (`useLegacyPackaging = false`) for `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`
- **Play Store Core APIs:** Integrated Google Play In-App Updates (`app-update-ktx`) & In-App Reviews (`review-ktx`)
- **System Overlays & Tiles:** Foreground Service with Special Use / Media Projection & Quick Settings `TileService`
- **Local Persistence:** Room Database 2.6, EncryptedSharedPreferences, and Jetpack DataStore
- **Open Source & Privacy:** 100% On-Device, Telemetry-Free, F-Droid metadata ready

---

## 🛠️ Building & Installation

### Prerequisites
- Android Studio Ladybug / Jellyfish or latest Command-Line Tools
- JDK 17 / OpenJDK 21
- Android SDK 36 (Android 16)

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Build Production Release Bundle (AAB)
```bash
./gradlew bundleRelease
```

The output `.aab` will be generated at `app/build/outputs/bundle/release/app-release.aab`.

---

## 📜 Privacy & Security
- **Data Safety**: No personal data sold or transmitted. 100% on-device processing.
- **Privacy Policy**: Read our official privacy policy at [quickdash.balajitechlab.com/privacy.html](https://quickdash.balajitechlab.com/privacy.html).

---

## ⚖️ License & Open Source Attribution

QuickDash is an open-source project published under a custom copyleft license. 

- **Original Base Project:** Fork of [PocketOps](https://github.com/IIXII-L192/PocketOps-app.git) by Aakarsh / IIXII™.
- **Modifications & Enhancements:** Includes >30% major functional additions (Quick Collect payment QR generator, ML Kit QR Quick Settings tile, pure vector icon launchers, container screenshot button, white canvas annotator with PDF export, Android 16 target SDK 36, 16 KB page alignment, Google Play Core APIs, and F-Droid build recipe).
- **Compliance:** All original copyright notices and license terms are preserved in [LICENSE](LICENSE).

---

© 2026 BalajiTechLabs. All Rights Reserved.
