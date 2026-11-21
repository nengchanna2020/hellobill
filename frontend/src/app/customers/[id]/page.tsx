"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { ErrorMessage } from "@/components/ErrorMessage";
import { InvoiceTable } from "@/components/InvoiceTable";
import type { Customer, InvoiceSummary } from "@/lib/types";
import { useApi } from "@/lib/use-api";

export default function CustomerPage() {
  const { id } = useParams<{ id: string }>();
  const { data: customer, error } = useApi<Customer>(`/customers/${id}`);
  const { data: invoices } = useApi<InvoiceSummary[]>(`/invoices?customerId=${id}`);

  if (error) return <ErrorMessage message={error} />;
  if (!customer) return <p className="muted">Loading…</p>;

  return (
    <>
      <div className="page-head">
        <h1>{customer.name}</h1>
        <Link href="/invoices/new" className="button">
          New invoice
        </Link>
      </div>
      <section className="card grid-2">
        <div>
          <span className="label">Email</span>
          <p>{customer.email}</p>
        </div>
        <div>
          <span className="label">Country and currency</span>
          <p>
            {customer.country} · {customer.currency}
          </p>
        </div>
        <div>
          <span className="label">Default payment terms</span>
          <p>Net {customer.defaultPaymentTermsDays} days</p>
        </div>
      </section>
      <section className="card">
        <h2>Invoices</h2>
        {invoices && <InvoiceTable invoices={invoices} />}
      </section>
    </>
  );
}
