package com.hellobill.invoice;

public enum InvoiceStatus {
  /** Being written. Can be edited or deleted. */
  DRAFT,
  /** Emailed to the customer, waiting for payment. */
  SENT,
  /** Paid in full. */
  PAID,
  /** Sent, not paid in full, and past its payment terms. */
  OVERDUE
}
