---
name: delegation
description: Delegate independent work through the subagent CodeExecution callback, with the ability to dispatch multiple subagents in parallel, subagents in background, and followups to existing subagents for continued work or questions and answers. Provides general guidance on subagents, reified by the specific skills that use them.
---

# Delegation Skill

Delegate work to a subagent through the `subagent` callback inside CodeExecution.
You choose a `name`; the same `subagent` call covers every kind of delegated
work via `config.$kind`.

## When to Use

Use this skill when:

- You need to delegate autonomous implementation work
- Multiple tasks can run independently in parallel
- A task is large enough to benefit from a separate agent context

## When NOT to Use

- Simple tasks that you can complete directly
- Tasks that require immediate user interaction
- Simple one-shot read-only exploration that a single `ripgrep` or `glob/read` tool can handle
- Quick file edits
- Analysis, planning, or debugging that needs your current full context

## Available Callbacks

### subagent

`subagent({ name, task, config })` is an async CodeExecution callback. Call it from the codeExecution tool.

**Parameters:**

- `name` (required): a short handle you pick, alphanumeric and `-` only, e.g. `"auth-fix"`. `subagent` always creates a new one (a live-name clash is auto-renamed), so to continue an existing subagent use `sendFollowup` with the result's `name`.
- `task` (required): the full task, question, or test plan. Put all detail here.
- `config.$kind` (required): the kind of subagent to create. This is specific to the skill that uses the subagent callback. A non-specialized subagent must use the $kind: "general".
- Other config values are specific to the skill that uses the subagent callback.

**Returns:** A job that resolves to the subagent's result.

#### General Subagent

A general subagent is a generic subagent that can be used for any task. The `config` object for a general subagent can include:
- `config.relevantFiles`: workspace-relative paths the subagent reads first. Files accept 1-based line ranges, e.g. `"src/auth.ts:10-50"`.
- `config.relevantSkills`: skill paths the subagent reads first.

For example:
```js
const authFixTask = "Fix the auth bug in src/auth.ts and summarize the changed files.";
const authfixResult = await subagent({
  name: "auth-fix",
  task: authFixTask,
  config: {
    $kind: "general",
    relevantFiles: ["src/auth.ts", "src/auth.test.ts:10-50,200-250"],
    relevantSkills: [".local/skills/auth/SKILL.md"],
  },
});
console.log(authfixResult.text); // Mainly the `text` matter. Unless the skill for a particular specialized subagent indicates otherwise.
```

### sendFollowup

`sendFollowup({ name, message })` continues an **existing** subagent, keeping its
history and context instead of starting fresh. This is the only way to follow up
on a subagent — calling `subagent` again creates a new one (see `name` above).

**Parameters:**

- `name` (required): the canonical `name` returned by the original `subagent` call. Note: `subagent` call can in rare cases return a different name for disambiguation. You will be told if that ever happens.
- `message` (required): the follow-up instruction or question. Put all detail here.

`sendFollowup` returns the same kind of job as `subagent` — await it for the
subagent's result (same `{ name, text, jobId, status }` shape) or run it in the background without awaiting it.

```js
const firstJob = subagent({ name: "auth-fix", task, config: { $kind: "general" } });
```

After the subagent finishes in the background, you can continue it with `sendFollowup`.
```js
// Continue the SAME subagent — do NOT call subagent() again.
const followup = await sendFollowup({ name: "auth-fix", message: "For your auth.tsx changes, make sure to comment out the tests for now." });
console.log(followup.text);
```

## Usage Patterns

Different $kinds of subagents return different values. The `subagent` callback returns a job that, when awaited, resolves to the subagent's result.

All subagent results have the following properties:
- `name`: the name of the subagent
- `text`: the text of the subagent's result
- `jobId`: the id of the subagent's job
- `status`: the status of the subagent's job

### Synchronous Usage

```js
const authFixTask = "Fix the auth bug in src/auth.ts and summarize the changed files.";
const authfixResult = await subagent({
  name: "auth-fix",...});
console.log(authfixResult.text);
```

### Using Promise.all to Run Multiple Subagents in Parallel

```js
const [charts, auth] = await Promise.all([
  subagent({ name: "build-charts", ...}),
  subagent({ name: "add-auth-regression-tests", ... }),
]);
console.log("Build charts result:");
console.log(charts.text);
console.log("================================================");
console.log("Add auth regression tests result:");
console.log(auth.text);
// Note: Use `Promise.all` / `Promise.race` only when this turn needs the subagent results before continuing.
```

### Asynchronous Usage (Background Subagent)

```js
const buildChartsJob = subagent({ ...});
// The job runs in the background while you do independent parent-agent work.
// After that work is done, join before finalizing.
const buildChartsResult = await waitForJob({ jobId: buildChartsJob.jobId, timeout: 600 });
console.log(buildChartsResult.text);
```

Background subagents are for parallelism while the main agent has other independent work to do. Do not finish the turn with a relevant subagent job still running. When your independent work is done, use `await waitForJob({ jobId: job.jobId, timeout: 600 })` to collect the result, inspect it, and continue. If `waitForJob` times out, call it again. If the subagent is no longer needed, call `cancelJob({ jobId: job.jobId })` instead of leaving it running.

## Best Practices

1. Delegate only work that can proceed independently.
2. Include the exact files, constraints, and relevant skill names in the `task`.
3. Use `Promise.all` / `Promise.race` only when this turn needs the subagent results before continuing.
4. Use background subagents only when you have independent work to do before joining them.
5. Wait for or cancel every relevant background subagent before finalizing user-facing work.
6. Verify subagent results before finalizing user-facing work.

The subagent can read and edit files, run commands, use diagnostics, and load relevant skills; the main agent remains responsible for final integration, verification, previews, and user-facing status.
