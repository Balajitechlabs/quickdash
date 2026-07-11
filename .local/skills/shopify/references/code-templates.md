# Shopify Code Templates

These are reference snippets. Adapt them to the app's routing, framework, and state patterns.

Admin setup scripts are orchestration only and should not run during integration onboarding. Only create products/inventory after the user explicitly asks for catalog setup. Scripts should resolve publication and location IDs dynamically, check every mutation's `userErrors`, and write only stable mapping data (handles, product IDs, variant IDs) after Shopify confirms success. Every Admin API call must go through the OpenInt proxy helper; do not call Shopify Admin endpoints directly or handle Admin tokens in generated app code.

## Create, price, stock, and publish a product

Use `shopify-admin-api.mjs` through the OpenInt proxy.

Do not replace this helper with a direct `https://{shop_domain}/admin/...` request, Shopify Admin SDK client, or custom OAuth/token flow. OpenInt owns Shopify Admin authentication.

```bash
node shopify-admin-api.mjs '{
  "query": "mutation ProductCreate($product: ProductCreateInput!) { productCreate(product: $product) { product { id title handle } userErrors { field message } } }",
  "variables": {
    "product": {
      "title": "Example T-shirt",
      "descriptionHtml": "<p>Soft cotton shirt.</p>",
      "status": "ACTIVE"
    }
  }
}'
```

After product creation, query variants and inventory item IDs. The variant ID is the cart `merchandiseId`; the inventory item ID is needed to set stock:

```graphql
query ProductVariants($productId: ID!) {
  product(id: $productId) {
    variants(first: 20) {
      nodes {
        id
        title
        price
        availableForSale
        inventoryItem { id }
      }
    }
  }
}
```

Set price and inventory before publishing. Use `productVariantsBulkUpdate` for prices:

```graphql
mutation ProductVariantsBulkUpdate($productId: ID!, $variants: [ProductVariantsBulkInput!]!) {
  productVariantsBulkUpdate(productId: $productId, variants: $variants) {
    productVariants { id price }
    userErrors { field message }
  }
}
```

For inventory, run three steps in order before any quantity write — newly created products are not tracked or activated at a location by default, so `inventorySetQuantities` against an inert inventory item silently leaves storefront stock unenforced or returns `userErrors`.

1. Enable tracking on the inventory item so quantity changes have effect:

```graphql
mutation InventoryItemUpdate($id: ID!, $input: InventoryItemInput!) {
  inventoryItemUpdate(id: $id, input: $input) {
    inventoryItem { id tracked }
    userErrors { field message }
  }
}
```

Variables (one call per inventory item):

```json
{ "id": "<inventoryItem.id>", "input": { "tracked": true } }
```

1. Activate the inventory item at the resolved location so a stock level exists there:

```graphql
mutation InventoryActivate($inventoryItemId: ID!, $locationId: ID!) {
  inventoryActivate(inventoryItemId: $inventoryItemId, locationId: $locationId) {
    inventoryLevel { id }
    userErrors { field message }
  }
}
```

1. Set the actual quantity:

```graphql
mutation InventorySetQuantities($input: InventorySetQuantitiesInput!) {
  inventorySetQuantities(input: $input) {
    inventoryAdjustmentGroup { id }
    userErrors { field message }
  }
}
```

Always inspect `userErrors` on each of these and stop on the first non-empty array; the activation step in particular can fail if `read_locations` / `write_inventory` scopes are missing or the location ID does not belong to the store.

Resolve the publication ID at runtime by querying Shopify; the connection payload does not expose one. Prefer the Replit-owned Sales Channel publication when present, and only fall back to a shop publication such as `Online Store` for Replit-created Vibe/dev stores or after explicit user confirmation on live stores:

```graphql
query Publications {
  publications(first: 20) {
    nodes { id name }
  }
}
```

Then publish with `publishablePublish`, after price and inventory are configured:

```graphql
mutation Publish($productId: ID!, $publicationId: ID!) {
  publishablePublish(id: $productId, input: [{ publicationId: $publicationId }]) {
    publishable { publishedOnPublication(publicationId: $publicationId) }
    userErrors { field message }
  }
}
```

If no suitable publication exists, skip publishing and tell the user the Shopify Sales Channel publication is not yet provisioned for this connection.

Resolve inventory location dynamically; do not hardcode location IDs:

```graphql
query Locations {
  locations(first: 1) {
    nodes { id name }
  }
}
```

## Query products in app code

The helper fetches the Shopify Store connection from OpenInt with `include_secrets=true`, extracts only `shop_domain` and `storefront_access_token` from `items[0].settings`, pins the Storefront API version in code, and calls Shopify Storefront API directly with the connector-provided Storefront token. Keep this helper server-side behind API routes, server actions, SSR-only modules, or setup scripts; browser UI should call your server boundary. App code should not ask the user to paste tokens or route buyer Storefront traffic through OpenInt by default.

```ts
import { shopifyStorefrontRequest } from "./shopifyStorefrontClient";

const PRODUCTS_QUERY = `#graphql
  query Products {
    products(first: 12) {
      nodes {
        id
        title
        handle
        featuredImage { url altText }
        priceRange { minVariantPrice { amount currencyCode } }
        variants(first: 10) { nodes { id title availableForSale } }
      }
    }
  }
`;

export async function getProducts() {
  return shopifyStorefrontRequest<{
    products: { nodes: Array<unknown> };
  }>(PRODUCTS_QUERY);
}
```

## Create a cart and redirect to Shopify checkout

The `variantId` below is the Shopify ProductVariant GID from the Storefront product query (`products.nodes[].variants.nodes[].id`) or from Admin GraphQL after creating the product. It is the value Shopify expects as `merchandiseId`.

```ts
import { shopifyStorefrontRequest } from "./shopifyStorefrontClient";

const CART_CREATE = `#graphql
  mutation CartCreate($variantId: ID!, $quantity: Int!) {
    cartCreate(input: { lines: [{ merchandiseId: $variantId, quantity: $quantity }] }) {
      cart { id checkoutUrl }
      userErrors { field message }
    }
  }
`;

export async function createCheckoutUrl(
  variantId: string,
  quantity = 1,
  // `useDevStorePreview` is caller-controlled. The Shopify Store connection
  // does not expose a dev-store flag; pass true when running against a
  // Vibe/dev preview environment and false (default) for live storefronts.
  options: { useDevStorePreview?: boolean } = {},
) {
  const data = await shopifyStorefrontRequest<{
    cartCreate: {
      cart: { checkoutUrl: string } | null;
      userErrors: Array<{ message: string }>;
    };
  }>(CART_CREATE, { variantId, quantity });

  const error = data.cartCreate.userErrors[0];
  if (error) {
    throw new Error(error.message);
  }

  if (!data.cartCreate.cart?.checkoutUrl) {
    throw new Error("Shopify did not return a checkout URL");
  }

  const checkoutUrl = new URL(data.cartCreate.cart.checkoutUrl);
  if (options.useDevStorePreview) {
    // Vibe-created development stores are password protected. Shopify's Vibe
    // docs recommend this preview parameter so checkout links work before the
    // merchant claims/transfers the store. Disable this for live stores.
    checkoutUrl.searchParams.set("channel", "online_store");
  }

  return checkoutUrl.toString();
}
```

Do not replace Storefront cart checkout with Admin API draft orders. Admin API is for setup/product writes; buyers should use Storefront cart + Shopify-hosted checkout.

If opening `checkoutUrl` hits the dev-store password page, keep the real cart and add `channel=online_store`. If `cartCreate` itself returns 401/403, refetch the Shopify Store connection settings once and retry with the direct Shopify Storefront API helper above; do not ask the user to paste a token.

## Go Live behavior

Do not bake transfer or launch steps into generated storefront code. Go Live is an integration-management workflow:

- The merchant explicitly clicks/asks for Go Live.
- The connection payload does not expose a claim link. Point the merchant to Replit's Shopify connection management page at the path `/integrations/shopify_store/apps/<REPL_ID>` (substitute the current Repl's `REPL_ID`) to initiate the transfer, presented as a clear call-to-action link that opens in a new tab rather than a bare URL — use an HTML anchor with `target="_blank"` and the path as `href`, e.g. `<a href="/integrations/shopify_store/apps/<REPL_ID>" target="_blank" rel="noopener noreferrer">Open your Shopify connection settings →</a>`. Resolve against the current Replit home origin — do NOT hardcode `https://replit.com` (the host is `localhost` in dev, `replit-staging` on staging, `replit.com` in prod). Do not have generated app code call an OpenInt connector RPC directly to mint a claim link. Surface `pending_transfer` status from the connection if useful.
- If the merchant provides first and last name, the Go Live surface can pass them along with the transfer request; otherwise Replit/OpenInt may derive a minimal display name from the email for Shopify's transfer API. Do not collect or persist raw Shopify credentials in generated app code.
- The generated app keeps reading the same Shopify connection. Dev-store preview behavior (`channel=online_store`) is caller-controlled — toggle it off once the app is targeting the live storefront.
- If Shopify Admin actions start returning reauthorization-required status after transfer, ask the merchant to reconnect Shopify rather than creating a new connection.
- Taxes, payments KYC, plan selection, and storefront password removal stay merchant-owned actions in Shopify.
