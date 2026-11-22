package com.hellobill.recurring;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringPlanResponse(
    Long id,
    Long customerId,
    String customerName,
    String currency,
    String description,
    BigDecimal amount,
    BigDecimal taxRate,
    boolean active,
    LocalDate nextRunDate,
    boolean autoSend) {

  static RecurringPlanResponse from(RecurringPlan p) {
    return new RecurringPlanResponse(
        p.getId(),
        p.getCustomer().getId(),
        p.getCustomer().getName(),
        p.getCustomer().getCurrency(),
        p.getDescription(),
        p.getAmount(),
        p.getTaxRate(),
        p.isActive(),
        p.getNextRunDate(),
        p.isAutoSend());
  }
}
