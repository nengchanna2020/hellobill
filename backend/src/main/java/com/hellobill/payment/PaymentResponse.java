package com.hellobill.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
    Long id, BigDecimal amount, LocalDate paidOn, String method, String note) {

  public static PaymentResponse from(Payment p) {
    return new PaymentResponse(p.getId(), p.getAmount(), p.getPaidOn(), p.getMethod(), p.getNote());
  }
}
