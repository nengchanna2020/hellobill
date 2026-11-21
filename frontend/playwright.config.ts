import { defineConfig, devices } from "@playwright/test";

// End-to-end tests drive the real app in a browser.
// Before running them, start the database and the API:
//   docker compose up -d        (from the repo root)
//   ./gradlew bootRun           (in backend/)
// Playwright starts the Next.js dev server itself.
export default defineConfig({
  testDir: "./e2e",
  timeout: 30_000,
  retries: process.env.CI ? 1 : 0,
  use: {
    baseURL: "http://localhost:3000",
    trace: "retain-on-failure",
    // Optional: point at a Chromium you already have instead of `npx playwright install`.
    launchOptions: { executablePath: process.env.PLAYWRIGHT_CHROMIUM_PATH || undefined },
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: {
    command: "npm run dev",
    url: "http://localhost:3000",
    reuseExistingServer: true,
    timeout: 120_000,
  },
});
