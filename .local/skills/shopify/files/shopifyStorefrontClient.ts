// Shopify Store stores its connection_settings privately in `settings`
// (encrypted at rest, Admin tokens, refresh state, plus the public-facing
// Storefront fields `shop_domain` and `storefront_access_token`).
// `connection.public_settings` is intentionally empty for this connector;
// the OpenInt `public_settings` split was rolled back per OpenInt #1703.
//
// This helper fetches `items[0].settings` from
// `/api/v2/connection?include_secrets=true&...` using the standard Replit
// connector proxy auth (REPL_IDENTITY / WEB_REPL_RENEWAL on
// `X_REPLIT_TOKEN`) and extracts ONLY `shop_domain` and
// `storefront_access_token` for app code. Other private settings (Admin
// tokens, refresh state, transfer status, `shop_id`) are never read or
// cached, so unrelated secrets stay out of generated app surface area.
// Shopify treats the Storefront access token as a public buyer-facing
// credential, so it is safe to send to Shopify's Storefront API from
// server-side app code.
//
// The Storefront API version is pinned to a known-good Shopify release
// rather than read from the connection. Dev-store preview behavior
// (appending `channel=online_store` to checkout URLs for password-gated
// Vibe stores) is no longer auto-detected from the connection — pass
// `useDevStorePreview` to `shopifyStorefrontRequest` callers / cart
// helpers when the agent context indicates a dev/preview environment.
const STOREFRONT_API_VERSION = "2026-04";

type ShopifyConnectionSettings = {
  shop_domain?: string;
  storefront_access_token?: string;
};

type ShopifyConnectionResponse = {
  items?: Array<{
    settings?: ShopifyConnectionSettings;
  }>;
};

type ShopifyStorefrontConfig = {
  shopDomain: string;
  storefrontAccessToken: string;
};

const CONFIG_CACHE_TTL_MS = 60_000;
const FETCH_TIMEOUT_MS = 10_000;

let cachedConfig:
  | { value: ShopifyStorefrontConfig; expiresAt: number }
  | undefined;

function getOpenIntConnectionConfig() {
  const hostname = process.env.REPLIT_CONNECTORS_HOSTNAME;
  const token = process.env.REPL_IDENTITY
    ? `repl ${process.env.REPL_IDENTITY}`
    : process.env.WEB_REPL_RENEWAL
      ? `depl ${process.env.WEB_REPL_RENEWAL}`
      : null;

  if (!hostname || !token) {
    throw new Error("Missing Replit connector environment variables");
  }

  const protocol = hostname.startsWith("localhost") ? "http" : "https";
  const connectionUrl = new URL(`${protocol}://${hostname}/api/v2/connection`);
  connectionUrl.searchParams.set("include_secrets", "true");
  connectionUrl.searchParams.set("connector_names", "shopify-store");
  connectionUrl.searchParams.set("refresh_policy", "none");

  return {
    connectionUrl: connectionUrl.toString(),
    token,
  };
}

export async function getShopifyStorefrontConfig(
  options: { forceRefresh?: boolean } = {},
): Promise<ShopifyStorefrontConfig> {
  if (cachedConfig && !options.forceRefresh && Date.now() < cachedConfig.expiresAt) {
    return cachedConfig.value;
  }

  const { connectionUrl, token } = getOpenIntConnectionConfig();
  // `cache: "no-store"` opts out of framework-enhanced fetch caching
  // (Next.js SSR / server actions, edge runtimes). This helper's own
  // 60s TTL + the 401/403 forceRefresh path are the only intended cache
  // boundaries; framework caching would defeat the retry by handing
  // back a stale connection response after OpenInt remints the
  // Storefront token (e.g. during Go Live).
  const resp = await fetch(connectionUrl, {
    headers: {
      Accept: "application/json",
      X_REPLIT_TOKEN: token,
    },
    cache: "no-store",
    signal: AbortSignal.timeout(FETCH_TIMEOUT_MS),
  });

  if (!resp.ok) {
    throw new Error(`Failed to fetch Shopify connection: ${resp.status}`);
  }

  const data = (await resp.json()) as ShopifyConnectionResponse;
  const settings = data.items?.[0]?.settings;
  if (!settings?.shop_domain || !settings.storefront_access_token) {
    throw new Error(
      "Shopify Store integration is missing Storefront settings. Recreate the integration after OpenInt provisions a Storefront token.",
    );
  }

  // Extract only the Storefront-facing fields so unrelated private
  // settings (Admin tokens, refresh state, transfer status, shop_id)
  // never reach generated app code or the in-memory cache.
  cachedConfig = {
    value: {
      shopDomain: settings.shop_domain,
      storefrontAccessToken: settings.storefront_access_token,
    },
    expiresAt: Date.now() + CONFIG_CACHE_TTL_MS,
  };
  return cachedConfig.value;
}

export async function shopifyStorefrontRequest<T>(
  query: string,
  variables?: Record<string, unknown>,
  options: { retryOnUnauthorized?: boolean } = {},
): Promise<T> {
  const config = await getShopifyStorefrontConfig();

  const resp = await fetch(
    `https://${config.shopDomain}/api/${STOREFRONT_API_VERSION}/graphql.json`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Shopify-Storefront-Access-Token": config.storefrontAccessToken,
      },
      body: JSON.stringify({ query, variables }),
      signal: AbortSignal.timeout(FETCH_TIMEOUT_MS),
    },
  );

  if (
    options.retryOnUnauthorized !== false &&
    (resp.status === 401 || resp.status === 403)
  ) {
    cachedConfig = undefined;
    await getShopifyStorefrontConfig({ forceRefresh: true });
    return shopifyStorefrontRequest<T>(query, variables, {
      retryOnUnauthorized: false,
    });
  }

  const text = await resp.text();
  const json = text ? safeJsonParse(text) : {};
  if (!resp.ok || json.errors?.length) {
    throw new Error(
      `Shopify Storefront API error (${resp.status}): ${JSON.stringify(json.errors ?? json)}`,
    );
  }

  return json.data as T;
}

function safeJsonParse(text: string) {
  try {
    return JSON.parse(text);
  } catch {
    return { errors: [{ message: text }] };
  }
}
