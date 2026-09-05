# Security and Privacy Model

## Zero-Tracker Architecture

QuickDash operates under a strict on-device processing guarantee:
- Zero Firebase libraries or telemetry frameworks.
- Zero analytics identifiers or advertising SDKs.
- Zero third-party network pings during normal utility execution.

## Backup Cryptography

Local backups (`.qdbackup`) are encrypted using industry-standard cryptography:
- Cipher: AES-256 in Galois/Counter Mode (GCM).
- Key Derivation: PBKDF2 with HMAC-SHA256 and a random 128-bit cryptographic salt.
- Integrity: GCM authentication tag guarantees tamper detection upon restore attempts.

## Crash Reporting Architecture

In the event of an unhandled exception, crash telemetry is routed directly to a private developer Telegram bot via HTTPS POST, containing only:
- Device model and manufacturer.
- Android OS version.
- Stack trace snippet.
- No personal data, clipboard contents, or storage paths are ever transmitted.

---
Copyright (c) 2026 ||BTL||™ (balajitechlabs)
