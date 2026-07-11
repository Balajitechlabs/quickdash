# Quiver AI

Proxy requests to Quiver AI via Replit-managed billing.

## Callback

Use `externalApi__quiver_ai` in `codeExecution`.

## Allowed operations

- `POST` `/svgs/generations` - Generate SVGs from a text prompt (JSON or SSE response).
- `POST` `/svgs/vectorizations` - Vectorize a raster image to SVG (JSON or SSE response).
- `GET` `/models{/:model_id}?` - List models or fetch a single model.

Authorization is handled automatically by Replit. Do not pass an `Authorization` header.

## Quickstart

1. Call the callback with a `path` and `method` exactly as listed under Allowed operations — do not add or remove version prefixes (e.g. `/scrape`, not `/v1/scrape`).
2. For GET, put URL params in `query`. For POST/PUT/PATCH, pass a JSON object as `body` (it is serialized for you).
3. Inspect `result.body`.

## Example

```javascript
const result = await externalApi__quiver_ai({
  path: '/svgs/generations',
  method: 'POST',
  body: {},
})

console.log(result.status)
console.log(result.body)
```
