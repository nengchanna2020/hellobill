"use client";

import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useState } from "react";
import { ErrorMessage } from "@/components/ErrorMessage";
import { PaymentForm } from "@/components/PaymentForm";
import { StatusBadge } from "@/components/StatusBadge";
import { api, ApiError } from "@/lib/api";
import { formatDate } from "@/lib/dates";
import { formatMoney } from "@/lib/money";
import type { Invoice } from "@/lib/types";
import { useApi } from "@/lib/use-api";

export default function InvoicePage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { data: invoice, error, setData } = useApi<Invoice>(`/invoices/${id}`);
  const [actionError, setActionError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function send() {
    setBusy(true);
    setActionError(null);
    try {
      setData(await api.post<Invoice>(`/invoices/${id}/send`));
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : "Could not send the invoice");
    } finally {
      setBusy(false);
    }
  }

  async function remove() {
    if (!window.confirm("Delete this draft?")) return;
    try {
      await api.delete(`/invoices/${id}`);
      router.push("/invoices");
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : "Could not delete the invoice");
    }
  }

  if (error) return <ErrorMessage message={error} />;
  if (!invoice) return <p className="muted">Loading…</p>;

  const money = (amount: number) => formatMoney(amount, invoice.currency);
  const canPay = invoice.status === "SENT" || invoice.status === "OVERDUE";

  return (
    <>
      <div className="page-head">
        <h1>
          {invoice.number} <StatusBadge status={invoice.status} />
        </h1>
        <div className="actions">
          {invoice.status === "DRAFT" && (
            <>
              <Link href={`/invoices/${invoice.id}/edit`} className="button secondary">
                Edit
              </Link>
              <button type="button" className="secondary danger" onClick={remove}>
                Delete
              </button>
            </>
          )}
          {invoice.status !== "PAID" && (
            <button type="button" onClick={send} disabled={busy}>
              {invoice.status === "DRAFT" ? "Send invoice" : "Send again"}
            </button>
          )}
        </div>
      </div>
      <ErrorMessage message={actionError} />

      <section className="card grid-2">
        <div>
          <span className="label">Bill to</span>
          <p>
            <Link href={`/customers/${invoice.customer.id}`}>{invoice.customer.name}</Link>
            <br />
            <span className="muted">{invoice.customer.email}</span>
          </p>
        </div>
        <div>
          <span className="label">Issued</span>
          <p>
            {formatDate(invoice.issueDate)}
            <br />
            <span className="muted">Payment terms: {invoice.paymentTermsDays} days</span>
          </p>
        </div>
        {invoice.sentAt && (
          <div>
            <span className="label">Sent</span>
            <p>{formatDate(invoice.sentAt)}</p>
          </div>
        )}
        {invoice.notes && (
          <div>
            <span className="label">Notes</span>
            <p>{invoice.notes}</p>
          </div>
        )}
      </section>

      <section className="card">
        <table className="table">
          <thead>
            <tr>
              <th>Description</th>
              <th className="num">Qty</th>
              <th className="num">Unit price</th>
              <th className="num">Line total</th>
            </tr>
          </thead>
          <tbody>
            {invoice.items.map((item, i) => (
              <tr key={i}>
                <td>{item.description}</td>
                <td className="num">{item.quantity}</td>
                <td className="num">{money(item.unitPrice)}</td>
                <td className="num">{money(item.lineTotal)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="totals">
          <div>
            <span>Subtotal</span>
            <span>{money(invoice.subtotal)}</span>
          </div>
          {invoice.discount > 0 && (
            <div>
              <span>Discount ({invoice.discountPercent}%)</span>
              <span>−{money(invoice.discount)}</span>
            </div>
          )}
          {invoice.tax > 0 && (
            <div>
              <span>Tax ({invoice.taxRate}%)</span>
              <span>{money(invoice.tax)}</span>
            </div>
          )}
          <div className="grand">
            <span>Total</span>
            <span data-testid="invoice-total">{money(invoice.total)}</span>
          </div>
          {invoice.amountPaid > 0 && (
            <>
              <div>
                <span>Paid</span>
                <span>−{money(invoice.amountPaid)}</span>
              </div>
              <div className="grand">
                <span>Balance due</span>
                <span data-testid="balance-due">{money(invoice.balanceDue)}</span>
              </div>
            </>
          )}
        </div>
      </section>

      {invoice.payments.length > 0 && (
        <section className="card">
          <h3>Payments</h3>
          <table className="table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Method</th>
                <th className="num">Amount</th>
              </tr>
            </thead>
            <tbody>
              {invoice.payments.map((p) => (
                <tr key={p.id}>
                  <td>{formatDate(p.paidOn)}</td>
                  <td>{p.method.replace("_", " ").toLowerCase()}</td>
                  <td className="num">{money(p.amount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {canPay && <PaymentForm invoice={invoice} onPaid={setData} />}
    </>
  );
}
