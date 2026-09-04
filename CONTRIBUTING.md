# Contributing to QuickDash

Welcome! QuickDash is an open-friendly Android floating utility app built with Jetpack Compose.
This guide helps you get started quickly.

## 🏗️ Project Structure

```
app/src/main/java/com/balajitechlabs/quickdash/
├── core/                   # Shared utilities, UI components, data
│   ├── data/               # DataStore, UserStore, EncryptedPrefs
│   ├── di/                 # Hilt dependency injection modules
│   ├── network/            # OkHttp client, GitHub release API
│   ├── services/           # Foreground services (clipboard, screenshot)
│   ├── shizuku/            # Shizuku privilege bridge
│   ├── ui/                 # QuickDashApp.kt, Composable components, theme
│   └── utils/              # UpdateManager, LogManager, QR helpers
├── features/               # One directory per feature (screens + VMs)
│   ├── clipboard/          # Clipboard history & auto-capture
│   ├── dashboard/          # FloatingDialogActivity — the main floating UI
│   ├── settings/           # All settings screens
│   ├── about/              # About screen with update flow
│   └── [...17 more tools]
├── widget/                 # Glance AppWidget
├── MainActivity.kt
└── QuickDashApplication.kt # App entry point — Hilt, crash reporting, channels
```

## 🔧 Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| State | StateFlow + ViewModel (MVVM/UDF) |
| Storage | Room (DB) + DataStore (prefs) |
| Network | OkHttp (GitHub release API only) |
| Crash Reporting | Telegram Bot (no Firebase) |
| Build | Gradle Version Catalog (`libs.versions.toml`) |

## 🚀 Getting Started

1. **Clone the repo:**
   ```bash
   git clone https://github.com/balajitechlabs/quickdash.git
   cd quickdash
   ```

2. **Create `local.properties`** in the project root:
   ```properties
   sdk.dir=/path/to/your/Android/sdk
   # Optional — only needed for your own Telegram crash bot
   TG_BOT_TOKEN=your_bot_token_here
   TG_CHAT_ID=your_chat_id_here
   TG_BROADCAST_BOT_TOKEN=your_broadcast_bot_token_here
   ```

3. **Build debug APK:**
   ```bash
   ./gradlew :app:assembleDebug
   ```

4. **Install to device:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
   ```

## 📋 Contribution Guidelines

- Follow **Conventional Commits**: `feat:`, `fix:`, `refactor:`, `docs:`, `chore:`
- Use **Kotlin idioms** — no Java-style boilerplate
- Keep composables **small and focused** — one responsibility per function
- Add a `contentDescription` to all interactive icons
- Test on a **real device** (not emulator) before submitting
- No hardcoded secrets — use `BuildConfig` fields from `local.properties`

## 🏷️ License

PocketOps Custom Open Source Fork License — see [LICENSE](LICENSE)
Copyright © 2026 ||BTL||™ (balajitechlabs)
