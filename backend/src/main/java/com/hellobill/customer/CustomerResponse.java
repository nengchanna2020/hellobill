package com.hellobill.customer;

import java.time.Instant;

public record CustomerResponse(
    Long id, String name, String email, String country, String currency, Instant createdAt) {

  static CustomerResponse from(Customer c) {
    return new CustomerResponse(
        c.getId(), c.getName(), c.getEmail(), c.getCountry(), c.getCurrency(), c.getCreatedAt());
  }
}
