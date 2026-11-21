"use client";

import { useState } from "react";
import { api, ApiError } from "@/lib/api";
import type { Customer } from "@/lib/types";
import { ErrorMessage } from "./ErrorMessage";

const CURRENCIES = ["USD", "EUR", "GBP", "KHR", "JPY", "CAD", "AUD", "SGD", "THB"];

export function CustomerForm({ onSaved }: { onSaved: (c: Customer) => void }) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [country, setCountry] = useState("");
  const [currency, setCurrency] = useState("USD");
  const [terms, setTerms] = useState("30");
  const [error, setError] = useState<string | null>(null);
  const [fields, setFields] = useState<Record<string, string>>({});

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setError(null);
    setFields({});
    try {
      const saved = await api.post<Customer>("/customers", {
        name,
        email,
        phone: phone.trim() || null,
        country: country.toUpperCase(),
        currency,
        defaultPaymentTermsDays: Number(terms),
      });
      setName("");
      setEmail("");
      setPhone("");
      setCountry("");
      onSaved(saved);
    } catch (e) {
      if (e instanceof ApiError) {
        setError(e.message);
        setFields(e.fields);
      } else {
        setError("Could not save the customer");
      }
    }
  }

  return (
    <form onSubmit={submit} className="card form-inline" aria-label="New customer">
      <h3>New customer</h3>
      <label>
        Name
        <input value={name} onChange={(e) => setName(e.target.value)} required />
      </label>
      <label>
        Email
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        {fields.email && <small className="error">{fields.email}</small>}
      </label>
      <label>
        Phone (optional)
        <input type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} maxLength={30} />
        {fields.phone && <small className="error">{fields.phone}</small>}
      </label>
      <label>
        Country code
        <input
          value={country}
          onChange={(e) => setCountry(e.target.value)}
          placeholder="KH"
          maxLength={2}
          required
        />
        {fields.country && <small className="error">{fields.country}</small>}
      </label>
      <label>
        Currency
        <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
          {CURRENCIES.map((c) => (
            <option key={c}>{c}</option>
          ))}
        </select>
      </label>
      <label>
        Default payment terms (days)
        <input
          type="number"
          min="0"
          max="365"
          value={terms}
          onChange={(e) => setTerms(e.target.value)}
          required
        />
        {fields.defaultPaymentTermsDays && (
          <small className="error">{fields.defaultPaymentTermsDays}</small>
        )}
      </label>
      <button type="submit">Add customer</button>
      <ErrorMessage message={error} />
    </form>
  );
}
