"use client";

import Link from "next/link";
import { ErrorMessage } from "@/components/ErrorMessage";
import { InvoiceTable } from "@/components/InvoiceTable";
import { formatAmounts } from "@/lib/money";
import type { Dashboard } from "@/lib/types";
import { useApi } from "@/lib/use-api";

export default function DashboardPage() {
  const { data, error } = useApi<Dashboard>("/dashboard");

  return (
    <>
      <div className="page-head">
        <h1>Dashboard</h1>
        <Link href="/invoices/new" className="button">
          New invoice
        </Link>
      </div>
      <ErrorMessage message={error} />
      {data && (
        <>
          <section className="stats">
            <div className="stat">
              <span className="label">Outstanding</span>
              <span className="value">{formatAmounts(data.outstanding)}</span>
              <span className="muted">
                {data.counts.SENT + data.counts.OVERDUE} unpaid invoices
              </span>
            </div>
            <div className="stat stat-warn">
              <span className="label">Overdue</span>
              <span className="value">{formatAmounts(data.overdue)}</span>
              <span className="muted">{data.counts.OVERDUE} overdue invoices</span>
            </div>
            <div className="stat stat-good">
              <span className="label">Paid this month</span>
              <span className="value">{formatAmounts(data.paidThisMonth)}</span>
              <span className="muted">{data.counts.PAID} paid invoices in total</span>
            </div>
            <div className="stat">
              <span className="label">Drafts</span>
              <span className="value">{data.counts.DRAFT}</span>
              <span className="muted">waiting to be sent</span>
            </div>
          </section>
          <section className="card">
            <h2>Recent invoices</h2>
            <InvoiceTable invoices={data.recentInvoices} />
          </section>
        </>
      )}
    </>
  );
}
