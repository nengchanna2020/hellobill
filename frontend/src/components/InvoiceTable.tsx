import Link from "next/link";
import { formatDate } from "@/lib/dates";
import { formatMoney } from "@/lib/money";
import type { InvoiceSummary } from "@/lib/types";
import { StatusBadge } from "./StatusBadge";

export function InvoiceTable({ invoices }: { invoices: InvoiceSummary[] }) {
  if (invoices.length === 0) {
    return <p className="muted">No invoices yet.</p>;
  }
  return (
    <table className="table">
      <thead>
        <tr>
          <th>Number</th>
          <th>Customer</th>
          <th>Issued</th>
          <th>Due</th>
          <th>Status</th>
          <th className="num">Total</th>
          <th className="num">Balance due</th>
        </tr>
      </thead>
      <tbody>
        {invoices.map((invoice) => (
          <tr key={invoice.id}>
            <td>
              <Link href={`/invoices/${invoice.id}`}>{invoice.number}</Link>
            </td>
            <td>{invoice.customerName}</td>
            <td>{formatDate(invoice.issueDate)}</td>
            <td>{formatDate(invoice.dueDate)}</td>
            <td>
              <StatusBadge status={invoice.status} />
            </td>
            <td className="num">{formatMoney(invoice.total, invoice.currency)}</td>
            <td className="num">{formatMoney(invoice.balanceDue, invoice.currency)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
