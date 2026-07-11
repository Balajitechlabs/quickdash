---
name: monetization
description: Master router for payments, checkout, subscriptions, paywalls, and monetization. Picks the right provider skill (Shopify, Stripe, Whop, or RevenueCat) before any provider-specific work begins. Read this BEFORE entering the shopify, stripe, whop, or revenuecat skills.
---

# Monetization

Pick the payment provider here, then hand off to skill `shopify`, `stripe`, `whop`, or `revenuecat`. Do not re-litigate the choice inside those skills.

Providers:

- **Shopify** — best for physical goods; handles inventory, shipping, and checkout out of the box.
- **Stripe** — most flexible; requires external Stripe-dashboard setup to go live.
- **Whop** — fastest; auto-provisioned store and checkout, no external dashboard.
- **RevenueCat** — native-mobile in-app purchases only (Expo / React Native / iOS). Not for plain web apps.

## Skip the routing question if any of these is true

The provider is already known — do not ask, just hand off to the matching provider skill.

- User explicitly named Shopify, Stripe, Whop, or RevenueCat. (Generic words — "payments", "subscriptions", "paywall", "monetize", "let users pay" — do NOT count.)
- A provider is already integrated: a `SHOPIFY_*` / `STRIPE_*` / `WHOP_*` / `REVENUECAT_*` env var or Replit Configuration is set.
- The integration for `shopify-store`, `stripe`, `whop`, or `revenuecat` is already connected.
- The conversation already established a provider.
- The user is asking for follow-up CRUD on an existing payment integration.
- The user's intent is clearly to sell **physical goods** (shipping-required items or inventory-based products) → Hand off to the `shopify` skill. Signals:
  - Explicit mention of physical/tangible items, inventory management, stock, shipping, or fulfillment.
  - A concrete tangible product noun, even when the user never says the word "physical". Treat nouns like pens, shirts, t-shirts, apparel, clothing, mugs, books, ceramics, pottery, jewelry, candles, soap, posters, prints, stickers, accessories, merch, or merchandise as physical-goods signals. "Build an ecommerce site for selling pens with checkout and payments" is a Shopify request — route to `shopify` and recommend the Shopify Store integration + Shopify-hosted checkout, NOT a custom Stripe/Express checkout.
  - **Digital/download exception (takes priority over the tangible-noun shortcut):** if the same item is sold as a digital file, download, print-on-demand digital asset, PDF, template, or other intangible deliverable — e.g. "downloadable poster prints", "printable sticker pack", "ebook" — it is NOT physical goods. A digital/download cue overrides the product noun: do not hand off to `shopify`. Route digital goods to Stripe (or ask the clarification below if it is genuinely unclear whether the item ships physically).
  - Generic words like "store", "shop", "storefront", "products", "checkout", or "ecommerce" with NO tangible product, inventory, shipping, or fulfillment cue are NOT sufficient — they apply equally to digital commerce. Ask the short clarification below instead of guessing.

## Otherwise, ask which provider to use

Pause any plan/task-list work and call the `AskQuestion` model tool:

```json
{
  "question": "What payment provider do you want to use?",
  "choices": [
    "Shopify (best for physical goods)",
    "Stripe (most flexible)",
    "Whop (fastest, no external setup)"
  ]
}
```

Add `"RevenueCat (best for iOS in-app payments, requires an account)"` as a fourth choice **only** if the app is native-mobile / Expo / React Native, or the user asked about IAP on iOS. Never offer RevenueCat for plain web apps.

Hand off to the picked provider's skill.
