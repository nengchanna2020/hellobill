# Plan for 001

## Approach
Backend and frontend split cleanly on the API contract in the spec, so two builders can work in parallel.
The backend builder owns all Java, the migration and `docs/api.md`. The frontend builder owns the Next files.

Key design point: `RecurringBillingService.runDuePlans()` is `@Transactional`. Calling `invoices.send()` inside it
would let a mail exception mark the whole transaction rollback-only and lose the other plans in the run. Call
`mailer.sendInvoice(...)` directly in a try/catch per plan, and set `SENT` and `sentAt` only on success. A failure
is logged, the invoice stays `DRAFT`, and `nextRunDate` still moves on.

The spec's fixed dates (2026-12-01, 2026-12-03) need a pinned clock: new tests override the `Clock` bean
(from `ClockConfig`) with a test-only `@Primary` fixed clock.

## Files
- T1 (backend):
  - backend/src/main/resources/db/migration/V7__add_recurring_plan_auto_send.sql (new)
  - backend/src/main/java/com/hellobill/recurring/RecurringPlan.java
  - backend/src/main/java/com/hellobill/recurring/RecurringPlanRequest.java
  - backend/src/main/java/com/hellobill/recurring/RecurringPlanResponse.java
  - backend/src/main/java/com/hellobill/recurring/RecurringBillingService.java
  - backend/src/main/java/com/hellobill/recurring/RecurringPlanController.java
  - backend/src/main/java/com/hellobill/recurring/AutoSendRequest.java (new)
  - backend/src/test/java/com/hellobill/recurring/RecurringBillingTest.java
  - docs/api.md
- T2 (frontend):
  - frontend/src/lib/types.ts
  - frontend/src/app/recurring/page.tsx
  - frontend/src/app/recurring/page.test.tsx (new)
