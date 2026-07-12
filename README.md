<h1 align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" width="120" alt="QuickDash Logo"/><br>
  QuickDash
</h1>

<p align="center">
  <a href="https://github.com/Balajitechlabs/quickdash-app/releases/download/v4.2.1-8/quickdash_v4.2.1.apk
">
    <img src="https://img.shields.io/badge/Download%20APK-2ea44f?style=for-the-badge&logo=android&logoColor=white" alt="Download APK">
  </a>
</p>

<p align="center">
  <strong>The Ultimate Floating Utility Hub for Android</strong><br>
  <em>Never switch apps again. All your essential tools, floating on your screen.</em>
</p>
<p align="center">
  <img src="https://img.shields.io/badge/Version-4.2.1-blue" alt="Version">
  <img src="https://img.shields.io/badge/Kotlin-100%25-blue.svg?logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Android-13.0%2B-green.svg?logo=android" alt="Android">
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-purple.svg" alt="Compose">
  <img src="https://img.shields.io/badge/Architecture-Room_DB_%7C_DataStore-orange.svg" alt="Architecture">
  <img src="https://img.shields.io/badge/License-MIT-brightgreen.svg" alt="License">
</p>
---

## 📸 Screenshots
<p align="center">
  <img src="screenshots/1.jpg" width="18%" alt="Screenshot 1">
  <img src="screenshots/2.jpg" width="18%" alt="Screenshot 2">
  <img src="screenshots/3.jpg" width="18%" alt="Screenshot 3">
  <img src="screenshots/4.jpg" width="18%" alt="Screenshot 4">
  <img src="screenshots/5.jpg" width="18%" alt="Screenshot 5">
</p>

## 🚀 What is QuickDash?
QuickDash is a hyper-productive Android utility application designed to save you time. Instead of minimizing your current app to open your notes, calculator, or QR generator, QuickDash lives as a **Floating Bubble** on your screen. Tap the bubble, and a gorgeous, blurred dashboard appears instantly.

## ✨ Core Features

### 🟢 The Floating Dashboard & Compliance
- **System-Wide Overlay:** Accessible over any app, game, or video.
- **Glassmorphic UI:** Stunning Material 3 design with a beautiful blur effect.
- **App Lock:** Secure the dashboard using device Biometrics (Fingerprint/Face).
- **100% Privacy Enforced:** Google Sign-In and OneSignal tracking SDKs have been completely purged to ensure absolute anonymity. Enforces strict HTTPS traffic.

### 🛠 Quick Tools & Customizations
- **📝 Quick Notes:** Jot down thoughts instantly. Backed by **Room Database** for crash-safe, persistent storage. Pin your most important notes to the top.
- **🔍 Quick Web Search:** Type a query and instantly launch Google or custom engines.
- **📋 Clipboard Manager:** Automatically track and retrieve your recently copied items. Features a pinned section with quick actions (Pin, Share, Copy, Delete) and crash-free layouts.
- **⏱️ Quick Timer & History:** Features lap records and timer presets stored in a clean history log.
- **📊 Live Traffic Monitor:** View real-time device traffic alongside editable server credentials in a swipeable pager.

### 💳 Fast Payments & QR Generation
- **UPI QR & Connected Divisions:** Set up Division Slots 1, 2, and 3 for quick UPI target generation. Generates codes with category labels (groceries, dining) and filters logs by date ranges. Wiped all hardcoded fallback IDs for a clean start.
- **Wi-Fi Sharer & QR:** Generates a Wi-Fi share QR with a direct scan-to-connect option in the Wi-Fi history logs dialog.

### 💬 Quick Chat Target Expansion
- Start conversations without saving contacts. Fully supports **WhatsApp, Telegram, Signal, and SMS** with tab-specific prefilled message templates. Supports direct Telegram username searches or wizard launches. Direct flag click pulls up Dial Code search list.

### 🎨 Personalization
- **Custom Font Families:** Choose between Poppins, Space Grotesk, and Nunito in both Onboarding and Settings.
- **11 Built-in Themes:** Handcrafted Material 3 color palettes.
- **Dynamic Color (Monet):** Adapts the app UI to match your Android 12+ system wallpaper.
- **Haptic Feedback Engine:** Premium vibration responses across the entire UI.

### 📣 Zero-Cost Global Push Announcements
- Intercepts broadcasts using a custom **Telegram Bot Poller** that polls a private Telegram Bot without using expensive Firebase servers.

---

## 💻 Developer Setup & Building

To clone and build the project locally, you must provide your own Telegram Bot keys.

1. Clone the repository:
   ```bash
   git clone https://github.com/balajitechlabs/quickdash.git
   cd quickdash
   ```
2. Create a file named `local.properties` in the root directory.
3. Add your Telegram Bot credentials (this file is gitignored):
   ```properties
   TG_BOT_TOKEN=your_telegram_bot_token_here
   TG_CHAT_ID=your_telegram_chat_id_here
   ```
4. Open the project in **Android Studio (Giraffe or newer)** and hit Build.

---

## 📝 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
<p align="center">Made with ❤️ by <strong>BalajiTechLabs</strong></p>
