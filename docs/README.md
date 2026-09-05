# QuickDash Documentation

Welcome to the technical documentation for QuickDash. This directory contains detailed architecture guides, design system specifications, testing standards, and architectural decision records (ADRs).

## Documentation Index

- [Architecture Overview](architecture.md): System hierarchy, Unidirectional Data Flow (UDF), and feature module boundaries.
- [Developer Setup & Build Guide](SETUP.md): Local environment configuration, JDK, Android SDK, and build instructions.
- [UI Guidelines & Design System](ui-guidelines.md): Material 3 design tokens, spacing scales, typography hierarchy, and motion physics.
- [Testing & Quality Gates](testing.md): Unit tests, Detekt static analysis, and CI/CD quality enforcement.
- [Security & Privacy Model](security.md): On-device zero-tracker execution, AES-256-GCM backup cryptography, and permissions analysis.
- [Release Engineering](releasing.md): Single-source versioning conventions and GitHub release pipelines.
- [Data Storage Layer](data-stores.md): Architecture comparison between UserStore (DataStore Preferences), Room DB, and Proto DataStore.

## Architectural Decision Records (ADR)

- [ADR 0001: Jetpack DataStore over SharedPreferences](adr/0001-data-store-over-sp.md)
- [ADR 0002: Lightweight OkHttp over Retrofit](adr/0002-okhttp-over-retrofit.md)
- [ADR 0003: Telegram Crash Bot over Firebase Crashlytics](adr/0003-telegram-crash-bot-over-firebase.md)
- [ADR 0004: Offline ZXing Embedded over Google ML Kit](adr/0004-zxing-embedded-over-mlkit.md)

---
Copyright (c) 2026 ||BTL||™ (balajitechlabs)
