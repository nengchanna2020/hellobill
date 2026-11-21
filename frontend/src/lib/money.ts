// Formatting money for display. The API does the real maths with BigDecimal;
// these helpers only show amounts.

/** Formats an amount in a currency, for example formatMoney(1234.5, "USD") = "$1,234.50". */
export function formatMoney(amount: number | string, currency: string): string {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
    // No fraction overrides: each currency uses its own digits (USD 2, JPY 0).
  }).format(Number(amount));
}

/** Formats a list of per-currency amounts, for example "$1,200.00 · €350.00". */
export function formatAmounts(amounts: { currency: string; amount: number }[]): string {
  if (amounts.length === 0) return "–";
  return amounts.map((a) => formatMoney(a.amount, a.currency)).join(" · ");
}
