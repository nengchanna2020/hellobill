import { fireEvent, render, screen } from "@testing-library/react";
import type { Customer } from "@/lib/types";
import { InvoiceForm } from "./InvoiceForm";

vi.mock("next/navigation", () => ({ useRouter: () => ({ push: vi.fn() }) }));

const customer = (id: number, name: string, terms: number): Customer => ({
  id,
  name,
  email: `${name}@example.com`,
  phone: null,
  country: "KH",
  currency: "USD",
  defaultPaymentTermsDays: terms,
  createdAt: "2026-01-01T00:00:00Z",
});

describe("InvoiceForm payment terms", () => {
  const customers = [customer(1, "Lotus", 30), customer(2, "Kranich", 14)];

  it("starts with the first customer's terms", () => {
    render(<InvoiceForm customers={[customer(1, "Lotus", 45)]} />);
    expect(screen.getByLabelText("Payment terms (days)")).toHaveValue(45);
  });

  it("switches to the terms of the customer you pick", () => {
    render(<InvoiceForm customers={customers} />);
    fireEvent.change(screen.getByLabelText("Customer"), { target: { value: "2" } });
    expect(screen.getByLabelText("Payment terms (days)")).toHaveValue(14);
  });
});
