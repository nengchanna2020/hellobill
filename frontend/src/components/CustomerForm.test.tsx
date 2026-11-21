import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { CustomerForm } from "./CustomerForm";

afterEach(() => vi.restoreAllMocks());

function mockFetch() {
  return vi.spyOn(globalThis, "fetch").mockResolvedValue(
    new Response(JSON.stringify({ id: 1 }), {
      status: 201,
      headers: { "Content-Type": "application/json" },
    }),
  );
}

function type(label: string, value: string) {
  fireEvent.change(screen.getByLabelText(label), { target: { value } });
}

function fill(phone: string) {
  type("Name", "Lotus Bakery");
  type("Email", "a@example.com");
  if (phone) type("Phone (optional)", phone);
  type("Country code", "kh");
  fireEvent.click(screen.getByRole("button", { name: "Add customer" }));
}

describe("CustomerForm phone", () => {
  it("sends the typed phone number", async () => {
    const fetchMock = mockFetch();
    render(<CustomerForm onSaved={() => {}} />);
    fill("+855 12 345 678");
    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    const body = JSON.parse(String(fetchMock.mock.calls[0][1]?.body));
    expect(body.phone).toBe("+855 12 345 678");
  });

  it("sends null when the phone is left empty", async () => {
    const fetchMock = mockFetch();
    render(<CustomerForm onSaved={() => {}} />);
    fill("");
    await waitFor(() => expect(fetchMock).toHaveBeenCalled());
    const body = JSON.parse(String(fetchMock.mock.calls[0][1]?.body));
    expect(body.phone).toBeNull();
  });
});
