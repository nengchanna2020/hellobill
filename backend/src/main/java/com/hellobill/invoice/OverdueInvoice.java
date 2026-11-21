package com.hellobill.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One row in the overdue list: the invoice summary plus how many days past its due date it is. */
public record OverdueInvoice(
    Long id,
    String number,
    InvoiceStatus status,
    Long customerId,
    String customerName,
    String currency,
    LocalDate issueDate,
    LocalDate dueDate,
    BigDecimal total,
    BigDecimal balanceDue,
    int daysLate) {

  static OverdueInvoice from(InvoiceSummary s, int daysLate) {
    return new OverdueInvoice(
        s.id(),
        s.number(),
        s.status(),
        s.customerId(),
        s.customerName(),
        s.currency(),
        s.issueDate(),
        s.dueDate(),
        s.total(),
        s.balanceDue(),
        daysLate);
  }
}
