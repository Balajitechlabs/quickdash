# Testing and Quality Standards

## Quality Gates

QuickDash enforces multiple static analysis and test validation gates:
1. **Unit Tests**: Executed with JUnit 4 and Google Truth assertions (`./gradlew :app:testDebugUnitTest`).
2. **Static Analysis**: Enforced via Detekt 1.23.8 (`./gradlew :app:detekt`).
3. **Compiler Zero-Warning Rule**: Zero tolerance for compiler warnings (`w:`) or errors (`e:`).

## Core Test Coverage

Key cryptographic and preference management components maintain dedicated unit test suites:
- `CryptoManagerTest.kt`: Tests AES-256-GCM encryption, decryption, IV isolation, and tampering detection.
- `UserStoreTest.kt`: Tests preference keys, serialization integrity, and theme contract compliance.
- `QrPayloadParserTest.kt`: Tests URI and semantic parsing for URLs, UPI payments, Wi-Fi credentials, and vCards.

---
Copyright (c) 2026 ||BTL||™ (balajitechlabs)
