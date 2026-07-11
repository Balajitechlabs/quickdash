---
name: debug-workflow-ports-issues
description: Diagnose and fix workflow port problems — DIDNT_OPEN_A_PORT, a workflow that times out waiting on a port, or a blank preview while the server seems to be running. Read this before restarting a failed workflow a second time. Not for code crashes, stack traces, or build failures; fix those first.
---

# Debugging workflow port issues

`DIDNT_OPEN_A_PORT` means the platform never saw the port the workflow waits on (`waitForPort`) become available. A crashed server is only one cause, and usually not the one. The others:

- the server bound a different port than `waitForPort` (most common);
- the server bound `127.0.0.1` instead of `0.0.0.0`, so it isn't detected;
- the port opened but was never forwarded (governed by `.replit` `[[ports]]` and, for artifacts, the application router);
- the server crashed or failed to start.

In the first three the server is healthy and `curl localhost:<port>` works, so don't assume the code is broken until you've checked. Restarting just reruns the same command and reproduces the same outcome — after two failed restarts with `DIDNT_OPEN_A_PORT`, stop and diagnose.

## Diagnose

1. Find the real port and confirm the server is up. Read the workflow's recent logs for the line the framework prints, e.g. `Local: http://localhost:5173/`, `Listening on 0.0.0.0:8000`, `ready on port 3000`. Then from a shell:

   ```bash
   # what is listening, and on what address?
   (ss -tlnp 2>/dev/null || netstat -tlnp 2>/dev/null) | grep LISTEN
   # does the waited-for port answer?
   curl -sS -o /dev/null -w "%{http_code}\n" http://localhost:<waitForPort>
   ```

2. Compare the bound port to `waitForPort`. This is the most common cause: the dev server came up on 5173/3000/8080 or a random high port while the workflow waits on something else.

3. Check the bind address. If the only listeners are on `127.0.0.1`, the server won't be detected — it must bind `0.0.0.0`.

4. Read `.replit` (and, for artifact services, the relevant `artifact.toml`) for the configured ports and any `[[ports]]` mappings.

## Fix, in priority order

**A. Port mismatch — make the server bind the waited-for port.** Make the server honor the port it's given rather than chasing it. Most frameworks read `$PORT` or take a flag:

- Vite: `vite --port $PORT --strictPort` (or `server.port` from `process.env.PORT` in `vite.config`). `--strictPort` fails loudly instead of silently moving to the next port.
- Next.js: `next dev -p $PORT`.
- Express/Node: `app.listen(process.env.PORT)`.
- Python (Flask/uvicorn/etc.): bind `int(os.environ["PORT"])`, host `0.0.0.0`.

If you can't change the bind port, set `waitForPort` to the port the server actually uses (only valid for non-artifact workflows — see below).

**B. Localhost bind — switch to `0.0.0.0`.** Add the framework's host flag (`--host 0.0.0.0`, `--hostname 0.0.0.0`, `host="0.0.0.0"`).


**C. Web apps must use port 5000 + webview.** A `webview` workflow's server must listen on 5000. If it's on anything else, change the server to bind 5000 (`--port 5000 --host 0.0.0.0`) rather than changing `waitForPort`. Backend/console workflows may use other supported ports.


After a fix, restart once and confirm readiness. Don't enter another restart loop.

## When the server is healthy but the port still won't open

If the logs show it listening on the correct `waitForPort` on `0.0.0.0`, `curl localhost:<port>` returns 200, and it still fails `DIDNT_OPEN_A_PORT` across restarts, this is a platform-side forwarding problem that more restarts or code changes won't fix. Tell the user what you verified — server up on the right port and address, but the platform isn't forwarding it — so they can escalate, and move on.
