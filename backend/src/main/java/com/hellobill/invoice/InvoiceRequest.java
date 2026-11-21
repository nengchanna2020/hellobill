package com.hellobill.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Create or edit an invoice. Fields left out get sensible defaults. */
public record InvoiceRequest(
    @NotNull Long customerId,
    LocalDate issueDate,
    @Min(0) @Max(365) Integer paymentTermsDays,
    @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal taxRate,
    @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal discountPercent,
    String notes,
    @NotEmpty @Valid List<InvoiceItemRequest> items) {}
