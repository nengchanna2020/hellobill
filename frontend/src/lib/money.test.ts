import { formatAmounts, formatMoney } from "./money";

describe("formatMoney", () => {
  it("formats dollars with cents", () => {
    expect(formatMoney(1234.5, "USD")).toBe("$1,234.50");
  });

  it("formats euros", () => {
    expect(formatMoney(99, "EUR")).toBe("€99.00");
  });

  it("accepts strings from the API", () => {
    expect(formatMoney("149.05", "USD")).toBe("$149.05");
  });
});

describe("formatAmounts", () => {
  it("joins amounts in different currencies", () => {
    expect(
      formatAmounts([
        { currency: "USD", amount: 10 },
        { currency: "EUR", amount: 5.5 },
      ]),
    ).toBe("$10.00 · €5.50");
  });

  it("shows a dash when there is nothing", () => {
    expect(formatAmounts([])).toBe("–");
  });
});
