---
name: code-review
description: Spawn a code review (architect) subagent for deep analysis, planning, and debugging. The architect specializes in strategic guidance rather than implementation. Architect should be called after building major features. Relies on `delegation` skill.
---

# Architect Skill

Spawn a code review (a.k.a architect) subagent for analysis and planning. The architect specializes in analysis and strategic guidance rather than implementation.

## When to Use

Use this skill when:

- You need deep architectural analysis or code understanding
- You want strategic recommendations about system design or patterns
- You need comprehensive analysis of code quality or technical debt
- You want root cause analysis and debugging assistance

## When NOT to Use

- Simple tasks that you can complete directly
- Tasks that require file edits or implementation (use delegation skill instead)
- Read-only operations (use ripgrep or glob/read tools instead)

## Available Function

### subagent({ name, task, config: { $kind: "architect", ... } })

Use `Promise.all` to run independent analyses in parallel. If you need to review one part of the codebase while working on another part, don't await to leave the job running in the background and collect the results later. (see delegation skill)

**Parameters:**

- `name` (str, required): Short handle, alphanumeric and `-` only, e.g. `"code-review"`; reuse the returned `name` for follow-ups.
- `task` (str, required): The analytical task or question.
- `config.$kind` (required): `"architect"`.
- `config.relevantFiles` (list[str], optional): Workspace-relative paths to analyze.
- `config.responsibility` (str, default `"evaluate_task"`): one of exactly `"evaluate_task"` (assess completed or ongoing work against goals), `"plan"` (create implementation plans with task decomposition and sequencing), or `"debug"` (root cause analysis, reproduction steps, and recommended fixes).
- `config.includeGitDiff` (bool, default `false`): Embed the working-tree diff (lockfiles/node_modules/dotfiles filtered); with an active task, also the diff since its base commit.
- `config.relevantGitCommits` (str, optional): Commit range or single commit (e.g. `"HEAD~3..HEAD"`) whose diff and changed files are embedded.

**Returns:** a job that, when awaited (or collected via `waitForJob`), resolves to a dict with analysis results:

```json
{
    "name": "code-review",
    "jobId": "code-review:0",
    "status": "completed",
    "text": "Full analysis output..." // only text is important for you to see if successful.
    ...
}
```

**Example:**

```javascript
// Plan a new feature
const rateLimitPlanTask = `Create a plan for implementing rate limiting on API endpoints.`;
const result = await subagent({
    name: "rate-limit-plan",
    task: rateLimitPlanTask,
    config: {
        $kind: "architect",
        relevantFiles: ["src/middleware/index.ts", "src/routes/api.ts"],
        responsibility: "plan"
    }
});
console.log(result.text);

// Debug an issue
const sessionDebugTask = `The UserAuthService.validateSession() returns false for valid tokens.`;
const result2 = await subagent({
    name: "session-debug",
    task: sessionDebugTask,
    config: {
        $kind: "architect",
        relevantFiles: ["src/services/UserAuthService.ts", "src/utils/jwt.ts"],
        responsibility: "debug",
        includeGitDiff: true
    }
});
console.log(result2.text);
```

## Best Practices

1. **Be specific in your task description**: Include concrete function names, error messages, or design goals
2. **Provide relevant files**: The architect can only analyze files you pass in `relevantFiles`
3. **Choose the right responsibility**: Use "plan" for new work, "debug" for issues, "evaluate_task" for reviewing progress
4. **Use `includeGitDiff`**: When reviewing or debugging recent work, set it so the diff is embedded up front and the architect does not spend turns rediscovering the changes
5. **Use `relevantGitCommits`**: When you need the architect to understand recent history (e.g., "HEAD~3..HEAD"); the range's diff is embedded for it

