# QuickDash Developer Setup Guide

This guide walks through configuring your local environment to build, run, and test QuickDash.

---

## 1. System Requirements
- **JDK:** Java 17 or higher (Java 21 supported).
- **Android SDK:** API Level 36 (Android 16 preview / Vanilla Ice Cream) with Build-Tools 36.0.0.
- **Gradle:** Managed via project Gradle wrapper (`./gradlew`).
- **Target Architecture:** ARM64-v8a and armeabi-v7a.

---

## 2. Initial Setup

1. **Clone the Repository:**
 ```bash
 git clone https://github.com/balajitechlabs/quickdash.git
 cd quickdash
 ```

2. **Configure `local.properties`:**
 Create a `local.properties` file in the project root:
 ```properties
 sdk.dir=/path/to/your/Android/sdk

 # Optional: Telegram bot configuration for local crash diagnostic delivery
 TG_BOT_TOKEN=your_bot_token_here
 TG_CHAT_ID=your_chat_id_here
 TG_BROADCAST_BOT_TOKEN=your_broadcast_bot_token_here
 ```

3. **Verify Environment:**
 Run the Kotlin compilation task:
 ```bash
 ./gradlew :app:compileDebugKotlin
 ```

---

## 3. Build & Test Commands

```bash
# Compile and run unit tests
./gradlew :app:testDebugUnitTest

# Assemble debug APK
./gradlew :app:assembleDebug

# Install on connected device via adb
adb install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

---

## 4. Key Documentation References
- [ARCHITECTURE.md](ARCHITECTURE.md): System architecture, navigation graph, and data flow.
- [CONTRIBUTING.md](CONTRIBUTING.md): Quality rubric, commit conventions, and pull request checklist.
- [docs/FILE_MAP.md](docs/FILE_MAP.md): Auto-generated file map listing all source files, modules, and purposes.
- [docs/AlgorithmAudit.md](docs/AlgorithmAudit.md): Algorithmic correctness audit for text categorization, QR parsing, and encryption.
