---
name: testing
description: Run automated UI tests against your application using a Playwright-based testing subagent. Use after implementing features to verify they work correctly.
---


# Testing Skill

A `subagent` with `config: { $kind: "testing" }` is a specialized Playwright-based testing subagent that drives your application in a real browser, watches both browser and backend logs, and reports back with screenshot evidence and technical diagnostics. End-to-end testing through it can uncover bugs not discoverable through conventional methods like `curl` or unit tests.

Each tester is a persistent conversation partner: it keeps its history, seeded data, and (usually) its browser state and logged-in sessions, so a follow-up (`sendFollowup`) can build on earlier work instead of starting from scratch. One caveat: the browser may have died or been restarted since the last message, so treat open pages and logged-in sessions as best-effort -- a follow-up that depends on them should say what to do if that state is gone (e.g. "if you're no longer logged in, log in again as test@example.com first").

Every tester has a name you choose; you can run several testers side by side by using distinct names (see Named Testers below).

See delegation skill for more information about the subagent callback in general.

## Testing subagents

`subagent({ name, task, config: { $kind: "testing" } })` is an async CodeExecution callback: await the returned future when you need the verdict before continuing, or fire it and keep working when the result can arrive later. Continue an existing tester with `sendFollowup({ name, message })`.

**Parameters:**

- `name` (str, required): The tester's name, alphanumeric and `-` only (e.g. "checkout"). A fresh tester is created under this name. Use the returned `name` for follow-ups (a collision auto-renames).
- `task` (str, required): What you want from the tester -- a test plan, or a question about something. Put all detail here.
- `config.$kind` (required): `"testing"`.

To continue a tester (keeping its history and browser state), call `sendFollowup({ name, message })` with the returned `name`. A follow-up to a tester that no longer exists (never created, or recycled at capacity) errors; start a fresh `subagent` instead.

**Returns:** a job that, when awaited, resolves to:

- `name`: The canonical name of the tester that handled the request. To continue the same session, `sendFollowup({ name, message })` with this name.
- `verdict` (well-formed runs only): One of "success", "failure" (a bug blocks the request), "unable" (blocked by something that is not an app bug), or "general" (an answer to a question, or other requested information). A malformed run omits `verdict` and `screenshots`, resolving with just `name` + `text`.
- `text`: Detailed test report and observations
- `screenshots` (well-formed runs only): List of `{ id, description }` entries the tester cited as evidence. Descriptions usually carry enough signal on their own; when you need to see the pixels (visual bugs, layout issues), pass an id to `viewImage` -- e.g. `await viewImage({ id: screenshot.id })`, or `await Promise.all(testRun.screenshots.map((s) => viewImage({ id: s.id })))` to view several at once.
... kind, jobId, status, ...

Notice that screenshot and verdict are provided whereas regular subagent only returns `text`.

**Example:**

```javascript
const loginTestTask = `
Test the user login flow:
1. [New Context] Create a new browser context
2. [Browser] Navigate to the login page (path: /login)
3. [Browser] Enter "test@example.com" in the email field
4. [Browser] Enter "password123" in the password field
5. [Browser] Click the "Sign In" button
6. [Verify]
- Assert redirect to the dashboard (path: /dashboard)
- Assert user name appears in the header

Technical context:
- Login endpoint: POST /api/auth/login
- Dashboard route: /dashboard
- User name displayed in #user-header element
- Relevant files: client/src/pages/Login.tsx, server/routes/auth.ts
`;
const testRun = await subagent({
    name: "auth-happy-path-test",
    config: { $kind: "testing" },
    task: loginTestTask
});

console.log(testRun.verdict);
console.log(testRun.text);
console.log(testRun.screenshots);
```

## Writing Test Plans

A good test plan derives from contextual understanding of the application: the relevant frontend and backend code, how to navigate to the feature, and the specific UI elements (selectors, labels) and API endpoints involved. If you just implemented the feature, you already have this context -- use it immediately. If a test fails due to insufficient context, iterate and add more details; if stuck after multiple attempts, stop and ask the user for help.

In the plan itself:

1. **Test one flow at a time**: Keep each message focused on a single user journey
2. **Include expected outcomes**: Specify what success looks like -- "A success toast should appear with message 'Saved!'"
3. **Provide technical context**: Append relevant DB schemas, API routes, test credentials, data-testids, component details, and paths to the relevant source files to the message -- pointing the tester at the right files up front saves it from having to explore for them
4. **Specify test data**: Use concrete values where you know them. For values the tester must generate at runtime (e.g. unique data to avoid collisions), name them with an angle-bracket placeholder and say how to produce them, then reuse the placeholder in later steps:
   - **Generate**: "a product name from `nanoid(6)` (remember it as `<product_name>`)"
   - **Reuse**: "assert the product name is `<product_name>`"
5. **Handle authentication**: If the app requires login, include login steps first
6. **Include setup steps**: If the test needs data to exist, explain how to create it
7. **Mention the viewport** if a specific one matters (e.g. mobile)

Batch multiple `[Verify]` checks on the same page together when no actions occur between them. But if `[Browser]`, `[API]`, or `[DB]` steps occur between verifications, keep them in separate `[Verify]` blocks -- verifications should be read-only blocks without side effects.

For UI testing, explicitly include interactions like hover effects, dialogs, modals, tooltips, dropdowns, and animations -- the testing agent sometimes needs special handling for these (e.g., being told to dismiss a dialog before clicking).

## Follow-up Messages

Because the tester keeps its conversation history, follow-ups can reference earlier work directly instead of restating the whole plan:

- "Re-run just the payment step from the checkout test, but with an expired card this time."
- "What did the cart page look like after step 4? Describe the layout."
- "Log out of the session you created earlier and verify the cart badge resets to zero."
- "Was there anything in the browser console or backend logs during the login flow?"

Use `sendFollowup` so the tester continues its existing session instead of starting over. A follow-up that depends on an open page or a logged-in session should account for the browser-state caveat above. Continuing the login-flow example:

```javascript
const followUp = await sendFollowup({
    name: "auth-happy-path-test",
    message: `
About the login flow you just tested: reload the dashboard and verify the
session survives the reload (user name still in the header, no redirect to
/login). If you are no longer logged in, log in again as test@example.com
first.
`
});

console.log(followUp);
```

## Named Testers and Parallel Testing

Each distinct `tester` name is its own persistent tester with its own browser, history, and sessions. Use separate names for independent flows -- e.g. keep an "admin" tester logged in as an admin while a "shopper" tester exercises the storefront -- or simply to start a test that has nothing to do with earlier work.

At most one testers can run at once; and reuse existing testers (with `sendFollowup`) when the earlier context is useful.

All testing subagents share the same development database as you (see Test Environment below).

## When to Use

- You have implemented or modified a feature and want to verify it works
- User flows through the application (login, forms, navigation, modals)
- UI components render and behave correctly, including visual changes (layout, styling)
- Frontend features that depend on JavaScript execution
- End-to-end flows spanning multiple pages or components
- Integrations like Stripe payments or authentication flows
- API testing that involves many steps or interacts with the UI -- include extra context about the API in the message. For simple API checks, use `curl` or standard HTTP clients instead.

Not for:

- Unit testing code logic -- use standard test frameworks instead. Reserve the tester for e2e validation; reserve unit tests for regressions and backend logic.
- When the application is not running or accessible
- Load testing or performance testing

## Test Environment

The testing environment uses the **same development database** as you and the user. The application is not in a fresh state -- it may contain existing data from prior usage.

- **Don't assume specific counts** -- tests that assert "there are exactly 3 products" will break if other data exists
- **Don't test empty states** or rely on data you didn't create as part of the test plan
- **Generate unique values** for usernames, emails, titles, etc. using `nanoid` to avoid conflicts across test runs and with user data (note: when messaging the agent, you don't need to execute the `nanoid` calls, merely instruct the subagent to use nanoid to generate unique IDs.)

Other limitations:


- The testing subagent has a maximum number of steps before it needs to report results
- Some complex interactions (drag-and-drop, canvas operations) may be challenging
- If the application is not accessible or crashes, tests will report as "unable"

## Example Test Plans

Test plans can vary in their prescriptiveness and complexity.

```text
1. [New Context] Create a new browser context
2. [Browser] Navigate to the product page (path: /products)
3. [Browser] Click on the first product link (note its id as <product1_id>)
4. [Verify] Assert redirect to the product page (path: /product/<product1_id>)
5. [Verify]
   - Ensure the product title is not too big
   - Ensure the overall color scheme is consistent with the rest of the page
   - Assert there are more than one products
   - Make sure the add to cart button is not hidden behind another element
   - Assert the product name is "Product 1"
6. [Browser] For the next dialog, accept the dialog.
7. [Browser] Click add to cart
8. [Browser] Click on cart
9. [Verify] Assert redirect to the cart page (path: /cart)
10. [Verify] Assert cart has the product displayed
```

```text
1. [New Context] Create a new browser context
2. [API] Create a new product by POST to the /api/products endpoint with a randomly generated product name (remember it as <product_name>), and price 100. Note the name and the id of the created product.
3. [Browser] Navigate to the product page (path: /products)
4. [Verify]
   - Ensure there is at least one product displayed
   - Assert the product name is "<product_name>"
   - Assert the product price is 100
```

```text
1. [New Context] Create a new browser context
2. [Browser] Navigate to the homepage (path: /)
3. [Browser] Enter a TODO list item with a title from nanoid(6) (remember it as <todo_title>) for future use.
4. [Browser] Click the add todo button
5. [Verify] Assert that the TODO list item is displayed with the title <todo_title>
```

## Database Testing

If the application uses a database and you need to inject data, set roles, or verify DB state during tests, see `database-testing.md` for how to use `[DB]` steps in test plans.

## External Services

If the application connects to external services, be mindful of side effects. Clean up resources created during tests, and limit notifications sent to third parties. Balance thorough testing with responsible use of external services.
