import { describe, expect, it } from "vitest";
import { formatDate } from "../lib/dates";

describe("formatDate with a date it can't read", () => {
  it("shows an en dash", () => {
    expect(formatDate("2026-13-45")).toBe("–");
    expect(formatDate("soon")).toBe("–");
    expect(formatDate(null)).toBe("–");
  });
  it("still formats real dates", () => {
    expect(formatDate("2026-03-01")).toBe("1 Mar 2026");
  });
});
