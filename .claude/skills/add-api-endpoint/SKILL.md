---
name: add-api-endpoint
description: Backend helpers.
---

# Add an API endpoint

Follow these steps in order. They're how every endpoint in HelloBill is built.

1. **Find the feature package** under `backend/src/main/java/com/hellobill/` (`customer`, `invoice`,
   `payment`, `recurring`, `dashboard`). New feature? New package, same shape.
2. **Controller**: thin. Copy the shape of `templates/Controller.java`. No logic, no repository calls.
3. **Service**: the logic, `@Transactional` (`readOnly = true` for reads). Shape: `templates/Service.java`.
   - Missing thing → `throw new NotFoundException("Customer 42 not found")` (it becomes a 404).
   - Bad input the annotations can't catch → `throw new BadRequestException("...")` (a 400).
   - Today's date comes from the injected `Clock`, never `LocalDate.now()`.
4. **Request and response**: Java records. Validate the request with annotations. Money is
   `BigDecimal` with `@Digits(integer = 10, fraction = 2)`. Responses have a `static from(Entity)`.
5. **Test**: `templates/ControllerTest.java`. Extend `IntegrationTest` and use its helpers
   (`createCustomer`, `createInvoice`, `send`). Three tests at least: the happy path with real
   values, a 404, and a 400.
6. **Document it**: add one row to `docs/api.md` (method, path, what it does, an example).
7. **Check**: `backend/gradlew -p backend spotlessApply test` from the repo root.

If the endpoint needs a new table or column, use the `db-migration` skill first.
