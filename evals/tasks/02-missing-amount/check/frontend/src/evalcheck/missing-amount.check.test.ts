import { describe, expect, it } from "vitest";
import { formatMoney } from "../lib/money";

describe("formatMoney with a missing amount", () => {
  it("shows an en dash for null, undefined, an empty string and text", () => {
    expect(formatMoney(null as never, "USD")).toBe("–");
    expect(formatMoney(undefined as never, "USD")).toBe("–");
    expect(formatMoney("", "USD")).toBe("–");
    expect(formatMoney("abc", "USD")).toBe("–");
  });
  it("still formats real amounts", () => {
    expect(formatMoney(1234.5, "USD")).toBe("$1,234.50");
    expect(formatMoney("0", "USD")).toBe("$0.00");
    expect(formatMoney("-3", "USD")).toBe("-$3.00");
  });
});
