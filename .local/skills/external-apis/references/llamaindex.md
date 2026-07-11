# LlamaIndex

Proxy requests to LlamaIndex via Replit-managed billing.

## Callback

Use `externalApi__llamaindex` in `codeExecution`.

## Allowed operations

- `POST` `/api/v2/parse` - Parse a file via LlamaParse v2. Bills page count × tier rate after polling status to terminal, with an explicit layout/chart add-on surcharge when requested.
- `POST` `/api/v1/beta/files` - Upload a file for a later billable LlamaCloud job. No charge — upload stores file bytes but does not consume document processing credits. File list/read/delete and page asset endpoints are intentionally not allowlisted for global credentials.

Authorization is handled automatically by Replit. Do not pass an `Authorization` header.

## Quickstart

1. Call the callback with a `path` and `method` exactly as listed under Allowed operations — do not add or remove version prefixes (e.g. `/scrape`, not `/v1/scrape`).
2. For GET, put URL params in `query`. For POST/PUT/PATCH, pass a JSON object as `body` (it is serialized for you).
3. Inspect `result.body`.

## Example

```javascript
const result = await externalApi__llamaindex({
  path: '/api/v2/parse',
  method: 'POST',
  body: {},
})

console.log(result.status)
console.log(result.body)
```
