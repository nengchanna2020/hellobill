import { describe, expect, it } from "vitest";
import { previewTotals } from "../lib/invoice-math";

const items = [{ description: "Design", quantity: "1", unitPrice: "100" }];

describe("previewTotals with a discount out of range", () => {
  it("treats a discount above 100 as 100", () => {
    const t = previewTotals(items, "150", "10");
    expect(t.discount).toBe(100);
    expect(t.total).toBe(0);
  });
  it("treats a negative discount as 0", () => {
    const t = previewTotals(items, "-10", "0");
    expect(t.discount).toBe(0);
    expect(t.total).toBe(100);
  });
  it("keeps normal discounts", () => {
    const t = previewTotals(items, "10", "10");
    expect(t.discount).toBe(10);
    expect(t.total).toBe(99);
  });
});
