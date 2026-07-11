# Exa

Proxy requests to Exa via Replit-managed billing.

## Callback

Use `externalApi__exa` in `codeExecution`.

## Allowed operations

- `POST` `/search` - Web search (auto, neural, fast, deep, deep-reasoning).
- `POST` `/contents` - Page contents retrieval (text, highlights, summary, subpages, livecrawl).
- `POST` `/findSimilar` - Find similar links by URL similarity (optional contents pulldown).
- `POST` `/answer` - Web-grounded answer (with optional SSE streaming).

Authorization is handled automatically by Replit. Do not pass an `Authorization` header.

## Skill

## Exa quickstart

Semantic web search through Exa passthrough billing. Send the
required fields as an object in `body` (it is serialized for
you — do not pre-stringify) — an empty body is rejected.

```javascript
const result = await externalApi__exa({
  path: '/search',
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: {query: 'latest advances in AI agents', numResults: 5},
})

for (const item of result.body.results) {
  console.log(item.title, item.url)
}
```

Authorization is managed by passthrough billing. Do not set an
`Authorization` header manually.

Be conservative: each search is billed and adds latency. Start with
one focused query and a small `numResults`, then refine — don't fan
out many searches at once. Keep no more than ~5 searches in flight
in parallel.

## Example

```javascript
const result = await externalApi__exa({
  path: '/search',
  method: 'POST',
  body: {},
})

console.log(result.status)
console.log(result.body)
```
