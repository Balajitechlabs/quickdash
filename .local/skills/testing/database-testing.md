---
name: database-testing
description: Complements the `testing` skill with direct database access during Playwright tests. Read this skill if a) you need to use the playwright testing skill and b) the application uses a database that you want to query or mutate during tests.
---

The testing subagent has direct access to the integration environment database. Use `[DB]` steps to:

- Inject test data before browser interactions
- Set user roles for multi-role applications
- Verify database state after actions

Provide relevant DB schemas in your message so the testing subagent can construct correct queries.

# Example Test Plan

```text
1. [New Context] Create a new browser context
... perform register steps ...

n. [DB] Update the user so it's an admin by "UPDATE users SET is_admin = true WHERE email = 'admin@example.com'"
... perform other steps ...

k. [DB] Check that the number of likes for the product has increased by 1 using the query: SELECT * FROM products WHERE id = '${product1Id}'
```
