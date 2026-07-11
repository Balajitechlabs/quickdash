# Firecrawl

Proxy requests to Firecrawl via Replit-managed billing.

## Callback

Use `externalApi__firecrawl` in `codeExecution`.

## Allowed operations

- `POST` `/scrape` - Scrape any URL and get its content in markdown, HTML, or other formats.
- `POST` `/batch/scrape` - Scrape multiple URLs and get their content in markdown, HTML, or other formats.
- `POST` `/map` - Discover all URLs on a website without scraping their content.
- `POST` `/crawl` - Scrape an entire website by following links from a starting URL. The same per-page scrape option costs apply to each page crawled.
- `POST` `/agent` - Autonomous web research agent. 5 daily runs free; usage-based pricing beyond that.
- `POST` `/search` - Search the web and optionally scrape the results. Rounded up per 10 results (e.g., 11 results = 4 credits). Additional per-page scrape costs apply to each result that is scraped. Enterprise ZDR search costs 10 credits / 10 results.
- `POST` | `GET` | `DELETE` `/*` - All other paths are no-charge.

Authorization is handled automatically by Replit. Do not pass an `Authorization` header.

## Skill

## Firecrawl quickstart

Scrape, crawl, and search the web through Firecrawl passthrough
billing. Send the required fields as an object in `body` (it is
serialized for you — do not pre-stringify) — an empty body is
rejected.

```javascript
const result = await externalApi__firecrawl({
  path: '/scrape',
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: {url: 'https://docs.replit.com', formats: ['markdown']},
})

console.log(result.body.data.markdown)
```

Authorization is managed by passthrough billing. Do not set an
`Authorization` header manually.

Be conservative: scraping is billed per request and adds latency.
Don't scrape every result from a search — pick the few most
relevant URLs. Keep no more than ~5 scrapes in flight at once and
prefer `/batch/scrape` over many parallel `/scrape` calls.

## Example

```javascript
const result = await externalApi__firecrawl({
  path: '/scrape',
  method: 'POST',
  body: {},
})

console.log(result.status)
console.log(result.body)
```
