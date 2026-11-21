# Backend: Spring Boot 4, Java 21, Gradle

Run Gradle from the repo root, exactly as written below, with no `cd` and no `&&` (your shell
starts at the repo root every time). A pipe into `tail`, `head` or `grep` is fine. The sandbox
lets only that shape out, because the tests need Docker:

- Checks: `backend/gradlew -p backend spotlessApply test` (CI runs `spotlessCheck test`)
- One test class: `backend/gradlew -p backend test --tests com.hellobill.invoice.InvoiceCalculatorTest`
- Start the API: `backend/gradlew -p backend bootRun` (port 8080, fills an empty database with demo data)

Rules:

- Get today's date from the injected `Clock` (`common/ClockConfig`), never a bare `LocalDate.now()`.
- Throw `BadRequestException` or `NotFoundException`. `ApiErrorHandler` turns them into problem details.
