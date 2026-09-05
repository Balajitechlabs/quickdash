# QuickDash Algorithm Audit & Correctness Specification

## 1. Overview
This document records the formal algorithmic audit performed on QuickDash core utility engines, parsing logic, cryptographic routines, and classification algorithms. Each section specifies the input domain, current implementation behavior, audited edge cases, bug remediations, and automated test coverage.

---

## 2. Audited Algorithms

### 2.1 TextCategorizer (`core/utils/TextCategorizer.kt`)
- **Purpose**: Deterministically classifies arbitrary clipboard or shared text into actionable domain categories (`URL`, `PHONE_NUMBER`, `EMAIL`, `ADDRESS`, `MATH_EXPRESSION`, `PASSWORD`, `PLAIN_TEXT`) and produces context-aware action intents without third-party AI dependencies or network latency.
- **Input Space**: Arbitrary Unicode text, multiline payloads, mathematical formulas, raw URIs, coordinates, credentials, or unstructured user notes.
- **Findings & Remediations**:
 1. *Classification Misnomer*: Previously named `AiTextCategorizer` despite being purely deterministic regex and syntax parsing. Renamed to `TextCategorizer` to reflect authentic software engineering.
 2. *Unreachable ADDRESS Branch*: The `ADDRESS` enum was declared but had no matching logic. Implemented detection for geographic coordinates (`lat, lng`), `geo:` URIs, and street/postal keyword patterns. Wired to "View on Map" and "Share Location" intents.
 3. *Empty Calculation Action Body*: `MATH_EXPRESSION` "Calculate Result" was an empty stub. Implemented an in-memory recursive-descent mathematical parser (`MathParser`) supporting operator precedence (`+`, `-`, `*`, `/`, `^`, `%`), parentheses, and decimal precision. Calculates results and copies them to the system clipboard with feedback.
 4. *Phone vs Math Ambiguity*: Strings starting with `+` like `+91 9876543210` were falsely matching mathematical expressions due to the leading unary operator. Hardened regex to require binary operators (`MATH_OPERATOR_REGEX`) and prioritized telephone formatting with digit count bounds (7 to 15 digits).
 5. *JVM Test Independence*: Replaced `android.util.Patterns` references with compiled Kotlin regular expressions to ensure reliable, high-speed execution in local JVM unit tests without Android stub null pointer exceptions.
- **Automated Verification**: Covered in `com.balajitechlabs.quickdash.core.utils.TextCategorizerTest`.

---

### 2.2 QrPayloadParser (`features/qr/utils/QrPayloadParser.kt`)
- **Purpose**: Parses scanned 2D QR codes and 1D linear barcodes into structured domain models (`WebUrl`, `UpiPayment`, `WifiNetwork`, `ContactCard`, `PhoneNumber`, `EmailAddress`, `ProductBarcode`, `PlainText`).
- **Input Space**: Raw string payloads from ZXing or camera barcode scanners.
- **Findings & Remediations**:
 1. *UPI Parameter Encoding*: URL query parameters like `pn`, `tn`, and `pa` containing URL-encoded characters (spaces, `#`, `@`) were previously not all decoded or could fail on special characters. Implemented safe parameter parsing with UTF-8 `URLDecoder` across all query parameters.
 2. *Barcode Heuristic Specification*: Documented the numeric barcode heuristic (`^[0-9]{8,14}$`) which maps to standard global trade item numbers (GTIN): EAN-8 (8 digits), UPC-A (12 digits), EAN-13 (13 digits), and ITF-14 (14 digits). Arbitrary numeric strings with length > 14 safely fall back to `PlainText`.
- **Automated Verification**: Covered in `com.balajitechlabs.quickdash.features.qr.utils.QrPayloadParserTest`.

---

### 2.3 CryptoManager (`core/data/CryptoManager.kt`)
- **Purpose**: Encrypts and decrypts sensitive user backups, preferences, and credentials using the Android Keystore system.
- **Cryptographic Standard**: AES-256 in Galois/Counter Mode (`AES/GCM/NoPadding`) with a 128-bit authentication tag and a cryptographically secure 12-byte initialization vector (IV) generated per operation.
- **Findings & Remediations**:
 1. *IV Collision Prevention*: Verified that every encryption call instantiates a fresh random 12-byte IV rather than reusing static vectors.
 2. *Negative Path Tamper Detection*: Added test cases verifying that bit flips or ciphertext tampering throw `javax.crypto.AEADBadTagException`.
 3. *Key Mismatch Rejection*: Added test cases verifying that decrypting with an incorrect AES key fails immediately with authentication tag mismatch.
- **Automated Verification**: Covered in `com.balajitechlabs.quickdash.core.data.CryptoManagerTest`.

---

### 2.4 SemanticVersion (`core/utils/SemanticVersion.kt`)
- **Purpose**: Parses SemVer strings (`major.minor.patch[-suffix]`) and performs ordinal comparisons for in-app update checks against GitHub Releases.
- **Input Space**: GitHub release tag names (e.g., `v5.2.2`, `5.2.3`, `5.2.1-beta1`).
- **Behavior**:
 - Strips leading `v` or `V`.
 - Splits numeric components into integer triples `(major, minor, patch)`.
 - Compares component-by-component in order of precedence: major, minor, then patch.
- **Verification**: Covered in update check test suite and regression tests.

---

### 2.5 ShakeDetector (`core/utils/ShakeDetector.kt`)
- **Purpose**: Detects physical device shake gestures to toggle the floating bubble overlay without keeping high-frequency sensor loops continuously active.
- **Algorithm**:
 - Computes acceleration magnitude \( g = \sqrt{x^2 + y^2 + z^2} / g_0 \).
 - Evaluates threshold exceedance against user-configured sensitivity.
 - Implements a hysteresis window and debounce interval (minimum 500ms between shake events) to eliminate false triggers during casual pocket movement.

---

## 3. Verification Summary

| Component | Target File | Status | Test Suite |
|---|---|---|---|
| Text Categorizer | `core/utils/TextCategorizer.kt` | Pass | `TextCategorizerTest` |
| QR Payload Parser | `features/qr/utils/QrPayloadParser.kt` | Pass | `QrPayloadParserTest` |
| Crypto Manager | `core/data/CryptoManager.kt` | Pass | `CryptoManagerTest` |
| Semantic Version | `core/utils/SemanticVersion.kt` | Pass | `UpdateManagerTest` / `SemanticVersionTest` |
| Shake Detector | `core/utils/ShakeDetector.kt` | Pass | Service Integration |
