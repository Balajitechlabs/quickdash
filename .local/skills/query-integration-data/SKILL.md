---
name: query-integration-data
description: Query and modify data in any connected integration (Linear, GitHub, HubSpot, Slack, Google services, etc.) or connected data warehouse (Databricks, Snowflake, BigQuery). Use listConnections() in the codeExecution sandbox to get credentials, then call APIs directly. Supports read operations (queries, counts, exports) and write operations (create, update, delete).
---

# Query Integration Data Skill

Connect to any Replit-supported integration to read or write data -- query issues, create tickets, send messages, update contacts, manage files, etc. This also includes querying supported data warehouse integrations like Databricks, Snowflake, and BigQuery.

All code runs inline in the `codeExecution` sandbox -- no script files needed.

## When to Use

Use this skill when the user asks you a **question in chat** that requires data from external services to answer, or when they need to perform data operations without building a visual interface.

- **Answer questions**: Query data to respond to user questions in the conversation (e.g., "How many issues were created this week?")
- **Fetch and export data**: Export data to CSV/JSON for later use or analysis
- **Write operations**: Create, update, delete, or modify data in a service
- **Ad-hoc queries**: One-time data lookups or investigations
- **Automate tasks**: Perform multi-step operations across the API

**Key point:** Use this skill when the output is an answer or data file, NOT when building a dashboard or visualization interface.

## When NOT to Use

- **The user wants to create a dashboard, visualization, or analytics interface** - use the `data-visualization` skill (it handles data fetching internally)
- **The user asks to "build", "create", or "make" a dashboard/app with data** - use the `data-visualization` skill
- The user needs to add an integration to their app (use the `integrations` skill)
- Production database operations (use the database pane directly)
- Asks to check deployment or server logs (use the `deployment` skill)

## File Structure

```text
.agents/
-- outputs/         # Generated artifacts (CSV, JSON, etc.)
```

Code runs inline in the `codeExecution` sandbox -- no script files are needed.

## Workflow

```text
1. SEARCH     -- searchIntegrations({ query }) to find the integration and read its status
   -- status added     -- EXECUTE -- OUTPUT
   -- status not_added
      or not_setup     -- CONNECT (via `integrations` skill) -- EXECUTE -- OUTPUT
2. EXECUTE    -- listConnections(connectorName) inside "use impure" for credentials, then call the API
```

- **SEARCH**: Call `searchIntegrations({ query })` (see "Calling `searchIntegrations`" below for the exact params and result shape). Its `status` field is the source of truth for whether a connection needs setup -- do NOT use `listConnections().length` to decide this (see "Deciding whether setup is needed" below).
  - `added` -- a connection is already bound to this Repl. Skip straight to EXECUTE.
  - `not_added` -- authorized at the account level but not bound to this Repl. Bind it via the `integrations` skill, then EXECUTE.
  - `not_setup` -- not authorized yet. Set it up via the `integrations` skill (`ProposeIntegration` drives OAuth), then EXECUTE.
- **CONNECT** (only when status is `not_setup` or `not_added`): Use `searchIntegrations`, `ProposeIntegration`, and `addIntegration` to set up the connection. See the `integrations` skill for the full lifecycle details.
- **EXECUTE**: This is the **only** step that calls `listConnections`, and only call it because you are about to use the credentials. Call `listConnections(connectorName)` inside a `"use impure"` function to fetch the `settings`, build your client, and run the API call in the same `codeExecution` block. Never call `listConnections` to "check" or "probe" a connection -- if you are not about to use the credentials, do not call it.
- **OUTPUT**: Return the answer or confirmation to the user.

> **Call `listConnections` only when you need the credentials.** It is a credential-fetch call, not a status check. The connect/no-connect decision belongs to `searchIntegrations` `status`; `listConnections` is for the EXECUTE step alone, when you are about to make the API call.

## Deciding whether setup is needed

Use `searchIntegrations({ query }).integrations[].status`, NOT `listConnections`, to decide whether a connection needs to be set up:

- `status: 'added'` -- bound to this Repl, ready to use. Go to EXECUTE.
- `status: 'not_added'` -- authorized at the account level but not bound here. Bind via the `integrations` skill.
- `status: 'not_setup'` -- not authorized. Drive OAuth via the `integrations` skill.

Do **not** branch on `listConnections(connectorName).length === 0` for this decision. `listConnections` exists to hand credentials to your sandbox code, and it drops any connection whose credentials are withheld (e.g. credential-blocked account contexts) -- so it can return an empty array even when an account-level connection genuinely exists. That makes it an unreliable presence check and would push you into a needless re-connect prompt. `searchIntegrations`'s `status` does not have that blind spot.

## Calling `searchIntegrations`

`searchIntegrations` takes exactly one argument -- an object with a single `query` field -- and takes no other params:

```ts
searchIntegrations({ query: string }): Promise<{
  integrations: Array<{
    id: string;            // "connector:<id>" or "connection:<id>"
    integrationType: 'connector' | 'connection';
    displayName: string;   // human-readable, e.g. "Google Sheets"
    description: string;
    status: 'added' | 'not_added' | 'not_setup';
  }>;
  askForBlueprintConfirmation: boolean;
}>
```

How to build the `query` so a match is found reliably:

- `query` is a **natural-language description of what you need**. The callback fetches the available connectors and connections, then uses an LLM classifier to pick the ones that semantically match your `query` -- it is **not** a literal substring or slug match, so `"spreadsheet"` can match `Google Sheets` and `"issue tracker"` can match `Linear`.
- Describe the capability or product in plain terms (`"Google Sheets"`, `"payments"`, `"issue tracker"`). You do not need the exact `displayName` or the connector slug.
- `query: ''` (empty string) skips the classifier and returns **every** available connector and connection (capped at 50). Use this to enumerate what is available when you are unsure what to ask for, then pick the right entry from `integrations` by `displayName`.
- If a specific `query` returns no matching entry, retry with `query: ''` and scan the full list rather than assuming the integration does not exist.

Read `status` (not array length) to decide setup, and read `id` to feed the other integration callbacks (`viewIntegration`, `addIntegration`) and the `ProposeIntegration` model tool.

**The result does not contain the connector slug** that `listConnections` needs. `searchIntegrations` exposes only `id` / `displayName` / `description` / `status` -- not the connector `name`/slug. Obtain the slug separately (see "Resolving the connector slug" below); do not assume `displayName` lowercased is the slug.

## Getting Connection Credentials

### `listConnections(connectorName)` -- call only when you need the credentials

`listConnections` is a credential-fetch call, not a status check. Call it **only** inside a `"use impure"` function when `searchIntegrations` already reports the connection as `added` **and** you are about to use the credentials in the same `codeExecution` block. Do not call it to test whether a connection exists, to "check" status, or speculatively before you actually need to hit the API -- that decision is `searchIntegrations`'s job. The credentials stay inside the sandbox and never enter the model context.

Fetch the credentials and use them in the same block -- never as a standalone "is it connected?" probe:

```javascript
const result = await (async function() {
  "use impure";
  const conns = await listConnections('linear');
  const client = await conns[0].getClient();   // initialized SDK client — no import, no raw token handling
  return { connectionId: conns[0].id, clientReady: Boolean(client) };
})();
console.log(result);
```

Each connection object has:

- `id`, `connectorConfigId`, `status`, `displayName`, `metadata`, `environment`
- `settings` -- credentials dict (access tokens, API keys, etc.)
- `getClient()` -- returns an initialized SDK client for supported connectors

Credential fields are available by direct property access, e.g. `conn.settings.access_token` or `conn.settings.api_key`. You may inspect `Object.keys(conn.settings)` to see which credential fields exist, but do **not** log, return, spread, or serialize the whole `settings` object. Use the credential value only to construct the API client or request headers, and return safe metadata/results instead.

#### Resolving the connector slug

`listConnections(connectorName)` is keyed on the **exact connector slug** (e.g. `google-sheet`, `linear`, `stripe`), filtered server-side. The slug is lowercase and often hyphenated, and it is **not** the `displayName` from `searchIntegrations` (`Google Sheets`) nor a reliable lowercasing of it. Passing the wrong slug returns `[]` -- the same shape as "no connection" -- so resolve the slug before concluding anything from an empty result:

- `viewIntegration` is the source of truth for the slug. Call `viewIntegration({ integrationId })` with the `id` from `searchIntegrations`; its rendered markdown spells out the slug as **Name:** `<slug>` (for a connector) or **Connector:** `<slug>` (for an existing connection). `addIntegration` binds that same connector name. Pass that exact value to `listConnections` -- do not guess it by lowercasing the `displayName`.

If `listConnections` returns `[]`, work through these in order before giving up:

1. **Wrong slug** -- the most common cause. Confirm the slug against `viewIntegration`/`addIntegration` output and retry with the correct value.
2. **Withheld credentials** -- only after the slug is confirmed correct and `searchIntegrations` reported the integration as `added`. A persistent empty result then means the credentials are being withheld for this context (e.g. a credential-blocked account) rather than missing. Surface that to the user instead of looping back into a connect proposal.

### Browse the Documentation

**Always browse `public_documentation_link`** before writing code, especially for write operations. This helps you understand:

- Required vs optional fields for creating resources
- Valid values for enums (status, priority, type, etc.)
- Relationships between resources (e.g., issues belong to projects)
- Rate limits and best practices

## Clarifying Questions

**Before write operations, gather required information.** Many APIs require IDs or specific values that the user may not know.

### When to Ask

Ask clarifying questions when the user's request requires:

- **Entity selection**: "Which project should this issue be created in?"
- **User assignment**: "Who should be assigned? Let me list the team members..."
- **Required fields**: "What priority - urgent, high, medium, or low?"
- **Ambiguous references**: "I found 3 projects matching 'backend'. Which one?"

### Pattern: Fetch Options First

For write operations, often run a read query first to get valid options:

```javascript
// User says: "Create a Linear ticket assigned to John"
// Problem: Need John's user ID, not just name

const result = await (async function() {
  "use impure";
  const conns = await listConnections('linear');
  const client = await conns[0].getClient();   // returns an initialized LinearClient — no import needed
  // Step 1: List users to find John's ID
  const users = await client.users();
  const matches = users.nodes
    .filter(u => u.name.includes('John'))
    .map(u => ({ id: u.id, name: u.name }));
  return { matches };
})();
console.log(result);

// Step 2: If ambiguous, ASK the user
// "I found John Smith and John Doe. Which one?"

// Step 3: After the user chooses, pass the selected JSON ID into a new
// "use impure" function that creates the issue.
```

### Common Multi-Step Patterns

| Action                     | First fetch...            |
| -------------------------- | ------------------------- |
| Create issue with assignee | List team members         |
| Create issue in project    | List projects             |
| Set status/priority        | Get valid workflow states |
| Add to channel             | List channels             |
| Assign to team             | List teams                |

## Running Code in the Sandbox

All connector credential work runs inside `"use impure"` functions in the `codeExecution` sandbox. Keep clients and credentials inside that function, and return only JSON-serializable results.

### Read Operations

Query data and return results:

```javascript
const result = await (async function() {
  "use impure";
  const conns = await listConnections('linear');
  const client = await conns[0].getClient();

  const issues = await client.issues({ first: 10 });
  return issues.nodes.map(issue => ({
    identifier: issue.identifier,
    title: issue.title,
    state: issue.state?.name ?? null,
  }));
})();
console.log(result);
```

### Write Operations

Create, update, or delete data:

```javascript
const result = await (async function(teamId, issueId, doneStateId) {
  "use impure";
  const conns = await listConnections('linear');
  const client = await conns[0].getClient();

  const created = await client.createIssue({ teamId, title: "Fix login bug" });
  await client.updateIssue(issueId, { stateId: doneStateId });
  await client.deleteIssue(issueId);

  return { created: created.issue?.identifier ?? null, updated: issueId, deleted: issueId };
})(team.id, issueId, doneState.id);
console.log(result);
```

### Multi-Step Operations

Keep clients and credentials inside `"use impure"` calls. Return JSON IDs or summaries, then pass those JSON values into the next impure function when another API call is needed:

```javascript
// Call 1: Get credentials and list teams
const result = await (async function() {
  "use impure";
  const conns = await listConnections('linear');
  const client = await conns[0].getClient();

  const teams = await client.teams();
  const team = teams.nodes[0];
  const users = await client.users();
  const assignee = users.nodes.find(u => u.name === 'John');

  return {
    team: { id: team.id, name: team.name },
    assignee: assignee ? { id: assignee.id, name: assignee.name } : null,
  };
})();
console.log(result);
```

```javascript
// Call 2: copy literal IDs from Call 1's output into a new impure function
const selectedTeamId = 'team-id-from-call-1-output';
const selectedAssigneeId = 'assignee-id-from-call-1-output';

const created = await (async function(teamId, assigneeId) {
  "use impure";
  const conns = await listConnections('linear');
  const client = await conns[0].getClient();

  const issue = await client.createIssue({
    teamId,
    ...(assigneeId === null ? {} : { assigneeId }),
    title: 'New feature request',
    description: 'Details here...',
  });

  return { identifier: issue.issue?.identifier ?? null, title: 'New feature request' };
})(selectedTeamId, selectedAssigneeId);
console.log(created);
```

## Databricks

When the user wants to connect to Databricks, use the `databricks-m2m` connector (not the plain `databricks` connector).

## Warehouse Data Exploration

When querying data warehouses (BigQuery, Snowflake, Databricks), large schemas can make serial exploration slow (7-10s per query round-trip). Use the parallel subagent pattern to explore schemas faster.

### CRITICAL: Warehouse queries are billed per byte scanned

Warehouse targets (`bigquery`, `databricks`, `snowflake`) cost real money per query -- a
careless `SELECT *` over a fact table can cost tens of dollars and a dashboard that
re-runs queries every few seconds can burn thousands of dollars per day. Before writing
any warehouse query (via `executeSql` or `executeSql({target: "bigquery"})`) follow
these rules:

- **Project exact columns** -- never `SELECT *` on wide tables.
- **Always `LIMIT`** when exploring; `LIMIT 5`--`LIMIT 100` is plenty for a sample.
- **Scope by partition / cluster** -- add `WHERE event_date >= ...` (or the table's
  clustering column) so the warehouse prunes data and you are billed for a tiny slice.
- **Prefer pre-aggregated tables** (e.g. `_daily`, `_summary`) over raw event tables.
- **Diff-only reads** for anything incremental -- `WHERE updated_at > :last_seen` and
  persist `last_seen` so subsequent queries only scan the delta.
- **Cache repeated results locally** -- if you run the same exploration query twice in
  the same session, store the first result in a variable or `.agents/outputs/*.csv`
  and reuse it instead of re-running the query.
- **Don't power a data app from this skill** -- if the user is building a dashboard,
  report, or explorer, hand off to the `data-visualization` skill so the app's API
  server can cache results (15-min TTL). The `query-integration-data` skill is for
  answering questions in chat, not for powering a live UI.

### When to Use Parallel Exploration

Use this pattern when ALL of the following are true:

- The target is a **warehouse** connection (`bigquery`, `snowflake`, or `databricks`)
- The initial INFORMATION_SCHEMA query returns **15+ tables**
- The user's question is **not about a specific known table** (e.g., they're asking a broad question like "what's our revenue trend?" or "show me customer data")

If the schema has fewer than 15 tables, serial exploration is fast enough -- just query tables one-by-one.

### 4-Step Parallel Workflow

**Step 1: Schema Discovery** -- Run a single `executeSql` call to get the full table list.

```javascript
// BigQuery
const tables = await executeSql({ sqlQuery: `SELECT table_schema, table_name, row_count FROM \`project.region-us\`.INFORMATION_SCHEMA.TABLES WHERE table_schema NOT IN ('INFORMATION_SCHEMA') ORDER BY table_schema, table_name`, target: "bigquery" });

// Snowflake
const tables = await executeSql({ sqlQuery: `SELECT TABLE_SCHEMA, TABLE_NAME, ROW_COUNT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA NOT IN ('INFORMATION_SCHEMA') ORDER BY TABLE_SCHEMA, TABLE_NAME`, target: "snowflake" });

// Databricks
const tables = await executeSql({ sqlQuery: `SELECT table_schema, table_name FROM information_schema.tables WHERE table_schema NOT IN ('information_schema') ORDER BY table_schema, table_name`, target: "databricks" });
```

`INFORMATION_SCHEMA.TABLES` is a metadata read and is essentially free. Sampling rows
from the tables it returns is **not** free -- see the next step.

**Step 2: Group Tables** -- Partition the table list into 2-4 clusters:

- By schema/dataset name (e.g., `analytics.*`, `sales.*`, `marketing.*`)
- By name prefix (e.g., `dim_*`, `fact_*`, `stg_*`)
- By estimated relevance to the user's question (most-likely-relevant tables first)

**Step 3: Launch Parallel Subagents** -- Start one general subagent per group: (see delegation skill)

```javascript
const exploreWarehouseTask = `Explore these warehouse tables to answer: "${userQuestion}"

Connection: Use executeSql({ sqlQuery: "...", target: "bigquery" }) -- always pass target.
Dialect: BigQuery (use backtick quoting for project.dataset.table)

Tables to explore:
- analytics.events
- analytics.sessions
- analytics.conversions

For each table:
1. Run: SELECT column_name, data_type, is_partitioning_column FROM \`project.dataset\`.INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'TABLE_NAME'
2. Identify the partition / cluster column from step 1 (e.g. event_date, _PARTITIONTIME).
3. Sample rows -- project exact columns, scope by partition, and LIMIT.
   NEVER \`SELECT *\` on a fact table on a billed warehouse -- \`LIMIT 5\` does not
   undo the bytes scanned by selecting every column.
   Pattern: SELECT <subset of columns from step 1>
            FROM \`project.dataset.TABLE_NAME\`
            WHERE <partition_col> >= CURRENT_DATE() - 7
            LIMIT 5

Return your findings in this exact format:

## Table Relevance
| Table | Relevant? | Why |
|-------|-----------|-----|
| ... | Yes/No | Brief reason |

## Column Details (relevant tables only)
| Table | Column | Type | Notes |
|-------|--------|------|-------|
| ... | ... | ... | Key field, foreign key, metric, etc. |

## Suggested Join Conditions
- table_a.id = table_b.a_id (describe relationship)

## Key Findings
- Bullet points about data patterns, date ranges, notable values`;
const group1 = subagent({
  name: "explore-warehouse",
  task: exploreWarehouseTask,
  config: { $kind: "general", relevantFiles: ['.local/skills/database/SKILL.md'] },
});

// Launch additional subagents for other groups (2-4 total)
const group2 = subagent({ /* same pattern, different tables, config: { $kind: "general" } */ });
const groupResults = await Promise.all([group1, group2]);
for (const groupResult of groupResults) {
  console.log(groupResult.text);
}
```

**Step 4: Synthesize and Query** -- Await the schema-discovery subagents. Once they complete, read their outputs, combine the relevant tables/columns, and write the final SQL query.

### Dialect-Specific Notes

| Dialect | Table Quoting | INFORMATION_SCHEMA Path | Sample Query (project columns + partition + LIMIT) |
|---------|--------------|------------------------|------------------------------------------------------|
| BigQuery | `` `project.dataset.table` `` | `` `project.dataset`.INFORMATION_SCHEMA.COLUMNS `` | `` SELECT col_a, col_b FROM `p.d.t` WHERE event_date >= CURRENT_DATE() - 7 LIMIT 5 `` |
| Snowflake | `"DATABASE"."SCHEMA"."TABLE"` | `DATABASE.INFORMATION_SCHEMA.COLUMNS` | `SELECT col_a, col_b FROM "DB"."SCH"."TBL" WHERE event_date >= DATEADD(day, -7, CURRENT_DATE) LIMIT 5` |
| Databricks | `` `catalog.schema.table` `` | `catalog.information_schema.columns` | `` SELECT col_a, col_b FROM `c.s.t` WHERE event_date >= current_date() - INTERVAL 7 DAY LIMIT 5 `` |

**Do NOT** use `SELECT *` in the sample query column above. `LIMIT 5` does not undo
the cost of reading every column for the matched rows on a billed warehouse.

### Tips

- Each subagent should run 3-6 SQL queries (column metadata + sample data per table)
- Keep subagent count to 2-4 -- more than 4 has diminishing returns
- The structured markdown output format ensures consistent, scannable results
- After synthesis, write a single well-commented SQL query that answers the user's question

## Output Guidelines

- **Simple answers** (counts, scalar values, short lists of < 20 records): Print directly with `console.log()`
- **Complex results** (tabular results): Write to `.agents/outputs/<filename>.csv` and summarize
- **Write confirmations**: Print what was created/updated/deleted with IDs
- **Errors**: Print clear error messages

```javascript
const fs = await import('node:fs');   // Node built-in — always available, no install needed

// Simple
console.log(`Answer: 42 issues created this week`);

// Tabular results - write CSV to .agents/outputs/
fs.mkdirSync('.agents/outputs', { recursive: true });
fs.writeFileSync('.agents/outputs/results.csv', csvContent);
console.log(`Exported 500 records to .agents/outputs/results.csv`);

// Write
console.log(`Created issue ENG-123: "Fix login bug"`);
console.log(`Updated 5 contacts`);
console.log(`Deleted message ID abc123`);
```

## Key Points

- **Decide setup via `searchIntegrations` `status`** (`added` / `not_added` / `not_setup`), not `listConnections().length`
- **Call `listConnections(connectorName)` only inside `"use impure"` when you need the credentials** -- it is a credential fetch for the EXECUTE step, never a status check or probe
- **Search -- propose -- add** when status is `not_setup` or `not_added` (see `integrations` skill)
- **All code runs in the `codeExecution` sandbox** -- no script files needed
- **Use `console.log()`** to see output -- functions execute silently without it (but never log credentials)
- **Prefer `conn.getClient()`** over importing an SDK — it returns an initialized client with no `await import` and no raw token handling.
- **State persists** across `codeExecution` calls -- reuse `conns`, clients, and extracted credentials instead of re-fetching (unless expired).
- **Browse `public_documentation_link`** to understand the API before coding
- **Ask clarifying questions** before write operations that need specific IDs or values
- **Fetch options first** when the user references something by name (users, projects, etc.)
- **Don't cache clients** -- access tokens expire; re-create clients from `listConnections` when needed
- **Write large outputs to `.agents/outputs/`** as CSV or JSON
