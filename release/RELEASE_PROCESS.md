# QuickDash Release Process & Quality Gate Rules

## Prerequisites
- All P0 and P1 items from current milestone completed.
- `main` branch is clean (no WIP commits).
- All CI checks pass on latest commit.
- Release notes written in `release/release_notes.md`.

## Step 1: Version Bump
- Run `./scripts/bump-version.sh patch|minor|major`
- Verify `versionCode` and `versionName` in `app/build.gradle.kts`.
- Verify `update.json` and F-Droid metadata updated.

## Step 2: Validation
- `./gradlew lint` — zero errors
- `./gradlew testDebugUnitTest` — 100% passing
- `./gradlew assembleDebug` — builds clean
- Manual smoke test on test device.

## Step 3: Build Release
- `./gradlew clean assembleRelease bundleRelease`
- Verify signatures: `apksigner verify --print-certs *.apk`
- Compute SHA-256 checksums.

## Step 4: GitHub Release
- Push to `main` to trigger `.github/workflows/release.yml`
- Or manually run `gh release create v5.1.1 *.apk *.aab`
