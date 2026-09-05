# ADR 0003: Private Telegram Crash Bot over Firebase Crashlytics

## Status
Accepted

## Context
Google Firebase Crashlytics introduces closed-source Google Play Services dependencies, background tracking, and telemetry frameworks that conflict with our zero-tracker commitment.

## Decision
Route unhandled application crashes directly to a private developer Telegram bot over HTTPS with sanitized stack traces.

## Consequences
- Complete removal of Google Services Gradle plugin and Firebase BOM.
- Full privacy compliance and verified zero-tracker rating on Exodus Privacy.
- Instant crash alerts received in real time on mobile devices.

---
Copyright (c) 2026 ||BTL||™ (balajitechlabs)
