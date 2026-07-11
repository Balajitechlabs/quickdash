# Whop Route Notes

This file is a fallback reference, not a drop-in implementation. Prefer the
project's existing server, routing, auth, and database patterns. If a snippet
doesn't compile in the target app, adapt it instead of preserving the snippet.

## Client Setup

Write `whopClient.ts` to the API server directory from `./references/whop-client.ts`.
It provides `getWhopClient()` and lazily fetches credentials from the Replit
connection API.

Install: `pnpm add @whop/sdk`

Most SDK methods require `company_id`; read it from `WHOP_COMPANY_ID`.

## Access Control

For Whop apps, Whop account login is the auth source:

```ts
const { userId } = await whopsdk.verifyUserToken(req.headers);
const access = await whopsdk.users.checkAccess(resourceId, { id: userId });
if (!access.has_access) {
  return res.status(403).json({ error: 'Access denied' });
}
```

Use the correct `resourceId` for the thing being protected:

- `prod_xxx` for a product
- `exp_xxx` for an experience
- `biz_xxx` for company-level access

Never unlock paid features from a redirect, client-provided email, client-provided
membership ID, checkout ID, or `success` query parameter.

## Checkout

Use Whop's hosted checkout. The app may create a checkout configuration and
redirect to `purchase_url`, but access is granted only after Whop verifies the
current user's account access or a server-verified membership/payment.

## List Payments

List payments for a checkout configuration (useful for verifying purchases):

```ts
const client = await getWhopClient();
const payments = await client.payments.list({
  company_id: process.env.WHOP_COMPANY_ID,
  checkout_configuration_ids: [checkoutConfigId],
});
```

REST equivalent via whop-api.mjs:

```bash
node whop-api.mjs GET '/api/v1/payments?company_id=biz_xxx&checkout_configuration_ids[]=ch_xxx'
```

## Manage Subscription

For cancel/modify actions, look up the membership server-side for the verified
Whop user or authenticated app user. Never accept a membership ID from the
client as authority to cancel or modify a subscription.
