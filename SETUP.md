# QuickDash Developer Setup

Welcome to the Developer Setup guide for QuickDash. This project is built using 100% Kotlin and Jetpack Compose (Material 3).

## Prerequisites
- **Android Studio:** Ladybug / Meerkat or newer (recommended).
- **JDK:** Java 17 or higher.
- **Android SDK:** API Level 36 (Android 16).

## Building & Verifying the Project

1. **Clone the Repository**
   ```bash
   git clone https://github.com/balajitechlabs/quickdash.git
   cd quickdash
   ```

2. **Configure Local Properties**
   QuickDash includes Telegram Bot integration for notification and diagnostics logging. Create a file named `local.properties` in the root directory (gitignored):
   ```properties
   TG_BOT_TOKEN=your_telegram_bot_token_here
   TG_CHAT_ID=your_telegram_chat_id_here
   ```

3. **Build & Test**
   ```bash
   # Run unit test suite
   ./gradlew testDebugUnitTest

   # Run Android Lint
   ./gradlew lintDebug

   # Assemble debug APK
   ./gradlew assembleDebug
   ```

## Architecture Overview
QuickDash is built entirely around a Floating Window Architecture.
- **Service-Based UI:** The app UI is hosted within `QuickDashService`, an Android `Service` using `WindowManager`.
- **State Management:** Uses Jetpack Compose state hoisting with `AnimatedContent` for snappy, beautiful transitions.
- **Data Persistence:** Uses a mix of **Room Database** (for complex relational data like Notes) and **DataStore** (for Preferences and configurations).
- **Security:** Strict HTTPS enforcements, disabled cleartext traffic, and zero external trackers (no Google Sign-In or commercial analytics SDKs). Crashlytics is the only included analytical service (requires valid `google-services.json` if building for release).

## Contributing
Please ensure all commits pass standard Lint checks and do not introduce unhandled intrinsic layout constraints. Test all floating window UI changes against different screen orientations.
