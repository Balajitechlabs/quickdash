<p align="center">
  <a href="https://quickdash.balajitechlab.com" target="_blank">
    <img src="https://i.imghippo.com/files/ehH4662Jo.png" width="75%" alt="QuickDash Banner" />
  </a>
  <br/><br/>
  <a href="https://play.google.com/store/apps/details?id=com.balajitechlabs.quickdash" target="_blank">
    <img src="https://i.imghippo.com/files/HWd3070AGs.png" height="38px" alt="Get it on Google Play" />
  </a>
  &nbsp;&nbsp;
  <a href="https://github.com/balajitechlabs/quickdash" target="_blank">
    <img src="https://i.imghippo.com/files/Hl6852EBQ.png" height="38px" alt="View on GitHub" />
  </a>
</p>

<hr>

<p align="center">
  <b>The Ultimate Floating Productivity Hub for Android</b><br>
  <i>100% On-Device Processing · Zero Trackers · Material Design 3 · Fluid 120Hz Animations</i>
</p>

<hr>

<p align="center">
  <a href="https://github.com/balajitechlabs/quickdash/releases/latest">
    <img src="https://img.shields.io/github/v/release/balajitechlabs/quickdash?style=for-the-badge&logo=github&color=2563eb&label=RELEASE" alt="Latest Release">
  </a>
  &nbsp;&nbsp;
  <a href="https://github.com/balajitechlabs/quickdash/actions/workflows/pr-checks.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/balajitechlabs/quickdash/pr-checks.yml?style=for-the-badge&label=CI%20BUILD" alt="CI Status">
  </a>
  &nbsp;&nbsp;
  <a href="https://github.com/balajitechlabs/quickdash/releases">
    <img src="https://img.shields.io/github/downloads/balajitechlabs/quickdash/total?style=for-the-badge&logo=github&logoColor=white&color=2ea44f&label=DOWNLOADS" alt="Downloads">
  </a>
  &nbsp;&nbsp;
  <a href="https://reports.exodus-privacy.eu.org/">
    <img src="https://img.shields.io/badge/EXODUS-0%20TRACKERS-brightgreen?style=for-the-badge&logo=shield" alt="Exodus Privacy">
  </a>
  &nbsp;&nbsp;
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/LICENSE-BTL%E2%84%A2%20CUSTOM%20OS%20FORK-orange?style=for-the-badge" alt="License">
  </a>
</p>

---

## ⚡ What is QuickDash?

**QuickDash** transforms your Android workflow with a lightweight, customizable floating companion that brings **15+ daily productivity utilities** to your fingertips — accessible anywhere without switching apps.

Whether you need to scan a QR code, generate an instant UPI payment request, translate on-screen text, capture an OTP, jot down a note, or evaluate arithmetic, QuickDash delivers immediate utility with zero ads and zero tracking.

---

## 🌟 Key Highlights

- 🎯 **Gesture Floating Window**: Draggable top capsule with spring-physics swipe-to-dismiss and floating bubble handoff.
- 📱 **Material 3 Expressive & Pitch-Black AMOLED**: Handcrafted dark aesthetic (`#000000` base, `#38393F` pods, `#B0C6FF` accents) with high-contrast typography.
- 📷 **Universal Offline QR & Barcode Scanner**: Built with open-source ZXing Android Embedded for instant 1D/2D scanning with integrated flashlight and offline gallery image decoding.
- ⚡ **120Hz Recomposition Boundaries**: Isolated Compose states and RenderThread spring physics for butter-smooth frame rates.
- 🔐 **Encrypted Local Backups**: Secure AES-256-GCM encrypted `.qdbackup` export and password-protected restore.
- 🛡️ **100% On-Device Privacy**: Pure offline execution. No Firebase, no analytics trackers, no advertising IDs, and no third-party telemetry.

---

## 🧰 Built-in Tool Suite

| Tool | Focus Area | Description |
|---|---|---|
| **QR & Barcode Scanner** | Utilities | Instant camera barcode & QR detection + on-screen screenshot decoding |
| **Quick Collect** | Payments | Instant UPI QR code generator with custom amounts and direct app handoff |
| **Smart Clipboard** | Productivity | Searchable clipboard history with sensitive OTP auto-masking & eye toggle |
| **Quick Chat** | Messaging | Direct WhatsApp & Telegram chat launcher without saving contacts |
| **Quick Notes** | Productivity | Floating scratchpad with Room DB auto-sync and instant markdown copy |
| **Quick Calculator** | Utilities | Floating arithmetic calculator with live expression history and copy actions |
| **Voice Memos** | Media | Background floating audio recorder with high-fidelity local storage |
| **Wi-Fi Hub** | Connectivity | View current network, generate Wi-Fi QR shares, and test transfer speeds |
| **Timer & Stopwatch** | Utilities | Floating countdown timers backed by precise Android `AlarmManager` |
| **Quick Translator** | Productivity | Fast multi-language translation interface running on-device |
| **Battery Diagnostics** | System | Real-time battery health, temperature, voltage, and cycle metrics |
| **In-App Updater** | System | SemVer GitHub release checker with channel toggle (Stable / Beta) |

---

## 📲 Installation Options

### 1. Direct GitHub Releases (Recommended)
Download the latest signed release APK directly from [GitHub Releases](https://github.com/balajitechlabs/quickdash/releases/latest).

### 2. Google Play Store
Official production builds and beta channel updates are available on [Google Play](https://play.google.com/store/apps/details?id=com.balajitechlabs.quickdash).

### 3. Obtainium & F-Droid
- **Obtainium**: Add `https://github.com/balajitechlabs/quickdash` for automated release updates.
- **F-Droid**: Build recipe metadata maintained in `metadata/com.balajitechlabs.quickdash.yml`.

---

## 💻 Build from Source

```bash
# Clone the repository
git clone https://github.com/balajitechlabs/quickdash.git
cd quickdash

# Build Debug APK
./gradlew :app:assembleDebug

# Run Unit Tests
./gradlew :app:testDebugUnitTest

# Install to connected device via ADB
adb install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

**Requirements:** Android Studio Meerkat+, JDK 17+, Android SDK 37 (Android 16).

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Language** | Kotlin 2.4.10 |
| **UI Framework** | Jetpack Compose + Material Design 3 Expressive |
| **Architecture** | MVVM / UDF + Clean Architecture |
| **Dependency Injection** | Dagger Hilt 2.59 (Pure KSP) |
| **Local Storage** | Room Database 2.8.4 + Proto DataStore + Preferences DataStore |
| **Security & Crypto** | AndroidX Security Crypto (EncryptedSharedPreferences, AES-256-GCM) |
| **Barcode / QR** | ZXing Core 3.5.3 + ZXing Android Embedded 4.3.0 |
| **Networking** | OkHttp 5.5.0 + GitHub Releases API |
| **Build System** | Gradle 9.3 + Kotlin DSL + Version Catalog (`libs.versions.toml`) |

---

## 📂 Project Architecture

```
quickdash/
├── app/                    # Main Android application module
│   ├── src/main/java/      # Kotlin source code (core, features, widgets)
│   └── src/main/res/       # Material 3 drawables, layouts, and strings
├── website/                # Showcase landing website (React + Vite)
├── workers/                # Cloudflare Workers API proxy
├── metadata/               # F-Droid store listing metadata
├── ARCHITECTURE.md         # Architecture and data-flow specifications
├── CHANGELOG.md            # Version release history
├── CONTRIBUTING.md         # Contributor guidelines
├── SECURITY.md             # Security policy and vulnerability disclosure
└── LICENSE                 # Open source license
```

---

## 👥 Contributing

Contributions, bug reports, and feature proposals are warmly welcomed! Please read our [Contributing Guide](CONTRIBUTING.md) before submitting pull requests.

---

## 📜 License & Attribution

QuickDash is licensed under the **PocketOps Custom Open Source Fork License**.

- **Original Base:** Forked from [PocketOps](https://github.com/IIXII-L192/PocketOps-app) by Aakarsh (L192) / IIXII™.
- **Modifications:** Over 40% major architectural rebuild, modern Compose M3 Expressive UI, ZXing integration, in-app semantic updater, and security hardening (see [LICENSE](LICENSE)).
- **Compliance:** All original notices, licenses, and attributions are strictly preserved in full.

Copyright © 2026 ||BTL||™ (balajitechlabs).
