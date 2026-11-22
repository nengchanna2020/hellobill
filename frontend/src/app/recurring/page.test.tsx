import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Customer, RecurringPlan } from "@/lib/types";
import RecurringPage from "./page";

const customer: Customer = {
  id: 1,
  name: "Acme Ltd",
  currency: "EUR",
} as Customer;

function plan(autoSend: boolean): RecurringPlan {
  return {
    id: 7,
    customerId: 1,
    customerName: "Acme Ltd",
    currency: "EUR",
    description: "Hosting",
    amount: 49,
    taxRate: 0,
    active: true,
    autoSend,
    nextRunDate: "2026-12-01",
  };
}

type Call = { method: string; path: string; body: unknown };

let calls: Call[];
let currentPlan: RecurringPlan;

function json(data: unknown) {
  return Promise.resolve(new Response(JSON.stringify(data), { status: 200 }));
}

beforeEach(() => {
  calls = [];
  currentPlan = plan(false);
  vi.stubGlobal(
    "fetch",
    vi.fn((url: string, init?: RequestInit) => {
      const method = init?.method ?? "GET";
      const body = init?.body ? JSON.parse(init.body as string) : undefined;
      calls.push({ method, path: url, body });
      if (method === "GET" && url === "/api/recurring-plans") return json([currentPlan]);
      if (method === "GET" && url === "/api/customers") return json([customer]);
      if (method === "POST" && url === "/api/recurring-plans/7/auto-send") {
        currentPlan = plan(body.autoSend);
        return json(currentPlan);
      }
      if (method === "POST" && url === "/api/recurring-plans") return json(plan(body.autoSend));
      return Promise.resolve(new Response("{}", { status: 404 }));
    }),
  );
});

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe("RecurringPage auto-send", () => {
  it("shows the Auto-send column and flips the value through the endpoint", async () => {
    render(<RecurringPage />);

    expect(await screen.findByRole("columnheader", { name: "Auto-send" })).toBeInTheDocument();
    const toggle = await screen.findByRole("button", { name: "Auto-send for Hosting" });
    expect(toggle).toHaveTextContent("Off");

    fireEvent.click(toggle);

    await waitFor(() =>
      expect(screen.getByRole("button", { name: "Auto-send for Hosting" })).toHaveTextContent("On"),
    );
    const post = calls.find((c) => c.method === "POST");
    expect(post).toEqual({
      method: "POST",
      path: "/api/recurring-plans/7/auto-send",
      body: { autoSend: true },
    });
  });

  it("turns auto-send off when it is on", async () => {
    currentPlan = plan(true);
    render(<RecurringPage />);

    const toggle = await screen.findByRole("button", { name: "Auto-send for Hosting" });
    expect(toggle).toHaveTextContent("On");
    fireEvent.click(toggle);

    await waitFor(() =>
      expect(screen.getByRole("button", { name: "Auto-send for Hosting" })).toHaveTextContent(
        "Off",
      ),
    );
    expect(calls.find((c) => c.method === "POST")?.body).toEqual({ autoSend: false });
  });

  it("has an unchecked 'Send automatically' checkbox and sends its value on create", async () => {
    render(<RecurringPage />);

    const checkbox = await screen.findByRole("checkbox", { name: "Send automatically" });
    expect(checkbox).not.toBeChecked();

    fireEvent.change(screen.getByLabelText("Description"), { target: { value: "Support" } });
    fireEvent.change(screen.getByLabelText("Amount"), { target: { value: "25" } });
    fireEvent.click(checkbox);
    fireEvent.click(screen.getByRole("button", { name: "Add plan" }));

    await waitFor(() =>
      expect(calls.some((c) => c.path === "/api/recurring-plans" && c.method === "POST")).toBe(
        true,
      ),
    );
    const create = calls.find((c) => c.method === "POST" && c.path === "/api/recurring-plans");
    expect(create?.body).toEqual({
      customerId: 1,
      description: "Support",
      amount: 25,
      taxRate: 0,
      autoSend: true,
    });
  });
});
