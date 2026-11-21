"use client";

import { ErrorMessage } from "@/components/ErrorMessage";
import { InvoiceForm } from "@/components/InvoiceForm";
import type { Customer } from "@/lib/types";
import { useApi } from "@/lib/use-api";

export default function NewInvoicePage() {
  const { data: customers, error } = useApi<Customer[]>("/customers");

  return (
    <>
      <h1>New invoice</h1>
      <ErrorMessage message={error} />
      {customers && <InvoiceForm customers={customers} />}
    </>
  );
}
