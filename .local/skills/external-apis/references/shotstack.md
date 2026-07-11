# Shotstack

Proxy requests to Shotstack via Replit-managed billing.

## Callback

Use `externalApi__shotstack` in `codeExecution`.

## Allowed operations

- `POST` `/edit/v1{/templates}?/render` - Submit a Shotstack render job (ad-hoc timeline or template-bound; bills on polled output duration for video/audio, flat per render for images).
- `GET` `/edit/v1/render/:id` - Poll a Shotstack render job for status and final duration (used by the billed render op to settle cost; not billed separately).
- `POST` | `GET` | `PUT` | `DELETE` `/edit/v1/templates{/:id}?` - Shotstack template CRUD: create (POST /edit/v1/templates), list (GET /edit/v1/templates), fetch (GET /edit/v1/templates/{id}), update (PUT /edit/v1/templates/{id}), delete (DELETE /edit/v1/templates/{id}). No charge — billed on render.
- `GET` `/edit/v1/probe/:url` - Inspect media metadata via FFprobe (GET /edit/v1/probe/{url-encoded-url}). No charge — utility endpoint, not on the Shotstack pricing page.
- `POST` | `GET` | `DELETE` `/serve/v1/assets{/render}?{/:id}?` - Shotstack Serve API: transfer (POST /serve/v1/assets), fetch (GET /serve/v1/assets/{id}), delete (DELETE /serve/v1/assets/{id}), list by render (GET /serve/v1/assets/render/{id}). No charge — see storage/bandwidth note above.
- `POST` | `GET` | `DELETE` `/ingest/v1/sources{/upload}?{/:id}?` - Shotstack Ingest API: ingest from URL (POST /ingest/v1/sources), direct upload (POST /ingest/v1/sources/upload), list (GET /ingest/v1/sources), fetch (GET /ingest/v1/sources/{id}), delete (DELETE /ingest/v1/sources/{id}). No charge — see storage/bandwidth note above.

Authorization is handled automatically by Replit. Do not pass an `Authorization` header.

## Quickstart

1. Call the callback with a `path` and `method` exactly as listed under Allowed operations — do not add or remove version prefixes (e.g. `/scrape`, not `/v1/scrape`).
2. For GET, put URL params in `query`. For POST/PUT/PATCH, pass a JSON object as `body` (it is serialized for you).
3. Inspect `result.body`.
