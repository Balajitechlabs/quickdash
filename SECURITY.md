# Security Policy

## Reporting a Vulnerability

QuickDash takes security seriously. If you discover a security vulnerability, please report it privately **before** creating a public issue.

### How to Report

1. **Email**: [quickdash@balajitechlab.com](mailto:quickdash@balajitechlab.com)
2. **GitHub**: Use the [Security Advisory](https://github.com/Balajitechlabs/quickdash/security/advisories/new) form (preferred).

You should receive a response within **48 hours**. If not, follow up via email.

### What to Include

- Description of the vulnerability.
- Steps to reproduce.
- Affected versions.
- Any proof of concept (if available).

## Scope

The following are in scope:
- The Android app (`app/` module).
- The website and API (`website/` directory).
- CI/CD workflows (`.github/workflows/`).
- Build and release scripts.

## Security Practices

- **Zero Tracking**: QuickDash collects no telemetry, no analytics, no user data.
- **On-Device Processing**: All translations, OCR, and AI features run locally.
- **Encrypted Storage**: Clipboard data and Wi-Fi passwords use EncryptedSharedPreferences.
- **HTTPS Only**: All network communication is encrypted. Cleartext traffic is blocked.
- **No Root Required**: The app works on stock, unmodified Android devices.

## Supported Versions

| Version | Supported |
|---------|-----------|
| 5.x     | ✅ |
| < 5.0   | ❌ |

## Disclosure Policy

We follow **coordinated disclosure**:
1. Reporter notifies us privately.
2. We confirm and develop a fix.
3. Fix is released.
4. Public disclosure occurs after users have had time to update.
