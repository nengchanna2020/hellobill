import { lineTotal, previewTotals } from "./invoice-math";

const item = (quantity: string, unitPrice: string) => ({ description: "x", quantity, unitPrice });

describe("previewTotals", () => {
  it("adds up the lines", () => {
    const t = previewTotals([item("3", "40"), item("1", "15.50")], "0", "0");
    expect(t.subtotal).toBe(135.5);
    expect(t.total).toBe(135.5);
  });

  it("adds tax", () => {
    const t = previewTotals([item("1", "200")], "0", "10");
    expect(t.tax).toBe(20);
    expect(t.total).toBe(220);
  });

  it("takes off a discount", () => {
    const t = previewTotals([item("4", "25")], "15", "0");
    expect(t.discount).toBe(15);
    expect(t.total).toBe(85);
  });

  it("does not suffer from floating point errors", () => {
    const t = previewTotals([item("1", "0.1"), item("1", "0.2")], "0", "0");
    expect(t.total).toBe(0.3);
  });

  it("treats empty fields as zero", () => {
    const t = previewTotals([item("", "")], "", "");
    expect(t.total).toBe(0);
  });
});

describe("lineTotal", () => {
  it("rounds to cents", () => {
    expect(lineTotal(item("1.5", "9.99"))).toBe(14.99);
  });
});
