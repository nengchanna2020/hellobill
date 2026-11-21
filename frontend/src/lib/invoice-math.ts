// A live preview of invoice totals for the invoice form, so people see the
// total while they type. Works in whole cents to avoid floating point errors.
// The API works out the real totals when the invoice is saved.

export interface DraftItem {
  description: string;
  quantity: string;
  unitPrice: string;
}

export interface PreviewTotals {
  subtotal: number;
  discount: number;
  tax: number;
  total: number;
}

const toCents = (value: number) => Math.round(value * 100);
const fromCents = (cents: number) => cents / 100;

function parse(value: string): number {
  const n = Number(value);
  return Number.isFinite(n) ? n : 0;
}

export function lineTotal(item: DraftItem): number {
  return fromCents(Math.round(parse(item.quantity) * toCents(parse(item.unitPrice))));
}

export function previewTotals(
  items: DraftItem[],
  discountPercent: string,
  taxRate: string,
): PreviewTotals {
  const subtotal = items.reduce(
    (sum, item) => sum + Math.round(parse(item.quantity) * toCents(parse(item.unitPrice))),
    0,
  );
  const discount = Math.round((subtotal * parse(discountPercent)) / 100);
  const taxable = subtotal - discount;
  const tax = Math.round((taxable * parse(taxRate)) / 100);
  return {
    subtotal: fromCents(subtotal),
    discount: fromCents(discount),
    tax: fromCents(tax),
    total: fromCents(taxable + tax),
  };
}
