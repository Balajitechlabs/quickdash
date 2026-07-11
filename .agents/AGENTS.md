# QuickDash Coding Rules & Stability Guidelines

To maintain absolute stability and compile-readiness for stable APK releases, follow these rules during development:

## 1. Jetpack Compose Thread Safety
- **CRITICAL:** All writes to Jetpack Compose state variables (`mutableStateOf`, `mutableStateListOf`, `collectAsState` updates, etc.) MUST execute on the Main Thread (`Dispatchers.Main`).
- When fetching data asynchronously via coroutines (e.g. within `scope.launch`), perform network or database I/O on `Dispatchers.IO` but ensure you switch back or update the UI state variables on `Dispatchers.Main`.

## 2. Intrinsic Measurements & SubcomposeLayout
- **DO NOT** place any `SubcomposeLayout`-based components (including `LazyRow`, `LazyColumn`, `TabRow`, or `BoxWithConstraints`) inside layouts that invoke intrinsic measurement queries.
- Material 3 `ListItem` uses intrinsic measurements to align its child slots (`headlineContent`, `supportingContent`, `trailingContent`). Avoid placing lazy lists inside `ListItem` slots. Use standard `Row` and `Column` layouts instead to prevent `IllegalStateException` crashes.

## 3. Intents from Floating / Service Contexts
- Any `Intent` launched from a background service, quick setting tile, or floating window activity context must be flagged with `Intent.FLAG_ACTIVITY_NEW_TASK` to allow Android to launch the activity inside a new task queue cleanly.

## 4. Multi-Colored Vector & Image Assets
- Always use the `androidx.compose.foundation.Image` composable (without a `colorFilter` or `tint` argument) instead of the `Icon` composable when displaying multi-colored images or logos (like the official App Logo). Using `Icon` enforces a monochrome tint that obscures the details of multi-colored graphics.

## 5. UPI Handle Validations
- Support flexible alpha-numeric UPI handles, allowing digits and special symbols (e.g., `^[a-zA-Z0-9.\-_]+@[a-zA-Z0-9.\-_]+$`), and handle fallback empty values properly for a clean new-install state.
