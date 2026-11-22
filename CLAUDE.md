# HelloBill

A demo billing app with fake data. Next.js frontend in `frontend/`, Spring Boot API in `backend/`,
Postgres and Mailpit in `docker-compose.yml`. Each folder has its own CLAUDE.md with its commands.

## Before you start

- `docker compose up -d` starts Postgres and Mailpit. The backend tests need Docker too.
- Never run `docker compose down -v` without asking me: it deletes the dev database.

## Rules for every change

- Money is `BigDecimal` in Java and `NUMERIC(12,2)` in Postgres, rounded half up. Never `double`.
- Database changes go in a new Flyway file (`V<next>__what.sql`). Never edit an old one.
- New behaviour gets its own test, and the test checks the real value, not just that it exists.
- Change files with your Edit and Write tools, not with shell commands (sed, python, cat >).
  The hooks format and test every edit, and they only see those tools.
- Before you say done, run the checks for every folder you changed, and show me the result.
- Then commit once, with a short subject that starts with a verb, like the git log.
- For a change to several files, get a plan from the `planner` agent first, and a review from the
  `reviewer` agent before you commit.
- New features start with a spec: `/spec`, then a person approves it, then `/feature NNN`. Bugs: `/fix`.

## When you need more

- How the backend fits together: `docs/agent/architecture.md`
- How to write tests here: `docs/agent/testing.md`
- GitHub, the browser and the database (MCP servers): `docs/agent/mcp-servers.md`
- The team of subagents and what each one does: `.claude/README.md`
