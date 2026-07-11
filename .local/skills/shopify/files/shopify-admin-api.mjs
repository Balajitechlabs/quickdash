const hostname = process.env.REPLIT_CONNECTORS_HOSTNAME;
const token = process.env.REPL_IDENTITY
  ? `repl ${process.env.REPL_IDENTITY}`
  : process.env.WEB_REPL_RENEWAL
    ? `depl ${process.env.WEB_REPL_RENEWAL}`
    : null;

const [bodyJson, pathArg] = process.argv.slice(2);
const path = pathArg || "/admin/api/2026-04/graphql.json";

if (!hostname || !token) {
  console.log("Missing Replit connector environment. Run inside the Repl shell.");
  process.exit(1);
}

if (!bodyJson) {
  console.log("Usage: node shopify-admin-api.mjs '{\"query\":\"query { shop { name } }\"}' [path]");
  process.exit(0);
}

const protocol = hostname.startsWith("localhost") ? "http" : "https";

const resp = await fetch(`${protocol}://${hostname}/api/v2/proxy${path}`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    "X-Replit-Token": token,
    "Connector-Name": "shopify-store",
  },
  body: bodyJson,
  signal: AbortSignal.timeout(30_000),
});

const text = await resp.text();
let parsed = null;
try {
  parsed = JSON.parse(text);
  console.log(JSON.stringify(parsed, null, 2));
} catch {
  console.log(text);
}

// Shopify Admin GraphQL returns HTTP 200 even when the response body has
// top-level `errors`, and mutation validation failures show up in nested
// `userErrors`. Fail loudly so chained setup scripts (`node ... && ...`)
// stop on those instead of treating a failed Admin call as success.
function hasUserErrors(node) {
  if (!node || typeof node !== "object") return false;
  if (Array.isArray(node)) return node.some(hasUserErrors);
  if (Array.isArray(node.userErrors) && node.userErrors.length > 0) return true;
  for (const value of Object.values(node)) {
    if (hasUserErrors(value)) return true;
  }
  return false;
}

const hasTopLevelErrors =
  parsed && Array.isArray(parsed.errors) && parsed.errors.length > 0;
const hasMutationUserErrors = parsed && hasUserErrors(parsed.data);

if (!resp.ok || hasTopLevelErrors || hasMutationUserErrors) {
  process.exitCode = 1;
}
