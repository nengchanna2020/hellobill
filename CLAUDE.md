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
- Before you say done, run the checks for every folder you changed, and show me the result.
- Then commit once, with a short subject that starts with a verb, like the git log.

## When you need more

- How the backend fits together: `docs/agent/architecture.md`
- How to write tests here: `docs/agent/testing.md`
