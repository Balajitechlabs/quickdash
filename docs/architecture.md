# System Architecture

## Architectural Principles

QuickDash is engineered following Clean Architecture and Unidirectional Data Flow (UDF) patterns. The architecture enforces strict separation of concerns across core infrastructure and decoupled feature modules.

```
QuickDash
├── core/
│ ├── data/ # Preference stores, crypto managers, and backup engines
│ ├── di/ # Hilt dependency injection graphs
│ ├── network/ # OkHttp networking and release fetchers
│ ├── services/ # Foreground bubble and floating window services
│ ├── shizuku/ # Elevated privilege integration bridge
│ ├── ui/ # Shared theme tokens, design components, and root scaffold
│ └── utils/ # In-app update manager and logger abstractions
└── features/ # Independent feature modules (presentation + domain)
 ├── clipboard/ # Clipboard capture, history, and search
 ├── dashboard/ # Floating window, radial tools, and grid
 ├── qr/ # Barcode/QR camera scanner and generator
 ├── settings/ # Category-based preference panels
 └── about/ # Brand showcase and update inspection
```

## Unidirectional Data Flow (UDF)

Each feature screen interacts with state through reactive streams:
1. State is hoisted in a `@HiltViewModel` and exposed as an immutable `StateFlow<UiState>`.
2. Composables collect state via `collectAsStateWithLifecycle()` to prevent background memory leaks.
3. User interactions trigger intention-revealing ViewModel methods (`onAction`), which update state atomically.

---
Copyright (c) 2026 ||BTL||™ (balajitechlabs)
