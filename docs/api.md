# HelloBill API

Every endpoint, one line each, with an example. The `add-api-endpoint` skill adds a line here
for every new endpoint. Errors are problem-details JSON (RFC 9457), with a `fields` map for
form errors: 400 for bad input, 404 when something doesn't exist.

| Method | Path | What it does | Example |
|---|---|---|---|
| GET | `/api/customers` | all customers, by name | `curl localhost:8080/api/customers` |
| GET | `/api/customers/{id}` | one customer | `curl localhost:8080/api/customers/1` |
| POST | `/api/customers` | add a customer (201) | `{"name": "Maple Studio", "email": "a@example.com", "country": "CA", "currency": "CAD"}` |
| PUT | `/api/customers/{id}` | change a customer | same body as POST |
| GET | `/api/customers/{id}/payments` | payments on a customer's invoices, newest first | `curl localhost:8080/api/customers/1/payments` |
| GET | `/api/dashboard` | totals for the home page | `curl localhost:8080/api/dashboard` |
| GET | `/api/invoices` | invoices, newest first (`?status=` and `?customerId=` filter) | `curl localhost:8080/api/invoices?status=SENT` |
| GET | `/api/invoices/{id}` | one invoice with items and totals | `curl localhost:8080/api/invoices/1` |
| POST | `/api/invoices` | a draft invoice (201) | `{"customerId": 1, "taxRate": 8, "discountPercent": 0, "items": [...]}` |
| PUT | `/api/invoices/{id}` | change a draft | same body as POST |
| DELETE | `/api/invoices/{id}` | delete a draft (204) | `curl -X DELETE localhost:8080/api/invoices/7` |
| POST | `/api/invoices/{id}/send` | email it, status SENT | `curl -X POST localhost:8080/api/invoices/7/send` |
| GET | `/api/invoices/{invoiceId}/payments` | payments for an invoice | `curl localhost:8080/api/invoices/1/payments` |
| POST | `/api/invoices/{invoiceId}/payments` | record a payment (201) | `{"amount": 100.00, "method": "BANK_TRANSFER"}` |
| GET | `/api/recurring-plans` | monthly billing plans | `curl localhost:8080/api/recurring-plans` |
| POST | `/api/recurring-plans` | add a plan (201) | `{"customerId": 1, "description": "Hosting", "amount": 49.00}` |
| POST | `/api/recurring-plans/{id}/pause` | pause a plan | |
| POST | `/api/recurring-plans/{id}/resume` | resume a plan | |
| POST | `/api/recurring-plans/run` | run monthly billing now | |
