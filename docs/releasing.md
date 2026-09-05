# Release Engineering

## Version Management

Version configuration is managed as a single source of truth in `gradle.properties`:
```properties
VERSION_CODE=524
VERSION_NAME=5.2.3
```

## Release Checklist

1. Verify `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:detekt` pass with zero warnings.
2. Update `CHANGELOG.md` following Keep a Changelog standards.
3. Tag the release commit using Semantic Versioning (`v5.2.3`).
4. Generate signed release APK and upload to GitHub Releases.

---
Copyright (c) 2026 ||BTL||™ (balajitechlabs)
