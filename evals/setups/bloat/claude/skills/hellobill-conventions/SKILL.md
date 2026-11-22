---
name: hellobill-conventions
description: HelloBill conventions and project knowledge. Use whenever working in this repository.
---

# HelloBill conventions


## Backend layout

- Each feature has its own package: customer, invoice, payment, recurring, dashboard.
- A controller only maps HTTP to the service; no logic in controllers.
- Services hold the logic and are @Transactional where they write.
- Repositories are Spring Data interfaces.
- Requests and responses are records.
- Entities are never returned from controllers.
- Money is BigDecimal with scale 2, rounded half up, via the Money class.
- Dates are LocalDate; the Clock bean gives today.

## Database

- Every change is a new Flyway file: V<n>__what.sql.
- Never edit an old migration.
- Money columns are NUMERIC(12,2).
- Foreign keys have indexes.
- Use NOT NULL with a default for new columns on existing tables.
- Names are snake_case.
- No business logic in SQL.
- Test migrations on a scratch database.

## API

- Paths are plural nouns: /api/invoices.
- Use 201 for create, 200 for read and update, 204 for delete.
- 400 for bad input, 404 for a missing id, 409 for a conflict.
- Lists are sorted in a stable order.
- Field names are camelCase.
- Every endpoint is listed in docs/api.md.
- Optional query parameters have sensible defaults.
- Never break an existing field: add new ones.

## Frontend layout

- Pages live in frontend/src/app, one folder per route.
- Shared components live in frontend/src/components.
- Helpers live in frontend/src/lib with a test next to them.
- Types for API data live in frontend/src/lib/types.ts.
- Fetch data with the useApi hook.
- Format money with formatMoney and dates with formatDate.
- No inline styles; use the classes in globals.css.
- Read node_modules/next/dist/docs before using a Next.js API.

## Git

- Small commits with a clear subject in the imperative.
- One change per commit.
- Never commit .env files.
- Run the checks before committing.
- Rebase on main before opening a pull request.
- Pull requests describe what, why and how it was tested.
- Link the issue in the pull request.
- Never push to main.

