---
name: security-scan
description: Run runDependencyAudit, runSastScan, and runHoundDogScan and return a concise, prioritized security summary with critical/high findings first. Must use this skill if security scanning is explicitly requested by the user.
---

# Security Scan Skill

## Opening the Security pane

You can render a one-click "Open in security" button that takes the user to the Security workspace pane — vulnerability findings, dependency risks, or prior scan results. To render it, write this tag on its own line at the end of your reply (it is replaced by the button; the user clicks it to open the pane). This is navigation only; run the scanners below when the user wants a fresh scan.

```
<open-in-pane tool="security"></open-in-pane>
```

Button behavior:

- Render it when the request is mainly navigation; when navigation is mixed with real work, still do the work with the appropriate tools.
- Keep your chat reply to one concise sentence naming what the button opens. Do not say the pane is already open, that you opened it, or describe the button's position.

| User asks | Tag to emit |
| --- | --- |
| "Open security scan results." | `<open-in-pane tool="security"></open-in-pane>` |
| "Where are my vulnerability findings?" | `<open-in-pane tool="security"></open-in-pane>` |

Run three independent scanners and summarize results:

- `runDependencyAudit()` for package/dependency vulnerabilities
- `runSastScan()` for static code findings
- `runHoundDogScan()` for privacy/security dataflow findings

## Orchestration

For full scans, run scanners in parallel and tolerate per-scanner failures.

```javascript
const [depResult, sastResult, hounddogResult] = await Promise.allSettled([
  runDependencyAudit(),
  runSastScan(),
  runHoundDogScan(),
]);

const dep = depResult.status === 'fulfilled' ? depResult.value : null;
const sast = sastResult.status === 'fulfilled' ? sastResult.value : null;
const hounddog =
  hounddogResult.status === 'fulfilled' ? hounddogResult.value : null;
```

Do not fail the whole scan because one scanner errors.

## Minimal Response Shape

- `runDependencyAudit()`
  - `metadata.vulnerabilities`: `{ info, low, moderate, high, critical }`
  - `vulnerabilities[]`: `id`, `package`, `severity`, `fix`, `source`
- `runSastScan()`
  - `results[]`: `checkId`, `message`, `severity`, `fingerprint`, `location`
- `runHoundDogScan()`
  - `vulnerabilities[]`: `hash`, `ruleIds`, `message`, `severity`, `location`, `privacyViolations`, `remediation*`

## Output Expectations

Return concise results instead of dumping full payloads:

1. Per scanner: status (`ok` or `error`) and count by severity.
2. Top critical/high findings with file path and short message.
3. A short remediation plan, with risky/breaking changes clearly called out.

## Dependency Remediation

When fixing dependency vulnerabilities, prefer upgrading the affected dependency
or its direct parent to the latest compatible version first. If the Replit
Package Firewall or security registry blocks a vulnerable package during
installation, do not bypass it and do not stop at the first block:

1. Try the latest available version of the same dependency.
2. For transitive findings, update the direct dependency that pulls it in.
3. If the latest version is still blocked or incompatible, choose a safer
   alternative package yourself and continue. Only involve the user if the
   replacement changes product behavior or there is no reasonable substitute.
