# HelloBill principles

Every spec, plan and review checks against these. Keep this page short.

1. **Money is exact.** `BigDecimal` and `NUMERIC(12,2)`, rounded half up. Never `double`.
2. **Every behaviour has a test that checks a real value.** A disabled or empty test is no test.
3. **Small, reversible changes.** A new Flyway file for every database change, never an edit to an old one.
4. **The API is the contract.** The backend and the frontend agree on field names in the spec before
   anyone builds. Each builder owns its own files.
5. **Nothing leaves without a person.** Emails go to Mailpit in dev. A person approves the spec, and a
   person merges the pull request.
