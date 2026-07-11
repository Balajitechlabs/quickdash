---
name: git-remote
description: Push, pull, and create pull requests on GitHub remote repositories.
---

# Git Remote Operations

Interact with GitHub remote repositories. When `provider` is omitted, the remote provider is auto-detected from the `origin` remote URL. `gitlab` and `bitbucket` are recognized provider names but are not supported yet.

## Prerequisites

- The repl must have a git repository initialized with an `origin` remote configured
- The user must have connected their GitHub account to Replit
## Opening the Git pane

You can render a one-click "Open in git" button that takes the user to the Git workspace pane — status, branches, commits, history, or changes. To render it, write this tag on its own line at the end of your reply (it is replaced by the button; the user clicks it to open the pane):

```
<open-in-pane tool="git"></open-in-pane>
```

Button behavior:

- Render it when the request is mainly navigation; when navigation is mixed with real work, still do the work with the appropriate tools.
- Keep your chat reply to one concise sentence naming what the button opens. Do not say the pane is already open, that you opened it, or describe the button's position.

| User asks | Tag to emit |
| --- | --- |
| "Take me to the Git history." | `<open-in-pane tool="git"></open-in-pane>` |
| "Where do I see my branches / changes?" | `<open-in-pane tool="git"></open-in-pane>` |

## Available Functions

### gitPush(branch?, force?, provider?)

Push the current branch to the remote repository.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `branch` | str | No | Remote branch name to push to. Defaults to the current local branch name. |
| `force` | bool | No | Force push (use with caution). Defaults to false. |
| `provider` | "github" \| "gitlab" \| "bitbucket" | No | Optional provider override. Omit to auto-detect from `origin`; only `github` is supported today. |

**Returns:** Dict with `branch`, `remote`, `provider`

**Example:**

```javascript
// Push current branch
const result = await gitPush({});
console.log(`Pushed to ${result.branch} on ${result.provider}`);

// Push to a specific branch
await gitPush({ branch: "feature-branch" });

// Force push (use with caution)
await gitPush({ force: true });
```

**Error Codes:**

| Code | Description |
|------|-------------|
| `NO_CREDENTIALS` | No credentials found for the detected provider |
| `NO_REMOTE` | No origin remote configured or provider not detected |
| `UNSUPPORTED_PROVIDER` | Provider is not supported |
| `CLI_ERROR` | Git command failed (e.g., merge conflicts, detached HEAD) |
| `DANGEROUS_CONFIG` | Dangerous git config detected |

### gitPull(branch?, provider?)

Pull changes from the remote repository into the current branch.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `branch` | str | No | Remote branch name to pull from. Defaults to the upstream tracking branch. |
| `provider` | "github" \| "gitlab" \| "bitbucket" | No | Optional provider override. Omit to auto-detect from `origin`; only `github` is supported today. |

**Returns:** Dict with `branch`, `remote`, `provider`

**Example:**

```javascript
// Pull from upstream tracking branch
const result = await gitPull({});
console.log(`Pulled ${result.branch} from ${result.provider}`);

// Pull from a specific branch
await gitPull({ branch: "main" });
```

**Error Codes:**

| Code | Description |
|------|-------------|
| `NO_CREDENTIALS` | No credentials found for the detected provider |
| `NO_REMOTE` | No origin remote configured or provider not detected |
| `UNSUPPORTED_PROVIDER` | Provider is not supported |
| `CLI_ERROR` | Git command failed (e.g., dirty worktree, merge conflicts, detached HEAD) |
| `DANGEROUS_CONFIG` | Dangerous git config detected |

### createPullRequest(title, body, baseBranch?, headBranch?, draft?, provider?)

Create a pull request on the remote repository.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `title` | str | Yes | Title of the pull request |
| `body` | str | Yes | Body/description of the pull request (supports markdown) |
| `baseBranch` | str | No | Target branch for the PR. Defaults to the repository's default branch. |
| `headBranch` | str | No | Source branch for the PR. Defaults to the current branch. |
| `draft` | bool | No | Create as a draft PR. Defaults to false. |
| `provider` | "github" \| "gitlab" \| "bitbucket" | No | Optional provider override. Omit to auto-detect from `origin`; only `github` is supported today. |

**Returns:** Dict with `url`, `number`, `provider`

**Example:**

```javascript
// Create a simple PR
const result = await createPullRequest({
  title: "Add authentication feature",
  body: "## Summary\n\nThis PR adds user authentication.\n\n## Test Plan\n\n- [ ] Test login flow"
});
console.log(`Created PR #${result.number}: ${result.url}`);

// Create a draft PR targeting a specific branch
await createPullRequest({
  title: "WIP: New feature",
  body: "Work in progress",
  baseBranch: "develop",
  draft: true
});
```

**Error Codes:**

| Code | Description |
|------|-------------|
| `NO_CREDENTIALS` | No credentials found for the detected provider |
| `NO_REMOTE` | No origin remote configured or provider not detected |
| `UNSUPPORTED_PROVIDER` | Provider is not supported |
| `CLI_ERROR` | PR creation failed (e.g., branch not pushed, PR already exists) |

## Providers

| Provider | Status |
|----------|--------|
| `github` | Supported when auto-detected or explicitly passed |
| `gitlab` | Recognized but not supported yet; calls return `UNSUPPORTED_PROVIDER` |
| `bitbucket` | Recognized but not supported yet; calls return `UNSUPPORTED_PROVIDER` |

## Best Practices

1. **Always push before creating a PR**: The head branch must exist on the remote before creating a pull request
2. **Use meaningful PR titles**: Keep titles concise but descriptive
3. **Include a test plan**: Document how reviewers can verify the changes
4. **Avoid force push on shared branches**: Force push can cause issues for collaborators

## Common Workflows

### Feature Branch Workflow

```javascript
// After completing work on a feature branch

// 1. Push the branch
await gitPush({});

// 2. Create a PR
await createPullRequest({
  title: "Add user authentication",
  body: "## Summary\n\nImplements OAuth2 login flow.\n\n## Test Plan\n\n- Log in with Google\n- Verify session persists"
});
```

### Sync with Remote

```javascript
// Pull latest changes from main
await gitPull({ branch: "main" });
```
