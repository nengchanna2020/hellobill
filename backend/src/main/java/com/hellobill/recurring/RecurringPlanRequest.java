package com.hellobill.recurring;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A new monthly plan. If startDate is left out, the first invoice is made on the 1st of next month.
 */
public record RecurringPlanRequest(
    @NotNull Long customerId,
    @NotBlank @Size(max = 500) String description,
    @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
    @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal taxRate,
    LocalDate startDate,
    Boolean autoSend) {

  /** A plan without auto-send. */
  public RecurringPlanRequest(
      Long customerId,
      String description,
      BigDecimal amount,
      BigDecimal taxRate,
      LocalDate startDate) {
    this(customerId, description, amount, taxRate, startDate, null);
  }
}
