const hostname = process.env.REPLIT_CONNECTORS_HOSTNAME;
const token = process.env.REPL_IDENTITY ? "repl " + process.env.REPL_IDENTITY
  : process.env.WEB_REPL_RENEWAL ? "depl " + process.env.WEB_REPL_RENEWAL : null;

const [method, path, bodyJson] = process.argv.slice(2);
if (!method || !path) {
  console.log("Usage: node whop-api.mjs <METHOD> <path> ['{...}']");
  console.log("  node whop-api.mjs GET  '/api/v1/products?company_id=biz_xxx'");
  console.log("  node whop-api.mjs POST /api/v1/plans '{\"company_id\":\"biz_xxx\",...}'");
  process.exit(0);
}

const resp = await fetch(`https://${hostname}/api/v2/proxy/${path}`, {
  method,
  headers: {
    "Content-Type": "application/json",
    "X-Replit-Token": token,
    "Connector-Name": "whop",
  },
  ...(bodyJson ? { body: bodyJson } : {}),
});
console.log(JSON.stringify(await resp.json(), null, 2));
