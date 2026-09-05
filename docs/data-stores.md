# Data Storage Architecture

QuickDash leverages three distinct persistence layers tailored to specific access patterns:

| Storage Layer | Technology | Primary Purpose |
|---|---|---|
| Preferences | Jetpack DataStore (Preferences) | High-frequency UI settings, theme selection, toggles |
| Structured Data | Room SQLite Database | Searchable clipboard history, quick notes, and logs |
| Sensitive Backups | AES-GCM Encrypted JSON File | Password-protected user state export and import |

---
Copyright (c) 2026 ||BTL||™ (balajitechlabs)
