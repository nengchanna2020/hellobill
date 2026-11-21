"use client";

import { useState } from "react";
import { ErrorMessage } from "@/components/ErrorMessage";
import { api, ApiError } from "@/lib/api";
import { formatDate } from "@/lib/dates";
import { formatMoney } from "@/lib/money";
import type { Customer, Invoice, RecurringPlan } from "@/lib/types";
import { useApi } from "@/lib/use-api";

export default function RecurringPage() {
  const { data: plans, error, reload } = useApi<RecurringPlan[]>("/recurring-plans");
  const { data: customers } = useApi<Customer[]>("/customers");
  const [customerId, setCustomerId] = useState("");
  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const [taxRate, setTaxRate] = useState("0");
  const [message, setMessage] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  async function create(event: React.FormEvent) {
    event.preventDefault();
    setFormError(null);
    try {
      await api.post("/recurring-plans", {
        customerId: Number(customerId || customers?.[0]?.id),
        description,
        amount: Number(amount),
        taxRate: Number(taxRate),
      });
      setDescription("");
      setAmount("");
      await reload();
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : "Could not save the plan");
    }
  }

  async function toggle(plan: RecurringPlan) {
    await api.post(`/recurring-plans/${plan.id}/${plan.active ? "pause" : "resume"}`);
    await reload();
  }

  async function runNow() {
    const created = await api.post<Invoice[]>("/recurring-plans/run");
    setMessage(
      created.length === 0
        ? "No plans were due today."
        : `Made ${created.length} draft invoice(s): ${created.map((i) => i.number).join(", ")}`,
    );
    await reload();
  }

  return (
    <>
      <div className="page-head">
        <h1>Monthly billing</h1>
        <button type="button" className="secondary" onClick={runNow}>
          Run due plans now
        </button>
      </div>
      <p className="muted">
        Each plan makes a draft invoice on its next run date, then moves on one month. The job runs
        every morning at 06:00. Drafts are not emailed until you send them.
      </p>
      {message && <p className="notice">{message}</p>}
      <ErrorMessage message={error} />
      <section className="card">
        {plans && plans.length === 0 && <p className="muted">No monthly plans yet.</p>}
        {plans && plans.length > 0 && (
          <table className="table">
            <thead>
              <tr>
                <th>Customer</th>
                <th>Description</th>
                <th className="num">Amount</th>
                <th>Next invoice</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {plans.map((plan) => (
                <tr key={plan.id}>
                  <td>{plan.customerName}</td>
                  <td>{plan.description}</td>
                  <td className="num">{formatMoney(plan.amount, plan.currency)}</td>
                  <td>{formatDate(plan.nextRunDate)}</td>
                  <td>{plan.active ? "Active" : "Paused"}</td>
                  <td>
                    <button type="button" className="link" onClick={() => toggle(plan)}>
                      {plan.active ? "Pause" : "Resume"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {customers && customers.length > 0 && (
        <form onSubmit={create} className="card form-inline" aria-label="New monthly plan">
          <h3>New monthly plan</h3>
          <label>
            Customer
            <select value={customerId} onChange={(e) => setCustomerId(e.target.value)}>
              {customers.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} ({c.currency})
                </option>
              ))}
            </select>
          </label>
          <label>
            Description
            <input value={description} onChange={(e) => setDescription(e.target.value)} required />
          </label>
          <label>
            Amount
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
            Tax rate (%)
            <input
              type="number"
              step="0.01"
              min="0"
              max="100"
              value={taxRate}
              onChange={(e) => setTaxRate(e.target.value)}
            />
          </label>
          <button type="submit">Add plan</button>
          <ErrorMessage message={formError} />
        </form>
      )}
    </>
  );
}
