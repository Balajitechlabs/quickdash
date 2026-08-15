# Contributing to QuickDash ⚡

Thank you for your interest in contributing to **QuickDash**! QuickDash is an open-source Android floating utility hub built with Kotlin, Jetpack Compose, and Material Design 3. We are committed to maintaining a fast, privacy-first, on-device app with zero telemetry and 120Hz fluid animations.

Whether you're fixing a bug, adding a new utility tool, improving accessibility, or refining documentation, your contributions are warmly welcomed!

---

## 📜 Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md). Please treat all community members with respect, kindness, and constructive feedback.

---

## 🌿 Branching Strategy & Workflow

QuickDash uses a two-tier branching strategy:

- **`master` / `main`**: Production-ready code and official releases. Direct pushes are protected.
- **`dev`**: Main development and integration branch. **All Pull Requests must target `dev`**.

### Branch Naming Conventions

Create focused branches from `dev` using descriptive prefixes:
- `feat/feature-name` (e.g., `feat/custom-bubble-tools`, `feat/glance-widgets`)
- `fix/bug-description` (e.g., `fix/timer-alarm-android-14`, `fix/webview-content-access`)
- `refactor/scope` (e.g., `refactor/120hz-graphics-layer`)
- `docs/topic` (e.g., `docs/architecture-guide`)
- `security/hardening-target` (e.g., `security/codeql-hardening`)

---

## 🛠️ Step-by-Step Contribution Guide

### 1. Fork & Clone
```bash
# Clone your fork
git clone https://github.com/<your-username>/quickdash.git
cd quickdash

# Add upstream remote
git remote add upstream https://github.com/Balajitechlabs/quickdash.git
git fetch upstream
```

### 2. Create a Feature Branch
```bash
git checkout -b feat/your-feature-name upstream/dev
```

### 3. Build & Test Locally
Ensure your environment meets the prerequisites (JDK 17+, Android SDK 36):
```bash
# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew testDebugUnitTest

# Run Lint Analysis
./gradlew lint
```

### 4. Commit Standards (Conventional Commits)
We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:
```
<type>(<optional scope>): <short description in present tense>

[optional body explaining rationale]
```

**Examples:**
- `feat(bubble): add customizable 4-slot radial menu in Settings`
- `fix(timer): prevent crash when setting exact alarms on Android 14+`
- `docs(readme): add dynamic total downloads badge and showcase banner`
- `security(crypto): enforce BIOMETRIC_STRONG for biometric authentication`

*(Tip: GPG / SSH signed commits are highly recommended for verified badge on GitHub).*

### 5. Push & Open Pull Request
```bash
git push origin feat/your-feature-name
```
- Open a Pull Request targeting the **`dev`** branch.
- Complete the PR template with a clear description and testing notes.
- Wait for automated GitHub Actions CI checks (`Android Build`, `Unit Tests`, `CodeQL`) to pass.

---

## 🛡️ QuickDash Coding Rules & Stability Guidelines

To maintain rock-solid stability, zero jank at 120Hz, and compile-readiness for stable Google Play releases, **all PRs must strictly adhere to these 8 rules**:

### 1. Jetpack Compose Thread Safety
- **CRITICAL:** All writes to Compose state variables (`mutableStateOf`, `mutableStateListOf`, DataStore collect updates) MUST execute on the Main Thread (`Dispatchers.Main`).
- When fetching data asynchronously via coroutines, perform I/O on `Dispatchers.IO` and switch back or post updates on `Dispatchers.Main`.

### 2. Intrinsic Measurements & SubcomposeLayout
- **DO NOT** place any `SubcomposeLayout`-based components (including `LazyRow`, `LazyColumn`, `TabRow`, or `BoxWithConstraints`) inside layouts that invoke intrinsic measurement queries.
- Material 3 `ListItem` uses intrinsic measurements to align its child slots (`headlineContent`, `supportingContent`, `trailingContent`). Use standard `Row` and `Column` layouts instead to prevent `IllegalStateException` runtime crashes.

### 3. Intents from Floating / Service Contexts
- Any `Intent` launched from a background service, quick setting tile, or floating window activity context MUST be flagged with `Intent.FLAG_ACTIVITY_NEW_TASK` to allow Android to launch the activity inside a clean task stack.

### 4. Multi-Colored Vector & Image Assets
- Always use `androidx.compose.foundation.Image` (without a `colorFilter` or `tint` argument) instead of the `Icon` composable when displaying multi-colored images or official logos (e.g., App Logo, UPI provider logos). Using `Icon` enforces a monochrome tint that obscures multi-colored graphics.

### 5. UPI Handle Validations
- Support flexible alpha-numeric UPI handles allowing digits, dots, hyphens, and underscores (e.g., `^[a-zA-Z0-9.\-_]+@[a-zA-Z0-9.\-_]+$`), and provide clean fallback empty values for new installs.

### 6. Google Play Store Policy Compliance
- **Never** add broad or prohibited permissions (such as `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, or `REQUEST_INSTALL_PACKAGES`) without prior core team approval. All storage operations should use the Android Storage Access Framework (SAF) or Photo Picker.

### 7. 120Hz Rendering & Animation Performance
- Avoid animating layout-phase modifiers like `Modifier.scale()` or `Modifier.alpha()` inside continuous loops.
- Use `Modifier.graphicsLayer { scaleX = ...; scaleY = ...; alpha = ... }` to offload transformations directly to the GPU RenderThread with zero recompositions.

### 8. Static Analysis & CodeQL Security Hygiene
- **Log Injection:** Sanitize all logcat outputs via `AppLogger.sanitize()` stripping carriage returns (`\r\n`).
- **Implicit PendingIntents:** Always specify `intent.setPackage(context.packageName)` on notification and alarm PendingIntents.
- **WebView Security:** Explicitly set `settings.allowContentAccess = false` and `settings.allowFileAccess = false` on WebViews.
- **Local Auth:** Enforce `BIOMETRIC_STRONG` and `DEVICE_CREDENTIAL` for cryptographic authentication.

---

## 🧪 Pre-PR Verification Checklist

Before submitting your PR, verify that:
- [ ] All unit tests pass: `./gradlew testDebugUnitTest`
- [ ] Lint analysis produces zero errors: `./gradlew lint`
- [ ] Code follows Kotlin style conventions (no wildcard imports, 100% Kotlin).
- [ ] No hardcoded strings in UI (use `strings.xml` for localization).
- [ ] Tested on light mode and dark mode with Material You dynamic theming.
- [ ] Pull Request targets the `dev` branch.

---

## 💬 Getting Help & Discussion

- **Ask Questions / Share Ideas**: [GitHub Discussions](https://github.com/Balajitechlabs/quickdash/discussions)
- **Report Issues**: [GitHub Issues](https://github.com/Balajitechlabs/quickdash/issues)
- **Architecture Overview**: [ARCHITECTURE.md](ARCHITECTURE.md)
- **Setup Guide**: [SETUP.md](SETUP.md)

Thank you for making QuickDash faster, safer, and better for everyone! 🚀✨
