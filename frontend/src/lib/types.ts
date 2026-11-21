// Shapes of the JSON the Spring Boot API returns. Money comes as numbers with
// 2 decimal places, for example 149.05.

export type InvoiceStatus = "DRAFT" | "SENT" | "PAID" | "OVERDUE";

export interface Customer {
  id: number;
  name: string;
  email: string;
  country: string;
  currency: string;
  createdAt: string;
}

export interface InvoiceItem {
  description: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface Payment {
  id: number;
  amount: number;
  paidOn: string;
  method: string;
  note: string | null;
}

export interface Invoice {
  id: number;
  number: string;
  status: InvoiceStatus;
  customer: { id: number; name: string; email: string };
  currency: string;
  issueDate: string;
  paymentTermsDays: number;
  taxRate: number;
  discountPercent: number;
  notes: string | null;
  sentAt: string | null;
  recurringPlanId: number | null;
  items: InvoiceItem[];
  subtotal: number;
  discount: number;
  tax: number;
  total: number;
  amountPaid: number;
  balanceDue: number;
  payments: Payment[];
}

export interface InvoiceSummary {
  id: number;
  number: string;
  status: InvoiceStatus;
  customerId: number;
  customerName: string;
  currency: string;
  issueDate: string;
  total: number;
  balanceDue: number;
}

export interface RecurringPlan {
  id: number;
  customerId: number;
  customerName: string;
  currency: string;
  description: string;
  amount: number;
  taxRate: number;
  active: boolean;
  nextRunDate: string;
}

export interface CurrencyAmount {
  currency: string;
  amount: number;
  invoices: number;
}

export interface Dashboard {
  counts: Record<InvoiceStatus, number>;
  outstanding: CurrencyAmount[];
  overdue: CurrencyAmount[];
  paidThisMonth: CurrencyAmount[];
  recentInvoices: InvoiceSummary[];
}
