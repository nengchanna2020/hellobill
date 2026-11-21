# How to write tests here

Backend (JUnit 5):
- API tests extend `IntegrationTest`: the whole app on a real Postgres in Docker (Testcontainers),
  mail mocked, demo data off, tables emptied before each test. Use its helpers (`createCustomer`,
  `createInvoice`, `send`) instead of writing JSON by hand.
- Pure logic (like `InvoiceCalculator`) gets a plain unit test, no Spring.
- For dates, use a fixed `Clock` instead of today, so the test gives the same result every day.

Frontend:
- Vitest and Testing Library, next to the file: `money.ts` → `money.test.ts`.
- Playwright end-to-end tests in `frontend/e2e/` need `docker compose up -d` and the API running.
