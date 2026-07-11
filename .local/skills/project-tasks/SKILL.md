---
name: project-tasks
description: Create and manage persistent project tasks visible to the user.
---

# Project Tasks

Manage persistent, user-visible project tasks that the main agent or a task agent can execute. Only task agents run in isolated environments. Use these to track high-level deliverables and milestones that the user cares about.

## Project Tasks vs Internal Task List

| Aspect | Project Tasks | Internal Task List |
|--------|--------------|-------------------|
| Purpose | User-visible deliverables | Agent's own work breakdown |
| Persistence | Persistent across sessions (PID2) | Current session only |
| Visibility | Shown to the user | Internal to the agent |
| Granularity | High-level milestones | Detailed implementation steps |

## When to Use

- Tracking user-requested features or deliverables
- Breaking a project into visible milestones
- Communicating progress to the user
- Managing tasks that persist across sessions

## When NOT to Use

- Internal agent work breakdown (use the internal task list)
- Temporary implementation steps
- Sub-tasks that only matter to the agent

## Task Identifiers

Tasks are identified by `taskRef` -- a short string like `"#1"`, `"#2"`, `"#3"`. Use it in all API calls and when referring to tasks in conversation: "Task #1 (Add authentication)".

## Available Functions

### getProjectTask(taskRef)

Get a project task by task ref.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `taskRef` | str | Yes | Task ref to retrieve |

**Returns:** `{ task }` — a dict with `taskRef`, `title`, `description`, `state`, `displayState`, `dependsOn`, `artifactKinds`, `createdAt`, `updatedAt`

**Example:**

```javascript
const { task } = await getProjectTask({ taskRef: "#1" });
console.log(`${task.taskRef} (${task.title})\n${task.description}`);
```

### surfaceProjectTasks(taskRefs)

Surface one or more existing tasks to the user in the conversation feed -- use this to draw their attention to a task that already exists (for example, an in-flight task that already covers what they just asked for). Read-only: it does not create, accept, or modify tasks.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `taskRefs` | array of str | Yes | Refs of existing tasks to surface (`"#1"` or `"123"`). At most 10 per call. |

**Returns:** `{ surfaced }` -- a list of sanitized task summaries

**Example:**

```javascript
// The user asked for something an existing task already covers -- point them at it.
await surfaceProjectTasks({
  taskRefs: ["#2"],
});
```

### updateProjectTask(taskRef, title=None, description=None, filePath=None, dependsOn=None, artifactKinds=None)

Update an existing project task's content. Only provided fields are updated. Editing a plan file alone does NOT update the task -- you must call `updateProjectTask` to persist changes.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `taskRef` | str | Yes | Task ref to update |
| `title` | str | No | New title |
| `description` | str | No | New description as an inline string. Use this when there is no plan file. Mutually exclusive with `filePath`. |
| `filePath` | str | No | Path to a plan file under `.local/tasks/`. If you've created/edited a plan file, pass its path here and its content becomes the new description. Mutually exclusive with `description`. |
| `dependsOn` | array of str | No | Full list of dependency task refs (replaces existing) |
| `artifactKinds` | array | No | Updated artifact kind tags. Pass `[]` to clear stale tags when the task is no longer artifact-producing. |

**Returns:** `{ task }` — the task dict (`taskRef`, `title`, `description`, `state`, `displayState`, `dependsOn`, `artifactKinds`, `createdAt`, `updatedAt`)

**Examples:**

```javascript
// Edited plan file -- push its content as the new description.
await updateProjectTask({
  taskRef: "#1",
  title: "Agentic canvas prototype (tldraw)",
  filePath: ".local/tasks/agentic-canvas-prototype.md",
});

// No plan file -- pass the new description inline.
await updateProjectTask({
  taskRef: "#1",
  description: "Add a Canvas toggle button to the preview pane.",
});
```

### markTaskInProgress(taskRef)

Resume work on an IMPLEMENTED task. Call this before making further changes to a task that was already marked complete.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `taskRef` | str | Yes | Task ref to resume |

**Returns:** `{ task }` — the task dict (`taskRef`, `title`, `description`, `state`, `displayState`, `dependsOn`, `artifactKinds`, `createdAt`, `updatedAt`)

**Example:**

```javascript
await markTaskInProgress({ taskRef: "#1" });
```

### searchProjectTasks(query, locale=None, limit=None)

Search project tasks by text query, ordered by relevance.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | str | Yes | Search query. Supports boolean syntax: `"exact phrase"`, `foo bar` (both words), `foo OR bar`, `-foo` (exclude) |
| `locale` | str | No | BCP 47 locale of the query (e.g. `"en"`, `"es"`, `"fr"`). Pass for non-English queries for better stemming. |
| `limit` | int | No | Maximum number of results (default: 20) |

**Returns:** `{ results }` — a list of match dicts (each with `taskRef`, `title`, `description`, `state`, `displayState`, `score`, `matchType`, `createdAt`, `updatedAt`), ordered by relevance. Each `description` is a short snippet (~500 chars); read a task's full plan with `getProjectTask` or `queryProjectTasks`.

**Example:**

```javascript
// Simple keyword search
const results = await searchProjectTasks({ query: "authentication" });

// Boolean syntax: find auth tasks that aren't about login
const results = await searchProjectTasks({ query: "authentication -login", limit: 5 });

// Exact phrase
const results = await searchProjectTasks({ query: '"payment integration"' });

// Non-English query -- pass locale for better stemming
const results = await searchProjectTasks({ query: "autenticaci--n", locale: "es" });

// The call returns { results } -- read matches from `result.results`.
const { results: matches } = await searchProjectTasks({
  query: "authentication",
});
for (const match of matches) {
    console.log(`${match.taskRef} (${match.title}) -- score: ${match.score}`);
}
```

### queryProjectTasks(taskRefs=None, states=None, executable=None, maxDescriptionChars=None, updatedSince=None, createdSince=None)

Inspect project tasks by exact refs or narrowing filters. An unfiltered call lists tasks, but the result is capped (at most 100 tasks, and bounded by total size), so it can be partial -- check `truncated` and narrow by `states` (or use `searchProjectTasks`) when it is set. `maxDescriptionChars` sizes the plan text per task: omit it (the default) for titles only, a high value for full plans. It is a ceiling -- when the total would exceed the size budget, every task's plan is trimmed to an equal share rather than dropping whole tasks. To read specific plans in full, pass their `taskRefs` with a high `maxDescriptionChars` (or use `getProjectTask` for one task).

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `taskRefs` | array of str | No | Specific task refs to retrieve. Cannot be combined with `executable`, `updatedSince`, or `createdSince` (you may still pass `states` to filter the fetched refs). |
| `states` | array of str | No | Filter by state, using values from the Task States table below (e.g. `["PENDING", "IN_PROGRESS"]`). Casing and separators are tolerant -- `"in-progress"` and `"In Progress"` also work. Each base state matches its main-Repl variant too (e.g. `"in_progress"` matches both `IN_PROGRESS` and `MAIN_IN_PROGRESS`). |
| `executable` | bool | No | When `true`, only tasks ready to execute. |
| `maxDescriptionChars` | int | No | Ceiling on each task's plan text (default: titles only). A high value returns full plans; when the total would exceed the size budget, every task's plan is trimmed to an equal share rather than dropping whole tasks. |
| `updatedSince` | str | No | ISO-8601 timestamp; only tasks updated at or after this time. |
| `createdSince` | str | No | ISO-8601 timestamp; only tasks created at or after this time. |

**Returns:** Dict with `tasks` (list of task summaries), `totalCount`, and `truncated`.

**Example:**

```javascript
// Casing/separators are tolerant; "in-progress" also matches MAIN_IN_PROGRESS.
const result = await queryProjectTasks({ states: ["PROPOSED", "PENDING", "IN_PROGRESS"] });
for (const task of result.tasks) {
    console.log(`${task.taskRef} (${task.title}): ${task.state}`);
}
```

### bulkCreateProjectTasks(tasks)

Create multiple tasks at once with dependency relationships. Each task is created in PROPOSED state. The plan file content becomes the task description.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `tasks` | array | Yes | List of task objects (see below) |

Each task object:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `alias` | str | No | Alias within the batch, referenced by other tasks' `dependsOn`. Auto-generated if omitted. |
| `title` | str | Yes | Short title for the task |
| `filePath` | str | Yes | Path to the plan file (e.g. `.local/tasks/payment-integration.md`). The file content becomes the task description. |
| `dependsOn` | array | No | List of `alias` values from other tasks in this batch, or task refs (`"#1"`, `"#2"`) of already-accepted tasks. Never depend on PROPOSED -- only PENDING or later. Tasks within the same batch may depend on each other freely. |
| `artifactKinds` | array | No | Artifact kinds for tasks creating new artifacts. Values: `web`, `mobile`, `video`, `slides`, `automation`, `data-app`, `design`. Omit for code-only or non-artifact work. |

**Returns:** Dict with a `proposed` list of created task dicts, each with `taskRef`, `title`, `state`, `displayState`, `dependsOn`, `createdAt`, `updatedAt`

**Examples:**

```javascript
// Single task (no dependencies)
const created = await bulkCreateProjectTasks({
    tasks: [
        {
            title: "Launch microsite",
            filePath: ".local/tasks/launch-microsite.md",
            artifactKinds: ["web"],
        },
    ]
});
console.log(created.proposed[0].taskRef);

// Multiple tasks with dependencies using batch aliases
const created = await bulkCreateProjectTasks({
    tasks: [
        {
            alias: "auth",
            title: "Add authentication",
            filePath: ".local/tasks/auth.md",
        },
        {
            alias: "payments",
            title: "Payment integration",
            filePath: ".local/tasks/payments.md",
            dependsOn: ["auth"],
        },
    ]
});
```

## Plan File Format

Write each project-task plan as a plain markdown document in `.local/tasks/`. The file content becomes the task description. Do not run `mkdir`; the write tool creates parent dirs.

By default, create one project task per user request. Combine related work into a single plan rather than splitting into many tasks. Only create multiple tasks if the user explicitly asks for them or the request contains clearly independent, unrelated goals.

Dependencies and artifact tags are not declared in the plan file -- pass them via `dependsOn` and `artifactKinds` when creating or updating tasks.

### Plan body

First line: a short, descriptive title (3-6 words) prefixed with `#`. Then include these sections:

- **What & Why** -- Brief description of the feature/change and its purpose.
- **Done looks like** -- Observable outcomes when complete (what the user sees, not code-level details).
- **Out of scope** -- What is explicitly NOT included.
- **Steps** -- Numbered implementation steps for the executor agent, not separate project tasks.
- **Relevant files** -- Existing files discovered during investigation that the executor should start from. Use backtick-wrapped paths only, no trailing descriptions. Only list files you verified exist.
  - Whole file: `src/api/billing.ts`
  - Specific lines: `src/api/billing.ts:12-85`
  - Multiple ranges: `src/api/billing.ts:12-85,200-250`
  - WRONG: `src/api/billing.ts` -- Billing API handlers (lines 12-85)

Assume features build on each other. If a new task depends on another task, declare that dependency via `dependsOn` rather than in the plan body. You may depend on existing tasks that are PENDING or later -- never on existing PROPOSED tasks. Tasks within the same batch may depend on each other freely.

Rules for the `## Steps` section:

- Each step should be describable in 1-2 sentences.
- Focus on what to build, not how to build it.
- Do not include file paths, code snippets, CSS classes, or line-level edits in step bullets. Put file references in `## Relevant files` instead.
- Draw clean boundaries so parallel executors won't conflict. Combine same-area steps into one project task.
- Add a short note for critical architectural constraints.

### Example

```markdown
# Payment Integration

## What & Why
Add Stripe payment processing so users can upgrade to paid plans.

## Done looks like
- Users can enter payment info and subscribe to a plan
- Successful payments activate the paid tier immediately
- Failed payments show a clear error message

## Out of scope
- Invoicing and receipts (future work)
- Multiple payment methods (Stripe only for now)

## Steps
1. **Stripe backend integration** -- Set up Stripe SDK, create endpoints for creating checkout sessions and handling webhooks
2. **Payment UI** -- Build the checkout page with plan selection and Stripe Elements for card input
3. **Tier activation** -- On successful payment, upgrade the user's account to the paid tier and reflect it in the UI

## Relevant files
- `src/api/billing.ts:12-85`
- `src/config/stripe.ts`
```

## Task States

When filtering by state (`states` on `queryProjectTasks`), values are matched leniently: casing and `-`/space separators are normalized, so `"in-progress"`, `"In Progress"`, and `"IN_PROGRESS"` are equivalent. The canonical tokens are: `PROPOSED`, `PENDING`, `IN_PROGRESS`, `IMPLEMENTED`, `MERGING`, `QUEUED`, `MERGED`, `CANCELLED`, plus the main-Repl variants `MAIN_PENDING`, `MAIN_IN_PROGRESS`, `MAIN_IMPLEMENTED`. Filtering by a base state also matches its main-Repl variant (e.g. `IN_PROGRESS` matches both `IN_PROGRESS` and `MAIN_IN_PROGRESS`); pass the explicit `MAIN_` token to match only the main-Repl variant.

| State | Description |
|-------|-------------|
| `PROPOSED` | Suggested but not yet accepted by the user. No implementation exists. |
| `PENDING` | Accepted, waiting to start. No implementation exists. |
| `IN_PROGRESS` | Being worked on by a task agent in a separate Repl. Changes are not visible in this Repl. |
| `IMPLEMENTED` | Work is done in a separate Repl, ready for merge. Changes are not visible in this Repl -- do not search the codebase for them. |
| `MERGING` | Currently being merged. Do not duplicate. |
| `QUEUED` | Queued for merge. |
| `MERGED` | Finished and merged. Changes are now visible in this Repl. |
| `CANCELLED` | Abandoned. |
| `MAIN_PENDING` / `MAIN_IN_PROGRESS` / `MAIN_IMPLEMENTED` | Same as the corresponding state above, but for work done directly on the main Repl rather than in a separate task-agent Repl. |

Drift blocking is a `blockedBy` reason, not a filterable state -- do not pass it to `state`/`states`. A drift-blocked task is surfaced with `blockedBy: "DRIFT"` (the task's `state` stays whatever it was). It means the task is blocked because an upstream task diverged from its plan and needs replanning.

## User Communication Rules

Follow these rules strictly when discussing tasks with the user:

1. **Always describe tasks by ref and title**: e.g. "Task #1 (Add authentication button)"
2. **Never use internal state names**: use these display names instead:
   - PROPOSED -- "Drafts"
   - PENDING -- "Active"
   - IN_PROGRESS -- "Active"
   - IMPLEMENTED -- "Ready"
   - MERGING -- "Merging"
   - QUEUED -- "Merging"
   - MERGED -- "Merged"
   - CANCELLED -- "Archived"
   - The `MAIN_*` variants use the same display name as their base state (e.g. `MAIN_IN_PROGRESS` -- "Active", `MAIN_IMPLEMENTED` -- "Ready"). Tasks also carry a `displayState` field with the `MAIN_` prefix stripped, so mapping on `displayState` covers them automatically.
   - A `blockedBy: "DRIFT"` task -- "Affected by another task that changed"
3. **Never expose implementation details**: Do not reveal function names (`bulkCreateProjectTasks`, `updateProjectTask`, etc.), API surface, or internal task system mechanics to the user

## Best Practices

1. **Prefer fewer tasks**: Default to one task per request unless the user explicitly asks for more
2. **Create tasks early**: Create the project task when you understand the user's goals
3. **Keep titles short**: Titles should be concise and descriptive
4. **Use descriptions for detail**: Put implementation details in the description field

## Example Workflow

```javascript
// 1. Write the plan file directly with the write tool -- it creates `.local/tasks/` for you.

// 2. Create the task -- file content becomes the description
const created = await bulkCreateProjectTasks({
    tasks: [
        {
            title: "Launch microsite",
            filePath: ".local/tasks/launch-microsite.md",
            artifactKinds: ["web"],
        },
    ]
});

// Created tasks start in PROPOSED; the user accepts them in the tasks panel.
// --- After the user accepts, the system handles scheduling ---

// 3. Check progress
const { tasks } = await queryProjectTasks();
for (const task of tasks) {
    console.log(`Task ${task.taskRef} (${task.title}): ${task.state}`);
}
```
