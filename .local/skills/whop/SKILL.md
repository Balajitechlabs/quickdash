---
name: whop
description: Guidelines for using Whop to integrate commerce, payment plans, checkout, and subscription management
---
IMPORTANT: You have everything you need. Do NOT ask the user for Whop Company ID, Plan ID, or API key — retrieve them yourself (Step 1 and Step 3 below). Use Whop's hosted checkout (redirect-based) for payments (Step 4). For access control, verify the user's Whop account and ask Whop whether that account has access; do not treat a redirect, email string, or client state as proof of purchase. Execute the steps directly — do not plan or re-read references before acting.

## Helper scripts — write these first

Write these two files to the project root using the sources in `./references/`. Run the CLI helpers via `shell_exec`. The proxy injects `api_key` automatically — no credentials needed.

**whop-mcp.mjs** (source: `./references/whop-mcp.mjs`) — for MCP tools:
```bash
node whop-mcp.mjs --list-tools                              # discover available tools
node whop-mcp.mjs --schema create_plan                      # check params before calling
node whop-mcp.mjs create_product '{"title":"Pro Access"}'   # call a tool
```

**whop-api.mjs** (source: `./references/whop-api.mjs`) — for REST API:
```bash
node whop-api.mjs POST /api/v1/plans '{"company_id":"biz_xxx","product_id":"prod_xxx","initial_price":9.99,"currency":"usd","billing_period":30}'
node whop-api.mjs POST /api/v1/checkout_configurations '{"plan_id":"plan_xxx","redirect_url":"https://myapp.repl.co/done"}'
node whop-api.mjs GET '/api/v1/payments?company_id=biz_xxx&first=10&order=created_at&direction=desc'
node whop-api.mjs GET '/api/v1/payments?company_id=biz_xxx&checkout_configuration_ids[]=ch_xxx'
```

Always run `--schema <tool>` before calling an unfamiliar MCP tool.

## Step 1: Connect and get credentials

1. Propose the Whop integration if not already connected (reference `integrations` skill).
   The connection is auto-provisioned — no user login or OAuth. A Whop store is created automatically.
2. Call `listConnections('whop')` in `code_execution` to get `company_id` (biz_xxx) and `api_key` (apik_xxx).
3. Store `company_id` as a Replit Configuration named `WHOP_COMPANY_ID`.

## Step 2: Write server code

Write **whopClient.ts** to the API server directory (source: `./references/whop-client.ts`). This provides `getWhopClient()` which lazily fetches credentials from the Replit connection API. Never import it in frontend code.

Install: `pnpm add @whop/sdk`

Use the app's existing server and auth patterns. Read `./references/code-templates.md` only if you need route shape or SDK method names; adapt it instead of copying it verbatim.

## Key facts and references

- **Whop plan = Stripe product/price.** When user says "create a product," they mean create a Whop plan.
- **Products** are just groupings — not directly purchasable.
- Prices are in **dollars** (9.99 = $9.99, 100 = $100). NOT cents like Stripe.
- Dates are **ISO 8601 strings** — parse with `new Date(str)`. Do NOT multiply by 1000.
- Plans reference products via `plan.product.id`, NOT `plan.product_id`.
- List endpoints return `{ data: [...] }`.
- `company_id` is required for most SDK methods — read from `process.env.WHOP_COMPANY_ID`.
- Whop is the system of record for purchases and membership access.
- Purchases are tied to the buyer's Whop account. If a user cannot access a purchase, they are often logged into the wrong Whop account/email.
- In a Whop app/iframe, verify the `x-whop-user-token` server-side to get the Whop `userId`, then call `users.checkAccess(resourceId, { id: userId })`. Gate paid features on Whop's response, not on local checkout state.
- For non-Whop-hosted apps, use a real auth bridge such as Whop OAuth or a server-verified membership/payment flow. Do not rely on client-supplied email, membership ID, checkout ID, or `success=true`.

- **API server directory**: `server/`
- **Client app directory**: `client/`


### Reference files

- ./references/code-templates.md -- Example routes and checkout flow using the Whop SDK
- ./references/whop-client.ts -- Server-side Whop SDK client that fetches credentials from the Replit connection API
- ./references/whop-mcp.mjs -- Helper script for calling Whop MCP tools via the OpenInt proxy
- ./references/whop-api.mjs -- Helper script for direct Whop REST API calls (plans, checkout configs, payments)

## Step 3: Create product and plan

Run these via `shell_exec` — do NOT ask the user for IDs:
1. `node whop-mcp.mjs create_product '{"title":"<product name>"}'` → get `prod_xxx`
2. `node whop-api.mjs POST /api/v1/plans '{"company_id":"biz_xxx","product_id":"prod_xxx","initial_price":<dollars>,"currency":"usd","billing_period":30}'` → get `plan_xxx`
3. Store `plan_xxx` as a Replit Configuration `WHOP_PLAN_ID`

## Step 4: Implement checkout flow

1. User clicks "Subscribe" → app calls server `POST /api/whop/checkout` with `plan_id`
2. Server creates checkout config: `POST /checkout_configurations` with `{ plan_id, redirect_url }` → returns `{ id: "ch_xxx", purchase_url }`
3. Frontend redirects user to `purchase_url` (Whop hosted checkout)
4. User pays → Whop redirects back to `redirect_url`
5. Server verifies purchase/account access with Whop before granting paid access:
   - For Whop apps, verify the Whop user token and call `users.checkAccess` for the product, experience, or company.
   - For external apps, verify the matching payment/membership server-side and bind it to the authenticated user.
6. Store only the minimal local mapping needed by the app; Whop remains the source of truth.

## Step 5: Add manage subscription page

Add `/account` or `/settings` page with cancel and view subscription. See `./references/code-templates.md` for routes.

## Step 6: Tell the user how to test and check their Whop account

When you finish the run setting up Whop, send this message to the user verbatim (do not paraphrase, do not skip — this is the only way users know their Whop account already exists):

> Your app has payments built-in via Whop! Open in a new tab to test.
>
> To check your Whop balance (revenue, customers, etc.) just visit Whop.com and login with the same email you use for Replit (your Whop account was just automatically created).

Send it as the final message of the Whop setup run, after the integration is wired up and any task list is complete.

## Rules (read these last — they override any conflicting info above)

- Do NOT ask the user for Company ID, Plan ID, or API key — get them yourself
- Use `whop-mcp.mjs` and `whop-api.mjs` to manage the Whop account (create products, plans, list payments, etc.) — do NOT ask the user to do these in the Whop dashboard
- Use Whop's hosted checkout (redirect-based) for all payment flows
- Use `getWhopClient()` from `whopClient.ts` for all server-side API calls in the user's app
- Do NOT import `whopClient.ts` in frontend code — it is server-only
- Do NOT duplicate product/plan catalogs in your database — query Whop
- Do NOT hardcode `api_key` in source — always fetch via `getWhopClient()`
- In Whop apps, use Whop account login: verify `x-whop-user-token` and check access with Whop
- For external apps, add a secure auth bridge before granting access
- Never grant paid access from a redirect, client-supplied email, membership ID, checkout ID, or `success` query param
- Store `company_id` in a Replit Configuration
- Store only minimal purchase/access mappings needed by the app; keep Whop as the source of truth
