import { expect, test } from "@playwright/test";

// The full happy path: add a customer, bill them, send the invoice, get paid.
// Needs Postgres, Mailpit and the API running (see playwright.config.ts).

test("bill a new customer from start to finish", async ({ page, request }) => {
  const name = `Test Customer ${Date.now()}`;

  // Add a customer
  await page.goto("/customers");
  const customerForm = page.getByRole("form", { name: "New customer" });
  await customerForm.getByLabel("Name").fill(name);
  await customerForm.getByLabel("Email").fill("e2e@example.com");
  await customerForm.getByLabel("Country code").fill("KH");
  await customerForm.getByLabel("Currency").selectOption("USD");
  await customerForm.getByRole("button", { name: "Add customer" }).click();
  await expect(page.getByRole("link", { name })).toBeVisible();

  // Make an invoice with two items
  await page.goto("/invoices/new");
  await page.getByLabel("Customer").selectOption({ label: `${name} (USD)` });
  await page.getByLabel("Item 1 description").fill("Website design");
  await page.getByLabel("Item 1 quantity").fill("2");
  await page.getByLabel("Item 1 unit price").fill("150");
  await page.getByRole("button", { name: "Add item" }).click();
  await page.getByLabel("Item 2 description").fill("Domain name");
  await page.getByLabel("Item 2 unit price").fill("20");
  await expect(page.getByTestId("preview-total")).toHaveText("$320.00");
  await page.getByRole("button", { name: "Create invoice" }).click();

  // The new invoice is a draft
  await expect(page.getByRole("heading", { level: 1 })).toContainText(/INV-\d{4}/);
  await expect(page.getByRole("heading", { level: 1 })).toContainText("Draft");
  await expect(page.getByTestId("invoice-total")).toHaveText("$320.00");
  const number = (await page.getByRole("heading", { level: 1 }).textContent())!.match(
    /INV-\d{4}/,
  )![0];

  // Send it, and check the email reached the Mailpit inbox
  await page.getByRole("button", { name: "Send invoice" }).click();
  await expect(page.getByRole("heading", { level: 1 })).toContainText("Sent");
  const mail = await request.get(
    `http://localhost:8025/api/v1/search?query=${encodeURIComponent(`subject:"${number}"`)}`,
  );
  expect((await mail.json()).messages_count).toBeGreaterThan(0);

  // A partial payment, then the rest
  const payment = page.getByRole("form", { name: "Record a payment" });
  await payment.getByLabel(/Amount/).fill("120");
  await payment.getByRole("button", { name: "Record payment" }).click();
  await expect(page.getByTestId("balance-due")).toHaveText("$200.00");

  await payment.getByLabel(/Amount/).fill("200");
  await payment.getByRole("button", { name: "Record payment" }).click();
  await expect(page.getByRole("heading", { level: 1 })).toContainText("Paid");
});

test("dashboard shows money by currency", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Dashboard" })).toBeVisible();
  await expect(page.getByText("Outstanding")).toBeVisible();
  await expect(page.getByRole("heading", { name: "Recent invoices" })).toBeVisible();
});
