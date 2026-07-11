# Browserbase

Proxy requests to Browserbase via Replit-managed billing.

## Callback

Use `externalApi__browserbase` in `codeExecution`.

## Allowed operations

- `POST` `/v1/fetch` - Fetch a URL via a managed headless browser. Billed per call; rate depends on the `proxies` flag and the configured tier.
- `POST` `/v1/search` - Web Search (Open Web Tools) — flat per-call rate; numResults (1–25) does not affect price.
- `POST` `/v1/sessions` - Create a browser session. Billed by Browser Minute + Proxy MB observed via polling GET /v1/sessions/{id} until terminal status or expiresAt passes.
- `POST` `/v1/sessions:id(/[^/]+)` - Close a browser session (REQUEST_RELEASE). No charge — session duration is billed on creation.
- `POST` | `GET` | `DELETE` `/v1/contexts/:path*` - Browserbase contexts: create (POST /v1/contexts), read (GET /v1/contexts/{id}), delete (DELETE /v1/contexts/{id}). No charge.

Authorization is handled automatically by Replit. Do not pass an `Authorization` header.

## Quickstart

1. Call the callback with a `path` and `method` exactly as listed under Allowed operations — do not add or remove version prefixes (e.g. `/scrape`, not `/v1/scrape`).
2. For GET, put URL params in `query`. For POST/PUT/PATCH, pass a JSON object as `body` (it is serialized for you).
3. Inspect `result.body`.

## Example

```javascript
const result = await externalApi__browserbase({
  path: '/v1/fetch',
  method: 'POST',
  body: {},
})

console.log(result.status)
console.log(result.body)
```
