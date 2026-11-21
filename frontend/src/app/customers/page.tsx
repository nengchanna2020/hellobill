"use client";

import Link from "next/link";
import { CustomerForm } from "@/components/CustomerForm";
import { ErrorMessage } from "@/components/ErrorMessage";
import type { Customer } from "@/lib/types";
import { useApi } from "@/lib/use-api";

export default function CustomersPage() {
  const { data, error, reload } = useApi<Customer[]>("/customers");

  return (
    <>
      <h1>Customers</h1>
      <ErrorMessage message={error} />
      <section className="card">
        {data && data.length === 0 && <p className="muted">No customers yet.</p>}
        {data && data.length > 0 && (
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Country</th>
                <th>Currency</th>
              </tr>
            </thead>
            <tbody>
              {data.map((c) => (
                <tr key={c.id}>
                  <td>
                    <Link href={`/customers/${c.id}`}>{c.name}</Link>
                  </td>
                  <td>{c.email}</td>
                  <td>{c.phone ?? "—"}</td>
                  <td>{c.country}</td>
                  <td>{c.currency}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
      <CustomerForm onSaved={() => void reload()} />
    </>
  );
}
