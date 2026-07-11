---
name: merge-resolution
description: "Use when you are resolving active git rebase or merge conflicts and are stuck, encountering issues, or the merge-resolution instructions you were given are not enough."
---

# Merge Resolution

Use this skill when you are resolving active reconcile or rebase conflicts after being instructed to handle merge resolution.

## Current Callbacks

Only valid while a merge-conflict resolution is in progress. Call after every conflicting file has been edited so no conflict markers remain: the driver verifies the files, stages everything, continues the rebase, and either returns the next round of conflicts or finishes the rebase.

```javascript
await continueMergeResolution({});
```

If this round's resolution semantically diverged from the original task plan, pass `divergenceSummary`. Only set it when the resolution actually diverged, not for trivial merge resolution.

```javascript
await continueMergeResolution({
  divergenceSummary: "Briefly describe the semantic divergence.",
});
```

Only valid while a merge-conflict resolution is in progress. Call when the conflicts cannot be resolved: the rebase is aborted and the task is handed back to the user.

```javascript
await abandonMergeResolution({
  reason: "Explain why the conflicts cannot be resolved.",
});
```

The `reason` is shown to the user.

## Protocol

1. Inspect the conflicting files listed in the latest merge-resolution brief.
2. Edit every conflicting file until no `<<<<<<<`, `=======`, or `>>>>>>>` markers remain.
3. Preserve both incoming main-branch changes and this task's changes whenever possible. Prefer keeping functionality over deleting other work.
4. For generated files and lockfiles, prefer accepting the incoming version or regenerating them after the rebase.
5. If a conflicting file is transient runtime state, untrack it with `git rm --cached <file>`, add it to `.gitignore`, and keep the `.gitignore` change.
6. Use shell git only for inspection or conflict mechanics: `git status`, `git log`, `git diff`, `git checkout --ours/--theirs <path>`, `git rm <path>`, or `git rebase --skip` for empty commits.
7. Do not switch branches, force-push, or run `git rebase --continue` / `git rebase --abort` directly. Always finish through `continueMergeResolution` or `abandonMergeResolution` so the result is verified and reported.

## Stale Instructions

Older persisted briefs may refer to top-level tools named `ContinueMergeResolution` or `AbandonMergeResolution`, and to a snake_case `divergence_summary` argument. Those instructions are stale.

Use the CodeExecution callbacks instead:

```javascript
await continueMergeResolution({ divergenceSummary: "..." });
await abandonMergeResolution({ reason: "..." });
```
