package com.hellobill.invoice;

import com.hellobill.payment.PaymentResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record InvoiceResponse(
    Long id,
    String number,
    InvoiceStatus status,
    CustomerSummary customer,
    String currency,
    LocalDate issueDate,
    int paymentTermsDays,
    LocalDate dueDate,
    BigDecimal taxRate,
    BigDecimal discountPercent,
    String notes,
    Instant sentAt,
    Long recurringPlanId,
    List<Item> items,
    BigDecimal subtotal,
    BigDecimal discount,
    BigDecimal tax,
    BigDecimal total,
    BigDecimal amountPaid,
    BigDecimal balanceDue,
    List<PaymentResponse> payments) {

  public record CustomerSummary(Long id, String name, String email) {}

  public record Item(
      String description, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
}
