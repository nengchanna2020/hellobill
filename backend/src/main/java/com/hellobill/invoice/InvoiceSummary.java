package com.hellobill.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One row in the invoice list. */
public record InvoiceSummary(
    Long id,
    String number,
    InvoiceStatus status,
    Long customerId,
    String customerName,
    String currency,
    LocalDate issueDate,
    LocalDate dueDate,
    BigDecimal total,
    BigDecimal balanceDue) {}
