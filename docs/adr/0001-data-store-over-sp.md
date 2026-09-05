# ADR 0001: Jetpack DataStore over SharedPreferences

## Status
Accepted

## Context
Traditional Android `SharedPreferences` suffers from synchronous I/O on the main thread, runtime parsing errors, and lack of transaction safety.

## Decision
Adopt Jetpack DataStore (Preferences) backed by Kotlin Coroutines and Flow. All preference reads and writes are asynchronous, transactional, and offload from the main thread.

## Consequences
- Guaranteed safe execution off the UI thread.
- Reactive updates via `Flow` and `collectAsStateWithLifecycle()`.
- Migration required for legacy key-value storage.

---
Copyright (c) 2026 ||BTL||™ (balajitechlabs)
