---
name: environment-secrets
description: Manage environment variables and secrets. View, set, delete env vars and request secrets from users.
---

# Environment And Secrets Skill

Use this skill to inspect and manage project environment variables and to request sensitive values from the user.

## Available Functions

All functions are available in the JavaScript code execution environment. Call callbacks with a single JSON object argument.

### viewEnvVars({ type, environment, keys })

View environment variables and/or secret existence.

Parameters:

- `type` (optional): `env`, `secret`, or `all`. Defaults to `all`.
- `environment` (optional): `shared`, `development`, or `production`. For `development` or `production`, shared env vars are also included.
- `keys` (optional): list of keys to filter to.

Environment variables return actual values grouped by environment. Secrets return existence status only, never values.

```javascript
return await viewEnvVars({ type: "all" });
```

### requestSecrets({ keys, userMessage })

Ask the user for sensitive values and stop the current turn. Use this instead of asking for API keys, tokens, passwords, private keys, or credentials in chat.

The user enters values into a secure form. Secret values are saved as Replit Secrets and are not returned to you.

```javascript
return await requestSecrets({
  keys: ["OPENAI_API_KEY"],
  userMessage: "Please provide your OpenAI API key.",
});
```

### requestEnvVars({ envVars, userMessage })

Ask the user for non-secret environment variable values and stop the current turn.

```javascript
return await requestEnvVars({
  envVars: [{ key: "LOG_LEVEL", environment: "shared" }],
});
```

Call `requestSecrets` or `requestEnvVars` as the final callback in the code execution snippet. The user submits a form outside the code execution runtime; a future turn will include a status message indicating whether the requested values were saved. Secret values are never shown back to you.

### setEnvVars({ values, environment })

Set non-secret environment variables.

Parameters:

- `values` (required): object of key-value pairs.
- `environment` (optional): `shared`, `development`, or `production`. Defaults to `shared`.

Use `requestSecrets` for sensitive values. This callback rejects runtime-managed keys.

```javascript
return await setEnvVars({
  environment: "shared",
  values: { NODE_ENV: "production" },
});
```

### deleteEnvVars({ keys, environment })

Delete non-secret environment variables from the specified environment.

```javascript
return await deleteEnvVars({
  environment: "shared",
  keys: ["NODE_ENV"],
});
```

## Guidance

- Default to the `shared` environment unless the user needs distinct development and production values.
- An environment variable in `shared` conflicts with the same key in `development` or `production`. Delete the conflicting key first, then add it in the target environment.
- Never set secrets with `setEnvVars`; use `requestSecrets({ keys, ... })`.
- Do not print secret values. `viewEnvVars` only tells you whether secrets exist.
- Runtime-managed keys currently include `DATABASE_URL`, `PGDATABASE`, `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `REPLIT_DOMAINS`, `REPLIT_DEV_DOMAIN`, `REPL_ID`. Do not request or set those manually.
