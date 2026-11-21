# Backend: Spring Boot 4, Java 21, Gradle

Run from `backend/`:

- Checks: `./gradlew spotlessApply test` (CI runs `spotlessCheck test`)
- One test class: `./gradlew test --tests com.hellobill.invoice.InvoiceCalculatorTest`
- Start the API: `./gradlew bootRun` (port 8080, fills an empty database with demo data)

Rules:

- Get today's date from the injected `Clock` (`common/ClockConfig`), never a bare `LocalDate.now()`.
- Throw `BadRequestException` or `NotFoundException`. `ApiErrorHandler` turns them into problem details.
