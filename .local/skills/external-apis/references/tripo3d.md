# Tripo3D

Proxy requests to Tripo3D via Replit-managed billing.

## Callback

Use `externalApi__tripo3d` in `codeExecution`.

## Allowed operations

- `POST` `/task` - Submit a Tripo3D generation task. Cost is read from data.consumed_credit on GET /v2/openapi/task/{task_id} after the task reaches a terminal status. Covers every billable body.type uniformly.
- `GET` `/task/:task_id` - Poll a previously submitted task by id. Free — Tripo does not charge for status queries.

Authorization is handled automatically by Replit. Do not pass an `Authorization` header.

## Quickstart

1. Call the callback with a `path` and `method` exactly as listed under Allowed operations — do not add or remove version prefixes (e.g. `/scrape`, not `/v1/scrape`).
2. For GET, put URL params in `query`. For POST/PUT/PATCH, pass a JSON object as `body` (it is serialized for you).
3. Inspect `result.body`.

## Example

```javascript
const result = await externalApi__tripo3d({
  path: '/task',
  method: 'POST',
  body: {},
})

console.log(result.status)
console.log(result.body)
```
