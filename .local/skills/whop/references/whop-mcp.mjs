const hostname = process.env.REPLIT_CONNECTORS_HOSTNAME;
const token = process.env.REPL_IDENTITY ? "repl " + process.env.REPL_IDENTITY
  : process.env.WEB_REPL_RENEWAL ? "depl " + process.env.WEB_REPL_RENEWAL : null;

async function mcpCall(method, params = {}) {
  const resp = await fetch(`https://${hostname}/api/v2/proxy/mcp`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json, text/event-stream",
      "X-Replit-Token": token,
      "Connector-Name": "whop",
    },
    body: JSON.stringify({ jsonrpc: "2.0", id: 1, method, params }),
  });
  const text = await resp.text();
  const lines = text.split('\n').filter(l => l.startsWith('data:'));
  const envelope = JSON.parse(lines.at(-1).replace(/^data:\s*/, ''));
  const result = envelope.result;
  const textContent = result?.content?.[0]?.text;
  return textContent ? JSON.parse(textContent) : result;
}

const [cmd, argsJson] = process.argv.slice(2);
if (cmd === "--list-tools") {
  const { tools } = await mcpCall("tools/list");
  tools.forEach(t => console.log(t.name + " — " + t.description));
} else if (cmd === "--schema") {
  const { tools } = await mcpCall("tools/list");
  const tool = tools.find(t => t.name === argsJson);
  console.log(tool ? JSON.stringify(tool.inputSchema, null, 2) : `Tool "${argsJson}" not found`);
} else if (cmd) {
  const result = await mcpCall("tools/call", {
    name: cmd,
    arguments: JSON.parse(argsJson || "{}"),
  });
  console.log(JSON.stringify(result, null, 2));
} else {
  console.log("Usage: node whop-mcp.mjs --list-tools | --schema <tool> | <tool> '{...}'");
}
