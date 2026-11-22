---
name: testing-guide
description: Testing guide. Use when writing or changing code, so the tests are right.
---

# Testing guide


## What to test

- Every behaviour change gets a test.
- Test real values, not just that something is not null.
- Test the edges: zero, one, the limit, one past the limit.
- Test the error paths as well as the happy path.
- A bug fix starts with a failing test.
- Don't test framework code.
- Don't test private methods directly.
- One reason to fail per test.

## Backend tests

- Unit tests for calculators and helpers: plain JUnit 5 and AssertJ.
- API tests extend IntegrationTest: real Postgres with Testcontainers.
- Use MockMvc and jsonPath for API responses.
- Every test starts with empty tables.
- Pin dates with a fixed Clock.
- Mock the mail sender, never send real mail.
- Use isEqualByComparingTo for BigDecimal.
- Run with backend/gradlew -p backend test.

## Frontend tests

- Vitest and Testing Library for components.
- Test what the user sees, not implementation details.
- Mock fetch at the edge.
- Use findBy for async content.
- Keep test data small and readable.
- Run with npm test in frontend.
- Playwright for end-to-end flows only.
- Never snapshot whole pages.

## Test style

- Arrange, act, assert, with a blank line between.
- Names say the behaviour.
- No logic in tests: no loops or ifs.
- Use helpers for setup that repeats.
- Keep each test independent.
- Never disable a test without an issue link.
- A flaky test is a bug: fix it.
- Delete tests that test nothing.

