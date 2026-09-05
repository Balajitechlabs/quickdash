# Contributing to QuickDash

Welcome to QuickDash. QuickDash is an open-friendly Android floating productivity suite built with Jetpack Compose, Kotlin coroutines, and Material 3 Expressive.

This guide details our engineering standards, architecture rubric, local verification workflows, and contribution process.

---

## 1. The 10-Category Quality Rubric

Every contribution is evaluated against our 10 quality gates:

1. **Self-Documentation**: Every source and test file must maintain a canonical header with an authentic description recorded in `docs/file_manifest.json`. `docs/FILE_MAP.md` is auto-generated and kept in sync via `tools/sync_file_headers.py`.
2. **Formatting & Consistency**: Zero wildcard imports, zero redundant inline fully-qualified packages, and consistent Kotlin style.
3. **Static Analysis**: Zero compiler warnings (`w:`) or errors (`e:`), clean Android Lint runs, and strict baseline discipline.
4. **Testing & Coverage**: Unit tests for all repositories, ViewModels, and parsers. Critical paths (`core/data`, `core/security`, `core/utils`) require negative and edge-case test suites.
5. **Repo Hygiene**: Zero machine paths, zero cached secrets, zero build binaries in git, and strict `.gitignore` rules.
6. **Architecture & Modularity**: Single-responsibility components. Megafiles are decomposed into focused presentation sections (e.g. `SettingsSecuritySection`, `SettingsDataSection`).
7. **No Dead or Invisible Code**: Every preference key, enum branch, action, and tool route must have an active, reachable call site or test.
8. **Algorithmic Correctness**: Core algorithms (parsers, cryptographic routines, classifiers) are documented in `docs/AlgorithmAudit.md` with explicit boundary validation.
9. **Contributor Onboarding**: Clean, reproducible build steps in `docs/SETUP.md` with complete architecture references in `ARCHITECTURE.md`.
10. **Engineering Process**: Conventional commits (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`, `perf:`), signed commits, and green CI validation.

---

## 2. Project Architecture

```
app/src/main/java/com/balajitechlabs/quickdash/
├── core/
│ ├── data/ # UserStore, CryptoManager, EncryptedPrefs, Room Database
│ ├── di/ # Hilt dependency injection modules
│ ├── navigation/ # QuickDashNavHost and route definitions
│ ├── network/ # OkHttp client, update models, CrashReporter
│ ├── quicktile/ # Android Quick Settings tile services
│ ├── security/ # IncognitoManager, SecurityGuardManager
│ ├── services/ # FloatingBubbleService, ShakeDetectorService
│ ├── shizuku/ # Shizuku privilege bridge
│ ├── ui/ # QuickDashApp scaffold, theme tokens, reusable components
│ └── utils/ # TextCategorizer, UpdateManager, QRCodeGenerator
├── features/ # Isolated feature modules (screens, ViewModels)
│ ├── about/ # Developer brand, social links, and update actions
│ ├── calculator/ # Scientific floating calculator
│ ├── clipboard/ # Clipboard history, auto-clean, search
│ ├── dashboard/ # FloatingDialogActivity and Spotlight launcher
│ ├── notes/ # Quick scratchpad notes backed by Room
│ ├── qr/ # UPI generation, camera scanner, payload parser
│ ├── settings/ # Modular preference categories
│ ├── timer/ # Multi-timer and stopwatch tools
│ └── [...tools] # Additional productivity modules
└── widget/ # Glance home-screen app widgets
```

---

## 3. Local Verification Commands

Before submitting changes, run these verification steps from the repository root:

```bash
# 1. Verify file headers and documentation sync
python3 tools/sync_file_headers.py --check
python3 tools/sync_file_headers.py --todo

# 2. Compile Kotlin sources (zero warnings, zero errors)
./gradlew :app:compileDebugKotlin

# 3. Execute unit test suite
./gradlew :app:testDebugUnitTest

# 4. Assemble debug APK
./gradlew :app:assembleDebug

# 5. Check git diff for trailing whitespace or formatting issues
git diff --check
```

---

## 4. How-To Guides

### Adding a Preference Key
1. Define the type-safe key in `core/data/prefs/PreferencesKeys.kt`:
 ```kotlin
 val MY_FEATURE_KEY = booleanPreferencesKey("my_feature_key")
 ```
2. Expose the reactive `Flow` and `suspend` mutation function in `core/data/UserStore.kt`:
 ```kotlin
 val myFeature: Flow<Boolean> = context.dataStore.data.map { it[MY_FEATURE_KEY] ?: false }
 suspend fun saveMyFeature(enabled: Boolean) {
 context.dataStore.edit { it[MY_FEATURE_KEY] = enabled }
 }
 ```
3. Consume the property in your ViewModel using `stateIn` or in Compose via `collectAsStateWithLifecycle()`.

### Adding a New Screen or Tool
1. Register the tool entry in `core/ui/QuickTool.kt` with a unique route name, title, category, and icon.
2. Create your presentation composable inside `features/<feature_name>/presentation/<FeatureScreen>.kt`.
3. Wire the route in `core/ui/QuickDashApp.kt` or `core/navigation/QuickDashNavHost.kt`.
4. Run `python3 tools/sync_file_headers.py` to index the new file. Add its description in `docs/file_manifest.json` and re-run with `--check`.

### Synchronizing Headers & File Map
Whenever you add or rename Kotlin files:
```bash
python3 tools/sync_file_headers.py --todo
# Add the 1-sentence description in docs/file_manifest.json
python3 tools/sync_file_headers.py
python3 tools/sync_file_headers.py --check
```

---

## 5. Contribution & Commit Standards

- **Conventional Commits**: Format commit titles as `<type>(<scope>): <summary>`.
 - Allowed types: `feat`, `fix`, `refactor`, `docs`, `perf`, `test`, `chore`.
 - Examples:
 - `feat(qr): add zoom toggle and tap-to-focus to camera scanner`
 - `fix(classifier): disambiguate telephone numbers from math operators`
 - `docs: sync file map and update algorithm audit specification`
- **Zero AI Slop**: Code must be self-documenting without superficial or robotic comments.
- **Brand Consistency**: Always write `balajitechlabs` in all lowercase letters.
- **License**: PocketOps Custom Open Source Fork License. Copyright `||BTL||™ (balajitechlabs)`.
