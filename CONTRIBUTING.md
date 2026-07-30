# Contributing to QuickDash

Thank you for your interest in contributing! QuickDash is an open-source Android app, and we welcome contributions of all kinds.

## Code of Conduct

By participating, you agree to uphold our [Code of Conduct](CODE_OF_CONDUCT.md). Be respectful, inclusive, and constructive.

## How to Contribute

### Report Bugs
1. Search [existing issues](https://github.com/Balajitechlabs/quickdash/issues) first.
2. If not found, [open a bug report](https://github.com/Balajitechlabs/quickdash/issues/new/choose).
3. Include: device model, Android version, steps to reproduce, screenshots (if applicable).

### Suggest Features
1. Check [existing feature requests](https://github.com/Balajitechlabs/quickdash/issues?q=is%3Aissue+label%3Aenhancement).
2. [Open a feature request](https://github.com/Balajitechlabs/quickdash/issues/new/choose) describing the feature and use case.

### Submit Code Changes

#### 1. Set Up
Follow [SETUP.md](SETUP.md) to get the project building locally.

#### 2. Create a Branch
```bash
git checkout -b feat/your-feature-name
```

#### 3. Make Changes
- Follow existing code style (Kotlin conventions, Compose patterns).
- Write tests for new functionality.
- Ensure zero lint errors: `./gradlew lint`
- Ensure all tests pass: `./gradlew testDebugUnitTest`

#### 4. Commit
Use clear, descriptive commit messages:
```
feat: add floating color picker tool
fix: resolve crash on Android 14 timer alarm
docs: update README with new screenshots
```

#### 5. Open a Pull Request
- Target the `main` branch.
- Link any related issues.
- Describe what your PR changes and why.

## Development Guidelines

### Architecture
- **MVVM** pattern — screens have a ViewModel, data flows through repositories.
- **Hilt** for dependency injection in new code.
- **DataStore** for preferences, **Room** for persistent data.

### Code Style
- 100% Kotlin — no Java files.
- Jetpack Compose for all UI.
- Material Design 3 theming via `Theme.kt`.
- Run `./gradlew lint` before committing.

### Testing
- Unit tests for ViewModels and repositories.
- Instrumented tests for UI (Compose testing).
- Run `./gradlew testDebugUnitTest` to verify.

## Getting Help
- Open a [GitHub Discussion](https://github.com/Balajitechlabs/quickdash/discussions).
- Check [ARCHITECTURE.md](ARCHITECTURE.md) for codebase guidance.
