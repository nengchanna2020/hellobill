# 001: Auto-send monthly invoices

Status: approved
<!-- Only a person changes this line to "Status: approved". The hook blocks code edits until then. -->

## What and why
A recurring plan can be set to auto-send. When monthly billing makes an invoice for such a plan, the
invoice is emailed to the customer straight away and its status is `SENT`, instead of waiting as a
`DRAFT` for someone to send it. Plans without auto-send behave exactly as today. This saves someone
sending every monthly invoice by hand on the 1st.

## Acceptance criteria
1. Given a plan for "Hosting", amount 49.00, taxRate 0.00, `autoSend` true, nextRunDate 2026-12-01, when
   billing runs on 2026-12-01, then one invoice is created with total 49.00, status `SENT` and a
   `sentAt` value, and one email with that invoice goes to the customer's address (Mailpit in dev).
2. Given the same plan with `autoSend` false, when billing runs on 2026-12-01, then the invoice has
   status `DRAFT`, `sentAt` is null and no email is sent.
3. Given a plan created without an `autoSend` field, then the response has `autoSend: false`.
   Plans that already exist in the database are `autoSend: false` after the migration.
4. Given a plan with `autoSend` true, when `POST /api/recurring-plans/{id}/auto-send` is called with
   `{"autoSend": false}`, then the response has `autoSend: false`, and the next run makes a `DRAFT`.
   The same call with `true` turns it on. An unknown id returns 404.
5. Given an `autoSend` plan and a mail server that fails, when billing runs, then that invoice stays
   `DRAFT` with `sentAt` null, the failure is logged, the plan's nextRunDate still moves from
   2026-12-01 to 2027-01-01, and other due plans in the same run are still billed (and sent).
6. Given an `autoSend` plan with nextRunDate 2026-12-01 and the app was down, when billing runs on
   2026-12-03, then the invoice date is 2026-12-01 and it is sent.
7. Given an `autoSend` plan is due, when `POST /api/recurring-plans/run` is called, then the same
   happens as for the scheduled job: the invoice is `SENT` in the response.
8. Given a paused plan with `autoSend` true, when billing runs, then nothing is created or sent.
9. Frontend: the new-plan form has a "Send automatically" checkbox (off by default); the plans table
   has an "Auto-send" column whose toggle calls the endpoint above and shows the new value.

## API contract
- `RecurringPlanRequest` (POST `/api/recurring-plans`): new optional field `autoSend` (boolean,
  default false). Other fields unchanged.
- `RecurringPlanResponse`: new field `autoSend` (boolean, always present).
- `POST /api/recurring-plans/{id}/auto-send`, body `{"autoSend": true|false}` (required, not null).
  Returns 200 with the updated `RecurringPlanResponse`; 404 if the plan doesn't exist; 400 if the body
  has no `autoSend`.
- `POST /api/recurring-plans/run`: unchanged shape. It returns `Invoice[]`; those that were sent have
  `status: "SENT"` and a `sentAt`.
- Database: a new Flyway file `V7__add_recurring_plan_auto_send.sql` adds
  `recurring_plans.auto_send BOOLEAN NOT NULL DEFAULT FALSE`.
- Docs: add the new endpoint and field to `docs/api.md`. Update `frontend/src/lib/types.ts`.

## Out of scope
- Changing the email's content, or sending reminders.
- Retrying a failed send automatically (it can be sent by hand with `POST /api/invoices/{id}/send`).
- Per-customer auto-send settings, or a different send time than the existing 06:00 job.

## Open questions
None.
