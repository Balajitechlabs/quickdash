# ADR 0004: Offline ZXing Embedded over Google ML Kit

## Status
Accepted

## Context
Google ML Kit Code Scanner requires Google Play Services binaries, which are unavailable on de-Googled devices, GrapheneOS, CalyxOS, or F-Droid distributions.

## Decision
Adopt open-source ZXing Android Embedded (4.3.0). All image analysis, binarization, and barcode decoding execute 100% locally and offline without external binary dependencies.

## Consequences
- 100% universal compatibility across all Android distributions.
- Zero Google Play Services dependency.
- Enhanced with multi-orientation decoding and inverted dark-mode luminance support.

---
Copyright (c) 2026 ||BTL||™ (balajitechlabs)
