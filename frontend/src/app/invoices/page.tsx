"use client";

import Link from "next/link";
import { useState } from "react";
import { ErrorMessage } from "@/components/ErrorMessage";
import { InvoiceTable } from "@/components/InvoiceTable";
import type { InvoiceStatus, InvoiceSummary } from "@/lib/types";
import { useApi } from "@/lib/use-api";

const FILTERS: (InvoiceStatus | "ALL")[] = ["ALL", "DRAFT", "SENT", "OVERDUE", "PAID"];

export default function InvoicesPage() {
  const [filter, setFilter] = useState<InvoiceStatus | "ALL">("ALL");
  const path = filter === "ALL" ? "/invoices" : `/invoices?status=${filter}`;
  const { data, error } = useApi<InvoiceSummary[]>(path);

  return (
    <>
      <div className="page-head">
        <h1>Invoices</h1>
        <Link href="/invoices/new" className="button">
          New invoice
        </Link>
      </div>
      <div className="filters" role="group" aria-label="Filter by status">
        {FILTERS.map((f) => (
          <button
            key={f}
            type="button"
            className={f === filter ? "chip active" : "chip"}
            onClick={() => setFilter(f)}
          >
            {f === "ALL" ? "All" : f.charAt(0) + f.slice(1).toLowerCase()}
          </button>
        ))}
      </div>
      <ErrorMessage message={error} />
      <section className="card">{data && <InvoiceTable invoices={data} />}</section>
    </>
  );
}
