# QuickDash Developer Setup

Welcome to the Developer Setup guide for QuickDash. This project is built using 100% Kotlin and Jetpack Compose (Material 3).

## Prerequisites
- **Android Studio:** Giraffe or newer (recommended).
- **JDK:** Java 17 or higher.
- **Android SDK:** API Level 34 (Android 14) or higher.

## Building the Project

1. **Clone the Repository**
   ```bash
   git clone https://github.com/balajitechlabs/quickdash.git
   cd quickdash
   ```

2. **Configure Local Properties**
   QuickDash relies on a private Telegram Bot Poller for its notification and crash logging systems. You must provide your own Telegram Bot keys to build the project locally, as these are injected via `BuildConfig`.
   
   Create a file named `local.properties` in the root directory (this file is gitignored):
   ```properties
   TG_BOT_TOKEN=your_telegram_bot_token_here
   TG_CHAT_ID=your_telegram_chat_id_here
   ```

3. **Sync and Build**
   Open the project in Android Studio. Wait for the Gradle sync to complete, then hit **Run** or use the terminal:
   ```bash
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
