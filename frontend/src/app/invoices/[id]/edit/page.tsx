"use client";

import { useParams } from "next/navigation";
import { ErrorMessage } from "@/components/ErrorMessage";
import { InvoiceForm } from "@/components/InvoiceForm";
import type { Customer, Invoice } from "@/lib/types";
import { useApi } from "@/lib/use-api";

export default function EditInvoicePage() {
  const { id } = useParams<{ id: string }>();
  const { data: invoice, error } = useApi<Invoice>(`/invoices/${id}`);
  const { data: customers } = useApi<Customer[]>("/customers");

  return (
    <>
      <h1>Edit {invoice?.number ?? "invoice"}</h1>
      <ErrorMessage message={error} />
      {invoice && invoice.status !== "DRAFT" && <p className="muted">Only drafts can be edited.</p>}
      {invoice && customers && invoice.status === "DRAFT" && (
        <InvoiceForm customers={customers} invoice={invoice} />
      )}
    </>
  );
}
