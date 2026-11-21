"use client";

import { useState } from "react";
import { api, ApiError } from "@/lib/api";
import { today } from "@/lib/dates";
import type { Invoice } from "@/lib/types";
import { ErrorMessage } from "./ErrorMessage";

const METHODS = ["BANK_TRANSFER", "CARD", "CASH", "OTHER"];

/** Records a full or partial payment against an invoice. */
export function PaymentForm({
  invoice,
  onPaid,
}: {
  invoice: Invoice;
  onPaid: (i: Invoice) => void;
}) {
  const [amount, setAmount] = useState(String(invoice.balanceDue));
  const [paidOn, setPaidOn] = useState(today());
  const [method, setMethod] = useState(METHODS[0]);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const updated = await api.post<Invoice>(`/invoices/${invoice.id}/payments`, {
        amount: Number(amount),
        paidOn,
        method,
      });
      setAmount(String(updated.balanceDue));
      onPaid(updated);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Could not record the payment");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={submit} className="card form-inline" aria-label="Record a payment">
      <h3>Record a payment</h3>
      <label>
        Amount ({invoice.currency})
        <input
          type="number"
          step="0.01"
          min="0.01"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          required
        />
      </label>
      <label>
        Paid on
        <input type="date" value={paidOn} onChange={(e) => setPaidOn(e.target.value)} required />
      </label>
      <label>
        Method
        <select value={method} onChange={(e) => setMethod(e.target.value)}>
          {METHODS.map((m) => (
            <option key={m} value={m}>
              {m.replace("_", " ").toLowerCase()}
            </option>
          ))}
        </select>
      </label>
      <button type="submit" disabled={saving}>
        {saving ? "Saving…" : "Record payment"}
      </button>
      <ErrorMessage message={error} />
    </form>
  );
}
