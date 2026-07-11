# Mindee

Proxy requests to Mindee via Replit-managed billing.

## Callback

Use `externalApi__mindee` in `codeExecution`.

## Allowed operations

- `POST` `/v2/products/:product(extraction|ocr|classification|crop|split)/enqueue` - Mindee inference enqueue (extraction / ocr / classification / crop / split). Bills after polling the job and reading the processed page count.
- `POST` `/v2/inferences/enqueue` - Deprecated Mindee extraction enqueue alias used by current SDKs. Bills after polling the job and reading the processed page count.
- `GET` `/v2/jobs/:job_id` - Mindee job status polling. No charge — enqueue billing already settles the processed page count.
- `GET` `/v2/products/:product(extraction|ocr|classification|crop|split)/results/:inference_id` - Mindee inference results retrieval (extraction / ocr / classification / crop / split). No charge — enqueue billing already settles the processed page count.
- `GET` `/v2/inferences/:inference_id` - Deprecated Mindee extraction result alias used by current SDKs. No charge — enqueue billing already settles the processed page count.

Authorization is handled automatically by Replit. Do not pass an `Authorization` header.

## Quickstart

1. Call the callback with a `path` and `method` exactly as listed under Allowed operations — do not add or remove version prefixes (e.g. `/scrape`, not `/v1/scrape`).
2. For GET, put URL params in `query`. For POST/PUT/PATCH, pass a JSON object as `body` (it is serialized for you).
3. Inspect `result.body`.
