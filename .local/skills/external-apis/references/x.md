# X (Twitter)

Proxy requests to X (Twitter) via Replit-managed billing.

## Callback

Use `externalApi__x` in `codeExecution`.

## Allowed operations

- `GET` `/2/(tweets|tweets/\d+|tweets/search/(?:recent|all)|tweets/\d+/quote_tweets|(?:lists|spaces)/[^/]+/tweets|users/[^/]+/(?:tweets|mentions)){/}?` - Posts: Read — every endpoint that returns Posts (search, lookup, user/list/space timelines, quote tweets). Billed per Post returned in `data`.
- `GET` `/2/(users|users/by|users/by/username/[^/]+|users/\d+|users/\d+/(?:affiliates|followers|following)|tweets/\d+/retweeted_by|lists/\d+/(?:followers|members)){/}?` - Users: Read — endpoints that return Users (multi/single lookup, retweeters, list followers/members, affiliates, followers, following). Billed per User returned in `data`.
- `GET` `/2/trends/by/woeid/:woeid(\d+){/}?` - Trend: Read — `GET /2/trends/by/woeid/{woeid}` returns trending topics for a Yahoo WOEID. Billed per Trend returned in `data`.
- `GET` `/2/(lists/\d+|users/\d+/(?:owned_lists|followed_lists|list_memberships)){/}?` - List: Read - reading public lists Billed per List returned in `data`.
- `GET` `/2/(spaces|spaces/by/creator_ids|spaces/search|spaces/[^/]+){/}?` - Space: Read — endpoints that return Spaces (multi-lookup, creator-id lookup, search, single lookup). Billed per Space returned in `data`.
- `GET` `/2/communities/:id(\d+){/}?` - Community: Read. Billed per Community returned in `data`.
- `GET` `/2/(tweets/counts/recent|news/search|news/\d{1,19}){/}?` - Counts: Recent — Post counts for last few days, news reads. All bill per HTTP request under X's shared Counts: Recent dashboard bucket.
- `GET` `/2/tweets/counts/all{/}?` - Counts: All — `GET /2/tweets/counts/all` returns per-bucket Post counts over the full archive. Billed per HTTP request, not per `data[]` bucket.

Authorization is handled automatically by Replit. Do not pass an `Authorization` header.

## Skill

## X (Twitter) quickstart

Read-only X API v2 access through passthrough billing. Use
concrete request paths (e.g. `/2/tweets/search/recent`) — never
send the route patterns literally. Put URL params in `query`.

```javascript
const result = await externalApi__x({
  path: '/2/tweets/search/recent',
  method: 'GET',
  query: {query: 'replit', max_results: '10'},
})

for (const tweet of result.body.data ?? []) {
  console.log(tweet.id, tweet.text)
}
```

Authorization is managed by passthrough billing. Do not set an
`Authorization` header manually.
