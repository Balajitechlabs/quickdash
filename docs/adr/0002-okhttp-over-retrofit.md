# ADR 0002: Lightweight OkHttp Client over Retrofit

## Status
Accepted

## Context
QuickDash only makes minimal network requests: querying the GitHub Releases API for update checks and notifying a Telegram error bot.

## Decision
Utilize a lean, centralized OkHttp singleton rather than bundling the heavyweight Retrofit reflection runtime and converter dependencies.

## Consequences
- Reduced APK size by ~600KB.
- Eliminated reflection-based interface proxies.
- Simple, auditable HTTP execution.

---
Copyright (c) 2026 ||BTL||™ (balajitechlabs)
