# How the backend fits together

- Package by feature under `com.hellobill`: `customer`, `invoice`, `payment`, `recurring`, `dashboard`,
  `mail`, and `common` for shared code. Each feature has a Controller, a Service and a Repository,
  plus Request and Response records.
- Flyway owns the schema (`backend/src/main/resources/db/migration/`). Hibernate only validates it.
- Totals: `invoice/InvoiceCalculator` with the helpers in `common/Money`. The frontend's invoice
  preview (`frontend/src/lib/invoice-math.ts`) does the same maths while you type.
- Invoice statuses: draft, sent, paid, overdue. Only drafts can be edited or deleted.
- Jobs: `OverdueJob` (01:00) and `RecurringBillingJob` (06:00) also run once when the API starts.
  `POST /api/recurring-plans/run` runs monthly billing now.
- Errors are problem-details JSON (RFC 9457) with a `fields` map for form errors.
