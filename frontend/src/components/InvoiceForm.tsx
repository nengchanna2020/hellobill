"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { api, ApiError } from "@/lib/api";
import { today } from "@/lib/dates";
import { type DraftItem, lineTotal, previewTotals } from "@/lib/invoice-math";
import { formatMoney } from "@/lib/money";
import type { Customer, Invoice } from "@/lib/types";
import { ErrorMessage } from "./ErrorMessage";

const emptyItem = (): DraftItem => ({ description: "", quantity: "1", unitPrice: "" });

interface Props {
  customers: Customer[];
  /** When given, the form edits this draft instead of making a new invoice. */
  invoice?: Invoice;
}

export function InvoiceForm({ customers, invoice }: Props) {
  const router = useRouter();
  const [customerId, setCustomerId] = useState(
    String(invoice?.customer.id ?? customers[0]?.id ?? ""),
  );
  const [issueDate, setIssueDate] = useState(invoice?.issueDate ?? today());
  const [terms, setTerms] = useState(String(invoice?.paymentTermsDays ?? 30));
  const [taxRate, setTaxRate] = useState(String(invoice?.taxRate ?? 0));
  const [discount, setDiscount] = useState(String(invoice?.discountPercent ?? 0));
  const [notes, setNotes] = useState(invoice?.notes ?? "");
  const [items, setItems] = useState<DraftItem[]>(
    invoice?.items.map((i) => ({
      description: i.description,
      quantity: String(i.quantity),
      unitPrice: String(i.unitPrice),
    })) ?? [emptyItem()],
  );
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const customer = customers.find((c) => String(c.id) === customerId);
  const currency = customer?.currency ?? "USD";
  const totals = previewTotals(items, discount, taxRate);

  function updateItem(index: number, change: Partial<DraftItem>) {
    setItems(items.map((item, i) => (i === index ? { ...item, ...change } : item)));
  }

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    const body = {
      customerId: Number(customerId),
      issueDate,
      paymentTermsDays: Number(terms),
      taxRate: Number(taxRate),
      discountPercent: Number(discount),
      notes: notes || null,
      items: items.map((i) => ({
        description: i.description,
        quantity: Number(i.quantity),
        unitPrice: Number(i.unitPrice),
      })),
    };
    try {
      const saved = invoice
        ? await api.put<Invoice>(`/invoices/${invoice.id}`, body)
        : await api.post<Invoice>("/invoices", body);
      router.push(`/invoices/${saved.id}`);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Could not save the invoice");
      setSaving(false);
    }
  }

  if (customers.length === 0) {
    return <p className="muted">Add a customer first, then come back to make an invoice.</p>;
  }

  return (
    <form onSubmit={submit} className="stack" aria-label="Invoice">
      <div className="card grid-2">
        <label>
          Customer
          <select
            value={customerId}
            onChange={(e) => setCustomerId(e.target.value)}
            disabled={Boolean(invoice)}
          >
            {customers.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} ({c.currency})
              </option>
            ))}
          </select>
        </label>
        <label>
          Issue date
          <input type="date" value={issueDate} onChange={(e) => setIssueDate(e.target.value)} />
        </label>
        <label>
          Payment terms (days)
          <input
            type="number"
            min="0"
            max="365"
            value={terms}
            onChange={(e) => setTerms(e.target.value)}
          />
        </label>
        <label>
          Tax rate (%)
          <input
            type="number"
            min="0"
            max="100"
            step="0.01"
            value={taxRate}
            onChange={(e) => setTaxRate(e.target.value)}
          />
        </label>
        <label>
          Discount (%)
          <input
            type="number"
            min="0"
            max="100"
            step="0.01"
            value={discount}
            onChange={(e) => setDiscount(e.target.value)}
          />
        </label>
        <label>
          Notes
          <input value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="Optional" />
        </label>
      </div>

      <div className="card">
        <h3>Items</h3>
        <table className="table items">
          <thead>
            <tr>
              <th>Description</th>
              <th className="num">Qty</th>
              <th className="num">Unit price</th>
              <th className="num">Line total</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {items.map((item, index) => (
              <tr key={index}>
                <td>
                  <input
                    aria-label={`Item ${index + 1} description`}
                    value={item.description}
                    onChange={(e) => updateItem(index, { description: e.target.value })}
                    required
                  />
                </td>
                <td className="num">
                  <input
                    aria-label={`Item ${index + 1} quantity`}
                    type="number"
                    step="0.01"
                    min="0.01"
                    value={item.quantity}
                    onChange={(e) => updateItem(index, { quantity: e.target.value })}
                    required
                  />
                </td>
                <td className="num">
                  <input
                    aria-label={`Item ${index + 1} unit price`}
                    type="number"
                    step="0.01"
                    min="0"
                    value={item.unitPrice}
                    onChange={(e) => updateItem(index, { unitPrice: e.target.value })}
                    required
                  />
                </td>
                <td className="num">{formatMoney(lineTotal(item), currency)}</td>
                <td>
                  {items.length > 1 && (
                    <button
                      type="button"
                      className="link"
                      onClick={() => setItems(items.filter((_, i) => i !== index))}
                    >
                      Remove
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <button
          type="button"
          className="secondary"
          onClick={() => setItems([...items, emptyItem()])}
        >
          Add item
        </button>
      </div>

      <div className="card totals" aria-label="Preview totals">
        <div>
          <span>Subtotal</span>
          <span>{formatMoney(totals.subtotal, currency)}</span>
        </div>
        <div>
          <span>Discount</span>
          <span>−{formatMoney(totals.discount, currency)}</span>
        </div>
        <div>
          <span>Tax</span>
          <span>{formatMoney(totals.tax, currency)}</span>
        </div>
        <div className="grand">
          <span>Total</span>
          <span data-testid="preview-total">{formatMoney(totals.total, currency)}</span>
        </div>
      </div>

      <ErrorMessage message={error} />
      <div>
        <button type="submit" disabled={saving}>
          {saving ? "Saving…" : invoice ? "Save draft" : "Create invoice"}
        </button>
      </div>
    </form>
  );
}
