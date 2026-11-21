package com.hellobill.customer;

import java.time.Instant;

public record CustomerResponse(
    Long id,
    String name,
    String email,
    String phone,
    String country,
    String currency,
    int defaultPaymentTermsDays,
    Instant createdAt) {

  static CustomerResponse from(Customer c) {
    return new CustomerResponse(
        c.getId(),
        c.getName(),
        c.getEmail(),
        c.getPhone(),
        c.getCountry(),
        c.getCurrency(),
        c.getDefaultPaymentTermsDays(),
        c.getCreatedAt());
  }
}
